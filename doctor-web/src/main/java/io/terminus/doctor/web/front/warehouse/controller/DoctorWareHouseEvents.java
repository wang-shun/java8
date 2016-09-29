package io.terminus.doctor.web.front.warehouse.controller;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.dto.DoctorMaterialInWareHouseDto;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseDto;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.doctor.warehouse.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.warehouse.service.DoctorMaterialInWareHouseReadService;
import io.terminus.doctor.warehouse.service.DoctorMaterialInWareHouseWriteService;
import io.terminus.doctor.warehouse.service.DoctorMaterialPriceInWareHouseReadService;
import io.terminus.doctor.warehouse.service.DoctorWareHouseReadService;
import io.terminus.doctor.web.front.warehouse.dto.DoctorConsumeProviderInputDto;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-25
 * Email:yaoqj@terminus.io
 * Descirbe: 对应的仓库事件信息录入
 */
@Slf4j
@Controller
@RequestMapping("/api/doctor/warehouse/event")
public class DoctorWareHouseEvents {

    private final DoctorMaterialInWareHouseWriteService doctorMaterialInWareHouseWriteService;

    private final DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService;

    private final UserReadService<User> userReadService;

    private final DoctorUserProfileReadService doctorUserProfileReadService;

    private final DoctorWareHouseReadService doctorWareHouseReadService;

    private final DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    private final DoctorBasicReadService doctorBasicReadService;

    private final DoctorFarmReadService doctorFarmReadService;

    @RpcConsumer
    private DoctorMaterialPriceInWareHouseReadService materialPriceInWareHouseReadService;
    @RpcConsumer
    private DoctorMaterialConsumeProviderReadService materialConsumeProviderReadService;

    @Autowired
    public DoctorWareHouseEvents(DoctorMaterialInWareHouseWriteService doctorMaterialInWareHouseWriteService,
                                 DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService,
                                 UserReadService<User> userReadService,
                                 DoctorBasicMaterialReadService doctorBasicMaterialReadService,
                                 DoctorWareHouseReadService doctorWareHouseReadService,
                                 DoctorFarmReadService doctorFarmReadService,
                                 DoctorBasicReadService doctorBasicReadService,
                                 DoctorUserProfileReadService doctorUserProfileReadService){
        this.doctorMaterialInWareHouseWriteService = doctorMaterialInWareHouseWriteService;
        this.doctorMaterialInWareHouseReadService = doctorMaterialInWareHouseReadService;
        this.userReadService = userReadService;
        this.doctorBasicMaterialReadService = doctorBasicMaterialReadService;
        this.doctorWareHouseReadService = doctorWareHouseReadService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorUserProfileReadService = doctorUserProfileReadService;
        this.doctorBasicReadService = doctorBasicReadService;
    }

