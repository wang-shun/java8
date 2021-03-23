package io.terminus.doctor.web.front.event.impl;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSkuReadService;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorGroupInputInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInputInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.InType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorEventModifyRequestWriteService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.event.service.DoctorModifyEventService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.user.service.PrimaryUserReadService;
import io.terminus.doctor.web.core.aspects.DoctorValidService;
import io.terminus.doctor.web.front.event.dto.DoctorBatchGroupEventDto;
import io.terminus.doctor.web.front.event.dto.DoctorBatchNewGroupEventDto;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.terminus.common.utils.Arguments.*;
import static io.terminus.common.utils.BeanMapper.map;
import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static io.terminus.doctor.common.utils.RespHelper.orServEx;
import static io.terminus.doctor.common.utils.RespWithExHelper.orInvalid;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
@Service
public class DoctorGroupWebServiceImpl implements DoctorGroupWebService {

    private final DoctorGroupWriteService doctorGroupWriteService;
    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorBasicReadService doctorBasicReadService;
    private final DoctorBarnReadService doctorBarnReadService;
    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorOrgReadService doctorOrgReadService;
    private final DoctorBasicWriteService doctorBasicWriteService;
    private final DoctorValidService doctorValidService;

    @RpcConsumer
    private DoctorWarehouseSkuReadService doctorWarehouseSkuReadService;

    @RpcConsumer
    private DoctorBasicMaterialReadService doctorBasicMaterialReadService;
    @RpcConsumer
    private DoctorUserProfileReadService doctorUserProfileReadService;
    @RpcConsumer
    private PrimaryUserReadService primaryUserReadService;
    @RpcConsumer
    private DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;
    @RpcConsumer
    private DoctorEventModifyRequestWriteService doctorEventModifyRequestWriteService;
    @RpcConsumer
    private DoctorModifyEventService doctorModifyEventService;

    @Autowired
    public DoctorGroupWebServiceImpl(DoctorGroupWriteService doctorGroupWriteService,
                                     DoctorFarmReadService doctorFarmReadService,
                                     DoctorBasicReadService doctorBasicReadService,
                                     DoctorBarnReadService doctorBarnReadService,
                                     DoctorGroupReadService doctorGroupReadService,
                                     DoctorOrgReadService doctorOrgReadService,
                                     DoctorBasicWriteService doctorBasicWriteService,
                                     DoctorValidService doctorValidService) {
        this.doctorGroupWriteService = doctorGroupWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorBasicReadService = doctorBasicReadService;
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorOrgReadService = doctorOrgReadService;
        this.doctorBasicWriteService = doctorBasicWriteService;
        this.doctorValidService = doctorValidService;
    }

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private static final Long CHANGE_TYPE_SALE = 109L;

