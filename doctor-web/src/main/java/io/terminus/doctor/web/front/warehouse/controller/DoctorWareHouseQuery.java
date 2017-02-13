package io.terminus.doctor.web.front.warehouse.controller;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.basic.dto.BarnConsumeMaterialReport;
import io.terminus.doctor.basic.dto.DoctorWareHouseDto;
import io.terminus.doctor.basic.dto.MaterialCountAmount;
import io.terminus.doctor.basic.dto.WarehouseEventReport;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorFarmWareHouseType;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.MaterialFactory;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.basic.service.DoctorMaterialInWareHouseReadService;
import io.terminus.doctor.basic.service.DoctorMaterialPriceInWareHouseReadService;
import io.terminus.doctor.basic.service.DoctorWareHouseReadService;
import io.terminus.doctor.basic.service.DoctorWareHouseWriteService;
import io.terminus.doctor.basic.service.MaterialFactoryReadService;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.util.JsonMapperUtil;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.warehouse.dto.DoctorMaterialDetailDto;
import io.terminus.doctor.web.front.warehouse.dto.DoctorWareHouseCreateDto;
import io.terminus.doctor.web.front.warehouse.dto.DoctorWareHouseEventCriteria;
import io.terminus.doctor.web.front.warehouse.dto.DoctorWareHouseEventData;
import io.terminus.doctor.web.front.warehouse.dto.DoctorWareHouseEventDto;
import io.terminus.doctor.web.front.warehouse.dto.DoctorWareHouseUpdateDto;
import io.terminus.doctor.web.front.warehouse.dto.MaterialReport;
import io.terminus.doctor.web.front.warehouse.dto.WarehouseReport;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-05-25
 * Email:yaoqj@terminus.io
 * Descirbe: 仓库信息获取查询
 */
@Slf4j
@Controller
@RequestMapping("/api/doctor/warehouse/query")
public class DoctorWareHouseQuery {

    private final DoctorWareHouseReadService doctorWareHouseReadService;

    private final DoctorWareHouseWriteService doctorWareHouseWriteService;

    private final DoctorFarmReadService doctorFarmReadService;

    private final UserReadService<User> userReadService;

    private final DoctorUserProfileReadService doctorUserProfileReadService;

    private final DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    private final DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService;

    @RpcConsumer
    private DoctorMaterialPriceInWareHouseReadService doctorMaterialPriceInWareHouseReadService;
    @RpcConsumer
    private DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;
    @RpcConsumer
    private MaterialFactoryReadService materialFactoryReadService;
    @Autowired
    private Exporter exporter;

    @Autowired
    public DoctorWareHouseQuery(DoctorWareHouseReadService doctorWareHouseReadService,
                                DoctorWareHouseWriteService doctorWareHouseWriteService,
                                DoctorFarmReadService doctorFarmReadService, UserReadService<User> userReadService,
                                DoctorUserProfileReadService doctorUserProfileReadService,
                                DoctorBasicMaterialReadService doctorBasicMaterialReadService,
                                DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService){
        this.doctorWareHouseReadService = doctorWareHouseReadService;
        this.doctorWareHouseWriteService = doctorWareHouseWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.userReadService = userReadService;
        this.doctorUserProfileReadService = doctorUserProfileReadService;
        this.doctorBasicMaterialReadService = doctorBasicMaterialReadService;
        this.doctorMaterialInWareHouseReadService = doctorMaterialInWareHouseReadService;
    }