    /**
     * 获取对应的仓库的 物料信息
     * @param farmId
     * @param wareHouseId
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DoctorMaterialInWareHouse> listDoctorMaterialInWareHouse(@RequestParam("farmId") Long farmId,
                                                                         @RequestParam("wareHouseId") Long wareHouseId){
        return RespHelper.or500(doctorMaterialInWareHouseReadService.queryDoctorMaterialInWareHouse(farmId, wareHouseId));
    }

    /**
     * 对应的仓库信息的分页查询方式
     * @param farmId
     * @param wareHouseId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/paging", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorMaterialInWareHouseDto> pagingDoctorMaterialInWareHouse(@RequestParam("farmId") Long farmId,
                                                                                @RequestParam(name = "wareHouseId", required = false) Long wareHouseId,
                                                                                @RequestParam(name = "materialId", required = false) Long materialId,
                                                                                @RequestParam(name = "materialName", required = false) String materialName,
                                                                                @RequestParam("pageNo") Integer pageNo,
                                                                                @RequestParam("pageSize") Integer pageSize){
        DateTime monthStart = DateTime.now().withTimeAtStartOfDay().withDayOfMonth(1);
        DateTime nextMonthStart = monthStart.plusMonths(1);

        Paging<DoctorMaterialInWareHouse> result = RespHelper.or500(
                doctorMaterialInWareHouseReadService.pagingDoctorMaterialInWareHouse(farmId, wareHouseId, materialId, materialName, pageNo, pageSize)
        );
        List<DoctorMaterialInWareHouseDto> list = result.getData().stream()
                .map(in -> {
                    DoctorMaterialInWareHouseDto dto = DoctorMaterialInWareHouseDto.buildDoctorMaterialInWareHouseInfo(in);
                    Double amount = RespHelper.or500(materialPriceInWareHouseReadService.findByWareHouseAndMaterialId(dto.getWarehouseId(), dto.getMaterialId())).stream()
                            .map(price -> price.getRemainder() * price.getUnitPrice())
                            .reduce((o1, o2) -> o1 + o2).orElse(0D);
                    dto.setCurrentAmount(amount);
                    RespHelper.or500(materialConsumeProviderReadService.warehouseEventReport(
                            farmId, dto.getWarehouseId(), null, dto.getMaterialId(), monthStart.toDate(), nextMonthStart.toDate())
                    ).forEach(report -> {
                        if(Objects.equals(report.getEventType(), DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue())){
                            dto.setOutAmount(report.getAmount());
                            dto.setOutCount(report.getCount());
                        }
                        if(Objects.equals(report.getEventType(), DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER.getValue())){
                            dto.setInAmount(report.getAmount());
                            dto.setInCount(report.getCount());
                        }
                    });
                    return dto;
                })
                .collect(Collectors.toList());

        return new Paging<>(result.getTotal(), list);
    }

    @RequestMapping(value = "/materialInWareHouse/delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean deleteMaterialInWareHouse(@RequestParam("materialInWareHouseId") Long materialInWareHouseId){
        Long userId;
        String userName;
        try{
            userId = UserUtil.getUserId();
            Response<User> userResponse = userReadService.findById(userId);
            checkState(userResponse.isSuccess(), "query.userInfo.error");
            userName = userResponse.getResult().getName();
        }catch (Exception e){
            log.error("get user info fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
        return RespHelper.or500(
                doctorMaterialInWareHouseWriteService.deleteMaterialInWareHouseInfo(
                        materialInWareHouseId, userId, userName));
    }

    /**
     * 消耗物品的领用
     * @param dto
     * @return
     */
    @RequestMapping(value = "/consume", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createConsumeEvent(@RequestBody @Valid DoctorConsumeProviderInputDto dto){
        DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto;
        DoctorMaterialConsumeProvider.EVENT_TYPE eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.from(dto.getEventType());
        if(eventType == null || !eventType.isOut()){
            throw new JsonResponseException("event.type.error");
        }
        try{

            DoctorMaterialInWareHouse doctorMaterialInWareHouse = RespHelper.orServEx(doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(
                    dto.getFarmId(),dto.getMaterialId(),dto.getWareHouseId()));
            checkState(!isNull(doctorMaterialInWareHouse), "input.materialInfo.error");

            Long currentUserId = dto.getStaffId() != null ? dto.getStaffId() : UserUtil.getUserId();
            Response<User> userResponse = userReadService.findById(currentUserId);
            String userName = RespHelper.orServEx(userResponse).getName();

            DoctorBasicMaterial doctorBasicMaterial = RespHelper.orServEx(doctorBasicMaterialReadService.findBasicMaterialById(dto.getMaterialId()));

            doctorMaterialConsumeProviderDto = DoctorMaterialConsumeProviderDto.builder()
                    .actionType(eventType.getValue()).type(doctorMaterialInWareHouse.getType())
                    .farmId(doctorMaterialInWareHouse.getFarmId()).farmName(doctorMaterialInWareHouse.getFarmName())
                    .wareHouseId(doctorMaterialInWareHouse.getWareHouseId()).wareHouseName(doctorMaterialInWareHouse.getWareHouseName())
                    .materialTypeId(doctorMaterialInWareHouse.getMaterialId()).materialName(doctorMaterialInWareHouse.getMaterialName())
                    .barnId(dto.getBarnId()).barnName(dto.getBarnName())
                    .groupId(dto.getGroupId()).groupCode(dto.getGroupCode())
                    .staffId(currentUserId).staffName(userName)
                    .count(dto.getCount()).consumeDays(dto.getConsumeDays())
                    .unitId(doctorBasicMaterial.getUnitId()).unitName(doctorBasicMaterial.getUnitName())
                    .unitGroupId(doctorBasicMaterial.getUnitGroupId()).unitGroupName(doctorBasicMaterial.getUnitGroupName())
                    .build();
        }catch (Exception e){
            log.error("consume material fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
        return RespHelper.or500(doctorMaterialInWareHouseWriteService.consumeMaterialInfo(doctorMaterialConsumeProviderDto));
    }

    /**
     * 初始化库存数据
     * @param materialId
     * @param warehouseId
     * @param unitId
     * @return
     */
    @RequestMapping(value = "/initMaterialInWarehouse", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public boolean initMaterialInWarehouse(@RequestParam Long materialId, @RequestParam Long warehouseId, Long unitId){
        DoctorBasicMaterial material = RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(materialId));
        if(material == null){
            throw new JsonResponseException("basicMaterial.not.found");
        }

        DoctorWareHouse wareHouse = RespHelper.or500(doctorWareHouseReadService.findById(warehouseId));
        if(wareHouse == null){
            throw new JsonResponseException("warehosue.not.found");
        }

        if(!wareHouse.getType().equals(material.getType())){
            throw new JsonResponseException("warehouse.material.type.not.match"); // 仓库与物料类型不一致
        }

        DoctorMaterialInWareHouse in = RespHelper.or500(doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(wareHouse.getFarmId(), materialId, warehouseId));
        if(in != null){
            throw new JsonResponseException("materialInWarehouse.exist");
        }

        in = new DoctorMaterialInWareHouse();
        // 药品.疫苗.易耗品, 前台必须传来单位
        if(Objects.equals(material.getType(), WareHouseType.CONSUME.getKey())
                || Objects.equals(material.getType(), WareHouseType.MEDICINE.getKey())
                || Objects.equals(material.getType(), WareHouseType.VACCINATION.getKey())){
            if(unitId != null){
                DoctorBasic doctorBasic = RespHelper.or500(doctorBasicReadService.findBasicById(unitId));
                if(doctorBasic == null || !Objects.equals(doctorBasic.getType(), DoctorBasic.Type.UNIT.getValue())){
                    throw new JsonResponseException("unit.miss");
                }
                in.setUnitName(doctorBasic.getName());
            }else{
                throw new JsonResponseException("unit.miss");
            }
        }else{
            in.setUnitName("千克");
        }
        in.setFarmId(wareHouse.getFarmId());
        in.setFarmName(wareHouse.getFarmName());
        in.setWareHouseId(warehouseId);
        in.setWareHouseName(wareHouse.getWareHouseName());
        in.setMaterialId(materialId);
        in.setMaterialName(material.getName());
        in.setType(material.getType());
        in.setLotNumber(0D);
        BaseUser user = UserUtil.getCurrentUser();
        in.setCreatorId(user.getId());
        in.setCreatorName(user.getName());
        in.setUpdatorId(user.getId());
        in.setUpdatorName(user.getName());
        return RespHelper.or500(doctorMaterialInWareHouseWriteService.create(in));
    }

    /**
     * 录入物品数据信息
     * @param dto
     * @return
     */
    @RequestMapping(value = "/provider", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createProviderEvent(@RequestBody @Valid DoctorConsumeProviderInputDto dto){
        DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto;
        DoctorMaterialConsumeProvider.EVENT_TYPE eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.from(dto.getEventType());
        if(eventType == null || !eventType.isIn()){
            throw new JsonResponseException("event.type.error");
        }
        try{
            DoctorWareHouseDto doctorWareHouseDto = RespHelper.orServEx(doctorWareHouseReadService.queryDoctorWareHouseById(dto.getWareHouseId()));

            Long userId = dto.getStaffId() != null ? dto.getStaffId() : UserUtil.getUserId();
            String userName = RespHelper.orServEx(doctorUserProfileReadService.findProfileByUserIds(Lists.newArrayList(userId))).get(0).getRealName();

            DoctorBasicMaterial doctorBasicMaterial = RespHelper.orServEx(doctorBasicMaterialReadService.findBasicMaterialById(dto.getMaterialId()));
            DoctorFarm doctorFarm = RespHelper.orServEx(doctorFarmReadService.findFarmById(dto.getFarmId()));

            doctorMaterialConsumeProviderDto = DoctorMaterialConsumeProviderDto.builder()
                    .actionType(eventType.getValue()).type(doctorBasicMaterial.getType())
                    .farmId(doctorFarm.getId()).farmName(doctorFarm.getName())
                    .wareHouseId(doctorWareHouseDto.getWarehouseId()).wareHouseName(doctorWareHouseDto.getWarehouseName())
                    .materialTypeId(doctorBasicMaterial.getId()).materialName(doctorBasicMaterial.getName())
                    .staffId(userId).staffName(userName)
                    .count(dto.getCount())
                    //.unitId(doctorBasicMaterial.getUnitId()).unitName(doctorBasicMaterial.getUnitName())
                    //.unitGroupId(doctorBasicMaterial.getUnitGroupId()).unitGroupName(doctorBasicMaterial.getUnitGroupName())
                    .unitPrice(dto.getUnitPrice())
                    .build();

            // 药品.疫苗.易耗品, 前台必须传来单位
            if(Objects.equals(doctorBasicMaterial.getType(), WareHouseType.CONSUME.getKey())
                    || Objects.equals(doctorBasicMaterial.getType(), WareHouseType.MEDICINE.getKey())
                    || Objects.equals(doctorBasicMaterial.getType(), WareHouseType.VACCINATION.getKey())){
                if(dto.getUnitId() != null){
                    DoctorBasic doctorBasic = RespHelper.or500(doctorBasicReadService.findBasicById(dto.getUnitId()));
                    doctorMaterialConsumeProviderDto.setUnitName(doctorBasic.getName());
                    doctorMaterialConsumeProviderDto.setUnitId(doctorBasic.getId());
                }else{
                    DoctorMaterialInWareHouse materialInWareHouse = RespHelper.orServEx(
                            doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(doctorFarm.getId(), dto.getMaterialId(), dto.getWareHouseId())
                    );
                    if(materialInWareHouse == null){
                        throw new ServiceException("unit.miss");
                    }
                }
            }
        }catch (Exception e){
            log.error("provider material fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
        return RespHelper.or500(doctorMaterialInWareHouseWriteService.providerMaterialInfo(doctorMaterialConsumeProviderDto));
    }

    /**
     * 仓库间物料转移
     * @param farmId 猪场id
     * @param fromWareHouseId 来源仓库
     * @param toWareHouseId 前往仓库
     * @param materialId 物料id
     * @param moveQuantity 调拨数量
     */
    @RequestMapping(value = "/moveMaterial", method = RequestMethod.POST)
    @ResponseBody
    public boolean moveMaterial(@RequestParam Long farmId, @RequestParam Long fromWareHouseId, @RequestParam Long toWareHouseId,
                             @RequestParam Long materialId, @RequestParam Double moveQuantity){
        if(moveQuantity == null || moveQuantity <= 0){
            throw new JsonResponseException("quantity.invalid");
        }
        if(fromWareHouseId.equals(toWareHouseId)){
            throw new JsonResponseException("fromWareHouseId.equal.toWareHouseId"); // 调出和调入仓库不能是同一个仓库
        }
        DoctorBasicMaterial material = RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(materialId));
        DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
        DoctorWareHouse consumeWarehouse = RespHelper.or500(doctorWareHouseReadService.findById(fromWareHouseId));
        DoctorWareHouse toWarehouse = RespHelper.or500(doctorWareHouseReadService.findById(toWareHouseId));
        DoctorMaterialInWareHouse materialInWareHouse = RespHelper.or500(doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(farmId, materialId, fromWareHouseId));
        if(materialInWareHouse == null){
            throw new JsonResponseException("no.material.consume");
        }
        Long userId = UserUtil.getUserId();
        String userName = RespHelper.or500(doctorUserProfileReadService.findProfileByUserIds(Lists.newArrayList(userId))).get(0).getRealName();

        DoctorMaterialConsumeProviderDto diaochu = DoctorMaterialConsumeProviderDto.builder()
                .actionType(DoctorMaterialConsumeProvider.EVENT_TYPE.DIAOCHU.getValue()).type(material.getType())
                .farmId(farmId).farmName(farm.getName())
                .materialTypeId(materialId).materialName(material.getName())
                .wareHouseId(fromWareHouseId).wareHouseName(consumeWarehouse.getWareHouseName())
                .staffId(userId).staffName(userName)
                .count(moveQuantity)
                .unitName(materialInWareHouse.getUnitName()).unitGroupName(materialInWareHouse.getUnitGroupName())
                .build();
        DoctorMaterialConsumeProviderDto diaoru = DoctorMaterialConsumeProviderDto.builder()
                .actionType(DoctorMaterialConsumeProvider.EVENT_TYPE.DIAORU.getValue()).type(material.getType())
                .farmId(farmId).farmName(farm.getName())
                .materialTypeId(materialId).materialName(material.getName())
                .wareHouseId(toWareHouseId).wareHouseName(toWarehouse.getWareHouseName())
                .staffId(userId).staffName(userName)
                .count(moveQuantity)
                .unitName(materialInWareHouse.getUnitName()).unitGroupName(materialInWareHouse.getUnitGroupName())
                .build();
        RespHelper.or500(doctorMaterialInWareHouseWriteService.moveMaterial(diaochu, diaoru));
        return true;
    }

    /**
     * 仓库物资盘点
     * @param farmId 猪场id
     * @param warehouseId 仓库id
     * @param materialId 物料id
     * @param count 新数量
     * @return
     */
    @RequestMapping(value = "/inventory", method = RequestMethod.POST)
    @ResponseBody
    public boolean inventory(@RequestParam Long farmId, @RequestParam Long warehouseId, @RequestParam Long materialId, @RequestParam Double count){
        DoctorMaterialInWareHouse materialInWareHouse = RespHelper.or500(doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(farmId, materialId, warehouseId));
        DoctorConsumeProviderInputDto dto = DoctorConsumeProviderInputDto.builder()
                .farmId(farmId).wareHouseId(warehouseId).materialId(materialId)
                .build();
        if (materialInWareHouse.getLotNumber() > count){
            // 盘亏 (出库)
            dto.setCount(materialInWareHouse.getLotNumber() - count);
            dto.setEventType(DoctorMaterialConsumeProvider.EVENT_TYPE.PANKUI.getValue());
            this.createConsumeEvent(dto);
        } else if(materialInWareHouse.getLotNumber() < count){
            // 盘盈(入库)
            dto.setCount(count - materialInWareHouse.getLotNumber());
            dto.setEventType(DoctorMaterialConsumeProvider.EVENT_TYPE.PANYING.getValue());
            this.createProviderEvent(dto);
        } else {
            return true;
        }
        return true;
    }

    /**
     * 仓库事件回滚
     * @param eventId DoctorMaterialConsumeProvider的id
     * @return
     */
    @RequestMapping(value = "/rollback", method = RequestMethod.GET)
    @ResponseBody
    public boolean rollback(@RequestParam Long eventId){
        return RespHelper.or500(doctorMaterialInWareHouseWriteService.rollback(eventId));
    }
}
