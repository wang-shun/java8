package io.terminus.doctor.web.front.new_warehouse;

import com.fasterxml.jackson.annotation.JsonView;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.AmountAndQuantityDto;
import io.terminus.doctor.basic.dto.DoctorWareHouseCriteria;
import io.terminus.doctor.basic.dto.warehouse.WarehouseMaterialDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehousePurchase;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.*;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.web.front.new_warehouse.dto.WarehouseDto;
import io.terminus.doctor.web.front.new_warehouse.vo.FarmWarehouseVo;
import io.terminus.doctor.web.front.new_warehouse.vo.WarehouseStatisticsVo;
import io.terminus.doctor.web.front.new_warehouse.vo.WarehouseStockVo;
import io.terminus.doctor.web.front.new_warehouse.vo.WarehouseVo;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
import io.terminus.parana.user.service.UserReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.math.BigDecimal;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by sunbo@terminus.io on 2017/8/8.
 */
@RestController
@RequestMapping("api/doctor/warehouse")
public class WarehouseController {


    @Autowired
    private DoctorFarmReadService doctorFarmReadService;

    @Autowired
    private UserReadService<User> userReadService;

    @Autowired
    private DoctorWareHouseWriteService doctorWareHouseWriteService;


    @Autowired
    private DoctorUserProfileReadService doctorUserProfileReadService;
    @RpcConsumer
    private NewDoctorWarehouseReaderService doctorWarehouseReaderService;


    @RpcConsumer
    private DoctorWarehouseMaterialHandleReadService doctorWarehouseMaterialHandleReadService;
    @RpcConsumer
    private DoctorWarehousePurchaseReadService doctorWarehousePurchaseReadService;

    @RpcConsumer
    private NewDoctorWarehouseWriterService newDoctorWarehouseWriterService;

    @RpcConsumer
    private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

    @RpcConsumer
    private DoctorFarmBasicReadService doctorFarmBasicReadService;

    @RpcConsumer
    private DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    @RpcConsumer
    private DoctorWarehouseStockReadService doctorWarehouseStockReadService;

    @RpcConsumer
    private DoctorWarehouseStockWriteService doctorWarehouseStockWriteService;

    /**
     * 创建仓库
     *
     * @param warehouseDto
     * @param errors
     */
    @RequestMapping(method = RequestMethod.POST)
    public void create(@RequestBody @Valid WarehouseDto warehouseDto, Errors errors) {

        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        Response<DoctorFarm> farmResponse = doctorFarmReadService.findFarmById(warehouseDto.getFarmId());
        checkState(farmResponse.isSuccess(), "read.farmInfo.fail");
        DoctorFarm doctorFarm = farmResponse.getResult();

        if (doctorFarm == null)
            throw new JsonResponseException("farm.not.found");

        UserProfile userProfile = RespHelper.orServEx(doctorUserProfileReadService.findProfileByUserId(warehouseDto.getManagerId()));

        Response<User> currentUserResponse = userReadService.findById(UserUtil.getUserId());
        User currentUser = currentUserResponse.getResult();
        if (null == currentUser)
            throw new JsonResponseException("user.not.login");

        DoctorWareHouse doctorWareHouse = DoctorWareHouse.builder()
                .wareHouseName(warehouseDto.getName())
                .farmId(warehouseDto.getFarmId()).farmName(doctorFarm.getName())
                .managerId(warehouseDto.getManagerId()).managerName(userProfile.getRealName())
                .address(warehouseDto.getAddress()).type(warehouseDto.getType())
                .creatorId(currentUser.getId()).creatorName(currentUser.getName())
                .build();
        doctorWareHouseWriteService.createWareHouse(doctorWareHouse);
    }


    @RequestMapping(method = RequestMethod.GET)
    public Paging<DoctorWareHouse> query(@Valid DoctorWareHouseCriteria criteria) {

        return doctorWarehouseReaderService.paging(criteria).getResult();
    }

