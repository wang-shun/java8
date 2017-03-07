package io.terminus.doctor.web.front.warehouse.controller;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.basic.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.basic.dto.DoctorMaterialInWareHouseDto;
import io.terminus.doctor.basic.dto.DoctorMoveMaterialDto;
import io.terminus.doctor.basic.dto.DoctorWareHouseDto;
import io.terminus.doctor.basic.model.*;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.basic.service.DoctorMaterialInWareHouseReadService;
import io.terminus.doctor.basic.service.DoctorMaterialInWareHouseWriteService;
import io.terminus.doctor.basic.service.DoctorMaterialPriceInWareHouseReadService;
import io.terminus.doctor.basic.service.DoctorWareHouseReadService;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.doctor.web.front.warehouse.dto.DoctorConsumeProviderInputDto;
import io.terminus.doctor.web.front.warehouse.dto.DoctorInventoryInputDto;
import io.terminus.doctor.web.front.warehouse.dto.DoctorMoveMaterialInputDto;
import io.terminus.doctor.web.front.warehouse.dto.DoctorWareHouseMaterialCriteria;
import io.terminus.doctor.web.front.warehouse.dto.DoctorWareHouseMaterialData;
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
import sun.tools.jconsole.inspector.Utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    private final DoctorWareHouseReadService doctorWareHouseReadService;

    private final DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    private final DoctorBasicReadService doctorBasicReadService;

    private final DoctorFarmReadService doctorFarmReadService;

    private final DoctorGroupWebService doctorGroupWebService;

    @Autowired
    private Exporter exporter;

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
                                 DoctorGroupWebService doctorGroupWebService,
                                 DoctorMaterialPriceInWareHouseReadService doctorMaterialPriceInWareHouseReadService){
        this.doctorMaterialInWareHouseWriteService = doctorMaterialInWareHouseWriteService;
        this.doctorMaterialInWareHouseReadService = doctorMaterialInWareHouseReadService;
        this.userReadService = userReadService;
        this.doctorBasicMaterialReadService = doctorBasicMaterialReadService;
        this.doctorWareHouseReadService = doctorWareHouseReadService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorBasicReadService = doctorBasicReadService;
        this.doctorGroupWebService = doctorGroupWebService;
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

        return pagingWareHouseMaterial(farmId, wareHouseId, materialId, materialName, pageNo, pageSize);
    }

    /**
     * 仓库物料导出
     * @param params 查询条件
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/material/export", method = RequestMethod.GET)
    public void wareHouseMaterialExport(@RequestParam Map<String, String> params,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        try {
            exporter.export(materialExportData(params), "web-material-export", request, response);
        } catch (Exception e) {
            log.error("ware.house.event.export.failed, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("ware.house.material.export.fail");
        }
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
        return RespHelper.or500(doctorMaterialInWareHouseWriteService.consumeMaterialInfo(buildMaterialConsumeInfo(dto)));
    }

    /**
     * 批量出库
     * @param dtoList
     * @return
     */
    @RequestMapping(value = "/batch/consume", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean batchCreateConsumeEvents(@RequestBody List<DoctorConsumeProviderInputDto> dtoList) {
        if (Arguments.isNullOrEmpty(dtoList)) {
            return Boolean.FALSE;
        }
        List<DoctorMaterialConsumeProviderDto> providerDtoList = dtoList.stream().map(this::buildMaterialConsumeInfo).collect(Collectors.toList());
        return RespHelper.or500(doctorMaterialInWareHouseWriteService.batchConsumeMaterialInfo(providerDtoList));
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
    public boolean initMaterialInWarehouse(@RequestParam Long materialId, @RequestParam Long warehouseId,
                                           @RequestParam(required = false) Long unitId){
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
                DoctorBasic doctorBasic = RespHelper.or500(doctorBasicReadService.findBasicByIdFilterByFarmId(wareHouse.getFarmId(), unitId));
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
        return RespHelper.or500(doctorMaterialInWareHouseWriteService.providerMaterialInfo(buildMaterialProviderInfo(dto)));
    }

    /**
     * 批量入库
     * @param dtoList
     * @return
     */
    @RequestMapping(value = "/batch/provider", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean batchCreateProviderEvents(@RequestBody List<DoctorConsumeProviderInputDto> dtoList){
        if (Arguments.isNullOrEmpty(dtoList)) {
            return Boolean.FALSE;
        }
        List<DoctorMaterialConsumeProviderDto> providerDtoList = dtoList.stream().map(this::buildMaterialProviderInfo).collect(Collectors.toList());
        return RespHelper.or500(doctorMaterialInWareHouseWriteService.batchProviderMaterialInfo(providerDtoList));

    }

    /**
     * 仓库间物料转移（调拨）
     */
    @RequestMapping(value = "/moveMaterial", method = RequestMethod.POST)
    @ResponseBody
    public boolean moveMaterial(@RequestBody DoctorMoveMaterialInputDto dto){
        RespHelper.or500(doctorMaterialInWareHouseWriteService.moveMaterial(buildMoveMaterialInfo(dto)));
        return true;
    }

    /**
     * 批量调拨
     */
    @RequestMapping(value = "/batch/moveMaterial", method = RequestMethod.POST)
    @ResponseBody
    public Boolean moveMaterial(@RequestBody List<DoctorMoveMaterialInputDto> dtoList){
        if (Arguments.isNullOrEmpty(dtoList)) {
            return Boolean.FALSE;
        }
        List<DoctorMoveMaterialDto> moveMaterialDtoList = dtoList.stream().map(this::buildMoveMaterialInfo).collect(Collectors.toList());
        return RespHelper.or500(doctorMaterialInWareHouseWriteService.batchMoveMaterial(moveMaterialDtoList));
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
    public boolean inventory(@RequestParam Long farmId, @RequestParam Long warehouseId, @RequestParam Long materialId,
                             @RequestParam Double count, @RequestParam(required = false) String eventAt){
        DoctorMaterialInWareHouse materialInWareHouse = RespHelper.or500(doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(farmId, materialId, warehouseId));
        DoctorConsumeProviderInputDto dto = DoctorConsumeProviderInputDto.builder()
                .farmId(farmId).wareHouseId(warehouseId).materialId(materialId)
                .eventAt(eventAt == null ? new Date() : DateUtil.toDate(eventAt))
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
     * 批量盘点
     * @param inputDtoList
     * @return
     */
    @RequestMapping(value = "/batch/inventory", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean batchInventory(@RequestBody List<DoctorInventoryInputDto> inputDtoList) {
        List<DoctorMaterialConsumeProviderDto> dtoList = inputDtoList.stream().map(inputDto -> {
            DoctorMaterialInWareHouse materialInWareHouse = RespHelper.or500(doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(inputDto.getFarmId(), inputDto.getMaterialId(), inputDto.getWareHouseId()));
            DoctorConsumeProviderInputDto dto = DoctorConsumeProviderInputDto.builder()
                    .farmId(inputDto.getFarmId()).wareHouseId(inputDto.getWareHouseId()).materialId(inputDto.getMaterialId())
                    .eventAt(inputDto.getEventAt() == null ? new Date() : DateUtil.toDate(inputDto.getEventAt()))
                    .build();
            Double count = inputDto.getCount();
            if (materialInWareHouse.getLotNumber() > count) {
                // 盘亏 (出库)
                dto.setCount(materialInWareHouse.getLotNumber() - count);
                dto.setEventType(DoctorMaterialConsumeProvider.EVENT_TYPE.PANKUI.getValue());
                return buildMaterialConsumeInfo(dto);
            } else if (materialInWareHouse.getLotNumber() < count) {
                // 盘盈(入库)
                dto.setCount(count - materialInWareHouse.getLotNumber());
                dto.setEventType(DoctorMaterialConsumeProvider.EVENT_TYPE.PANYING.getValue());
                return buildMaterialProviderInfo(dto);
            } else {
                return null;
            }
        }).filter(dto -> dto != null).collect(Collectors.toList());
        return RespHelper.or500(doctorMaterialInWareHouseWriteService.batchInventory(dtoList));
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

    /**
     * 构建物料入库信息
     * @param dto
     * @return
     */
    private DoctorMaterialConsumeProviderDto buildMaterialProviderInfo(DoctorConsumeProviderInputDto dto) {
        DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto;
        DoctorMaterialConsumeProvider.EVENT_TYPE eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.from(dto.getEventType());
        if(eventType == null || !eventType.isIn()){
            throw new JsonResponseException("event.type.error");
        }
        try{
            DoctorWareHouseDto doctorWareHouseDto = RespHelper.orServEx(doctorWareHouseReadService.queryDoctorWareHouseById(dto.getWareHouseId()));

            Long userId = dto.getStaffId() != null ? dto.getStaffId() : UserUtil.getUserId();

            DoctorBasicMaterial doctorBasicMaterial = RespHelper.orServEx(doctorBasicMaterialReadService.findBasicMaterialById(dto.getMaterialId()));
            DoctorFarm doctorFarm = RespHelper.orServEx(doctorFarmReadService.findFarmById(dto.getFarmId()));

            doctorMaterialConsumeProviderDto = DoctorMaterialConsumeProviderDto.builder()
                    .actionType(eventType.getValue()).type(doctorBasicMaterial.getType())
                    .farmId(doctorFarm.getId()).farmName(doctorFarm.getName())
                    .wareHouseId(doctorWareHouseDto.getWarehouseId()).wareHouseName(doctorWareHouseDto.getWarehouseName())
                    .materialTypeId(doctorBasicMaterial.getId()).materialName(doctorBasicMaterial.getName())
                    .staffId(userId).staffName(RespHelper.orServEx(doctorGroupWebService.findRealName(userId)))
                    .count(dto.getCount())
                    .eventTime(dto.getEventAt() == null ? new Date() : dto.getEventAt())
                    .providerFactoryId(dto.getFactoryId()).providerFactoryName(dto.getFactoryName())
                    .unitPrice(dto.getUnitPrice())
                    .build();

            // 药品.疫苗.易耗品, 前台必须传来单位
            if(Objects.equals(doctorBasicMaterial.getType(), WareHouseType.CONSUME.getKey())
                    || Objects.equals(doctorBasicMaterial.getType(), WareHouseType.MEDICINE.getKey())
                    || Objects.equals(doctorBasicMaterial.getType(), WareHouseType.VACCINATION.getKey())){
                if(dto.getUnitId() != null){
                    DoctorBasic doctorBasic = RespHelper.or500(doctorBasicReadService.findBasicByIdFilterByFarmId(dto.getFarmId(), dto.getUnitId()));
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
            return doctorMaterialConsumeProviderDto;
        }catch (Exception e){
            log.error("provider material fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
    }

    /**
     * 物料出库信息构建
     * @param dto 物料出库信息输入
     * @return
     */
    private DoctorMaterialConsumeProviderDto buildMaterialConsumeInfo(DoctorConsumeProviderInputDto dto) {
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

            DoctorBasicMaterial doctorBasicMaterial = RespHelper.orServEx(doctorBasicMaterialReadService.findBasicMaterialById(dto.getMaterialId()));

            doctorMaterialConsumeProviderDto = DoctorMaterialConsumeProviderDto.builder()
                    .actionType(eventType.getValue()).type(doctorMaterialInWareHouse.getType())
                    .farmId(doctorMaterialInWareHouse.getFarmId()).farmName(doctorMaterialInWareHouse.getFarmName())
                    .wareHouseId(doctorMaterialInWareHouse.getWareHouseId()).wareHouseName(doctorMaterialInWareHouse.getWareHouseName())
                    .materialTypeId(doctorMaterialInWareHouse.getMaterialId()).materialName(doctorMaterialInWareHouse.getMaterialName())
                    .barnId(dto.getBarnId()).barnName(dto.getBarnName())
                    .groupId(dto.getGroupId()).groupCode(dto.getGroupCode())
                    .staffId(currentUserId).staffName(RespHelper.orServEx(doctorGroupWebService.findRealName(currentUserId)))
                    .count(dto.getCount()).consumeDays(dto.getConsumeDays())
                    .eventTime(dto.getEventAt() == null ? new Date() : dto.getEventAt())
                    .unitId(doctorBasicMaterial.getUnitId()).unitName(doctorBasicMaterial.getUnitName())
                    .unitGroupId(doctorBasicMaterial.getUnitGroupId()).unitGroupName(doctorBasicMaterial.getUnitGroupName())
                    .build();
            return doctorMaterialConsumeProviderDto;
        }catch (Exception e){
            log.error("consume material fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
    }

    /**
     * 物料调拨信息构建
     * @param dto 调拨输入信息
     * @return 调拨信息
     */
    private DoctorMoveMaterialDto buildMoveMaterialInfo(DoctorMoveMaterialInputDto dto) {
        if(dto.getMoveQuantity() == null || dto.getMoveQuantity() <= 0){
            throw new JsonResponseException("quantity.invalid");
        }
        if(dto.getFromWareHouseId().equals(dto.getToWareHouseId())){
            throw new JsonResponseException("fromWareHouseId.equal.toWareHouseId"); // 调出和调入仓库不能是同一个仓库
        }
        DoctorBasicMaterial material = RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(dto.getMaterialId()));
        DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(dto.getFarmId()));
        DoctorWareHouse consumeWarehouse = RespHelper.or500(doctorWareHouseReadService.findById(dto.getFromWareHouseId()));
        DoctorWareHouse toWarehouse = RespHelper.or500(doctorWareHouseReadService.findById(dto.getToWareHouseId()));
        DoctorMaterialInWareHouse materialInWareHouse = RespHelper.or500(doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(dto.getFarmId(), dto.getMaterialId(), dto.getFromWareHouseId()));
        if(materialInWareHouse == null){
            throw new JsonResponseException("no.material.consume");
        }
        Long userId = UserUtil.getUserId();
        String realName = RespHelper.or500(doctorGroupWebService.findRealName(userId));

        DoctorMaterialConsumeProviderDto diaochu = DoctorMaterialConsumeProviderDto.builder()
                .actionType(DoctorMaterialConsumeProvider.EVENT_TYPE.DIAOCHU.getValue()).type(material.getType())
                .farmId(dto.getFarmId()).farmName(farm.getName())
                .materialTypeId(dto.getMaterialId()).materialName(material.getName())
                .wareHouseId(dto.getFromWareHouseId()).wareHouseName(consumeWarehouse.getWareHouseName())
                .staffId(userId).staffName(realName)
                .count(dto.getMoveQuantity())
                .eventTime(dto.getEventAt() == null ? new Date() : DateUtil.toDate(dto.getEventAt()))
                .unitName(materialInWareHouse.getUnitName()).unitGroupName(materialInWareHouse.getUnitGroupName())
                .build();
        DoctorMaterialConsumeProviderDto diaoru = DoctorMaterialConsumeProviderDto.builder()
                .actionType(DoctorMaterialConsumeProvider.EVENT_TYPE.DIAORU.getValue()).type(material.getType())
                .farmId(dto.getFarmId()).farmName(farm.getName())
                .materialTypeId(dto.getMaterialId()).materialName(material.getName())
                .wareHouseId(dto.getToWareHouseId()).wareHouseName(toWarehouse.getWareHouseName())
                .staffId(userId).staffName(realName)
                .count(dto.getMoveQuantity())
                .eventTime(dto.getEventAt() == null ? new Date() : DateUtil.toDate(dto.getEventAt()))
                .unitName(materialInWareHouse.getUnitName()).unitGroupName(materialInWareHouse.getUnitGroupName())
                .build();
        return new DoctorMoveMaterialDto(diaochu, diaoru);
    }

    /**
     * 查询构建导出数据
     * @param criteriaMap 查询条件
     * @return 导出数据集
     */
    public List<DoctorWareHouseMaterialData> materialExportData(Map<String, String> criteriaMap) {
        DoctorWareHouseMaterialCriteria criteria = BeanMapper.map(criteriaMap, DoctorWareHouseMaterialCriteria.class);
        Map<String, Object> map = Maps.newHashMap();
        double numbersOut = 0;
        double priceOut = 0;
        double numberIn = 0;
        double priceIn = 0;
        List<DoctorMaterialConsumeProvider> list = RespHelper.or500(materialConsumeProviderReadService.findMaterialConsume(criteria.getFarmId(), criteria.getWareHouseId(), criteria.getMaterialId(),
                criteria.getMaterialName(), DateUtil.stringToDate(criteria.getStartDate()), DateUtil.stringToDate(criteria.getEndDate()), criteria.getPageNo(), criteria.getSize()));
        for (int i = 0; i < list.size()-1; i++) {
            if (list.get(i).getMaterialId().equals(list.get(i+1).getMaterialId())) {
                list.get(i).setExtra(list.get(i).getExtra());
                if (list.get(i).getExtraMap().get("consumePrice") != null) {

                    if (list.get(i).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER)
                            || list.get(i).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.DIAOCHU)
                            || list.get(i).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.PANKUI)
                            || list.get(i).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.FORMULA_RAW_MATERIAL)) {



                    } else {



                    }

                } else {
                    if (list.get(i).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER)
                            || list.get(i).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.DIAOCHU)
                            || list.get(i).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.PANKUI)
                            || list.get(i).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.FORMULA_FEED)) {

                        numbersOut += list.get(i).getEventCount();
                        priceOut += list.get(i).getUnitPrice() * list.get(i).getEventCount();

                    } else {

                        numberIn += list.get(i).getEventCount();
                        priceIn += list.get(i).getUnitPrice() * list.get(i).getEventCount();

                    }
                }
            } else{
                //最后一次数据处理
                list.get(i).setExtra(list.get(i+1).getExtra());
                if (list.get(i+1).getExtraMap().get("consumePrice") != null) {

                    if (list.get(i+1).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER) || list.get(i+1).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.DIAOCHU)
                            || list.get(i+1).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.PANKUI)) {


                    } else {

                    }

                } else {
                    if (list.get(i+1).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER) || list.get(i+1).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.DIAOCHU)
                            || list.get(i+1).getEventType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.PANKUI)) {

                        numbersOut += list.get(i+1).getEventCount();
                        priceOut += list.get(i+1).getUnitPrice() * list.get(i+1).getEventCount();

                    } else {

                        numberIn += list.get(i+1).getEventCount();
                        priceIn += list.get(i+1).getUnitPrice() * list.get(i+1).getEventCount();

                    }
                }

                //数据塞入

                DoctorMaterialPriceInWareHouse doctorMaterialPriceInWareHouse = RespHelper.or500(materialPriceInWareHouseReadService.findMaterialData(criteria.getFarmId(), list.get(i+1).getMaterialId(),
                        criteria.getWareHouseId(),
                        DateUtil.stringToDate(criteria.getEndDate())));

                DoctorMaterialInWareHouse doctorMaterialInWareHouse = RespHelper.or500(doctorMaterialInWareHouseReadService.findMaterialUnits(criteria.getFarmId(), list.get(i+1).getMaterialId(),
                        criteria.getWareHouseId()));

                numbersOut = 0;
                priceOut = 0;
                numberIn = 0;
                priceIn = 0;
            }
        }

        return null;
    }

    /**
     * 分页查询仓库物料信息
     * @param farmId 猪场id
     * @param wareHouseId 仓库id
     * @param materialId 物料id
     * @param materialName 物料名
     * @param pageNo 页码
     * @param pageSize 分页大小
     * @return 分页结果
     */
    private Paging<DoctorMaterialInWareHouseDto> pagingWareHouseMaterial(Long farmId, Long wareHouseId, Long materialId, String materialName, Integer pageNo, Integer pageSize) {
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
                            farmId, dto.getWarehouseId(), dto.getMaterialId(), null, null, null, null, null, DateUtil.toDateString(monthStart.toDate()), DateUtil.toDateString(nextMonthStart.toDate()))
                    ).forEach(report -> {
                        if(Objects.equals(report.getEventType(), DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue())){
                            dto.setOutAmount(dto.getOutAmount() + report.getAmount());
                            dto.setOutCount(dto.getOutCount() + report.getCount());
                        }
                        if(Objects.equals(report.getEventType(), DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER.getValue())){
                            dto.setInAmount(dto.getInAmount() + report.getAmount());
                            dto.setInCount(dto.getInCount() + report.getCount());
                        }
                        if(Objects.equals(report.getEventType(), DoctorMaterialConsumeProvider.EVENT_TYPE.DIAORU.getValue())){
                            dto.setDiaoruAmount(dto.getDiaoruAmount() + report.getAmount());
                            dto.setDiaoruCount(dto.getDiaoruCount() + report.getCount());
                        }
                        if(Objects.equals(report.getEventType(), DoctorMaterialConsumeProvider.EVENT_TYPE.DIAOCHU.getValue())){
                            dto.setDiaochuAmount(dto.getDiaochuAmount() + report.getAmount());
                            dto.setDiaochuCount(dto.getDiaochuCount() + report.getCount());
                        }
                    });
                    return dto;
                })
                .collect(Collectors.toList());
        return new Paging<>(result.getTotal(), list);
    }
}
