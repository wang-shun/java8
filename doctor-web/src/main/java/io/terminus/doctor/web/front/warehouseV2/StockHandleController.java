package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.*;
import io.terminus.doctor.basic.service.DoctorWareHouseReadService;
import io.terminus.doctor.basic.service.warehouseV2.*;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.warehouseV2.vo.StockHandleExportVo;
import io.terminus.doctor.web.front.warehouseV2.vo.StockHandleVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 库存操作单据
 * 出库
 * 入库
 * 调拨
 * 盘点
 * Created by sunbo@terminus.io on 2017/10/31.
 */
@Slf4j
@RestController
@RequestMapping("api/doctor/warehouse/receipt")
public class StockHandleController {

    @Autowired
    private Exporter exporter;

    @RpcConsumer
    private DoctorWarehouseStockHandleReadService doctorWarehouseStockHandleReadService;
    @RpcConsumer
    private DoctorWarehouseStockHandleWriteService doctorWarehouseStockHandleWriteService;
    @RpcConsumer
    private DoctorWarehouseMaterialHandleWriteService doctorWarehouseMaterialHandleWriteService;
    @RpcConsumer
    private DoctorWarehouseMaterialHandleReadService doctorWarehouseMaterialHandleReadService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private DoctorWareHouseReadService doctorWareHouseReadService;
    @RpcConsumer
    private DoctorWarehouseSkuReadService doctorWarehouseSkuReadService;
    @RpcConsumer
    private DoctorWarehouseVendorReadService doctorWarehouseVendorReadService;
    @RpcConsumer
    private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

    @RpcConsumer
    private DoctorWarehouseSettlementService doctorWarehouseSettlementService;

    @RpcConsumer
    private DoctorGroupReadService doctorGroupReadService;