    /**
     * 猪厂下同类型的仓库列表
     *
     * @param type
     * @param farmId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "type/{type}")
    @JsonView(WarehouseVo.WarehouseWithOutStatisticsView.class)
    public List<WarehouseVo> sameTypeWarehouse(@PathVariable Integer type, @RequestParam Long farmId) {
        DoctorWareHouse criteria = new DoctorWareHouse();
        criteria.setType(type);
        criteria.setFarmId(farmId);
        Response<List<DoctorWareHouse>> warehouseResponse = doctorWarehouseReaderService.list(criteria);
        if (!warehouseResponse.isSuccess())
            throw new JsonResponseException(warehouseResponse.getError());
        List<WarehouseVo> vos = new ArrayList<>(warehouseResponse.getResult().size());
        warehouseResponse.getResult().forEach(wareHouse -> {
            WarehouseVo vo = new WarehouseVo();
            vo.setId(wareHouse.getId());
            vo.setName(wareHouse.getWareHouseName());
            vo.setType(wareHouse.getType());
            vo.setManagerName(wareHouse.getManagerName());
            vo.setManagerId(wareHouse.getManagerId());
            vos.add(vo);
        });
        return vos;
    }

    @RequestMapping(method = RequestMethod.GET, value = "type/statistics")
    @JsonView(WarehouseVo.WarehouseView.class)
    public List<WarehouseVo> sameTypeWarehouseStatistics(@RequestParam Long farmId) {

        Response<List<DoctorWareHouse>> warehousesResponse = doctorWarehouseReaderService.findByFarmId(farmId);
        if (!warehousesResponse.isSuccess())
            throw new JsonResponseException(warehousesResponse.getError());

        Map<Long, Integer> warehouseTypes = new HashMap<>();
        for (DoctorWareHouse wareHouse : warehousesResponse.getResult()) {
            warehouseTypes.put(wareHouse.getId(), wareHouse.getType());
        }

        Response<List<DoctorWarehousePurchase>> purchasesResponse = doctorWarehousePurchaseReadService.list(DoctorWarehousePurchase.builder()
                .farmId(farmId)
                .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
                .build());
        if (!purchasesResponse.isSuccess())
            throw new JsonResponseException(purchasesResponse.getError());

        //统计余量
        Map<Integer/*warehouseType*/, BigDecimal> quantityStatistics = new HashMap<>();
        for (DoctorWarehousePurchase purchase : purchasesResponse.getResult()) {
            Integer type = warehouseTypes.get(purchase.getWarehouseId());
            if (!quantityStatistics.containsKey(type)) {
                quantityStatistics.put(type, purchase.getQuantity().multiply(purchase.getHandleQuantity()));
            } else {
                BigDecimal quantity = quantityStatistics.get(type);
                quantityStatistics.put(type, purchase.getQuantity().multiply(purchase.getHandleQuantity()).add(quantity));
            }
        }

        //饲料最近一次领用记录
        Response<List<DoctorWarehouseMaterialApply>> feedapplyResponse = doctorWarehouseMaterialApplyReadService.listOrderByHandleDate(DoctorWarehouseMaterialApply.builder()
                .type(WareHouseType.FEED.getKey())
                .build(), 1);
        if (!feedapplyResponse.isSuccess())
            throw new JsonResponseException(feedapplyResponse.getError());
        //原料最近一次领用记录
        Response<List<DoctorWarehouseMaterialApply>> materialApplyResponse = doctorWarehouseMaterialApplyReadService.listOrderByHandleDate(DoctorWarehouseMaterialApply.builder()
                .type(WareHouseType.MATERIAL.getKey())
                .build(), 1);
        if (!materialApplyResponse.isSuccess())
            throw new JsonResponseException(materialApplyResponse.getError());
        //疫苗最近一次领用记录
        Response<List<DoctorWarehouseMaterialApply>> vaccinationApplyResponse = doctorWarehouseMaterialApplyReadService.listOrderByHandleDate(DoctorWarehouseMaterialApply.builder()
                .type(WareHouseType.VACCINATION.getKey())
                .build(), 1);
        if (!vaccinationApplyResponse.isSuccess())
            throw new JsonResponseException(vaccinationApplyResponse.getError());
        //疫苗最近一次领用记录
        Response<List<DoctorWarehouseMaterialApply>> medicineApplyResponse = doctorWarehouseMaterialApplyReadService.listOrderByHandleDate(DoctorWarehouseMaterialApply.builder()
                .type(WareHouseType.MEDICINE.getKey())
                .build(), 1);
        if (!medicineApplyResponse.isSuccess())
            throw new JsonResponseException(medicineApplyResponse.getError());
        //疫苗最近一次领用记录
        Response<List<DoctorWarehouseMaterialApply>> consumerApplyResponse = doctorWarehouseMaterialApplyReadService.listOrderByHandleDate(DoctorWarehouseMaterialApply.builder()
                .type(WareHouseType.CONSUME.getKey())
                .build(), 1);
        if (!consumerApplyResponse.isSuccess())
            throw new JsonResponseException(consumerApplyResponse.getError());

