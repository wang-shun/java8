package io.terminus.doctor.web.front.warehouseV2;

import com.fasterxml.jackson.annotation.JsonView;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.dto.DoctorWareHouseCriteria;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseMaterialDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockStatisticsDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.*;
import io.terminus.doctor.basic.service.warehouseV2.*;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.web.front.warehouseV2.dto.WarehouseDto;
import io.terminus.doctor.web.front.warehouseV2.vo.*;
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

    @RpcConsumer
    private DoctorWarehouseReportReadService doctorWarehouseReportReadService;

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
    @JsonView(WarehouseVo.WarehouseStatisticsView.class)
    public List<WarehouseVo> sameTypeWarehouseStatistics(@RequestParam Long farmId) {

        Response<List<DoctorWareHouse>> warehousesResponse = doctorWarehouseReaderService.findByFarmId(farmId);
        if (!warehousesResponse.isSuccess())
            throw new JsonResponseException(warehousesResponse.getError());
        Response<Map<Integer, AmountAndQuantityDto>> balanceResponse = doctorWarehouseReportReadService.countBalanceEachWarehouseType(farmId);
        if (!balanceResponse.isSuccess())
            throw new JsonResponseException(balanceResponse.getError());
        Map<Integer, AmountAndQuantityDto> balance = balanceResponse.getResult();

        Response<Map<Integer, DoctorWarehouseMaterialApply>> eachWarehouseTypeLastApplyResponse = doctorWarehouseMaterialApplyReadService.listEachWarehouseTypeLastApply(farmId);
        if (!eachWarehouseTypeLastApplyResponse.isSuccess())
            throw new JsonResponseException(eachWarehouseTypeLastApplyResponse.getError());

        Date feedLastApplyDate, materialLastApplyDate, vaccinationLastApplyDate, medicineLastApplyDate, consumeLastApplyDate;
        if (null == eachWarehouseTypeLastApplyResponse.getResult().get(WareHouseType.FEED.getKey()))
            feedLastApplyDate = null;
        else
            feedLastApplyDate = eachWarehouseTypeLastApplyResponse.getResult().get(WareHouseType.FEED.getKey()).getApplyDate();
        if (null == eachWarehouseTypeLastApplyResponse.getResult().get(WareHouseType.MATERIAL.getKey()))
            materialLastApplyDate = null;
        else
            materialLastApplyDate = eachWarehouseTypeLastApplyResponse.getResult().get(WareHouseType.MATERIAL.getKey()).getApplyDate();
        if (null == eachWarehouseTypeLastApplyResponse.getResult().get(WareHouseType.VACCINATION.getKey()))
            vaccinationLastApplyDate = null;
        else
            vaccinationLastApplyDate = eachWarehouseTypeLastApplyResponse.getResult().get(WareHouseType.VACCINATION.getKey()).getApplyDate();
        if (null == eachWarehouseTypeLastApplyResponse.getResult().get(WareHouseType.MEDICINE.getKey()))
            medicineLastApplyDate = null;
        else
            medicineLastApplyDate = eachWarehouseTypeLastApplyResponse.getResult().get(WareHouseType.MEDICINE.getKey()).getApplyDate();
        if (null == eachWarehouseTypeLastApplyResponse.getResult().get(WareHouseType.CONSUME.getKey()))
            consumeLastApplyDate = null;
        else
            consumeLastApplyDate = eachWarehouseTypeLastApplyResponse.getResult().get(WareHouseType.CONSUME.getKey()).getApplyDate();


        List<WarehouseVo> vos = new ArrayList<>(5);
        vos.add(WarehouseVo.builder()
                .type(WareHouseType.FEED.getKey())
                .balanceQuantity(balance.containsKey(WareHouseType.FEED.getKey()) ?
                        balanceResponse.getResult().get(WareHouseType.FEED.getKey()).getQuantity() : new BigDecimal(0))
                .lastApplyDate(feedLastApplyDate)
                .build());
        vos.add(WarehouseVo.builder()
                .type(WareHouseType.MATERIAL.getKey())
                .balanceQuantity(balance.containsKey(WareHouseType.MATERIAL.getKey()) ?
                        balanceResponse.getResult().get(WareHouseType.MATERIAL.getKey()).getQuantity() : new BigDecimal(0))
                .lastApplyDate(materialLastApplyDate)
                .build());
        vos.add(WarehouseVo.builder()
                .type(WareHouseType.VACCINATION.getKey())
                .balanceQuantity(balance.containsKey(WareHouseType.VACCINATION.getKey()) ?
                        balanceResponse.getResult().get(WareHouseType.VACCINATION.getKey()).getQuantity() : new BigDecimal(0))
                .lastApplyDate(vaccinationLastApplyDate)
                .build());
        vos.add(WarehouseVo.builder()
                .type(WareHouseType.MEDICINE.getKey())
                .balanceQuantity(balance.containsKey(WareHouseType.MEDICINE.getKey()) ?
                        balanceResponse.getResult().get(WareHouseType.MEDICINE.getKey()).getQuantity() : new BigDecimal(0))
                .lastApplyDate(medicineLastApplyDate)
                .build());
        vos.add(WarehouseVo.builder()
                .type(WareHouseType.CONSUME.getKey())
                .balanceQuantity(balance.containsKey(WareHouseType.CONSUME.getKey()) ?
                        balanceResponse.getResult().get(WareHouseType.CONSUME.getKey()).getQuantity() : new BigDecimal(0))
                .lastApplyDate(consumeLastApplyDate)
                .build());
        return vos;
    }


    @RequestMapping(method = RequestMethod.GET, value = "type/{type}/statistics")
    @JsonView(WarehouseVo.WarehouseView.class)
    public List<WarehouseVo> sameTypeWarehouseStatistics(@PathVariable Integer type, @RequestParam Long farmId) {
        Response<List<DoctorWareHouse>> wareHouseResponse = doctorWarehouseReaderService.list(DoctorWareHouse.builder()
                .farmId(farmId)
                .type(type)
                .build());
        if (!wareHouseResponse.isSuccess())
            throw new JsonResponseException(wareHouseResponse.getError());

        List<WarehouseVo> warehouseVos = new ArrayList<>();
        for (DoctorWareHouse wareHouse : wareHouseResponse.getResult()) {

            Response<List<DoctorWarehouseMaterialApply>> lastApplyResponse = doctorWarehouseMaterialApplyReadService.listOrderByHandleDate(DoctorWarehouseMaterialApply.builder()
                    .warehouseId(wareHouse.getId())
                    .build(), 1);
            if (!lastApplyResponse.isSuccess())
                throw new JsonResponseException(lastApplyResponse.getError());

            Date lastApplyDate = null;
            if (null != lastApplyResponse.getResult() && !lastApplyResponse.getResult().isEmpty()) {
                lastApplyDate = lastApplyResponse.getResult().get(0).getApplyDate();
            }

            Response<AmountAndQuantityDto> balanceResponse = doctorWarehouseReportReadService.countWarehouseBalance(wareHouse.getId());
            if (!balanceResponse.isSuccess())
                throw new JsonResponseException(balanceResponse.getError());

            warehouseVos.add(WarehouseVo.builder()
                    .id(wareHouse.getId())
                    .name(wareHouse.getWareHouseName())
                    .type(type)
                    .lastApplyDate(lastApplyDate)
                    .balanceQuantity(balanceResponse.getResult().getQuantity())
                    .build());
        }
        return warehouseVos;
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

        Calendar now = Calendar.getInstance();
        Response<Map<Long, WarehouseStockStatisticsDto>> statisticsResponse = doctorWarehouseReportReadService.countMaterialHandleByFarm(farmId, now,
                WarehouseMaterialHandleType.IN,
                WarehouseMaterialHandleType.OUT,
                WarehouseMaterialHandleType.TRANSFER_IN,
                WarehouseMaterialHandleType.TRANSFER_OUT);
        if (!statisticsResponse.isSuccess())
            throw new JsonResponseException(statisticsResponse.getError());

        Response<Map<Long, AmountAndQuantityDto>> warehouseBalanceResponse = doctorWarehouseReportReadService.countEachWarehouseBalance(farmId);
        if (!warehouseBalanceResponse.isSuccess())
            throw new JsonResponseException(warehouseBalanceResponse.getError());

        List<FarmWarehouseVo> vos = new ArrayList<>();
        warehouseResponse.getResult().forEach(wareHouse -> {
            FarmWarehouseVo vo = new FarmWarehouseVo();
            vo.setId(wareHouse.getId());
            vo.setName(wareHouse.getWareHouseName());
            vo.setType(wareHouse.getType());
            vo.setManagerName(wareHouse.getManagerName());

            WarehouseStockStatisticsDto warehouseStatistics = statisticsResponse.getResult().get(wareHouse.getId());
            if (null == warehouseStatistics) {
                vo.setInAmount(0);
                vo.setInQuantity(new BigDecimal(0));
                vo.setOutAmount(0);
                vo.setOutQuantity(new BigDecimal(0));
                vo.setTransferInAmount(0);
                vo.setTransferInQuantity(new BigDecimal(0));
                vo.setTransferOutAmount(0);
                vo.setTransferOutQuantity(new BigDecimal(0));
            } else {
                vo.setInAmount(warehouseStatistics.getIn().getAmount());
                vo.setInQuantity(warehouseStatistics.getIn().getQuantity());
                vo.setOutAmount(warehouseStatistics.getOut().getAmount());
                vo.setOutQuantity(warehouseStatistics.getOut().getQuantity());
                vo.setTransferOutAmount(warehouseStatistics.getTransferOut().getAmount());
                vo.setTransferOutQuantity(warehouseStatistics.getTransferOut().getQuantity());
                vo.setTransferInAmount(warehouseStatistics.getTransferIn().getAmount());
                vo.setTransferInQuantity(warehouseStatistics.getTransferIn().getQuantity());
            }

            AmountAndQuantityDto balance = warehouseBalanceResponse.getResult().get(wareHouse.getId());
            if (null == balance) {
                vo.setBalanceQuantity(new BigDecimal(0));
                vo.setBalanceAmount(0);
            } else {
                vo.setBalanceAmount(balance.getAmount());
                vo.setBalanceQuantity(balance.getQuantity());
            }
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

        Response<WarehouseStockStatisticsDto> statisticsResponse = doctorWarehouseReportReadService.countMaterialHandleByWarehouse(id, date, WarehouseMaterialHandleType.IN, WarehouseMaterialHandleType.OUT);
        if (!statisticsResponse.isSuccess())
            throw new JsonResponseException(statisticsResponse.getError());


        Response<AmountAndQuantityDto> amountAndQuantityResponse = doctorWarehouseReaderService.countWarehouseBalance(id);
        if (!amountAndQuantityResponse.isSuccess())
            throw new JsonResponseException(amountAndQuantityResponse.getError());


        return WarehouseStatisticsVo.builder()
                .id(id)
                .balanceQuantity(amountAndQuantityResponse.getResult().getQuantity())
                .balanceAmount(amountAndQuantityResponse.getResult().getAmount())
                .inAmount(statisticsResponse.getResult().getIn().getAmount())
                .inQuantity(statisticsResponse.getResult().getIn().getQuantity())
                .outAmount(statisticsResponse.getResult().getOut().getAmount())
                .outQuantity(statisticsResponse.getResult().getOut().getQuantity())
                .build();
    }


    @RequestMapping(method = RequestMethod.GET, value = "{id}/material/{materialId}/statistics")
    public WarehouseStockStatisticsVo materialStatistics(@PathVariable Long id, @PathVariable Long materialId) {
        Calendar now = Calendar.getInstance();

        Response<WarehouseStockStatisticsDto> statisticsDtoResponse = doctorWarehouseReportReadService.countMaterialHandleByMaterial(id, materialId, now,
                WarehouseMaterialHandleType.IN,
                WarehouseMaterialHandleType.OUT,
                WarehouseMaterialHandleType.TRANSFER_OUT,
                WarehouseMaterialHandleType.TRANSFER_IN);
        if (!statisticsDtoResponse.isSuccess())
            throw new JsonResponseException(statisticsDtoResponse.getError());
        Response<AmountAndQuantityDto> balanceResponse = doctorWarehouseReportReadService.countMaterialBalance(id, materialId);
        if (!balanceResponse.isSuccess())
            throw new JsonResponseException(balanceResponse.getError());

        Response<List<DoctorWarehouseStock>> stockResponse = doctorWarehouseStockReadService.listMergeVendor(DoctorWarehouseStock.builder()
                .warehouseId(id)
                .materialId(materialId)
                .build());
        if (!stockResponse.isSuccess())
            throw new JsonResponseException(stockResponse.getError());
        if (null == stockResponse.getResult() || stockResponse.getResult().isEmpty())
            throw new JsonResponseException("stock.not.found");

        WarehouseStockStatisticsVo vo = new WarehouseStockStatisticsVo();
        vo.setId(stockResponse.getResult().get(0).getId());
        vo.setMaterialId(stockResponse.getResult().get(0).getMaterialId());
        vo.setMaterialName(stockResponse.getResult().get(0).getMaterialName());
        vo.setUnit(stockResponse.getResult().get(0).getUnit());

        vo.setOutQuantity(statisticsDtoResponse.getResult().getOut().getQuantity());
        vo.setOutAmount(statisticsDtoResponse.getResult().getOut().getAmount());
        vo.setInAmount(statisticsDtoResponse.getResult().getIn().getAmount());
        vo.setInQuantity(statisticsDtoResponse.getResult().getIn().getQuantity());
        vo.setTransferInAmount(statisticsDtoResponse.getResult().getTransferIn().getAmount());
        vo.setTransferInQuantity(statisticsDtoResponse.getResult().getTransferIn().getQuantity());
        vo.setTransferOutAmount(statisticsDtoResponse.getResult().getTransferOut().getAmount());
        vo.setTransferOutQuantity(statisticsDtoResponse.getResult().getTransferOut().getQuantity());

        vo.setBalanceQuantity(balanceResponse.getResult().getQuantity());
        vo.setBalanceAmount(balanceResponse.getResult().getAmount());

        return vo;
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
    public Paging<WarehouseStockVo> material(@PathVariable Long id,
                                             @RequestParam(required = false) String materialName,
                                             @RequestParam(required = false) Integer pageNo,
                                             @RequestParam(required = false) Integer pageSize) {

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("warehouseId", id);
        criteria.put("materialNameLike", materialName);
        Response<Paging<DoctorWarehouseStock>> stockResponse = doctorWarehouseStockReadService.pagingMergeVendor(pageNo, pageSize, criteria);

        if (!stockResponse.isSuccess())
            throw new JsonResponseException(stockResponse.getError());

        Paging<WarehouseStockVo> vo = new Paging<>();
        List<WarehouseStockVo> data = new ArrayList<>(stockResponse.getResult().getData().size());
        for (DoctorWarehouseStock stock : stockResponse.getResult().getData()) {
            data.add(WarehouseStockVo.builder()
                    .materialId(stock.getMaterialId())
                    .materialName(stock.getMaterialName())
                    .quantity(stock.getQuantity())
                    .unit(stock.getUnit())
                    .build());
        }
        vo.setData(data);
        vo.setTotal(stockResponse.getResult().getTotal());

        return vo;
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


}