    @InitBinder
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
    }

    //退料入库
    //得到仓库类型，仓库名称，仓库管理员，所属公司
    @RequestMapping(method = RequestMethod.GET, value = "/getFarmData")
    public List<Map> getFarmData(@RequestParam Long id) {
        List<Map> maps = RespHelper.or500(doctorWarehouseMaterialHandleReadService.getFarmData(id));
        maps.forEach( mp ->{
            DoctorWarehouseMaterialApply materialApply = RespHelper.or500(doctorWarehouseMaterialApplyReadService.findByMaterialHandleAndFarmId((Long) mp.get("material_handle_id"),(Long)mp.get("farm_id")));
            //判断猪群是否关闭
            if((materialApply.getPigGroupId()!=null&&!materialApply.getPigGroupId().equals(""))&&materialApply.getApplyType()==1){
                DoctorGroup doctorGroup = RespHelper.or500(doctorGroupReadService.findGroupById(materialApply.getPigGroupId()));
                if(doctorGroup.getStatus()==-1){
                    mp.put("status",-1);
                }else{
                    mp.put("status",1);
                }
            }else{
                mp.put("status",0);
            }
        });
        return maps;
    }

    //得到领料出库的物料名称
    @RequestMapping(method = RequestMethod.GET, value = "/getMaterialNameByID")
    public List<Map> getMaterialNameByID(@RequestParam Long id) {
        List<Map> maps = RespHelper.or500(doctorWarehouseMaterialHandleReadService.getMaterialNameByID(id));
        return maps;
    }


    //根据物料名称得到 物料名称，物料编号，厂家，规格，单位，可退数量，备注
    @RequestMapping(method = RequestMethod.GET, value = "/getDataByMaterialName")
    public List<Map> getDataByMaterialName(@RequestParam Long id) {
        List<Map> maps = RespHelper.or500(doctorWarehouseMaterialHandleReadService.getDataByMaterialName(id));
        return maps;
    }

    //退料入库前的数据展示
    @RequestMapping(method = RequestMethod.GET, value = "/getRetreatingData")
    public List<Map> getRetreatingData(@RequestParam Long id) {

        List<Map> maps=new ArrayList<Map>();
        //根据物料名称得到 物料名称，物料编号，厂家，规格，单位，可退数量，备注
        List<Map> mp = RespHelper.or500(doctorWarehouseMaterialHandleReadService.getDataByMaterialName(id));
        //得到领用猪群，领用猪舍的名称
        for(Map mpp :mp){
            //得到可退数量
            BigDecimal RefundableNumber = new BigDecimal(0);
            //得到领料出库的数量
            BigDecimal LibraryQuantity = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findLibraryById((Long) mpp.get("material_handle_id"),(String) mpp.get("material_name")));
            //得到在此之前退料入库的数量和
            BigDecimal RetreatingQuantity = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findRetreatingById((Long) mpp.get("material_handle_id"),(String) mpp.get("material_name"),id));
            RefundableNumber = LibraryQuantity.add(RetreatingQuantity);
            mpp.put("refundableQuantity",RefundableNumber.doubleValue());

            DoctorWarehouseMaterialApply materialApply = RespHelper.or500(doctorWarehouseMaterialApplyReadService.findByMaterialHandleAndFarmId((Long) mpp.get("material_handle_id"),(Long)mpp.get("farm_id")));
            mpp.put("applyBarnId",materialApply.getPigBarnId());
            mpp.put("applyGroupId",materialApply.getPigGroupId());
            if(materialApply.getPigBarnName()==null)
                mpp.put("applyBarnName","--");
            else
                mpp.put("applyBarnName",materialApply.getPigBarnName());
            if(materialApply.getPigGroupName()==null)
                mpp.put("applyGroupName","--");
            else
                mpp.put("applyGroupName",materialApply.getPigGroupName());

            //判断猪群是否关闭：如果领用到猪群,并且猪群已经关闭，则不能退料入库

            if((materialApply.getPigGroupId()!=null&&!materialApply.getPigGroupId().equals(""))&&materialApply.getApplyType()==1){
                DoctorGroup doctorGroup = RespHelper.or500(doctorGroupReadService.findGroupById(materialApply.getPigGroupId()));
                if(doctorGroup.getStatus()!=-1){
                    maps.add(mpp);
                }
            }
            //母猪
            if(materialApply.getPigGroupId()!=null&&materialApply.getApplyType()==2){
                maps.add(mpp);
            }
            //猪舍
            if(materialApply.getPigGroupId()==null&&materialApply.getApplyType()==0){
                maps.add(mpp);
            }
        };

        return maps;
    }

    //单据数据展示
    @RequestMapping(value = "/paging", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Paging<DoctorWarehouseStockHandle> paging(@RequestParam(required = false) Long farmId,
                                                     @RequestParam(required = false) Integer pageNo,
                                                     @RequestParam(required = false) Integer pageSize,
                                                     @RequestParam(required = false) Date startDate,
                                                     @RequestParam(required = false) Date endDate,
                                                     @RequestParam(required = false) Date updatedAt,
                                                     @RequestParam(required = false) Long operatorId,
                                                     @RequestParam(required = false) Long warehouseId,
                                                     @RequestParam(required = false) Integer type,
                                                     @RequestParam(required = false) Integer subType) {

        if (null != startDate && null != endDate && startDate.after(endDate))
            throw new JsonResponseException("start.date.after.end.date");

        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmId);
        params.put("warehouseId", warehouseId);
        params.put("operatorId", operatorId);
        params.put("handleType", type);
        params.put("handleSubType", subType);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("updatedAt", updatedAt);
        return RespHelper.or500(doctorWarehouseStockHandleReadService.paging(pageNo, pageSize, params));
    }


    //查询
    @RequestMapping(method = RequestMethod.GET, value = "{id:\\d+}")
    public StockHandleVo query(@PathVariable Long id,
                               @RequestParam(required = false) Long orgId,
                               @RequestParam(required = false) String date) {

        //单据表
        DoctorWarehouseStockHandle stockHandle = RespHelper.or500(doctorWarehouseStockHandleReadService.findById(id));
        if (null == stockHandle)
            return null;

        StockHandleVo vo = new StockHandleVo();
        BeanUtils.copyProperties(stockHandle, vo);

        vo.setDetails(
                //单据明细表
                RespHelper.or500(doctorWarehouseMaterialHandleReadService.findByStockHandle(stockHandle.getId()))
                        .stream()
                        .map(mh -> {
                            StockHandleVo.Detail detail = new StockHandleVo.Detail();
                            //单据明细里面的值全部复制到detail里面去
                            BeanUtils.copyProperties(mh, detail);

                            //  编辑单据时判断是否有物料已盘点：如果已盘点，则不可编辑 （陈娟 2018-09-19）
                            String desc=new String();
                            DoctorWarehouseMaterialHandle material = RespHelper.or500(doctorWarehouseMaterialHandleReadService.getMaxInventoryDate(stockHandle.getWarehouseId(), mh.getMaterialId(), stockHandle.getHandleDate()));
                            if(material!=null){
                                if(material!=null){
                                    if(!material.getStockHandleId().equals(stockHandle.getId())){
                                        if(stockHandle.getUpdatedAt().compareTo(material.getHandleDate())<0){
                                            detail.setIsInventory(1);
                                            vo.setHasInventory(1);
                                            desc = desc + "【该物料已盘点,不可编辑】;";
                                        }
                                    }
                                }
                            }

                            if ((!stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.IN.getValue()))&&(!stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue()))) {
                                try {
                                    //会计年月支持选择未结算过的会计年月，如果选择未结算的会计区间，则报表不显示金额和单价
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                                    boolean b;
                                    if (orgId != null && date != null) {
                                        b = doctorWarehouseSettlementService.isSettled(orgId, sdf.parse(date));
                                    } else {
                                        b = doctorWarehouseSettlementService.isSettled(mh.getOrgId(), mh.getSettlementDate());
                                    }
                                    if (!b) {
                                        if (!stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.IN.getValue()) || stockHandle.getHandleSubType() != WarehouseMaterialHandleType.IN.getValue()) {
                                            detail.setUnitPrice(BigDecimal.ZERO);
                                            detail.setAmount(BigDecimal.ZERO);
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            //物料表
                            DoctorWarehouseSku sku = RespHelper.or500(doctorWarehouseSkuReadService.findById(mh.getMaterialId()));
                            if (null != sku) {
                                detail.setVendorName(RespHelper.or500(doctorWarehouseVendorReadService.findNameById(sku.getVendorId())));
                                detail.setMaterialCode(sku.getCode());
                                //得到单位名称
                                String nameByUnit = RespHelper.or500(doctorWarehouseStockHandleReadService.getNameByUnit(Long.parseLong(sku.getUnit())));
                                detail.setUnit(nameByUnit);
                                detail.setUnitId(sku.getUnit());
                                detail.setMaterialSpecification(sku.getSpecification());
                            } else {
                                log.warn("sku not found,{}", mh.getMaterialId());
                            }

                            //领料出库
                            if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.OUT.getValue())) {
                                DoctorWarehouseMaterialApply apply = RespHelper.or500(doctorWarehouseMaterialApplyReadService.findByMaterialHandleAndFarmId(mh.getId(),mh.getFarmId()));
                                if (null != apply) {
                                    detail.setApplyPigBarnName(apply.getPigBarnName());
                                    detail.setApplyPigBarnId(apply.getPigBarnId());
                                    if(apply.getPigGroupName()!=null){
                                        detail.setApplyPigGroupName(apply.getPigGroupName());
                                    }else{
                                        detail.setApplyPigGroupName("--");
                                    }
                                    detail.setApplyPigGroupId(apply.getPigGroupId());
                                    detail.setApplyStaffName(apply.getApplyStaffName());
                                    detail.setApplyStaffId(apply.getApplyStaffId());
                                } else
                                    log.warn("material apply not found,by material handle {}", mh.getId());

                                //判断猪群是否关闭

                                if((apply.getPigGroupId()!=null&&!apply.getPigGroupId().equals(""))&&apply.getApplyType()==1){

                                    DoctorGroup doctorGroup = RespHelper.or500(doctorGroupReadService.findGroupById(apply.getPigGroupId()));
                                    detail.setGroupStatus(doctorGroup.getStatus());
                                    if(doctorGroup.getStatus()==-1){
                                        vo.setStatus(doctorGroup.getStatus());
                                        desc = desc + "【该猪群已关闭,不可编辑】;";
                                    }
                                }

                                //得到该领料出库的退料入库的数量
                                Integer count = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findCountByRelMaterialHandleId(mh.getId(), mh.getFarmId()));
                                detail.setRetreatingCount(count);
                                if(count>=1){
                                    desc = desc + "【该物料有退料入库,不可编辑】;";
                                }

                            }

                            //退料入库-->可退数量
                            if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.RETURN.getValue())) {
                                DoctorWarehouseMaterialApply apply = RespHelper.or500(doctorWarehouseMaterialApplyReadService.findByMaterialHandleAndFarmId(mh.getRelMaterialHandleId(),mh.getFarmId()));
                                if (null != apply) {
                                    detail.setApplyPigBarnName(apply.getPigBarnName());
                                    detail.setApplyPigBarnId(apply.getPigBarnId());
                                    if(apply.getPigGroupName()!=null){
                                        detail.setApplyPigGroupName(apply.getPigGroupName());
                                    }else{
                                        detail.setApplyPigGroupName("--");
                                    }
                                    detail.setApplyPigGroupId(apply.getPigGroupId());
                                } else {
                                    log.warn("material apply not found,by material handle {}", mh.getId());
                                }
                                BigDecimal RefundableNumber = new BigDecimal(0);
                                //得到领料出库的数量
                                BigDecimal LibraryQuantity = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findLibraryById(mh.getRelMaterialHandleId(),mh.getMaterialName()));
                                //得到在此之前退料入库的数量和
                                BigDecimal RetreatingQuantity = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findRetreatingById(mh.getRelMaterialHandleId(),mh.getMaterialName(),stockHandle.getId()));
                                RefundableNumber = LibraryQuantity.add(RetreatingQuantity);
                                detail.setRefundableQuantity(RefundableNumber.doubleValue());

                                //判断猪群是否关闭

                                if((apply.getPigGroupId()!=null&&!apply.getPigGroupId().equals(""))&&apply.getApplyType()==1){

                                    DoctorGroup doctorGroup = RespHelper.or500(doctorGroupReadService.findGroupById(apply.getPigGroupId()));
                                    detail.setGroupStatus(doctorGroup.getStatus());
                                    if(doctorGroup.getStatus()==-1){
                                        vo.setStatus(doctorGroup.getStatus());
                                        desc = desc + "【该猪群已关闭,不可编辑】;";
                                    }
                                }
                            }

                            //调出
                            if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.TRANSFER_OUT.getValue())) {
                                //单据明细表
                                DoctorWarehouseMaterialHandle transferInHandle = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findById(mh.getRelMaterialHandleId()));
                                if (transferInHandle != null) {
                                    DoctorWareHouse wareHouse = RespHelper.or500(doctorWareHouseReadService.findById(transferInHandle.getWarehouseId()));
                                    if (wareHouse != null) {
                                        detail.setTransferInWarehouseName(wareHouse.getWareHouseName());
                                        detail.setTransferInWarehouseId(wareHouse.getId());
                                        detail.setTransferInFarmName(wareHouse.getFarmName());
                                        detail.setTransferInFarmId(wareHouse.getFarmId());
                                    } else
                                        log.warn("warehouse not found,{}", transferInHandle.getWarehouseId());
                                } else
                                    log.warn("other transfer in handle not found,{}", mh.getRelMaterialHandleId());
                            }

                            //配方生产出库
                            if (stockHandle.getHandleSubType().equals( WarehouseMaterialHandleType.FORMULA_OUT.getValue())) {
                                DoctorWarehouseStockHandle sh = RespHelper.or500(doctorWarehouseStockHandleReadService.findwarehouseName(stockHandle.getRelStockHandleId()));
                                detail.setStorageWarehouseIds(sh.getWarehouseId());
                                detail.setStorageWarehouseNames(sh.getWarehouseName());
                            }

                            // 设置注释 （陈娟 2018-09-19）
                            detail.setDesc(desc);

                            return detail;
                        })
                        .collect(Collectors.toList()));

        //配方生产出库
        if (stockHandle.getHandleSubType().equals( WarehouseMaterialHandleType.FORMULA_OUT.getValue())) {
            DoctorWarehouseStockHandle sh = RespHelper.or500(doctorWarehouseStockHandleReadService.findwarehouseName(stockHandle.getRelStockHandleId()));
            vo.setStorageWarehouseId(sh.getWarehouseId());
            vo.setStorageWarehouseName(sh.getWarehouseName());
        }


        DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(vo.getFarmId()));
        if (farm != null) {
            vo.setFarmName(farm.getName());
            vo.setOrgName(farm.getOrgName());
        }

        DoctorWareHouse wareHouse = RespHelper.or500(doctorWareHouseReadService.findById(vo.getWarehouseId()));
        if (wareHouse != null) {
            vo.setWarehouseManagerName(wareHouse.getManagerName());
        }

        if (!vo.getDetails().isEmpty()) {
            vo.setWarehouseType(vo.getDetails().get(0).getWarehouseType());
        }

        BigDecimal totalQuantity = new BigDecimal(0);
        BigDecimal totalUnitPrice = new BigDecimal(0);
        double totalAmount = 0L;
        for (StockHandleVo.Detail detail : vo.getDetails()) {
            totalQuantity = totalQuantity.add(detail.getQuantity());
            totalUnitPrice = totalUnitPrice.add(null == detail.getUnitPrice() ? new BigDecimal(0) : detail.getUnitPrice());
            totalAmount += detail.getQuantity().multiply(detail.getUnitPrice()).doubleValue();
        }

        vo.setTotalQuantity(totalQuantity.doubleValue());
        vo.setTotalAmount(totalAmount);
        //vo.setTotalAmount(totalQuantity.multiply(totalUnitPrice).doubleValue());
        return vo;
    }

    //删除单据表以及对应的单据明细表
    @RequestMapping(method = RequestMethod.DELETE, value = "{id:\\d+}")
    public Response<String> delete(@PathVariable Long id,@RequestParam(required = false) Long orgId,@RequestParam(required = false) String settlementDate) {
        //是否该公司正在结算中
        if (doctorWarehouseSettlementService.isUnderSettlement(orgId))
            throw new JsonResponseException("under.settlement");
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");
        Date date = null;
        try {
           date  = sdf.parse(settlementDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (doctorWarehouseSettlementService.isSettled(orgId,date))
            throw new JsonResponseException("already.settlement");

        //  删除单据时判断是否有物料已盘点 （陈娟 2018-09-18）
        String str = RespHelper.or500(doctorWarehouseMaterialHandleReadService.deleteCheckInventory(id));
        if(str!=null&&!str.equals("")){
            throw new JsonResponseException(str);
        }

        return doctorWarehouseStockHandleWriteService.delete(id);
           /*if (!response.isSuccess())
               throw new JsonResponseException(response.getError());
           return true;*/
    }

    //根据id判断是否有退料入库
    @RequestMapping(method = RequestMethod.GET, value = "/findByRelMaterialHandleId")
    public Response<Integer> findByRelMaterialHandleId(@RequestParam Long id,@RequestParam Long farmId) {
        Response<Integer> count = doctorWarehouseMaterialHandleReadService.findCountByRelMaterialHandleId(id, farmId);
        return count;
    }

    //删除单据明细表
    @RequestMapping(method = RequestMethod.DELETE, value = "/deleteById/{id:\\d+}")
    public Response<String> deleteById(@PathVariable Long id,@RequestParam(required = false) Long orgId) {
        //是否该公司正在结算中
        if (doctorWarehouseSettlementService.isUnderSettlement(orgId))
            throw new JsonResponseException("under.settlement");

        return doctorWarehouseMaterialHandleWriteService.delete(id);
    }

    //导出
    @RequestMapping(method = RequestMethod.GET, value = "{id:\\d+}/export")
    public void export(@PathVariable Long id,
                       HttpServletRequest request,
                       HttpServletResponse response) {

        //单据表
        DoctorWarehouseStockHandle stockHandle = RespHelper.or500(doctorWarehouseStockHandleReadService.findById(id));
        if (null == stockHandle)
            throw new JsonResponseException("warehouse.stock.handle.not.found");

        //猪场表Model类
        DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(stockHandle.getFarmId()));
        if (null == farm)
            throw new JsonResponseException("farm.not.found");

        //仓库
        DoctorWareHouse wareHouse = RespHelper.or500(doctorWareHouseReadService.findById(stockHandle.getWarehouseId()));
        if (null == wareHouse)
            throw new JsonResponseException("warehouse.not.found");

        String farmName = farm.getName();
        String operatorTypeName = "";
        switch (stockHandle.getHandleType()) {
            case 1:
                operatorTypeName = "入库单";
                break;
            case 2:
                operatorTypeName = "出库单";
                break;
            case 3:
                operatorTypeName = "调拨单";
                break;
            case 4:
                operatorTypeName = "盘点单";
                break;
        }

        List<StockHandleExportVo> exportVos = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findByStockHandle(id))
                .stream()
                .map(mh -> {
                    StockHandleExportVo vo = new StockHandleExportVo();
                    BeanUtils.copyProperties(mh, vo);
                    vo.setHandleType(mh.getType());
                    vo.setBeforeInventoryQuantity(mh.getBeforeStockQuantity());

                    //会计年月支持选择未结算过的会计年月，如果选择未结算的会计区间，则报表不显示金额和单价
                    boolean b = doctorWarehouseSettlementService.isSettled(mh.getOrgId(), mh.getSettlementDate());
                    if(!b){
                        if (!stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.IN.getValue())||stockHandle.getHandleSubType()!=WarehouseMaterialHandleType.IN.getValue()) {
                            vo.setUnitPrice(0.0);
                            vo.setAmount(0.0);
                        }
                    }

                    //物料表
                    DoctorWarehouseSku sku = RespHelper.or500(doctorWarehouseSkuReadService.findById(mh.getMaterialId()));
                    if (null != sku) {
                        vo.setVendorName(RespHelper.or500(doctorWarehouseVendorReadService.findNameById(sku.getVendorId())));
                        vo.setMaterialCode(sku.getCode());
                        //得到单位名称
                        String nameByUnit = RespHelper.or500(doctorWarehouseStockHandleReadService.getNameByUnit(Long.parseLong(sku.getUnit())));
                        vo.setUnit(nameByUnit);
                        vo.setMaterialSpecification(sku.getSpecification());
                    }else
                        log.warn("DoctorWarehouseSku found", mh.getMaterialId());

                    //领料出库
                    if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.OUT.getValue())) {
                        //物料领用表
                        DoctorWarehouseMaterialApply apply = RespHelper.or500(doctorWarehouseMaterialApplyReadService.findByMaterialHandleAndFarmId(mh.getId(),mh.getFarmId()));
                        if (null != apply) {
                            vo.setApplyPigBarnName(apply.getPigBarnName());
                            if(apply.getPigGroupName()!=null){
                                vo.setApplyPigGroupName(apply.getPigGroupName());
                            }else{
                                vo.setApplyPigGroupName("--");
                            }
                            vo.setApplyStaffName(apply.getApplyStaffName());
                        } else
                            log.warn("material apply not found,by material handle {}", mh.getId());
                    }

                    //调出
                    if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.TRANSFER_OUT.getValue())) {
                        DoctorWarehouseMaterialHandle transferInHandle = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findById(mh.getRelMaterialHandleId()));
                        if (transferInHandle != null) {
                            //单据明细表
                            DoctorWareHouse transferInWarehouse = RespHelper.or500(doctorWareHouseReadService.findById(transferInHandle.getWarehouseId()));
                            if (transferInWarehouse != null) {
                                vo.setTransferInWarehouseName(transferInWarehouse.getWareHouseName());
                                vo.setTransferInFarmName(transferInWarehouse.getFarmName());
                            } else
                                log.warn("warehouse not found,{}", transferInHandle.getWarehouseId());
                        } else
                            log.warn("other transfer in handle not found,{}", mh.getRelMaterialHandleId());
                    }

                    //退料入库-->可退数量
                    if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.RETURN.getValue())) {
                        DoctorWarehouseMaterialApply apply = RespHelper.or500(doctorWarehouseMaterialApplyReadService.findByMaterialHandleAndFarmId(mh.getRelMaterialHandleId(),mh.getFarmId()));
                        if (null != apply) {
                            vo.setApplyPigBarnName(apply.getPigBarnName());
                            if(apply.getPigGroupName()!=null){
                                vo.setApplyPigGroupName(apply.getPigGroupName());
                            }else{
                                vo.setApplyPigGroupName("--");
                            }
                        }
                        BigDecimal RefundableNumber = new BigDecimal(0);
                        //得到领料出库的数量
                        BigDecimal LibraryQuantity = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findLibraryById(mh.getRelMaterialHandleId(),mh.getMaterialName()));
                        //得到在此之前退料入库的数量和
                        BigDecimal RetreatingQuantity = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findRetreatingById(mh.getRelMaterialHandleId(),mh.getMaterialName(),stockHandle.getId()));
                        RefundableNumber = LibraryQuantity.add(RetreatingQuantity);
                        vo.setBeforeInventoryQuantity(RefundableNumber);
                    }

                    //配方生产出库
                    if (stockHandle.getHandleSubType().equals( WarehouseMaterialHandleType.FORMULA_OUT.getValue())) {
                        DoctorWarehouseStockHandle sh = RespHelper.or500(doctorWarehouseStockHandleReadService.findwarehouseName(stockHandle.getRelStockHandleId()));
                        vo.setTransferInWarehouseName(sh.getWarehouseName());
                    }

                    vo.setUnitPrice(mh.getUnitPrice().divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    vo.setAmount(mh.getUnitPrice().multiply(vo.getQuantity()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    vo.setUnitPrice(mh.getUnitPrice().doubleValue());
                    vo.setAmount(mh.getAmount()!=null?mh.getAmount().doubleValue():0);

                    return vo;
                })
                .collect(Collectors.toList());


        //开始导出
        try {
            //导出名称
            exporter.setHttpServletResponse(request, response, "仓库单据");

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                //表
                Sheet sheet = workbook.createSheet();
                sheet.createRow(0).createCell(0).setCellValue(farmName + operatorTypeName);

                Row head = sheet.createRow(1);
                head.createCell(0).setCellValue(operatorTypeName + "时间");
                head.createCell(1).setCellValue(DateUtil.toDateString(stockHandle.getHandleDate()));
                head.createCell(2).setCellValue("仓库类型");
                head.createCell(3).setCellValue(WareHouseType.from(stockHandle.getWarehouseType()).getDesc() + "仓库");
                head.createCell(4).setCellValue("仓库名称");
                head.createCell(5).setCellValue(stockHandle.getWarehouseName());
                head.createCell(6).setCellValue("会计年月");
                if(stockHandle.getSettlementDate()!=null&&!stockHandle.getSettlementDate().equals("")){
                    Date settlementDate = stockHandle.getSettlementDate();
                    SimpleDateFormat format=new SimpleDateFormat("yyyy年MM月");
                    String ss = format.format(settlementDate);
                    head.createCell(7).setCellValue(ss);
                }else{
                    head.createCell(7).setCellValue("");
                }
                head.createCell(8).setCellValue("单据编号");
                head.createCell(9).setCellValue(stockHandle.getSerialNo());

                Row title = sheet.createRow(2);
                int pos = 3;

                //入库单-->采购入库
                if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.IN.getValue())) {
                    title.createCell(0).setCellValue("物料名称");
                    title.createCell(1).setCellValue("厂家");
                    title.createCell(2).setCellValue("物料编码");
                    title.createCell(3).setCellValue("规格");
                    title.createCell(4).setCellValue("单位");
                    title.createCell(5).setCellValue("数量");
                    title.createCell(6).setCellValue("单价（元）");
                    title.createCell(7).setCellValue("金额（元）");
                    title.createCell(8).setCellValue("备注");

                    BigDecimal totalQuantity = new BigDecimal(0);
                    double totalAmount = 0L;
                    for (StockHandleExportVo vo : exportVos) {
                        Row row = sheet.createRow(pos++);
                        row.createCell(0).setCellValue(vo.getMaterialName());
                        row.createCell(1).setCellValue(vo.getVendorName());
                        row.createCell(2).setCellValue(vo.getMaterialCode());
                        row.createCell(3).setCellValue(vo.getMaterialSpecification());
                        row.createCell(4).setCellValue(vo.getUnit());
                        row.createCell(5).setCellValue(vo.getQuantity().doubleValue());
                        row.createCell(6).setCellValue(vo.getUnitPrice());
                        row.createCell(7).setCellValue(vo.getAmount());
                        row.createCell(8).setCellValue(vo.getRemark());

                        totalQuantity = totalQuantity.add(vo.getQuantity());
                        totalAmount += vo.getAmount();
                    }

                    Row countRow = sheet.createRow(pos);
                    //表格范围
                    CellRangeAddress countRange = new CellRangeAddress(pos, pos, 0, 4);
                    //合并区域
                    sheet.addMergedRegion(countRange);

                    Cell countCell = countRow.createCell(0);
                    CellStyle style = workbook.createCellStyle();
                    //对齐
                    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                    countCell.setCellStyle(style);
                    countCell.setCellValue("合计");

                    countRow.createCell(5).setCellValue(totalQuantity.doubleValue());
                    countRow.createCell(7).setCellValue(totalAmount);
                    pos++;

                    //出库单-->领料出库
                } else if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.OUT.getValue())) {
                    title.createCell(0).setCellValue("物料名称");
                    title.createCell(1).setCellValue("物料编码");
                    title.createCell(2).setCellValue("厂家");
                    title.createCell(3).setCellValue("规格");
                    title.createCell(4).setCellValue("单位");
                    title.createCell(5).setCellValue("领用猪舍");
                    title.createCell(6).setCellValue("领用猪群");
                    title.createCell(7).setCellValue("饲养员");
                    title.createCell(8).setCellValue("数量");
                    title.createCell(9).setCellValue("单价（元）");
                    title.createCell(10).setCellValue("金额（元）");
                    title.createCell(11).setCellValue("备注");

                    BigDecimal totalQuantity = new BigDecimal(0);
                    double totalAmount = 0L;
                    for (StockHandleExportVo vo : exportVos) {
                        Row row = sheet.createRow(pos++);
                        row.createCell(0).setCellValue(vo.getMaterialName());
                        row.createCell(2).setCellValue(vo.getVendorName());
                        row.createCell(1).setCellValue(vo.getMaterialCode());
                        row.createCell(3).setCellValue(vo.getMaterialSpecification());
                        row.createCell(4).setCellValue(vo.getUnit());
                        row.createCell(5).setCellValue(vo.getApplyPigBarnName());
                        row.createCell(6).setCellValue(vo.getApplyPigGroupName());
                        row.createCell(7).setCellValue(vo.getApplyStaffName());
                        row.createCell(8).setCellValue(vo.getQuantity().doubleValue());
                        CellStyle style = workbook.createCellStyle();
                        //对齐
                        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                        row.createCell(9).setCellStyle(style);
                        row.createCell(10).setCellStyle(style);
                        if(vo.getUnitPrice()==0.0){
                            row.createCell(9).setCellValue("--");
                        }else{
                            row.createCell(9).setCellValue(vo.getUnitPrice());
                        }

                        if(vo.getAmount()==0.0){
                            row.createCell(10).setCellValue("--");
                        }else{
                            row.createCell(10).setCellValue(vo.getAmount());
                        }
                        row.createCell(11).setCellValue(vo.getRemark());

                        totalQuantity = totalQuantity.add(vo.getQuantity());
                        totalAmount += vo.getAmount();
                    }

                    Row countRow = sheet.createRow(pos);
                    CellRangeAddress cra = new CellRangeAddress(pos, pos, 0, 7);
                    sheet.addMergedRegion(cra);

                    Cell countCell = countRow.createCell(0);
                    CellStyle style = workbook.createCellStyle();
                    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                    countCell.setCellStyle(style);
                    countRow.createCell(10).setCellStyle(style);
                    countCell.setCellValue("合计");

                    countRow.createCell(8).setCellValue(totalQuantity.doubleValue());
                    if(totalAmount==0.0){
                        countRow.createCell(10).setCellValue("--");
                    }else{
                        countRow.createCell(10).setCellValue(totalAmount);
                    }

                    pos++;

                }
                //盘盈
                else if (stockHandle.getHandleSubType() .equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue())) {
                    title.createCell(0).setCellValue("物料名称");
                    title.createCell(1).setCellValue("物料编码");
                    title.createCell(2).setCellValue("厂家");
                    title.createCell(3).setCellValue("规格");
                    title.createCell(4).setCellValue("单位");
                    title.createCell(5).setCellValue("账面数量");
                    title.createCell(6).setCellValue("入库数量");
                    title.createCell(7).setCellValue("单价");
                    title.createCell(8).setCellValue("金额（元）");
                    title.createCell(9).setCellValue("备注");

                    BigDecimal totalQuantity = new BigDecimal(0);
                    for (StockHandleExportVo vo : exportVos) {

                        Row row = sheet.createRow(pos++);
                        row.createCell(0).setCellValue(vo.getMaterialName());
                        row.createCell(2).setCellValue(vo.getVendorName());
                        row.createCell(1).setCellValue(vo.getMaterialCode());
                        row.createCell(3).setCellValue(vo.getMaterialSpecification());
                        row.createCell(4).setCellValue(vo.getUnit());
                        row.createCell(5).setCellValue(vo.getBeforeInventoryQuantity().doubleValue());
                        row.createCell(6).setCellValue(vo.getQuantity().doubleValue());
                        if(vo.getUnitPrice()==0.0&&vo.getAmount()==0.0){
                            CellStyle style = workbook.createCellStyle();
                            //对齐
                            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                            row.createCell(7).setCellStyle(style);
                            row.createCell(8).setCellStyle(style);
                            row.createCell(7).setCellValue("--");
                            row.createCell(8).setCellValue("--");
                        }else{
                            row.createCell(7).setCellValue(vo.getUnitPrice());
                            row.createCell(8).setCellValue(vo.getAmount());
                        }
                        row.createCell(9).setCellValue(vo.getRemark());

                        totalQuantity = vo.getQuantity();
                    }

                    Row countRow = sheet.createRow(pos);
                    //表格范围
                    CellRangeAddress countRange = new CellRangeAddress(pos, pos, 0, 5);
                    //合并区域
                    sheet.addMergedRegion(countRange);

                    Cell countCell = countRow.createCell(0);
                    CellStyle style = workbook.createCellStyle();
                    //对齐
                    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                    countCell.setCellStyle(style);
                    countCell.setCellValue("盘盈");

                    countRow.createCell(6).setCellValue(totalQuantity.doubleValue());
                    pos++;

                }
                //盘亏
                else if (stockHandle.getHandleSubType() .equals(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue())) {
                        title.createCell(0).setCellValue("物料名称");
                        title.createCell(1).setCellValue("物料编码");
                        title.createCell(2).setCellValue("厂家");
                        title.createCell(3).setCellValue("规格");
                        title.createCell(4).setCellValue("单位");
                        title.createCell(5).setCellValue("账面数量");
                        title.createCell(6).setCellValue("出库数量");
                        title.createCell(7).setCellValue("单价");
                        title.createCell(8).setCellValue("金额（元）");
                        title.createCell(9).setCellValue("备注");

                        BigDecimal totalQuantity = new BigDecimal(0);
                       // BigDecimal inventoryQuantity = new BigDecimal(0);
                        double totalUnitPrice = 0L;
                        double totalAmount = 0L;

                        for (StockHandleExportVo vo : exportVos) {

                            Row row = sheet.createRow(pos++);
                            row.createCell(0).setCellValue(vo.getMaterialName());
                            row.createCell(2).setCellValue(vo.getVendorName());
                            row.createCell(1).setCellValue(vo.getMaterialCode());
                            row.createCell(3).setCellValue(vo.getMaterialSpecification());
                            row.createCell(4).setCellValue(vo.getUnit());
                            row.createCell(5).setCellValue(vo.getBeforeInventoryQuantity().doubleValue());
                            row.createCell(6).setCellValue(vo.getQuantity().doubleValue());
                            if(vo.getUnitPrice()==0.0&&vo.getAmount()==0.0){
                                CellStyle style = workbook.createCellStyle();
                                //对齐
                                style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                                row.createCell(7).setCellStyle(style);
                                row.createCell(8).setCellStyle(style);
                                row.createCell(7).setCellValue("--");
                                row.createCell(8).setCellValue("--");
                            }else{
                                row.createCell(7).setCellValue(vo.getUnitPrice());
                                row.createCell(8).setCellValue(vo.getAmount());
                            }
                            row.createCell(9).setCellValue(vo.getRemark());

                            totalQuantity = totalQuantity.add(vo.getQuantity());
                            //inventoryQuantity = vo.getBeforeInventoryQuantity();
                            totalUnitPrice += vo.getUnitPrice();
                            totalAmount += vo.getAmount();
                        }

                        Row countRow = sheet.createRow(pos);
                        //表格范围
                        CellRangeAddress countRange = new CellRangeAddress(pos, pos, 0, 5);
                        //合并区域
                        sheet.addMergedRegion(countRange);

                        Cell countCell = countRow.createCell(0);
                        CellStyle style = workbook.createCellStyle();
                        //对齐
                        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                        countCell.setCellStyle(style);
                        countCell.setCellValue("盘亏");

                        countRow.createCell(6).setCellValue(totalQuantity.doubleValue());

                        if(totalUnitPrice==0.0){
                            countRow.createCell(7).setCellValue("--");
                        }else{
                            countRow.createCell(7).setCellValue(totalUnitPrice);
                        }

                        if(totalAmount==0.0){
                            countRow.createCell(8).setCellValue("--");
                        }else{
                            countRow.createCell(8).setCellValue(totalAmount);
                        }
                        pos++;

                    }
                //调拨出库
                else if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.TRANSFER_OUT.getValue())) {
                    title.createCell(0).setCellValue("物料名称");
                    title.createCell(1).setCellValue("物料编码");
                    title.createCell(2).setCellValue("厂家");
                    title.createCell(3).setCellValue("规格");
                    title.createCell(4).setCellValue("单位");
                    title.createCell(5).setCellValue("当前数量");
                    title.createCell(6).setCellValue("调入猪场");
                    title.createCell(7).setCellValue("调入仓库");
                    title.createCell(8).setCellValue("出库数量");
                    title.createCell(9).setCellValue("单价");
                    title.createCell(10).setCellValue("金额（元）");
                    title.createCell(11).setCellValue("备注");

                    BigDecimal totalQuantity = new BigDecimal(0);
                    double totalAmount = 0L;

                    for (StockHandleExportVo vo : exportVos) {
                        Row row = sheet.createRow(pos++);
                        row.createCell(0).setCellValue(vo.getMaterialName());
                        row.createCell(1).setCellValue(vo.getMaterialCode());
                        row.createCell(2).setCellValue(vo.getVendorName());
                        row.createCell(3).setCellValue(vo.getMaterialSpecification());
                        row.createCell(4).setCellValue(vo.getUnit());
                        row.createCell(5).setCellValue(vo.getBeforeInventoryQuantity().doubleValue());
                        row.createCell(6).setCellValue(vo.getTransferInFarmName());
                        row.createCell(7).setCellValue(vo.getTransferInWarehouseName());
                        row.createCell(8).setCellValue(vo.getQuantity().doubleValue());
                        if(vo.getUnitPrice()==0.0&&vo.getAmount()==0.0){
                            CellStyle style = workbook.createCellStyle();
                            //对齐
                            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                            row.createCell(9).setCellStyle(style);
                            row.createCell(10).setCellStyle(style);
                            row.createCell(9).setCellValue("--");
                            row.createCell(10).setCellValue("--");
                        }else{
                            row.createCell(9).setCellValue(vo.getUnitPrice());
                            row.createCell(10).setCellValue(vo.getAmount());
                        }

                        row.createCell(11).setCellValue(vo.getRemark());

                        totalQuantity = totalQuantity.add(vo.getQuantity());
                        totalAmount += vo.getAmount();
                    }
                    Row countRow = sheet.createRow(pos);
                    //表格范围
                    CellRangeAddress countRange = new CellRangeAddress(pos, pos, 0, 5);
                    //合并区域
                    sheet.addMergedRegion(countRange);

                    Cell countCell = countRow.createCell(0);
                    CellStyle style = workbook.createCellStyle();
                    //对齐
                    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                    countCell.setCellStyle(style);
                    countCell.setCellValue("合计：");

                    countRow.createCell(8).setCellValue(totalQuantity.doubleValue());

                    if(totalAmount==0.0){
                        countRow.createCell(10).setCellValue("--");
                    }else{
                        countRow.createCell(10).setCellValue(totalAmount);
                    }
                    pos++;
                }
                //调拨入库
                else if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.TRANSFER_IN.getValue())) {
                    title.createCell(0).setCellValue("物料名称");
                    title.createCell(1).setCellValue("物料编码");
                    title.createCell(2).setCellValue("厂家");
                    title.createCell(3).setCellValue("规格");
                    title.createCell(4).setCellValue("单位");
                    title.createCell(5).setCellValue("入库数量");
                    title.createCell(6).setCellValue("单价");
                    title.createCell(7).setCellValue("金额（元）");
                    title.createCell(8).setCellValue("备注");

                    BigDecimal totalQuantity = new BigDecimal(0);
                    double totalAmount = 0L;

                    for (StockHandleExportVo vo : exportVos) {
                        Row row = sheet.createRow(pos++);
                        row.createCell(0).setCellValue(vo.getMaterialName());
                        row.createCell(1).setCellValue(vo.getMaterialCode());
                        row.createCell(2).setCellValue(vo.getVendorName());
                        row.createCell(3).setCellValue(vo.getMaterialSpecification());
                        row.createCell(4).setCellValue(vo.getUnit());
                        row.createCell(5).setCellValue(vo.getQuantity().doubleValue());
                        if(vo.getUnitPrice()==0.0&&vo.getAmount()==0.0){
                           CellStyle style = workbook.createCellStyle();
                           //对齐
                           style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                           row.createCell(6).setCellStyle(style);
                           row.createCell(7).setCellStyle(style);
                           row.createCell(6).setCellValue("--");
                           row.createCell(7).setCellValue("--");
                       }else{
                           row.createCell(6).setCellValue(vo.getUnitPrice());
                           row.createCell(7).setCellValue(vo.getAmount());
                       }
                        row.createCell(8).setCellValue(vo.getRemark());

                        totalQuantity = totalQuantity.add(vo.getQuantity());
                        totalAmount += vo.getAmount();
                    }

                    Row countRow = sheet.createRow(pos);
                    //表格范围
                    CellRangeAddress countRange = new CellRangeAddress(pos, pos, 0, 4);
                    //合并区域
                    sheet.addMergedRegion(countRange);

                    Cell countCell = countRow.createCell(0);
                    CellStyle style = workbook.createCellStyle();
                    //对齐
                    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                    countCell.setCellStyle(style);
                    countCell.setCellValue("合计");

                    countRow.createCell(5).setCellValue(totalQuantity.doubleValue());
                    countRow.createCell(7).setCellValue(totalAmount);

                    pos++;
                }
                //配方生成出库
                else if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.FORMULA_OUT.getValue())) {
                    title.createCell(0).setCellValue("物料名称");
                    title.createCell(1).setCellValue("物料编码");
                    title.createCell(2).setCellValue("厂家");
                    title.createCell(3).setCellValue("规格");
                    title.createCell(4).setCellValue("单位");
                    title.createCell(5).setCellValue("出库数量");
                    title.createCell(6).setCellValue("单价");
                    title.createCell(1).setCellValue("金额（元）");
                    title.createCell(8).setCellValue("备注");

                    for (StockHandleExportVo vo : exportVos) {
                        Row row = sheet.createRow(pos++);
                        row.createCell(0).setCellValue(vo.getMaterialName());
                        row.createCell(1).setCellValue(vo.getMaterialCode());
                        row.createCell(2).setCellValue(vo.getVendorName());
                        row.createCell(3).setCellValue(vo.getMaterialSpecification());
                        row.createCell(4).setCellValue(vo.getUnit());
                        row.createCell(5).setCellValue(vo.getQuantity().doubleValue());
                        if(vo.getUnitPrice()==0.0&&vo.getAmount()==0.0){
                            CellStyle style = workbook.createCellStyle();
                            //对齐
                            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                            row.createCell(6).setCellStyle(style);
                            row.createCell(7).setCellStyle(style);
                            row.createCell(6).setCellValue("--");
                            row.createCell(7).setCellValue("--");
                        }else{
                            row.createCell(6).setCellValue(vo.getUnitPrice());
                            row.createCell(7).setCellValue(vo.getAmount());
                        }
                        row.createCell(8).setCellValue(vo.getRemark());
                    }

                }
                //配方生产入库
                else if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.FORMULA_IN.getValue())) {
                    title.createCell(0).setCellValue("物料名称");
                    title.createCell(1).setCellValue("物料编码");
                    title.createCell(2).setCellValue("厂家");
                    title.createCell(3).setCellValue("规格");
                    title.createCell(4).setCellValue("单位");
                    title.createCell(5).setCellValue("入库数量");
                    title.createCell(6).setCellValue("单价");
                    title.createCell(7).setCellValue("金额（元）");
                    title.createCell(8).setCellValue("备注");

                    for (StockHandleExportVo vo : exportVos) {
                        Row row = sheet.createRow(pos++);
                        row.createCell(0).setCellValue(vo.getMaterialName());
                        row.createCell(1).setCellValue(vo.getMaterialCode());
                        row.createCell(2).setCellValue(vo.getVendorName());
                        row.createCell(3).setCellValue(vo.getMaterialSpecification());
                        row.createCell(4).setCellValue(vo.getUnit());
                        row.createCell(5).setCellValue(vo.getQuantity().doubleValue());
                        if(vo.getUnitPrice()==0.0&&vo.getAmount()==0.0){
                            CellStyle style = workbook.createCellStyle();
                            //对齐
                            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                            row.createCell(6).setCellStyle(style);
                            row.createCell(7).setCellStyle(style);
                            row.createCell(6).setCellValue("--");
                            row.createCell(7).setCellValue("--");
                        }else{
                            row.createCell(6).setCellValue(vo.getUnitPrice());
                            row.createCell(7).setCellValue(vo.getAmount());
                        }
                        row.createCell(8).setCellValue(vo.getRemark());
                    }
                }
                //退料入库
                else if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.RETURN.getValue())) {
                    title.createCell(0).setCellValue("物料名称");
                    title.createCell(1).setCellValue("物料编码");
                    title.createCell(2).setCellValue("厂家");
                    title.createCell(3).setCellValue("规格");
                    title.createCell(4).setCellValue("单位");
                    title.createCell(5).setCellValue("领用猪舍");
                    title.createCell(6).setCellValue("领用猪群");
                    title.createCell(7).setCellValue("可退数量");
                    title.createCell(8).setCellValue("退料数量");
                    title.createCell(9).setCellValue("单价(元)");
                    title.createCell(10).setCellValue("金额(元)");
                    title.createCell(11).setCellValue("备注");

                    BigDecimal totalQuantity = new BigDecimal(0);
                    double totalAmount = 0L;
                    for (StockHandleExportVo vo : exportVos) {
                        Row row = sheet.createRow(pos++);
                        row.createCell(0).setCellValue(vo.getMaterialName());
                        row.createCell(2).setCellValue(vo.getVendorName());
                        row.createCell(1).setCellValue(vo.getMaterialCode());
                        row.createCell(3).setCellValue(vo.getMaterialSpecification());
                        row.createCell(4).setCellValue(vo.getUnit());
                        row.createCell(5).setCellValue(vo.getApplyPigBarnName());
                        row.createCell(6).setCellValue(vo.getApplyPigGroupName());
                        row.createCell(7).setCellValue(vo.getBeforeInventoryQuantity().doubleValue());
                        row.createCell(8).setCellValue(vo.getQuantity().doubleValue());
                        CellStyle style = workbook.createCellStyle();
                        //对齐
                        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                        row.createCell(9).setCellStyle(style);
                        row.createCell(10).setCellStyle(style);
                        if(vo.getUnitPrice()==0.0){
                            row.createCell(9).setCellValue("--");
                        }else{
                            row.createCell(9).setCellValue(vo.getUnitPrice());
                        }

                        if(vo.getAmount()==0.0){
                            row.createCell(10).setCellValue("--");
                        }else{
                            row.createCell(10).setCellValue(vo.getAmount());
                        }
                        row.createCell(11).setCellValue(vo.getRemark());

                        totalQuantity = totalQuantity.add(vo.getQuantity());
                        totalAmount += vo.getAmount();
                    }

                    Row countRow = sheet.createRow(pos);
                    CellRangeAddress cra = new CellRangeAddress(pos, pos, 0, 5);
                    sheet.addMergedRegion(cra);

                    Cell countCell = countRow.createCell(0);
                    CellStyle style = workbook.createCellStyle();
                    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                    countCell.setCellStyle(style);
                    countRow.createCell(8).setCellStyle(style);
                    countCell.setCellValue("合计");

                    countRow.createCell(8).setCellValue(totalQuantity.doubleValue());

                    if(totalAmount==0.0){
                        countRow.createCell(10).setCellValue("--");
                    }else{
                        countRow.createCell(10).setCellValue(totalAmount);
                    }

                    pos++;

                }

                Row foot = sheet.createRow(pos);
                foot.createCell(0).setCellValue("仓管员");
                foot.createCell(1).setCellValue(wareHouse.getManagerName());
                foot.createCell(2).setCellValue("操作人");
                foot.createCell(3).setCellValue(stockHandle.getOperatorName());
                foot.createCell(4).setCellValue("所属公司");
                foot.createCell(5).setCellValue(farm.getOrgName());

                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    @RequestMapping(method = RequestMethod.GET, value = "/stockPage")
    public Paging<DoctorWarehouseStockHandle> stockPage(
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer farmId,
            @RequestParam(required = false,value = "warehouseId") String warehouseId,
            @RequestParam(required = false,value = "operatorId") String operatorId,
            @RequestParam(required = false,value = "handleSubType") Integer handleSubType,
            @RequestParam(required = false,value = "handleDateStart") Date handleDateStart,
            @RequestParam(required = false,value = "handleDateEnd") Date handleDateEnd,
            @RequestParam(required = false,value = "updatedAtStart") Date updatedAtStart,
            @RequestParam(required = false,value = "updatedAtEnd") Date updatedAtEnd
    ) {

        if (null != handleDateStart && null != handleDateEnd && handleDateStart.after(handleDateEnd))
            throw new JsonResponseException("start.date.after.end.date");

        if (null != updatedAtStart && null != updatedAtEnd && updatedAtStart.after(updatedAtEnd))
            throw new JsonResponseException("start.date.after.end.date");

        Map<String, Object> params = new HashMap<>();
        if(warehouseId!=null&&!"".equals(warehouseId))
        params.put("warehouseId", warehouseId);
        if(operatorId!=null&&!"".equals(operatorId))
        params.put("operatorId", operatorId);
        params.put("handleSubType", handleSubType);
        params.put("handleDateStart", handleDateStart);
        params.put("handleDateEnd", handleDateEnd);
        params.put("updatedAtStart", updatedAtStart);
        params.put("farmId",farmId);
        params.put("updatedAtEnd", updatedAtEnd);
        return RespHelper.or500(doctorWarehouseStockHandleReadService.paging(pageNo, pageSize, params));
    }

}
