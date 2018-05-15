package io.terminus.doctor.web.front.warehouseV2;

import com.google.api.client.util.Charsets;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.*;
import io.terminus.doctor.basic.service.DoctorWareHouseReadService;
import io.terminus.doctor.basic.service.warehouseV2.*;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.warehouseV2.vo.StockHandleExportVo;
import io.terminus.doctor.web.front.warehouseV2.vo.StockHandleVo;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseEventExportVo;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
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
import org.springframework.boot.autoconfigure.web.ConditionalOnEnabledResourceChain;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import io.terminus.common.model.Response;

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

    @InitBinder
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
    }

    //退料入库
    //得到仓库类型，仓库名称，仓库管理员，所属公司
    @RequestMapping(method = RequestMethod.GET, value = "/getFarmData")
    public List<Map> getFarmData(@RequestParam Long id) {
        List<Map> maps = RespHelper.or500(doctorWarehouseMaterialHandleReadService.getFarmData(id));
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
    public List<Map> getDataByMaterialName(@RequestParam Long id,
                                           @RequestParam String materialName) {
//        if(null==handleDate){
//            Date dd=new Date(System.currentTimeMillis());
//            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
//            handleDate=sdf.format(dd);
//        }
        List<Map> maps = RespHelper.or500(doctorWarehouseMaterialHandleReadService.getDataByMaterialName(id,materialName));
        return maps;
    }

    //退料入库前的数据展示
    @RequestMapping(method = RequestMethod.GET, value = "/getRetreatingData")
    public List<Map> getRetreatingData(@RequestParam Long id) {

        //得到领料出库的物料名称
        List<Map> map= RespHelper.or500(doctorWarehouseMaterialHandleReadService.getMaterialNameByID(id));
        List<Map> maps=new ArrayList<Map>();
//        if(null==handleDate){
//            Date dd=new Date(System.currentTimeMillis());
//            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
//            handleDate=sdf.format(dd);
//        }
        for(Map mm:map){
            String materialName = String.valueOf(mm.get("material_name"));
            //根据物料名称得到 物料名称，物料编号，厂家，规格，单位，可退数量，备注
            List<Map> mp = RespHelper.or500(doctorWarehouseMaterialHandleReadService.getDataByMaterialName(id, materialName));
            maps.addAll(mp);
        }

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
    public StockHandleVo query(@PathVariable Long id) {

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

                            //物料表
                            DoctorWarehouseSku sku = RespHelper.or500(doctorWarehouseSkuReadService.findById(mh.getMaterialId()));
                            if (null != sku) {
                                detail.setVendorName(RespHelper.or500(doctorWarehouseVendorReadService.findNameById(sku.getVendorId())));
                                detail.setMaterialCode(sku.getCode());
                                //得到单位名称
                                String nameByUnit = RespHelper.or500(doctorWarehouseStockHandleReadService.getNameByUnit(Long.parseLong(sku.getUnit())));
                                detail.setUnit(nameByUnit);
                                detail.setMaterialSpecification(sku.getSpecification());
                            } else {
                                log.warn("sku not found,{}", mh.getMaterialId());
                            }

                            //物料领用表
                            if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.OUT.getValue())) {
                                DoctorWarehouseMaterialApply apply = RespHelper.or500(doctorWarehouseMaterialApplyReadService.findByMaterialHandle(mh.getId()));
                                if (null != apply) {
                                    detail.setApplyPigBarnName(apply.getPigBarnName());
                                    detail.setApplyPigBarnId(apply.getPigBarnId());
                                    detail.setApplyPigGroupName(apply.getPigGroupName());
                                    detail.setApplyPigGroupId(apply.getPigGroupId());
                                    detail.setApplyStaffName(apply.getApplyStaffName());
                                    detail.setApplyStaffId(apply.getApplyStaffId());
                                } else
                                    log.warn("material apply not found,by material handle {}", mh.getId());
                            }

                            //退料入库-->可退数量
                            if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.RETURN.getValue())) {
                                BigDecimal RefundableNumber = new BigDecimal(0);
                                //得到领料出库的数量
                                BigDecimal LibraryQuantity = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findLibraryById(mh.getRelMaterialHandleId(),mh.getMaterialName()));
                                //得到在此之前退料入库的数量和
                                BigDecimal RetreatingQuantity = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findRetreatingById(mh.getRelMaterialHandleId(),mh.getMaterialName(),stockHandle.getId()));
                                RefundableNumber = LibraryQuantity.subtract(RetreatingQuantity);
                                detail.setRefundableQuantity(RefundableNumber.doubleValue());
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

                            return detail;
                        })
                        .collect(Collectors.toList()));

        //配方生产出库
        if (stockHandle.getHandleSubType().equals( WarehouseMaterialHandleType.FORMULA_OUT.getValue())) {
            String warehouseName = RespHelper.or500(doctorWarehouseStockHandleReadService.findwarehouseName(stockHandle.getRelStockHandleId()));
            vo.setStorageWarehouseName(warehouseName);
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
        for (StockHandleVo.Detail detail : vo.getDetails()) {
            totalQuantity = totalQuantity.add(detail.getQuantity());
            totalUnitPrice = totalUnitPrice.add(null == detail.getUnitPrice() ? new BigDecimal(0) : detail.getUnitPrice());
        }
        vo.setTotalQuantity(totalQuantity.doubleValue());
        vo.setTotalAmount(totalQuantity.multiply(totalUnitPrice).doubleValue());

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
        return doctorWarehouseStockHandleWriteService.delete(id);
           /*if (!response.isSuccess())
               throw new JsonResponseException(response.getError());
           return true;*/
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
    public void export(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {

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
                        DoctorWarehouseMaterialApply apply = RespHelper.or500(doctorWarehouseMaterialApplyReadService.findByMaterialHandle(mh.getId()));
                        if (null != apply) {
                            vo.setApplyPigBarnName(apply.getPigBarnName());
                            vo.setApplyPigGroupName(apply.getPigGroupName());
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
                        BigDecimal RefundableNumber = new BigDecimal(0);
                        //得到领料出库的数量
                        BigDecimal LibraryQuantity = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findLibraryById(mh.getRelMaterialHandleId(),mh.getMaterialName()));
                        //得到在此之前退料入库的数量和
                        BigDecimal RetreatingQuantity = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findRetreatingById(mh.getRelMaterialHandleId(),mh.getMaterialName(),stockHandle.getId()));
                        RefundableNumber = LibraryQuantity.subtract(RetreatingQuantity);
                        vo.setBeforeInventoryQuantity(RefundableNumber);
                    }

                    //配方生产出库
                    if (stockHandle.getHandleSubType().equals( WarehouseMaterialHandleType.FORMULA_OUT.getValue())) {
                        String warehouseName = RespHelper.or500(doctorWarehouseStockHandleReadService.findwarehouseName(stockHandle.getRelStockHandleId()));
                        vo.setTransferInWarehouseName(warehouseName);
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
                        row.createCell(9).setCellValue(vo.getUnitPrice());
                        row.createCell(10).setCellValue(vo.getAmount());
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
                    countCell.setCellValue("合计");

                    countRow.createCell(8).setCellValue(totalQuantity.doubleValue());
                    countRow.createCell(10).setCellValue(totalAmount);
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
                    title.createCell(6).setCellValue("盘点数量");
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
                        row.createCell(6).setCellValue(vo.getBeforeInventoryQuantity().add(vo.getQuantity()).doubleValue());
                        row.createCell(7).setCellValue(vo.getUnitPrice());
                        row.createCell(8).setCellValue(vo.getAmount());
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
                        title.createCell(6).setCellValue("盘点数量");
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
                            row.createCell(6).setCellValue(vo.getBeforeInventoryQuantity().subtract(vo.getQuantity()).doubleValue());
                            row.createCell(7).setCellValue(vo.getUnitPrice());
                            row.createCell(8).setCellValue(vo.getAmount());
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
                        countCell.setCellValue("盘亏");

                        countRow.createCell(6).setCellValue(totalQuantity.doubleValue());
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
                        row.createCell(9).setCellValue(vo.getUnitPrice());
                        row.createCell(10).setCellValue(vo.getAmount());
                        row.createCell(11).setCellValue(vo.getRemark());
                    }
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

                    for (StockHandleExportVo vo : exportVos) {
                        Row row = sheet.createRow(pos++);
                        row.createCell(0).setCellValue(vo.getMaterialName());
                        row.createCell(1).setCellValue(vo.getMaterialCode());
                        row.createCell(2).setCellValue(vo.getVendorName());
                        row.createCell(3).setCellValue(vo.getMaterialSpecification());
                        row.createCell(4).setCellValue(vo.getUnit());
                        row.createCell(5).setCellValue(vo.getQuantity().doubleValue());
                        row.createCell(6).setCellValue(vo.getUnitPrice());
                        row.createCell(7).setCellValue(vo.getAmount());
                        row.createCell(8).setCellValue(vo.getRemark());
                    }
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
                        row.createCell(6).setCellValue(vo.getUnitPrice());
                        row.createCell(7).setCellValue(vo.getAmount());
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
                        row.createCell(6).setCellValue(vo.getUnitPrice());
                        row.createCell(7).setCellValue(vo.getAmount());
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
                    title.createCell(5).setCellValue("可退数量");
                    title.createCell(6).setCellValue("退料数量");
                    title.createCell(7).setCellValue("单价(元)");
                    title.createCell(8).setCellValue("金额(元)");
                    title.createCell(9).setCellValue("备注");

                    BigDecimal totalQuantity = new BigDecimal(0);
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
                        row.createCell(7).setCellValue(vo.getUnitPrice());
                        row.createCell(8).setCellValue(vo.getAmount());
                        row.createCell(9).setCellValue(vo.getRemark());

                        totalQuantity = totalQuantity.add(vo.getQuantity());
                        totalAmount += vo.getAmount();
                    }

                    Row countRow = sheet.createRow(pos);
                    CellRangeAddress cra = new CellRangeAddress(pos, pos, 0, 4);
                    sheet.addMergedRegion(cra);

                    Cell countCell = countRow.createCell(0);
                    CellStyle style = workbook.createCellStyle();
                    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                    countCell.setCellStyle(style);
                    countCell.setCellValue("合计");

                    countRow.createCell(6).setCellValue(totalQuantity.doubleValue());
                    countRow.createCell(8).setCellValue(totalAmount);
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