        List<WarehouseVo> vos = new ArrayList<>(5);
        vos.add(WarehouseVo.builder()
                .type(WareHouseType.FEED.getKey())
                .balanceQuantity(quantityStatistics.get(WareHouseType.FEED.getKey()))
                .lastApplyDate(feedapplyResponse.getResult().get(0).getApplyDate())
                .build());
        vos.add(WarehouseVo.builder()
                .type(WareHouseType.MATERIAL.getKey())
                .balanceQuantity(quantityStatistics.get(WareHouseType.MATERIAL.getKey()))
                .lastApplyDate(materialApplyResponse.getResult().get(0).getApplyDate())
                .build());
        vos.add(WarehouseVo.builder()
                .type(WareHouseType.VACCINATION.getKey())
                .balanceQuantity(quantityStatistics.get(WareHouseType.FEED.getKey()))
                .lastApplyDate(vaccinationApplyResponse.getResult().get(0).getApplyDate())
                .build());
        vos.add(WarehouseVo.builder()
                .type(WareHouseType.MEDICINE.getKey())
                .balanceQuantity(quantityStatistics.get(WareHouseType.FEED.getKey()))
                .lastApplyDate(medicineApplyResponse.getResult().get(0).getApplyDate())
                .build());
        vos.add(WarehouseVo.builder()
                .type(WareHouseType.CONSUME.getKey())
                .balanceQuantity(quantityStatistics.get(WareHouseType.FEED.getKey()))
                .lastApplyDate(consumerApplyResponse.getResult().get(0).getApplyDate())
                .build());
        return vos;
    }

    /**
     * 猪厂下所有仓库，及仓库的出入库等统计信息
     *
     * @param farmId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "farm/{farmId}")
    public List<FarmWarehouseVo> query(@PathVariable Long farmId) {

        Response<List<DoctorWareHouse>> warehouseResponse = doctorWarehouseReaderService.findByFarmId(farmId);
        if (!warehouseResponse.isSuccess())
            throw new JsonResponseException(warehouseResponse.getError());

        List<FarmWarehouseVo> vos = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        warehouseResponse.getResult().forEach(wareHouse -> {

            FarmWarehouseVo vo = new FarmWarehouseVo();
            vo.setId(wareHouse.getId());
            vo.setName(wareHouse.getWareHouseName());
            vo.setType(wareHouse.getType());
            vo.setManagerName(wareHouse.getManagerName());

            DoctorWarehouseMaterialHandle handleCriteria = new DoctorWarehouseMaterialHandle();
            handleCriteria.setWarehouseId(wareHouse.getId());
            handleCriteria.setHandleYear(now.get(Calendar.YEAR));
            handleCriteria.setHandleMonth(now.get(Calendar.MONTH) + 1);
            Response<List<DoctorWarehouseMaterialHandle>> thisMonthHandlesResponse = doctorWarehouseMaterialHandleReadService.list(handleCriteria);
            if (!thisMonthHandlesResponse.isSuccess())
                throw new JsonResponseException(thisMonthHandlesResponse.getError());

            thisMonthHandlesResponse.getResult().forEach(handle -> {
                long money = handle.getQuantity().multiply(new BigDecimal(handle.getUnitPrice())).longValue();
                if (handle.getType() == WarehouseMaterialHandleType.IN.getValue()) {
                    vo.setInAmount(vo.getInAmount() + money);
                    vo.setInQuantity(vo.getInQuantity().add(handle.getQuantity()));
                } else if (handle.getType() == WarehouseMaterialHandleType.OUT.getValue()) {
                    vo.setOutAmount(vo.getOutAmount() + money);
                    vo.setOutQuantity(vo.getOutQuantity().add(handle.getQuantity()));
                } else if (handle.getType() == WarehouseMaterialHandleType.TRANSFER.getValue()) {
                    vo.setTransferOutAmount(vo.getTransferOutAmount() + money);
                    vo.setTransferOutQuantity(vo.getTransferOutQuantity().add(handle.getQuantity()));
                }
            });
            //调拨，调入
            handleCriteria = new DoctorWarehouseMaterialHandle();
            handleCriteria.setTargetWarehouseId(wareHouse.getId());
            handleCriteria.setHandleYear(now.get(Calendar.YEAR));
            handleCriteria.setHandleMonth(now.get(Calendar.MONTH) + 1);
            handleCriteria.setType(WarehouseMaterialHandleType.TRANSFER.getValue());
            thisMonthHandlesResponse = doctorWarehouseMaterialHandleReadService.list(handleCriteria);
            if (!thisMonthHandlesResponse.isSuccess())
                throw new JsonResponseException(thisMonthHandlesResponse.getError());
            thisMonthHandlesResponse.getResult().forEach(handle -> {
                vo.setTransferInAmount(vo.getTransferInAmount() + handle.getQuantity().multiply(new BigDecimal(handle.getUnitPrice())).longValue());
                vo.setTransferInQuantity(vo.getTransferInQuantity().add(handle.getQuantity()));
            });


            //余额和余量
            DoctorWarehousePurchase purchaseCriteria = new DoctorWarehousePurchase();
            purchaseCriteria.setHandleFinishFlag(1);
            purchaseCriteria.setWarehouseId(wareHouse.getId());
            Response<List<DoctorWarehousePurchase>> warehousePurchasesResponse = doctorWarehousePurchaseReadService.list(purchaseCriteria);
            if (!warehousePurchasesResponse.isSuccess())
                throw new JsonResponseException(warehousePurchasesResponse.getError());
            BigDecimal totalQuantity = new BigDecimal(0);
            long totalMoney = 0L;
            for (DoctorWarehousePurchase purchase : warehousePurchasesResponse.getResult()) {
                BigDecimal leftQuantity = purchase.getQuantity().subtract(purchase.getHandleQuantity());
                totalQuantity = totalQuantity.add(leftQuantity);
                totalMoney += leftQuantity.multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
            }
            vo.setBalanceQuantity(totalQuantity);
            vo.setBalanceAmount(totalMoney);
            vos.add(vo);

        });
        return vos;
    }

    /**
     * 仓库及仓库最近一次领用信息
     *
     * @param id
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    @JsonView(WarehouseVo.WarehouseView.class)
    public WarehouseVo find(@PathVariable Long id) {
        Response<DoctorWareHouse> wareHouseResponse = doctorWarehouseReaderService.findById(id);
        if (!wareHouseResponse.isSuccess())
            throw new JsonResponseException(wareHouseResponse.getError());
        if (null == wareHouseResponse.getResult())
            throw new JsonResponseException("warehouse.not.found");

        //最近一次领用记录
        DoctorWarehouseMaterialApply applyCriteria = new DoctorWarehouseMaterialApply();
        applyCriteria.setWarehouseId(id);
        Response<List<DoctorWarehouseMaterialApply>> applyResponse = doctorWarehouseMaterialApplyReadService.listOrderByHandleDate(applyCriteria, 1);
        if (!applyResponse.isSuccess())
            throw new JsonResponseException(applyResponse.getError());

        WarehouseVo vo = new WarehouseVo();
        vo.setId(id);
        vo.setName(wareHouseResponse.getResult().getWareHouseName());
        vo.setType(wareHouseResponse.getResult().getType());
        vo.setManagerId(wareHouseResponse.getResult().getManagerId());
        vo.setManagerName(wareHouseResponse.getResult().getManagerName());

        if (null != applyResponse && !applyResponse.getResult().isEmpty())
            vo.setLastApplyDate(applyResponse.getResult().get(0).getApplyDate());

        Response<AmountAndQuantityDto> amountAndQuantityDtoResponse = doctorWarehouseReaderService.countWarehouseBalance(id);
        if (!amountAndQuantityDtoResponse.isSuccess())
            throw new JsonResponseException(amountAndQuantityDtoResponse.getError());

        vo.setBalanceQuantity(amountAndQuantityDtoResponse.getResult().getQuantity());

        return vo;
    }

    /**
     * 仓库的出入库，余额，余量统计
     *
     * @param id
     * @param date
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "{id}/statistics")
    public WarehouseStatisticsVo warehouseInAndOut(@PathVariable Long id,
                                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM") Calendar date) {


        DoctorWarehouseMaterialHandle handleCriteria = DoctorWarehouseMaterialHandle.builder()
                .handleYear(date.get(Calendar.YEAR))
                .handleMonth(date.get(Calendar.MONTH) + 1)
                .warehouseId(id)
                .type(WarehouseMaterialHandleType.IN.getValue())
                .build();

        Response<List<DoctorWarehouseMaterialHandle>> inHandlesResponse = doctorWarehouseMaterialHandleReadService.list(handleCriteria);
        if (!inHandlesResponse.isSuccess())
            throw new JsonResponseException(inHandlesResponse.getError());

        long totalInAmount = 0;
        BigDecimal totalInQuantity = new BigDecimal(0);
        for (DoctorWarehouseMaterialHandle inHandle : inHandlesResponse.getResult()) {
            totalInQuantity = totalInQuantity.add(inHandle.getQuantity());
            totalInAmount += inHandle.getQuantity().multiply(new BigDecimal(inHandle.getUnitPrice())).longValue();
        }

        handleCriteria.setType(WarehouseMaterialHandleType.OUT.getValue());
        Response<List<DoctorWarehouseMaterialHandle>> outHandlesResponse = doctorWarehouseMaterialHandleReadService.list(handleCriteria);
        if (!outHandlesResponse.isSuccess())
            throw new JsonResponseException(outHandlesResponse.getError());
        long totalOutAmount = 0;
        BigDecimal totalOutQuantity = new BigDecimal(0);
        for (DoctorWarehouseMaterialHandle outHandle : outHandlesResponse.getResult()) {
            totalOutQuantity = totalOutQuantity.add(outHandle.getQuantity());
            totalOutAmount += outHandle.getQuantity().multiply(new BigDecimal(outHandle.getUnitPrice())).longValue();
        }


        Response<AmountAndQuantityDto> amountAndQuantityResponse = doctorWarehouseReaderService.countWarehouseBalance(id);
        if (!amountAndQuantityResponse.isSuccess())
            throw new JsonResponseException(amountAndQuantityResponse.getError());


        return WarehouseStatisticsVo.builder()
                .id(id)
                .balanceQuantity(amountAndQuantityResponse.getResult().getQuantity())
                .balanceAmount(amountAndQuantityResponse.getResult().getAmount())
                .inAmount(totalInAmount)
                .inQuantity(totalInQuantity)
                .outAmount(totalOutAmount)
                .outQuantity(totalOutQuantity)
                .build();
    }


    /**
     * 仓库可以添加的物料列表
     */
    @RequestMapping(method = RequestMethod.GET, value = "material")
    public List<DoctorBasicMaterial> farmMaterail(@RequestParam Long farmId, @RequestParam Long type) {
        return RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialsOwned(farmId, type, null));
    }

    /**
     * 仓库的已添加的物料
     *
     * @param id
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "{id}/material")
    public List<WarehouseStockVo> material(@PathVariable Long id,
                                           @RequestParam(required = false) String materialName,
                                           @RequestParam(required = false) Integer pageNo,
                                           @RequestParam(required = false) Integer pageSize) {

        //TODO 添加物料名的模糊搜索
        Response<Paging<DoctorWarehouseStock>> stockResponse = doctorWarehouseStockReadService.pagingMergeVendor(pageNo, pageSize, DoctorWarehouseStock.builder()
                .warehouseId(id)
                .build());

        if (!stockResponse.isSuccess())
            throw new JsonResponseException(stockResponse.getError());


        List<WarehouseStockVo> vos = new ArrayList<>(stockResponse.getResult().getData().size());
        for (DoctorWarehouseStock stock : stockResponse.getResult().getData()) {
            vos.add(WarehouseStockVo.builder()
                    .materialId(stock.getMaterialId())
                    .materialName(stock.getMaterialName())
                    .quantity(stock.getQuantity())
                    .unit(stock.getUnit())
                    .build());
        }

        return vos;
    }


    /**
     * 仓库下添加物料
     *
     * @param id
     * @param warehouseMaterialDto
     */
    @RequestMapping(method = RequestMethod.POST, value = "{id}/material")
    public void material(@PathVariable Long id,
                         @RequestBody @Valid WarehouseMaterialDto warehouseMaterialDto,
                         Errors errors) {

        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        DoctorBasicMaterial material = RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(warehouseMaterialDto.getMaterialId()));
        if (material == null) {
            throw new JsonResponseException("basicMaterial.not.found");
        }
        DoctorWareHouse wareHouse = RespHelper.or500(doctorWarehouseReaderService.findById(id));
        if (wareHouse == null) {
            throw new JsonResponseException("warehouse.not.found");
        }
        if (!wareHouse.getType().equals(material.getType())) {
            throw new JsonResponseException("warehouse.material.type.not.match"); // 仓库与物料类型不一致
        }
        Response<DoctorFarmBasic> farmBasicResponse = doctorFarmBasicReadService.findFarmBasicByFarmId(wareHouse.getFarmId());
        if (!farmBasicResponse.isSuccess())
            throw new ServiceException(farmBasicResponse.getError());
        DoctorFarmBasic farmBasic = farmBasicResponse.getResult();
        if (null == farmBasic)
            throw new JsonResponseException("farm.basic.not.found");

        List<Long> currentFarmSupportedMaterials = farmBasic.getMaterialIdList();
        if (!currentFarmSupportedMaterials.contains(warehouseMaterialDto.getMaterialId()))
            throw new JsonResponseException("material.not.allow.in.this.warehouse");

        Response<Boolean> existedResponse = doctorWarehouseStockReadService.existed(DoctorWarehouseStock.builder()
                .warehouseId(id)
                .materialId(warehouseMaterialDto.getMaterialId())
                .build());
        if (!existedResponse.isSuccess())
            throw new JsonResponseException(existedResponse.getError());
        if (existedResponse.getResult())
            throw new JsonResponseException("warehouse.stock.existed");

        Response<Long> createResponse = doctorWarehouseStockWriteService.create(DoctorWarehouseStock.builder()
                .farmId(wareHouse.getFarmId())
                .warehouseId(id)
                .warehouseName(wareHouse.getWareHouseName())
                .warehouseType(wareHouse.getType())
                .vendorName(DoctorWarehouseStockWriteService.DEFAULT_VENDOR_NAME)
                .materialId(warehouseMaterialDto.getMaterialId())
                .materialName(material.getName())
                .quantity(new BigDecimal(0))
                .unit(warehouseMaterialDto.getUnitName())
                .build());

        if (!createResponse.isSuccess())
            throw new JsonResponseException(createResponse.getError());
    }