    @Override
    public RespWithEx<Long> createNewGroup(DoctorNewGroupInput newGroupInput) {
        try {
            log.info("web createNewGroup starting, input:{}", newGroupInput);
            //1.构造猪群信息
            expectTrue(notEmpty(newGroupInput.getGroupCode()), "groupCode.not.empty");
            DoctorGroup doctorGroup = getNewGroup(newGroupInput);
            newGroupInput = doctorValidService.valid(newGroupInput, newGroupInput.getGroupCode());
            return doctorGroupWriteService.createNewGroup(doctorGroup, newGroupInput);
        } catch (InvalidException e) {
            log.error("create new group failed, input:{}, cause:{}", newGroupInput, Throwables.getStackTraceAsString(e));
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("create new group failed, input:{}, cause:{}", newGroupInput, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("create new group failed, input:{}, cause:{}", newGroupInput, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.create.fail");
        }

    }

    @Override
    public RespWithEx<Boolean> batchNewGroupEvent(DoctorBatchNewGroupEventDto batchNewGroupEventDto) {
        try {
            log.info("web batch newGroupEvent starting");
            if (batchNewGroupEventDto == null || Arguments.isNullOrEmpty(batchNewGroupEventDto.getNewGroupInputList())) {
                log.error("batch.event.input.empty");
                return RespWithEx.fail("batch.event.input.empty");
            }
            List<DoctorNewGroupInputInfo> newGroupInputInfoList = batchNewGroupEventDto.getNewGroupInputList().stream()
                    .map(doctorNewGroupInput -> {
                        expectTrue(notEmpty(doctorNewGroupInput.getGroupCode()), "groupCode.not.empty");
                        try {
                            doctorNewGroupInput = doctorValidService.valid(doctorNewGroupInput, doctorNewGroupInput.getGroupCode());
                            return new DoctorNewGroupInputInfo(getNewGroup(doctorNewGroupInput), doctorNewGroupInput);
                        } catch (InvalidException e) {
                            throw new InvalidException(true, e.getError(), doctorNewGroupInput.getGroupCode(), e.getParams());
                        } catch (ServiceException e) {
                            throw new InvalidException(true, e.getMessage(), doctorNewGroupInput.getGroupCode());
                        }
                    }).collect(Collectors.toList());
            return doctorGroupWriteService.batchNewGroupEventHandle(newGroupInputInfoList);
        } catch (InvalidException e) {
            log.error("create new group failed, cause:{}", Throwables.getStackTraceAsString(e));
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("create new group failed, cause:{}", Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("create new group failed, cause:{}", Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.create.fail");
        }
    }

    //构造新建猪群信息
    private DoctorGroup getNewGroup(DoctorNewGroupInput newGroupInput) {
        newGroupInput.setIsAuto(IsOrNot.NO.getValue());
        newGroupInput.setEventSource(SourceType.INPUT.getValue());
        newGroupInput.setBarnName(getBarnName(newGroupInput.getBarnId()));
        newGroupInput.setBreedName(getBasicName(newGroupInput.getBreedId()));
        newGroupInput.setGeneticName(getBasicName(newGroupInput.getGeneticId()));

        //事件录入人信息
        newGroupInput.setCreatorId(UserUtil.getUserId());
        newGroupInput.setCreatorName(UserUtil.getCurrentUser().getName());

        DoctorGroup group = map(newGroupInput, DoctorGroup.class);
        group.setRemark(null);  //dozer不需要转换remark
        group.setStaffId(UserUtil.getUserId());
        group.setStaffName(orServEx(this.findRealName(UserUtil.getUserId())));

        //设置猪场公司信息
        DoctorFarm farm = orServEx(doctorFarmReadService.findFarmById(group.getFarmId()));
        expectTrue(notNull(farm), "farm.not.null", group.getFarmId());
        group.setFarmName(farm.getName());

        DoctorOrg org = orServEx(doctorOrgReadService.findOrgById(farm.getOrgId()));
        expectTrue(notNull(org), "org.not.null", farm.getOrgId());
        group.setOrgId(org.getId());
        group.setOrgName(org.getName());

        return group;
    }

    @Override
    public RespWithEx<Boolean> createGroupEvent(Long groupId, Integer eventType, String data) {
        try {
            log.info("create group event, groupId:{}, eventType:{}, data:{}", groupId, eventType, data);

            //1.校验猪群是否存在
            DoctorGroupDetail groupDetail = checkGroupExist(groupId);

            //2.校验能否操作此事件
            checkEventTypeIllegal(groupDetail, eventType);

            Map<String, Object> params = JSON_MAPPER.fromJson(data, JSON_MAPPER.createCollectionType(Map.class, String.class, Object.class));


            //3.校验事件的时间
            Date eventAt = DateUtil.toDate((String) params.get("eventAt"));
            checkEventAt(groupId, eventAt);

            //4.新增校验事件为最后一次时间以后(孔景军)
            DoctorGroupEvent lastEventDate = doctorGroupReadService.findLastEvent(groupId);
            if(eventAt.getTime()<lastEventDate.getEventAt().getTime()){
                throw new InvalidException("事件不能小于最后一次时间");
            }

            String groupCode = getGroupCode(groupId);
            //4.根据不同的事件类型调用不同的录入接口
            GroupEventType groupEventType = checkNotNull(GroupEventType.from(eventType));
            params.put("eventType", eventType);
            params.put("eventSource", SourceType.INPUT.getValue());
            switch (groupEventType) {
                case MOVE_IN:
                    params.put("breedName", getBasicName(getLong(params, "breedId")));
                    params.put("inTypeName", InType.from(getInteger(params, "inType")).getDesc());
                    params.put("toBarnName", getBarnName(getLong(params, "toBarnId")));
                    orInvalid(doctorGroupWriteService.groupEventMoveIn(groupDetail, doctorValidService.valid(map(putBasicFields(params), DoctorMoveInGroupInput.class), groupCode)));
                    break;
                case CHANGE:
                    checkParam(params, groupDetail.getGroup().getGroupCode());
                    params.put("changeTypeName", getBasicName(getLong(params, "changeTypeId")));
                    params.put("changeReasonName", getChangeReasonName(getLong(params, "changeReasonId")));
                    if (params.get("customerName") == null || params.get("customerName") == "") {
                        params.put("customerName", getCustomerName(getLong(params, "customerId")));
                    }
                    DoctorChangeGroupInput changeInput = map(putBasicFields(params), DoctorChangeGroupInput.class);
                    //添加客户
                    Long customerId = orServEx(doctorBasicWriteService.addCustomerWhenInput(groupDetail.getGroup().getFarmId(),
                            groupDetail.getGroup().getFarmName(), changeInput.getCustomerId(), changeInput.getCustomerName(),
                            UserUtil.getUserId(), UserUtil.getCurrentUser().getName()));

                    params.put("customerId", customerId);
                    params.put("fcrFeed", RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, groupId, null, null), 0D));
                    orInvalid(doctorGroupWriteService.groupEventChange(groupDetail, doctorValidService.valid(changeInput, groupCode)));
                    break;
                case TRANS_GROUP:
                    params.put("toBarnName", getBarnName(getLong(params, "toBarnId")));
                    params.put("breedName", getBasicName(getLong(params, "breedId")));

                    //如果不新建猪群, 拼上转入猪群号
                    if (Integer.valueOf(String.valueOf(params.get("isCreateGroup"))).equals(IsOrNot.NO.getValue())) {
                        params.put("toGroupCode", getGroupCode(getLong(params, "toGroupId")));
                    }
                    params.put("fcrFeed", RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, groupId, null, null), 0D));
                    orInvalid(doctorGroupWriteService.groupEventTransGroup(groupDetail, doctorValidService.valid(map(putBasicFields(params), DoctorTransGroupInput.class), groupCode)));
                    break;
                case TURN_SEED:
                    params.put("breedName", getBasicName(getLong(params, "breedId")));
                    params.put("geneticName", getBasicName(getLong(params, "geneticId")));
                    params.put("toBarnName", getBarnName(getLong(params, "toBarnId")));
                    params.put("fcrFeed", RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, groupId, null, null), 0D));
                    orInvalid(doctorGroupWriteService.groupEventTurnSeed(groupDetail, doctorValidService.valid(map(putBasicFields(params), DoctorTurnSeedGroupInput.class), groupCode)));
                    break;
                case LIVE_STOCK:
                    orInvalid(doctorGroupWriteService.groupEventLiveStock(groupDetail, doctorValidService.valid(map(putBasicFields(params), DoctorLiveStockGroupInput.class), groupCode)));
                    break;
                case DISEASE:
                    params.put("diseaseName", getBasicName(getLong(params, "diseaseId")));
                    params.put("doctorName", orServEx(this.findRealName(getLong(params, "doctorId"))));
                    orInvalid(doctorGroupWriteService.groupEventDisease(groupDetail, doctorValidService.valid(map(putBasicFields(params), DoctorDiseaseGroupInput.class), groupCode)));
                    break;
                case ANTIEPIDEMIC:
                    params.put("vaccinName", getVaccinName(getLong(params, "vaccinId")));
                    params.put("vaccinStaffName", orServEx(this.findRealName(getLong(params, "vaccinStaffId"))));
                    params.put("vaccinItemName", getVaccinItemName(getLong(params, "vaccinItemId")));
                    orInvalid(doctorGroupWriteService.groupEventAntiepidemic(groupDetail, doctorValidService.valid(map(putBasicFields(params), DoctorAntiepidemicGroupInput.class), groupCode)));
                    break;
                case TRANS_FARM:
                    params.put("toFarmName", getFarmName(getLong(params, "toFarmId")));
                    params.put("toBarnName", getBarnName(getLong(params, "toBarnId")));
                    params.put("breedName", getBasicName(getLong(params, "breedId")));

                    //如果不新建猪群, 拼上转入猪群号
                    if (Integer.valueOf(String.valueOf(params.get("isCreateGroup"))).equals(IsOrNot.NO.getValue())) {
                        params.put("toGroupCode", getGroupCode(getLong(params, "toGroupId")));
                    }
                    params.put("fcrFeed", RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, groupId, null, null), 0D));
                    DoctorTransFarmGroupInput input = doctorValidService.valid(map(putBasicFields(params), DoctorTransFarmGroupInput.class), groupCode);
                    input.setAvgWeight(input.getWeight() / input.getQuantity());
                    orInvalid(doctorGroupWriteService.groupEventTransFarm(groupDetail, input));
                    break;
                case CLOSE:
                    orInvalid(doctorGroupWriteService.groupEventClose(groupDetail, doctorValidService.valid(map(putBasicFields(params), DoctorCloseGroupInput.class), groupCode)));
                    break;
                default:
                    throw new InvalidException("event.type.illegal", groupEventType, groupDetail.getGroup().getGroupCode());
            }
            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            log.error("create new group failed, cause:{}", Throwables.getStackTraceAsString(e));
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("create new group failed, cause:{}", Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("create group event failed, groupId:{}, eventType:{}, params:{}, cause:{}",
                    groupId, eventType, data, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("create.group.event.fail");
        }
    }

    @Override
    public RespWithEx<Boolean> batchGroupEvent(DoctorBatchGroupEventDto batchGroupEventDto) {
        try {
            log.info("web batch group event starting");
            if (batchGroupEventDto == null || Arguments.isNullOrEmpty(batchGroupEventDto.getInputList())) {
                log.error("batch group event input empty");
                return RespWithEx.fail("batch.event.input.empty");
            }
            List<DoctorGroupInputInfo> groupInputInfoList = batchGroupEventDto.getInputList()
                    .stream().map(inputInfo -> {
                        String groupCode = getGroupCode(inputInfo.getGroupId());
                        try {
                            DoctorGroupInputInfo groupInputInfo = buildGroupEventInputInfo(inputInfo.getGroupId(), batchGroupEventDto.getEventType(), inputInfo.getInputJson());
                            return doctorValidService.valid(groupInputInfo, groupInputInfo.getGroupDetail().getGroup().getGroupCode());
                        } catch (InvalidException e) {
                            throw new InvalidException(true, e.getError(), groupCode, e.getParams());
                        } catch (ServiceException e) {
                            throw new InvalidException(true, e.getMessage(), groupCode);
                        }
                    }).collect(Collectors.toList());
            return doctorGroupWriteService.batchGroupEventHandle(groupInputInfoList, batchGroupEventDto.getEventType());

        } catch (InvalidException e) {
            log.error("batch group event failed, batchGroupEventDto:{}, cause:{}", batchGroupEventDto, Throwables.getStackTraceAsString(e));
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("batch group event failed, batchGroupEventDto:{}, cause:{}", batchGroupEventDto, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("batch group event failed, batchGroupEventDto:{}, cause:{}", batchGroupEventDto, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.create.fail");
        }
    }

    @Override
    public RespWithEx<Boolean> createGroupModifyEventRequest(Long groupId, Integer eventType, Long eventId, String data) {
        try {
            DoctorGroupDetail groupDetail = expectNotNull(RespHelper.or500(doctorGroupReadService.findGroupDetailByGroupId(groupId)), "group.detail.not.found");
            Map<String, Object> params = JSON_MAPPER.fromJson(data, JSON_MAPPER.createCollectionType(Map.class, String.class, Object.class));
            DoctorGroupInputInfo groupInputInfo = buildGroupEventInfo(eventType, groupDetail, params);
            return doctorModifyEventService.modifyGroupEvent(groupInputInfo.getInput(), eventId, eventType);
        } catch (InvalidException e) {
            log.error("batch group event failed, groupId:{}, eventType:{}, eventId:{}, data:{}, cause:{}", groupId, eventType, eventId, data, Throwables.getStackTraceAsString(e));
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("batch group event failed, groupId:{}, eventType:{}, eventId:{}, data:{}, cause:{}", groupId, eventType, eventId, data, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("batch group event failed, groupId:{}, eventType:{}, eventId:{}, data:{}, cause:{}", groupId, eventType, eventId, data, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.create.fail");
        }
    }

    /**
     * 构建猪群事件信息
     *
     * @param groupId   猪群id
     * @param eventType 事件类型
     * @param data      输入数据
     * @return 猪群事件信息封装
     */
    private DoctorGroupInputInfo buildGroupEventInputInfo(Long groupId, Integer eventType, String data) {
        //1.校验猪群是否存在
        DoctorGroupDetail groupDetail = checkGroupExist(groupId);

        //2.校验能否操作此事件
        checkEventTypeIllegal(groupDetail, eventType);

        Map<String, Object> params = JSON_MAPPER.fromJson(data, JSON_MAPPER.createCollectionType(Map.class, String.class, Object.class));

        //3.校验事件的时间
        Date eventAt = DateUtil.toDate((String) params.get("eventAt"));
        checkEventAt(groupId, eventAt);

        //批量操作猪群事件，校验新建事件时间小于最后一次事件时间(孔景军)
        DoctorGroupEvent lastEventDate = doctorGroupReadService.findLastEvent(groupId);
        if(eventAt.getTime()<lastEventDate.getEventAt().getTime()){
            throw new InvalidException("事件不能小于最后一次时间");
        }
        //4.根据不同的事件类型调用不同的录入接口
        return buildGroupEventInfo(eventType, groupDetail, params);
    }

    private DoctorGroupInputInfo buildGroupEventInfo(Integer eventType, DoctorGroupDetail groupDetail, Map<String, Object> params) {
        Long groupId = groupDetail.getGroup().getId();
        GroupEventType groupEventType = checkNotNull(GroupEventType.from(eventType));
        params.put("eventType", eventType);
        params.put("eventSource", SourceType.INPUT.getValue());
        switch (groupEventType) {
            case MOVE_IN:
                params.put("breedName", getBasicName(getLong(params, "breedId")));
                params.put("inTypeName", InType.from(getInteger(params, "inType")).getDesc());
                params.put("toBarnName", getBarnName(getLong(params, "toBarnId")));
                return new DoctorGroupInputInfo(groupDetail, map(putBasicFields(params), DoctorMoveInGroupInput.class));
            case CHANGE:
                if(params.get("weight") == null){
                    params.put("weight",0);
                }
                checkParam(params, groupDetail.getGroup().getGroupCode());
                params.put("changeTypeName", getBasicName(getLong(params, "changeTypeId")));
                params.put("changeReasonName", getChangeReasonName(getLong(params, "changeReasonId")));
                if (params.get("customerName") == null || params.get("customerName") == "") {
                    params.put("customerName", getCustomerName(getLong(params, "customerId")));
                }
                DoctorChangeGroupInput changeInput = map(putBasicFields(params), DoctorChangeGroupInput.class);
                //添加客户
                Long customerId = orServEx(doctorBasicWriteService.addCustomerWhenInput(groupDetail.getGroup().getFarmId(),
                        groupDetail.getGroup().getFarmName(), changeInput.getCustomerId(), changeInput.getCustomerName(),
                        UserUtil.getUserId(), UserUtil.getCurrentUser().getName()));

                params.put("customerId", customerId);
                params.put("fcrFeed", RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, groupId, null, null), 0D));
                return new DoctorGroupInputInfo(groupDetail, changeInput);
            case TRANS_GROUP:
                params.put("toBarnName", getBarnName(getLong(params, "toBarnId")));
                params.put("breedName", getBasicName(getLong(params, "breedId")));

                //如果不新建猪群, 拼上转入猪群号
                if (Integer.valueOf(String.valueOf(params.get("isCreateGroup"))).equals(IsOrNot.NO.getValue())) {
                    params.put("toGroupCode", getGroupCode(getLong(params, "toGroupId")));
                }
                params.put("fcrFeed", RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, groupId, null, null), 0D));
                return new DoctorGroupInputInfo(groupDetail, map(putBasicFields(params), DoctorTransGroupInput.class));
            case TURN_SEED:
                params.put("breedName", getBasicName(getLong(params, "breedId")));
                params.put("geneticName", getBasicName(getLong(params, "geneticId")));
                params.put("toBarnName", getBarnName(getLong(params, "toBarnId")));
                params.put("fcrFeed", RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, groupId, null, null), 0D));
                return new DoctorGroupInputInfo(groupDetail, map(putBasicFields(params), DoctorTurnSeedGroupInput.class));
            case LIVE_STOCK:
                return new DoctorGroupInputInfo(groupDetail, map(putBasicFields(params), DoctorLiveStockGroupInput.class));
            case DISEASE:
                params.put("diseaseName", getBasicName(getLong(params, "diseaseId")));
                params.put("doctorName", orServEx(this.findRealName(getLong(params, "doctorId"))));
                return new DoctorGroupInputInfo(groupDetail, map(putBasicFields(params), DoctorDiseaseGroupInput.class));
            case ANTIEPIDEMIC:
                params.put("vaccinName", getVaccinName(getLong(params, "vaccinId")));
                params.put("vaccinStaffName", orServEx(this.findRealName(getLong(params, "vaccinStaffId"))));
                params.put("vaccinItemName", getVaccinItemName(getLong(params, "vaccinItemId")));
                return new DoctorGroupInputInfo(groupDetail, map(putBasicFields(params), DoctorAntiepidemicGroupInput.class));
            case TRANS_FARM:
                params.put("toFarmName", getFarmName(getLong(params, "toFarmId")));
                params.put("toBarnName", getBarnName(getLong(params, "toBarnId")));
                params.put("breedName", getBasicName(getLong(params, "breedId")));

                //如果不新建猪群, 拼上转入猪群号
                if (Integer.valueOf(String.valueOf(params.get("isCreateGroup"))).equals(IsOrNot.NO.getValue())) {
                    params.put("toGroupCode", getGroupCode(getLong(params, "toGroupId")));
                }
                params.put("fcrFeed", RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, groupId, null, null), 0D));
                DoctorTransFarmGroupInput input = map(putBasicFields(params), DoctorTransFarmGroupInput.class);
                input.setAvgWeight(input.getWeight() / input.getQuantity());
                return new DoctorGroupInputInfo(groupDetail, input);
            case CLOSE:
                return new DoctorGroupInputInfo(groupDetail, map(putBasicFields(params), DoctorCloseGroupInput.class));
            default:
                throw new InvalidException("event.type.illegal", groupEventType, groupDetail.getGroup().getGroupCode());
        }
    }

    @Override
    public Response<String> generateGroupCode(String barnName) {
        if (isEmpty(barnName)) {
            return Response.ok();
        }
        return Response.ok(barnName + "(" + DateUtil.toDateString(new Date()) + ")");
    }

    @Override
    public Response<String> generateGroupCode(Long barnId) {
        try {
            List<DoctorGroup> groups = RespHelper.orServEx(doctorGroupReadService.findGroupByCurrentBarnId(barnId));
            if (notEmpty(groups)) {
                return Response.ok(groups.get(0).getGroupCode());
            }
            return generateGroupCode(getBarnName(barnId));
        } catch (Exception e) {
            log.error("generate group code failed, barnId:{}, cause:{}", barnId, Throwables.getStackTraceAsString(e));
            return Response.ok();
        }
    }

    //校验猪群是否存在
    private DoctorGroupDetail checkGroupExist(Long groupId) {
        DoctorGroupDetail groupDetail = expectNotNull(RespHelper.or500(doctorGroupReadService.findGroupDetailByGroupId(groupId)), "group.detail.not.found");
        if (!Objects.equals(groupDetail.getGroup().getStatus(), DoctorGroup.Status.CREATED.getValue())) {
            throw new InvalidException("group.is.closed", groupDetail.getGroup().getGroupCode());
        }
        return groupDetail;
    }

    //校验事件类型是否合法
    private void checkEventTypeIllegal(DoctorGroupDetail groupDetail, Integer eventType) {
        List<Integer> eventTypes = RespHelper.or500(doctorGroupReadService.findEventTypesByGroupIds(Lists.newArrayList(groupDetail.getGroup().getId())));
        if (!eventTypes.contains(eventType)) {
            throw new InvalidException("event.type.illegal", checkNotNull(GroupEventType.from(eventType)), groupDetail.getGroup().getGroupCode());
        }
        //若当前没有猪只,不能变动, 转群, 转种猪, 存栏,疾病, 防疫, 转场等等
        if (groupDetail.getGroupTrack().getQuantity() <= 0 && GroupEventType.EMPTY_GROUPS.contains(eventType)) {
            throw new InvalidException("empty.group.can.not.event", groupDetail.getGroup().getGroupCode());
        }
    }

    //获取猪场名称
    private String getFarmName(Long farmId) {
        return expectNotNull(RespHelper.or(doctorFarmReadService.findFarmById(farmId), new DoctorFarm()).getName(), "farm.not.null", farmId);
    }

    //获取猪群号
    private String getGroupCode(Long groupId) {
        DoctorGroup group = expectNotNull(orServEx(doctorGroupReadService.findGroupById(groupId)), "event.group.not.found", groupId);
        return group.getGroupCode();
    }

    //获取猪舍名称
    private String getBarnName(Long barnId) {
        return barnId == null ? null : expectNotNull(RespHelper.or(doctorBarnReadService.findBarnById(barnId), new DoctorBarn()), "barn.not.null", barnId).getName();
    }

    //获取基础数据名称
    private String getBasicName(Long basicId) {
        return basicId == null ? null : expectNotNull(RespHelper.or(doctorBasicReadService.findBasicById(basicId), new DoctorBasic()), "basic.not.null", basicId).getName();
    }

    //获取疫苗名称
    private String getVaccinName(Long vaccinId) {
        return vaccinId == null ? null : expectNotNull(RespHelper.or(doctorWarehouseSkuReadService.findById(vaccinId), new DoctorWarehouseSku()), "basic.material.not.null", vaccinId).getName();
    }

    //获取变动原因
    private String getChangeReasonName(Long changeReasonId) {
        return changeReasonId == null ? null : expectNotNull(RespHelper.or(doctorBasicReadService.findChangeReasonById(changeReasonId), new DoctorChangeReason()), "change.reason.not.null", changeReasonId).getReason();
    }

    @Override
    public Response<String> findRealName(Long userId) {
        if (userId != null) {
            UserProfile profile = RespHelper.orServEx(doctorUserProfileReadService.findProfileByUserId(userId));
            if (profile != null) {
                return Response.ok(profile.getRealName());
            }
        }
        return Response.ok();
    }

    //获取防疫项目名称
    private String getVaccinItemName(Long vaccinItemId) {
        return vaccinItemId == null ? null : expectNotNull(RespHelper.or(doctorBasicReadService.findBasicById(vaccinItemId), new DoctorBasic()), "basic.not.null", vaccinItemId).getName();
    }

    //获取客户名称
    private String getCustomerName(Long customerId) {
        return customerId == null ? null : expectNotNull(RespHelper.or(doctorBasicReadService.findCustomerById(customerId), new DoctorCustomer()), "basic.not.null", customerId).getName();
    }

    //put一些关联字段
    private Map<String, Object> putBasicFields(Map<String, Object> params) {
        params = Params.filterNullOrEmpty(params);

        //手工录入, 记录下创建人
        params.put("isAuto", IsOrNot.NO.getValue());
        params.put("creatorId", UserUtil.getUserId());
        UserProfile profile = RespHelper.orServEx(doctorUserProfileReadService.findProfileByUserId(UserUtil.getUserId()));
        String realName = UserUtil.getCurrentUser().getName();
        if (notNull(profile) && !com.google.common.base.Strings.isNullOrEmpty(profile.getRealName())) {
            realName = profile.getRealName();
        }
        params.put("creatorName", realName);

        //id关联字段
        params.put("barnName", getBarnName(getLong(params, "barnId")));
        params.put("breedName", getBasicName(getLong(params, "breedId")));
        params.put("geneticName", getBasicName(getLong(params, "geneticId")));
        return Params.filterNullOrEmpty(params);
    }

    //put一下更新人信息
    private Map<String, Object> pubUpdatorFields(Map<String, Object> params) {
        params = Params.filterNullOrEmpty(params);
        params.put("updatorId", UserUtil.getUserId());
        params.put("updatorName", UserUtil.getCurrentUser().getName());
        return Params.filterNullOrEmpty(params);
    }

    private Long getLong(Map<String, Object> params, String key) {
        Object o = params.get(key);
        return o == null || Strings.isNullOrEmpty(o.toString()) ? null : Long.valueOf(String.valueOf(o));
    }

    private Integer getInteger(Map<String, Object> params, String key) {
        Object o = params.get(key);
        return o == null ? null : Integer.valueOf(String.valueOf(o));
    }

    //校验是否是系统自动生成的事件, 自动事件不可编辑!
    private static void checkEventAuto(DoctorGroupEvent event) {
        if (Objects.equals(event.getIsAuto(), IsOrNot.YES.getValue())) {
            throw new InvalidException("group.event.is.auto", event.getId());
        }
    }

    //检验参数是否为空
    private void checkParam(Map<String, Object> params, String groupCode) {

        expectNotNull(params.get("changeTypeId"), "change.type.id.is.null", groupCode);
        expectNotNull(params.get("sowQty"), "sow.qty.is.null", groupCode);
        expectNotNull(params.get("boarQty"), "boar.qty.is.null", groupCode);
        expectNotNull(params.get("weight"), "weight.is.null", groupCode);
        Boolean isSaleType = Objects.equals(getLong(params, "changeTypeId"), CHANGE_TYPE_SALE);
        Boolean canGetCustomer = getLong(params, "customerId") != null || params.get("customerName") != null;
        if (isSaleType && (!canGetCustomer || getLong(params, "price") == null)) {
            throw new InvalidException("price.or.customer.info.is.null.when.sale", groupCode);
        }
    }

    /**
     * 猪群事件时间限制
     *
     * @param eventAt
     * @return
     */
    private void checkEventAt(Long groupId, Date eventAt) {
        DoctorGroupEvent newEvent = RespHelper.orServEx(doctorGroupReadService.findNewGroupEvent(groupId));
        if (notNull(newEvent) && Dates.startOfDay(eventAt).before(Dates.startOfDay(newEvent.getEventAt()))) {
            throw new InvalidException("move.in.event.at.before.new.event.at", DateUtil.toDateString(eventAt), DateUtil.toDateString(newEvent.getEventAt()));
        }
    }
}