    @RequestMapping(value = "/listWareHouseType", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DoctorFarmWareHouseType> pagingDoctorWareHouseType(@RequestParam("farmId") Long farmId){
        return RespHelper.or500(doctorWareHouseReadService.queryDoctorFarmWareHouseType(farmId));
    }

    @RequestMapping(value = "/pagingDoctorWareHouseDto", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorWareHouseDto> pagingDoctorWareHouseDto(@RequestParam("farmId") Long farmId,
                                                               @RequestParam(value = "type", required = false) Integer type,

                                                               @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                               @RequestParam(value = "pageSize", required = false) Integer pageSize){
        return RespHelper.or500(doctorWareHouseReadService.queryDoctorWarehouseDto(farmId, type, pageNo, pageSize));
    }

    /**
     * 获取猪场下的所有仓库
     * @param farmId 猪场id
     * @param type
     * @return
     */
    @RequestMapping(value = "/queryAllDoctorWareHouseDto", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorWareHouseDto> queryAllDoctorWareHouseDto(@RequestParam("farmId") Long farmId,
                                                               @RequestParam(value = "type", required = false) Integer type) {
        return RespHelper.or500(doctorWareHouseReadService.queryDoctorWarehouseDto(farmId, type, 1, Integer.MAX_VALUE));
    }

    @RequestMapping(value = "/listDoctorWareHouseDto", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DoctorWareHouseDto> listDoctorWareHouseDto(@RequestParam("farmId") Long farmId,
                                                           @RequestParam(value = "type", required = false) Integer type,
                                                           @RequestParam(value = "warehouseName", required = false) String warehouseName){
        return RespHelper.or500(doctorWareHouseReadService.listDoctorWareHouseDto(farmId, type, warehouseName));
    }

    @RequestMapping(value = "/findMaterialFactory", method = RequestMethod.GET)
    @ResponseBody
    public List<MaterialFactory> findMaterialFactory(@RequestParam("farmId") Long farmId){
        return RespHelper.or500(materialFactoryReadService.findByFarmId(farmId));
    }

    /**
     * 通过WareHouseId 获取仓库详细信息内容
     * @param warehouseId
     * @return
     */
    @RequestMapping(value = "/queryDtoByWareHouseId", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorWareHouseDto queryDtoByWareHouseId(@RequestParam("warehouseId") Long warehouseId){
        return RespHelper.or500(doctorWareHouseReadService.queryDoctorWareHouseById(warehouseId));
    }

    @RequestMapping(value = "/updateWareHouse", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateWareHouse(@RequestBody DoctorWareHouseUpdateDto doctorWareHouseUpdateDto){
        DoctorWareHouse doctorWareHouse;
        try{
            // get user reader info
            Response<User> userResponse = userReadService.findById(UserUtil.getUserId());
            checkState(userResponse.isSuccess(), "read.userInfo.fail");
            User user = userResponse.getResult();

            doctorWareHouse = DoctorWareHouse.builder().id(doctorWareHouseUpdateDto.getDoctorWareHouseId())
                    .address(doctorWareHouseUpdateDto.getAddress())
                    .managerId(doctorWareHouseUpdateDto.getManagerId()).managerName(doctorWareHouseUpdateDto.getWarehouseName())
                    .wareHouseName(doctorWareHouseUpdateDto.getWarehouseName())
                    .updatorName(user.getName()).build();
        }catch (Exception e){
            log.error("update warehouse fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("update.warehouse.fail");
        }

        return RespHelper.or500(doctorWareHouseWriteService.updateWareHouse(doctorWareHouse));
    }

    @RequestMapping(value = "/createWareHouse", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createWareHouseInfo(@RequestBody DoctorWareHouseCreateDto doctorWareHouseCreateDto){
        DoctorWareHouse doctorWareHouse;
        try{
            // get farm info
            Response<DoctorFarm> farmResponse = doctorFarmReadService.findFarmById(doctorWareHouseCreateDto.getFarmId());
            checkState(farmResponse.isSuccess(), "read.farmInfo.fail");
            DoctorFarm doctorFarm = farmResponse.getResult();

            // get user reader info
            UserProfile userProfile = RespHelper.orServEx(
                    doctorUserProfileReadService.findProfileByUserIds(
                            Lists.newArrayList(doctorWareHouseCreateDto.getManagerId()))).get(0);

            Response<User> currentUserResponse = userReadService.findById(UserUtil.getUserId());
            User currentUser = currentUserResponse.getResult();

            doctorWareHouse = DoctorWareHouse.builder()
                    .wareHouseName(doctorWareHouseCreateDto.getWareHouseName())
                    .farmId(doctorWareHouseCreateDto.getFarmId()).farmName(doctorFarm.getName())
                    .managerId(doctorWareHouseCreateDto.getManagerId()).managerName(userProfile.getRealName())
                    .address(doctorWareHouseCreateDto.getAddress()).type(doctorWareHouseCreateDto.getType())
                    .creatorId(currentUser.getId()).creatorName(currentUser.getName())
                    .build();
        }catch (Exception e){
            log.error("create ware house info fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
        return RespHelper.or500(doctorWareHouseWriteService.createWareHouse(doctorWareHouse));
    }

    /**
     * 查询指定仓库中不存在的基础物料
     * @param type 基础物料类型
     * @see io.terminus.doctor.common.enums.WareHouseType
     * @param srm 输入码
     * @param wareHouseId 仓库id
     * @param exIds 排除掉的ids
     * @return
     */
    @RequestMapping(value = "/findBasicMaterialNotInWareHouse", method = RequestMethod.GET)
    @ResponseBody
    public List<DoctorBasicMaterial> findBasicMaterialNotInWareHouse(@RequestParam(value = "type", required = false) Integer type,
                                                                     @RequestParam(value = "srm", required = false) String srm,
                                                                     @RequestParam(value = "exIds", required = false) String exIds,
                                                                     @RequestParam Long wareHouseId){
        DoctorWareHouse wareHouse = RespHelper.or500(doctorWareHouseReadService.findById(wareHouseId));
        //已存在的物料id
        List<Long> exist = RespHelper.or500(doctorMaterialInWareHouseReadService.queryDoctorMaterialInWareHouse(wareHouse.getFarmId(), wareHouseId))
                .stream()
                .map(DoctorMaterialInWareHouse::getMaterialId)
                .collect(Collectors.toList());
        return RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialByTypeFilterBySrm(type, srm, exIds))
                .stream()
                .filter(basicMaterial -> !exist.contains(basicMaterial.getId()))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/materials", method = RequestMethod.GET)
    @ResponseBody
    public List<DoctorBasicMaterial> getDoctorBasicMaterials(@RequestParam Long farmId,
                                                             @RequestParam(value = "type", required = false) Long type,
                                                             @RequestParam(value = "srm", required = false) String srm){
        return RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialsOwned(farmId, type, srm));
    }

    /**
     * 获取物料信息列表
     * @param farmId 猪场id
     * @param wareHouseId 仓库id
     * @param type
     * @param srm
     * @return
     */
    @RequestMapping(value = "/materialDetails", method = RequestMethod.GET)
    @ResponseBody
    public List<DoctorMaterialDetailDto> getMaterialDetails(@RequestParam Long farmId,
                                                            @RequestParam Long wareHouseId,
                                                            @RequestParam(value = "type", required = false) Long type,
                                                            @RequestParam(value = "srm", required = false) String srm){
        Response<List<DoctorBasicMaterial>> listResponse = doctorBasicMaterialReadService.findBasicMaterialsOwned(farmId, type, srm);
        if (!listResponse.isSuccess() || Arguments.isNullOrEmpty(listResponse.getResult())) {
            return Collections.emptyList();
        }
        List<DoctorBasicMaterial> materialList = listResponse.getResult();
        List<DoctorMaterialDetailDto> detailDtoList = Lists.newArrayList();
        materialList.forEach(doctorBasicMaterial -> {
            DoctorMaterialInWareHouse materialInWareHouse = RespHelper.or500(doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(farmId, doctorBasicMaterial.getId(), wareHouseId));
            DoctorMaterialDetailDto detailDto = BeanMapper.map(doctorBasicMaterial, DoctorMaterialDetailDto.class);
            if (materialInWareHouse != null) {
                detailDto.setLotNumber(materialInWareHouse.getLotNumber());
                detailDtoList.add(detailDto);
            }
        });
        return detailDtoList;
    }

    /**
     * 分页查询仓库历史出入记录
     * @param farmId 猪场id , 必传
     * @param warehouseId 仓库id
     * @param materialId 物料id
     * @param eventType 事件类型
     *                  @see DoctorMaterialConsumeProvider.EVENT_TYPE
     * @param materilaType 物料(仓库)类型
     *                     @see io.terminus.doctor.common.enums.WareHouseType
     * @param staffId 事件员工id
     * @param startAt 开始日期范围
     * @param endAt 结束日期范围
     * @param pageNo 第几页
     * @param size 每页数量
     * @return
     */
    @RequestMapping(value = "/pageConsumeProvideHistory", method = RequestMethod.GET)
    @ResponseBody
    public Paging<DoctorWareHouseEventDto> pageConsumeProvideHistory(
            @RequestParam("farmId") Long farmId, // 此参数必须有
            @RequestParam(name = "warehouseId", required = false) Long warehouseId,
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) Integer eventType,
            @RequestParam(required = false) String eventTypes,
            @RequestParam(required = false) Integer materilaType,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) String startAt,
            @RequestParam(required = false) String endAt,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer size){
        List<Integer> types = Lists.newArrayList();
        if(!Strings.isNullOrEmpty(eventTypes)){
            types = Splitters.splitToInteger(eventTypes, Splitters.UNDERSCORE);
        }
        Response<Paging<DoctorMaterialConsumeProvider>> pagingResponse = doctorMaterialConsumeProviderReadService.page(farmId, warehouseId, materialId, eventType, types,
                materilaType, staffId, startAt, endAt, pageNo, size);
        if (!pagingResponse.isSuccess()) {
            return Paging.empty();
        }
        List<DoctorWareHouseEventDto> eventDtoList = pagingResponse.getResult().getData().stream()
                .map(provider -> {
                    DoctorWareHouseEventDto eventDto = BeanMapper.map(provider, DoctorWareHouseEventDto.class);
                    //设置仓库事件是否可回滚, 若获取事件是否可回滚错误则默认不可回滚
                    boolean isRollback = false;
                    Response<Boolean> isRollbackResponse = doctorMaterialConsumeProviderReadService.eventCanRollback(provider.getId());
                    if (isRollbackResponse.isSuccess()) {
                        isRollback = true;
                    }
                    eventDto.setIsRollback(isRollback);
                    return eventDto;
                }).collect(Collectors.toList());
        return new Paging<>(pagingResponse.getResult().getTotal(), eventDtoList);
    }

    /**
     * 仓库事件导出
     * @param criteria
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @ResponseBody
    public void exportWareHouseEvent(@Valid DoctorWareHouseEventCriteria criteria,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        try {
            exporter.export("web-wareHouse-event", JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.getMapper().convertValue(criteria, Map.class), 1, 200, this::pagingExportData ,request, response);
        } catch (Exception e) {
            log.error("ware.house.event.export.failed, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("ware.house.event.export.fail");
        }
    }

    public Paging<DoctorWareHouseEventData> pagingExportData(Map<String, String> wareHouseEventCriteriaMap) {
        DoctorWareHouseEventCriteria criteria = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.getMapper().convertValue(wareHouseEventCriteriaMap, DoctorWareHouseEventCriteria.class);
        Response<Paging<DoctorMaterialConsumeProvider>> pagingResponse =  doctorMaterialConsumeProviderReadService.page(criteria.getFarmId(), criteria.getWarehouseId(), criteria.getMaterialId(), criteria.getEventType(), criteria.getTypes(),
                criteria.getMaterilaType(), criteria.getStaffId(), criteria.getStartAt(), criteria.getEndAt(), Integer.parseInt(wareHouseEventCriteriaMap.get("pageNo")), Integer.parseInt(wareHouseEventCriteriaMap.get("size")));
        if (!pagingResponse.isSuccess() || Arguments.isNullOrEmpty(pagingResponse.getResult().getData())) {
            return Paging.empty();
        }
        List<DoctorWareHouseEventData> dataList = pagingResponse.getResult().getData().stream().map(provide ->{
            DoctorWareHouseEventData data = BeanMapper.map(provide, DoctorWareHouseEventData.class);
            Response<DoctorBasicMaterial> basicMaterialResponse = doctorBasicMaterialReadService.findBasicMaterialById(provide.getMaterialId());
            if (basicMaterialResponse.isSuccess()) {
                data.setUnitName(basicMaterialResponse.getResult().getUnitName());
            }
            data.setAmount(provide.getUnitPrice()*provide.getEventCount());
            return data;
        }).collect(Collectors.toList());
        return new Paging<>(pagingResponse.getResult().getTotal(), dataList);
    }

    /**
     * 仓库数量和金额的统计
     * @param farmId 猪场id , 必传
     * @param warehouseId 仓库id
     * @param materialId 物料id
     * @param eventType 事件类型
     *                  @see DoctorMaterialConsumeProvider.EVENT_TYPE
     * @param materilaType 物料(仓库)类型
     *                     @see io.terminus.doctor.common.enums.WareHouseType
     * @param staffId 事件员工id
     * @param startAt 开始日期范围
     * @param endAt 结束日期范围
     * @param barnId 猪舍id
     * @param groupId 猪群id
     * @param pageNo 第几页
     * @param size 每页数量
     * @return
     */
    @RequestMapping(value = "/countAmount", method = RequestMethod.GET)
    @ResponseBody
    public Paging<MaterialCountAmount> countAmount(@RequestParam Long farmId,  // 此参数必须有
                                                 @RequestParam(required = false) Long warehouseId,
                                                 @RequestParam(required = false) Long materialId,
                                                 @RequestParam(required = false) Integer eventType,
                                                 @RequestParam(required = false) Integer materilaType,
                                                 @RequestParam(required = false) Long barnId,
                                                 @RequestParam(required = false) Long groupId,
                                                 @RequestParam(required = false) Long staffId,
                                                 @RequestParam(required = false) String startAt,
                                                 @RequestParam(required = false) String endAt,
                                                 @RequestParam(required = false) Integer pageNo,
                                                 @RequestParam(required = false) Integer size){
        return RespHelper.or500(doctorMaterialConsumeProviderReadService.countAmount(farmId, warehouseId, materialId, eventType, materilaType,
                barnId, groupId, staffId, startAt, endAt, pageNo, size));
    }

    /**
     * 仓库报表
     * @param farmId 猪场id
     * @param wareHouseType 仓库类型
     *                      @see io.terminus.doctor.common.enums.WareHouseType
     * @param startAt
     * @param endAt
     * @return
     */
    @RequestMapping(value = "/warehouseReport", method = RequestMethod.GET)
    @ResponseBody
    public WarehouseReport warehouseReport(@RequestParam Long farmId,
                                           @RequestParam Integer wareHouseType,
                                           @RequestParam(required = false) String startAt,
                                           @RequestParam(required = false) String endAt){
        Date end = endAt == null ? null : DateTime.parse(endAt).plusDays(1).toDate();
        List<WarehouseEventReport> warehouseEventReports = RespHelper.or500(doctorMaterialConsumeProviderReadService.warehouseEventReport(
                farmId, null, null, null, null, null, wareHouseType, null, startAt, DateUtil.toDateString(end)
        ));

        Map<Long, WarehouseReport.Report> reportmap = new HashMap<>();
        for(WarehouseEventReport report : warehouseEventReports){
            Long houseId = report.getWarehouseId();
            DoctorMaterialConsumeProvider.EVENT_TYPE eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.from(report.getEventType());
            if(eventType == null){
                continue;
            }
            if(!reportmap.containsKey(houseId)){
                reportmap.put(houseId, new WarehouseReport.Report());
            }
            WarehouseReport.Report inner = reportmap.get(houseId);
            if(eventType.equals(DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER)
                    || eventType.equals(DoctorMaterialConsumeProvider.EVENT_TYPE.FORMULA_FEED)){
                inner.setInAmount(inner.getInAmount() + report.getAmount());
                inner.setInCount(inner.getInCount() + report.getCount());
            }
            if(eventType.equals(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER)
                    || eventType.equals(DoctorMaterialConsumeProvider.EVENT_TYPE.FORMULA_RAW_MATERIAL)){
                inner.setOutAmount(inner.getOutAmount() + report.getAmount());
                inner.setOutCount(inner.getOutCount() + report.getCount());
            }
        }

        // 各仓库当前库存的金额
        Map<Long, Double> stockAmount = RespHelper.or500(doctorMaterialPriceInWareHouseReadService.stockAmount(farmId, null, WareHouseType.from(wareHouseType)));
        // 各仓库基本信息及 track 信息
        List<DoctorWareHouseDto> warehouses = RespHelper.or500(doctorWareHouseReadService.listDoctorWareHouseDto(farmId, wareHouseType, null));

        WarehouseReport result = new WarehouseReport();
        //每个仓库的报表
        for(DoctorWareHouseDto warehouseDto : warehouses){
            Long houseId = warehouseDto.getWarehouseId();
            WarehouseReport.Report inner = new WarehouseReport.Report();
            inner.setWarehouseId(warehouseDto.getWarehouseId());
            inner.setWarehouseName(warehouseDto.getWarehouseName());
            if(reportmap.containsKey(houseId)){
                inner.setOutCount(reportmap.get(houseId).getOutCount());
                inner.setOutAmount(reportmap.get(houseId).getOutAmount());
                inner.setInCount(reportmap.get(houseId).getInCount());
                inner.setInAmount(reportmap.get(houseId).getInAmount());
            }
            inner.setCurrentStock(warehouseDto.getRemainder());
            if(stockAmount.get(houseId) != null){
                inner.setCurrentStockAmount(stockAmount.get(houseId));
            }
            result.getWarehouseReports().add(inner);
        }
        // 所有仓库合计的报表
        result.setTotalReport(result.getWarehouseReports().stream().reduce((report1, report2) -> {
            WarehouseReport.Report inner = new WarehouseReport.Report();
            inner.setInAmount(report1.getInAmount() + report2.getInAmount());
            inner.setInCount(report1.getInCount() + report2.getInCount());
            inner.setOutAmount(report1.getOutAmount() + report2.getOutAmount());
            inner.setOutCount(report1.getOutCount() + report2.getOutCount());
            inner.setCurrentStock(report1.getCurrentStock() + report2.getCurrentStock());
            inner.setCurrentStockAmount(report1.getCurrentStockAmount() + report2.getCurrentStockAmount());
            return inner;
        }).orElse(new WarehouseReport.Report()));
        return result;
    }

    @RequestMapping(value = "/materialReport", method = RequestMethod.GET)
    @ResponseBody
    public MaterialReport materialReport(@RequestParam Long farmId,
                                         @RequestParam Long warehouseId,
                                         @RequestParam(required = false) Long materialId,
                                         @RequestParam(required = false) String materialName,
                                         @RequestParam(required = false) Integer eventType,
                                         @RequestParam(required = false) List<Integer> eventTypes,
                                         @RequestParam(required = false) String startAt,
                                         @RequestParam(required = false) String endAt){
        Date end = endAt == null ? null : DateTime.parse(endAt).plusDays(1).toDate();

        // 该仓库各事件的数量和金额
        List<WarehouseEventReport> warehouseEventReports = RespHelper.or500(
                doctorMaterialConsumeProviderReadService.warehouseEventReport(
                        farmId, warehouseId, materialId, materialName, eventType, eventTypes, null, null, startAt, DateUtil.toDateString(end)
        ));
        // 仓库基本信息及 track 信息
        DoctorWareHouseDto wareHouseDto = RespHelper.or500(doctorWareHouseReadService.queryDoctorWareHouseById(warehouseId));

        WarehouseReport.Report total = new WarehouseReport.Report();
        total.setWarehouseId(warehouseId);
        total.setWarehouseName(wareHouseDto.getWarehouseName());
        total.setCurrentStock(wareHouseDto.getRemainder());
        for(WarehouseEventReport report : warehouseEventReports){
            DoctorMaterialConsumeProvider.EVENT_TYPE event_type = DoctorMaterialConsumeProvider.EVENT_TYPE.from(report.getEventType());
            if(event_type == null){
                continue;
            }
            if(event_type.isIn()){
                total.setInAmount(total.getInAmount() + report.getAmount());
                total.setInCount(total.getInCount() + report.getCount());
            }
            if(event_type.isOut()){
                total.setOutAmount(total.getOutAmount() + report.getAmount());
                total.setOutCount(total.getOutCount() + report.getCount());
            }
        }
        // 该仓库当前库存的价值
        Double amount = RespHelper.or500(doctorMaterialPriceInWareHouseReadService.stockAmount(farmId, warehouseId, null)).get(warehouseId);
        total.setCurrentStockAmount(amount);

        MaterialReport result = new MaterialReport();
        result.setTotalReport(total);
        List<MaterialReport.MaterialConsumeProviderDto> events = RespHelper.or500(doctorMaterialConsumeProviderReadService.list(
                farmId, warehouseId, materialId, materialName, eventType, eventTypes, null, null, startAt, DateUtil.toDateString(end)
        )).stream()
                .map(cp -> {
                    MaterialReport.MaterialConsumeProviderDto dto = BeanMapper.map(cp, MaterialReport.MaterialConsumeProviderDto.class);
                    if (Objects.equals(cp.getEventType(), DoctorMaterialConsumeProvider.EVENT_TYPE.DIAORU.getValue())
                            || Objects.equals(cp.getEventType(), DoctorMaterialConsumeProvider.EVENT_TYPE.DIAOCHU.getValue())) {
                        Long relEventId = null;
                        try {
                            relEventId = Long.valueOf(cp.getExtraMap().get("relEventId").toString());
                        } catch (RuntimeException e) {
                        }
                        if (relEventId != null) {
                            DoctorMaterialConsumeProvider relCP = RespHelper.or500(doctorMaterialConsumeProviderReadService.findById(relEventId));
                            dto.setDiaoboWarehouseId(relCP.getWareHouseId());
                            dto.setDiaoboWarehouseName(relCP.getWareHouseName());
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        result.setEvents(events);
        return result;
    }

    /**
     * 以猪舍为维度统计物资领用情况
     * @param farmId
     * @param warehouseId
     * @param materialId
     * @param materialName
     * @param warehouseType
     * @param barnId
     * @param staffId
     * @param creatorId
     * @param startAt
     * @param endAt
     */
    @RequestMapping(value = "/barnReport", method = RequestMethod.GET)
    @ResponseBody
    public Paging<BarnConsumeMaterialReport> barnReport(
            @RequestParam Long farmId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) Integer warehouseType,
            @RequestParam(required = false) Long barnId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) String startAt,
            @RequestParam(required = false) String endAt,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
    ){
        Date end = endAt == null ? null : DateTime.parse(endAt).plusDays(1).toDate();
        return RespHelper.or500(doctorMaterialConsumeProviderReadService.barnConsumeMaterialReport(
                farmId, warehouseId, materialId, materialName, WareHouseType.from(warehouseType), barnId, staffId,
                creatorId, startAt, DateUtil.toDateString(end), pageNo, pageSize
        ));
    }
}