//    @RequestMapping(method = RequestMethod.PUT, value = "in")
//    public void in(@RequestBody @Validated(WarehouseStockDto.InWarehouseValid.class) WarehouseStockDto dto) {
//
//        dto.setType(WarehouseMaterialHandleType.IN.getValue());
//        newDoctorWarehouseWriterService.handler(dto);
//    }
//
//
//    @RequestMapping(method = RequestMethod.PUT, value = "out")
//    public void out(@RequestBody @Validated(WarehouseStockDto.OutWarehouseValid.class) WarehouseStockDto dto) {
//        dto.setType(WarehouseMaterialHandleType.OUT.getValue());
//        newDoctorWarehouseWriterService.handler(dto);
//    }
//
//    @RequestMapping(method = RequestMethod.PUT, value = "inventory")
//    public void inventory(@RequestBody @Validated(WarehouseStockDto.InventoryWarehouseValid.class) WarehouseStockDto dto) {
//        dto.setType(WarehouseMaterialHandleType.INVENTORY.getValue());
//        newDoctorWarehouseWriterService.handler(dto);
//    }
//
//    @RequestMapping(method = RequestMethod.PUT, value = "transfer")
//    public void transfer(@RequestBody @Validated(WarehouseStockDto.TransferWarehouseValid.class) WarehouseStockDto dto) {
//        dto.setType(WarehouseMaterialHandleType.TRANSFER.getValue());
//        newDoctorWarehouseWriterService.handler(dto);
//    }


}
