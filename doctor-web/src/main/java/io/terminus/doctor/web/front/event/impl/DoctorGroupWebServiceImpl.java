package io.terminus.doctor.web.front.event.impl;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.edit.DoctorAntiepidemicGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorChangeGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorDiseaseGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorLiveStockGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorMoveInGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorNewGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorTransEdit;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.user.service.PrimaryUserReadService;
import io.terminus.doctor.warehouse.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.terminus.common.utils.Arguments.isEmpty;
import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.common.utils.BeanMapper.map;
import static io.terminus.doctor.common.utils.RespHelper.orServEx;

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

    @RpcConsumer
    private DoctorBasicMaterialReadService doctorBasicMaterialReadService;
    @RpcConsumer
    private DoctorUserProfileReadService doctorUserProfileReadService;
    @RpcConsumer
    private PrimaryUserReadService primaryUserReadService;
    @RpcConsumer
    private DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;

    @Autowired
    public DoctorGroupWebServiceImpl(DoctorGroupWriteService doctorGroupWriteService,
                                     DoctorFarmReadService doctorFarmReadService,
                                     DoctorBasicReadService doctorBasicReadService,
                                     DoctorBarnReadService doctorBarnReadService,
                                     DoctorGroupReadService doctorGroupReadService,
                                     DoctorOrgReadService doctorOrgReadService,
                                     DoctorBasicWriteService doctorBasicWriteService) {
        this.doctorGroupWriteService = doctorGroupWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorBasicReadService = doctorBasicReadService;
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorOrgReadService = doctorOrgReadService;
        this.doctorBasicWriteService = doctorBasicWriteService;
    }

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private static final Long CHANGE_TYPE_SALE = 109L;

    @Override
    public Response<Long> createNewGroup(DoctorNewGroupInput newGroupInput) {
        try {
            //1.构造猪群信息
            return doctorGroupWriteService.createNewGroup(getNewGroup(newGroupInput), newGroupInput);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("create new group failed, newGroupInput:{}, cause:{}", newGroupInput, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.create.fail");
        }
    }

    //构造新建猪群信息
    private DoctorGroup getNewGroup(DoctorNewGroupInput newGroupInput) {
        newGroupInput.setIsAuto(IsOrNot.NO.getValue());

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
        group.setFarmName(farm.getName());

        DoctorOrg org = orServEx(doctorOrgReadService.findOrgById(farm.getOrgId()));
        group.setOrgId(org.getId());
        group.setOrgName(org.getName());

        //根据猪舍id设置猪类
        DoctorBarn doctorBarn = orServEx(doctorBarnReadService.findBarnById(newGroupInput.getBarnId()));
        group.setPigType(doctorBarn.getPigType());
        return group;
    }

    @Override
    public Response<Boolean> createGroupEvent(Long groupId, Integer eventType, String data) {
        try {
            log.info("create group event, groupId:{}, eventType:{}, data:{}", groupId, eventType, data);

            //1.校验猪群是否存在
            DoctorGroupDetail groupDetail = checkGroupExist(groupId);

            //2.校验能否操作此事件
            checkEventTypeIllegal(groupDetail, eventType);

            Map<String, Object> params = JSON_MAPPER.fromJson(data, JSON_MAPPER.createCollectionType(Map.class, String.class, Object.class));

            //3.校验事件的时间
            Date eventAt = DateUtil.toDate((String) params.get("eventAt"));
            checkEventAt(groupId, eventType, eventAt);

            //4.根据不同的事件类型调用不同的录入接口
            GroupEventType groupEventType = checkNotNull(GroupEventType.from(eventType));
            switch (groupEventType) {
                case MOVE_IN:
                    params.put("breedName", getBasicName(getLong(params, "breedId")));
                    params.put("inTypeName", DoctorMoveInGroupEvent.InType.from(getInteger(params, "inType")).getDesc());
                    params.put("toBarnName", getBarnName(getLong(params, "toBarnId")));
                    orServEx(doctorGroupWriteService.groupEventMoveIn(groupDetail, map(putBasicFields(params), DoctorMoveInGroupInput.class)));
                    break;
                case CHANGE:
                    checkParam(params);
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
                    orServEx(doctorGroupWriteService.groupEventChange(groupDetail, changeInput));
                    break;
                case TRANS_GROUP:
                    params.put("toBarnName", getBarnName(getLong(params, "toBarnId")));
                    params.put("breedName", getBasicName(getLong(params, "breedId")));

                    //如果不新建猪群, 拼上转入猪群号
                    if (Integer.valueOf(String.valueOf(params.get("isCreateGroup"))).equals(IsOrNot.NO.getValue())) {
                        params.put("toGroupCode", getGroupCode(getLong(params, "toGroupId")));
                    }
                    params.put("fcrFeed", RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, groupId, null, null), 0D));
                    orServEx(doctorGroupWriteService.groupEventTransGroup(groupDetail, map(putBasicFields(params), DoctorTransGroupInput.class)));
                    break;
                case TURN_SEED:
                    params.put("breedName", getBasicName(getLong(params, "breedId")));
                    params.put("geneticName", getBasicName(getLong(params, "geneticId")));
                    params.put("toBarnName", getBarnName(getLong(params, "toBarnId")));
                    params.put("fcrFeed", RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, groupId, null, null), 0D));
                    orServEx(doctorGroupWriteService.groupEventTurnSeed(groupDetail, map(putBasicFields(params), DoctorTurnSeedGroupInput.class)));
                    break;
                case LIVE_STOCK:
                    orServEx(doctorGroupWriteService.groupEventLiveStock(groupDetail, map(putBasicFields(params), DoctorLiveStockGroupInput.class)));
                    break;
                case DISEASE:
                    params.put("diseaseName", getBasicName(getLong(params, "diseaseId")));
                    params.put("doctorName", orServEx(this.findRealName(getLong(params, "doctorId"))));
                    orServEx(doctorGroupWriteService.groupEventDisease(groupDetail, map(putBasicFields(params), DoctorDiseaseGroupInput.class)));
                    break;
                case ANTIEPIDEMIC:
                    params.put("vaccinName", getVaccinName(getLong(params, "vaccinId")));
                    params.put("vaccinStaffName", orServEx(this.findRealName(getLong(params, "vaccinStaffId"))));
                    params.put("vaccinItemName", getVaccinItemName(getLong(params, "vaccinItemId")));
                    orServEx(doctorGroupWriteService.groupEventAntiepidemic(groupDetail, map(putBasicFields(params), DoctorAntiepidemicGroupInput.class)));
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
                    orServEx(doctorGroupWriteService.groupEventTransFarm(groupDetail, map(putBasicFields(params), DoctorTransFarmGroupInput.class)));
                    break;
                case CLOSE:
                    orServEx(doctorGroupWriteService.groupEventClose(groupDetail, map(putBasicFields(params), DoctorCloseGroupInput.class)));
                    break;
                default:
                    return Response.fail("event.type.error");
            }
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("create group event failed, groupId:{}, eventType:{}, params:{}, cause:{}",
                    groupId, eventType, data, Throwables.getStackTraceAsString(e));
            return Response.fail("create.group.event.fail");
        }
    }

    @Override
    public Response<Boolean> editGroupEvent(Long eventId, String data) {
        try {
            //1.校验猪群是否存在
            DoctorGroupEvent event = RespHelper.or500(doctorGroupReadService.findGroupEventById(eventId));
            DoctorGroupDetail groupDetail = checkGroupExist(event.getGroupId());

            //2.校验系统自动事件, 自动事件不可编辑
            checkEventAuto(event);

            //3.根据不同的事件类型调用不同的接口
            Map<String, Object> params = JSON_MAPPER.fromJson(data, JSON_MAPPER.createCollectionType(Map.class, String.class, Object.class));
            if (params == null || params.isEmpty()) {
                log.info("edit group event data is empty, eventId:{}, data:{}", eventId, data);
                return Response.ok(Boolean.TRUE);
            }

            switch (checkNotNull(GroupEventType.from(event.getType()))) {
                case NEW:
                    params.put("breedName", getBasicName(getLong(params, "breedId")));
                    params.put("geneticName", getBasicName(getLong(params, "geneticId")));
                    orServEx(doctorGroupWriteService.editEventNew(groupDetail, event, map(pubUpdatorFields(params), DoctorNewGroupEdit.class)));
                    break;
                case MOVE_IN:
                    params.put("breedName", getBasicName(getLong(params, "breedId")));
                    orServEx(doctorGroupWriteService.editEventMoveIn(groupDetail, event, map(pubUpdatorFields(params), DoctorMoveInGroupEdit.class)));
                    break;
                case CHANGE:
                    checkParam(params);
                    params.put("changeReasonName", getChangeReasonName(getLong(params, "changeReasonId")));
                    params.put("customerName", getCustomerName(getLong(params, "customerId")));
                    params.put("breedName", getBasicName(getLong(params, "breedId")));
                    orServEx(doctorGroupWriteService.editEventChange(groupDetail, event, map(pubUpdatorFields(params), DoctorChangeGroupEdit.class)));
                    break;
                case TRANS_GROUP:
                    params.put("breedName", getBasicName(getLong(params, "breedId")));
                    orServEx(doctorGroupWriteService.editEventTrans(groupDetail, event, map(pubUpdatorFields(params), DoctorTransEdit.class)));
                    break;
                case LIVE_STOCK:
                    orServEx(doctorGroupWriteService.editEventLiveStock(groupDetail, event, map(pubUpdatorFields(params), DoctorLiveStockGroupEdit.class)));
                    break;
                case DISEASE:
                    params.put("diseaseName", getBasicName(getLong(params, "diseaseId")));
                    params.put("doctorName", orServEx(this.findRealName(getLong(params, "doctorId"))));
                    orServEx(doctorGroupWriteService.editEventDisease(groupDetail, event, map(pubUpdatorFields(params), DoctorDiseaseGroupEdit.class)));
                    break;
                case ANTIEPIDEMIC:
                    params.put("vaccinStaffName", orServEx(this.findRealName(getLong(params, "vaccinStaffId"))));
                    params.put("vaccinItemName", getVaccinItemName(getLong(params, "vaccinItemId")));
                    orServEx(doctorGroupWriteService.editEventAntiepidemic(groupDetail, event, map(pubUpdatorFields(params), DoctorAntiepidemicGroupEdit.class)));
                    break;
                case TRANS_FARM:
                    params.put("breedName", getBasicName(getLong(params, "breedId")));
                    orServEx(doctorGroupWriteService.editEventTrans(groupDetail, event, map(pubUpdatorFields(params), DoctorTransEdit.class)));
                    break;
                default:
                    return Response.fail("event.type.error");
            }
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("edit group event failed, eventId:{}, data:{}, cause:{}", eventId, data, Throwables.getStackTraceAsString(e));
            return Response.fail("edit.group.event.fail");
        }
    }

    @Override
    public Response<String> generateGroupCode(String barnName) {
        if (isEmpty(barnName)) {
            return Response.ok();
        }
        return Response.ok(barnName + "(" +DateUtil.toDateString(new Date()) + ")");
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
        DoctorGroupDetail groupDetail = checkNotNull(RespHelper.or500(doctorGroupReadService.findGroupDetailByGroupId(groupId)), "group.not.exist");
        if (!Objects.equals(groupDetail.getGroup().getStatus(), DoctorGroup.Status.CREATED.getValue())) {
            throw new ServiceException("group.is.closed");
        }
        return groupDetail;
    }

    //校验事件类型是否合法
    private void checkEventTypeIllegal(DoctorGroupDetail groupDetail, Integer eventType) {
        List<Integer> eventTypes = RespHelper.or500(doctorGroupReadService.findEventTypesByGroupIds(Lists.newArrayList(groupDetail.getGroup().getId())));
        if (!eventTypes.contains(eventType)) {
            throw new ServiceException("event.type.illegal");
        }
        //若当前没有猪只,不能变动, 转群, 转种猪, 存栏,疾病, 防疫, 转场等等
        if (groupDetail.getGroupTrack().getQuantity() <= 0 && GroupEventType.EMPTY_GROUPS.contains(eventType)) {
            throw new ServiceException("empty.group.can.not.event");
        }
    }

    //获取猪场名称
    private String getFarmName(Long farmId) {
        return farmId == null ? null : RespHelper.or(doctorFarmReadService.findFarmById(farmId), new DoctorFarm()).getName();
    }

    //获取猪群号
    private String getGroupCode(Long groupId) {
        return orServEx(doctorGroupReadService.findGroupById(groupId)).getGroupCode();
    }

    //获取猪舍名称
    private String getBarnName(Long barnId) {
        return barnId == null ? null : RespHelper.or(doctorBarnReadService.findBarnById(barnId), new DoctorBarn()).getName();
    }

    //获取基础数据名称
    private String getBasicName(Long basicId) {
        return basicId == null ? null : RespHelper.or(doctorBasicReadService.findBasicById(basicId), new DoctorBasic()).getName();
    }

    //获取疫苗名称
    private String getVaccinName(Long vaccinId) {
        return vaccinId == null ? null : RespHelper.or(doctorBasicMaterialReadService.findBasicMaterialById(vaccinId), new DoctorBasicMaterial()).getName();
    }

    //获取变动原因
    private String getChangeReasonName(Long changeReasonId) {
        return changeReasonId == null ? null : RespHelper.or(doctorBasicReadService.findChangeReasonById(changeReasonId), new DoctorChangeReason()).getReason();
    }

    @Override
    public Response<String> findRealName(Long userId) {
        if (userId != null) {
            UserProfile profile = RespHelper.orServEx(doctorUserProfileReadService.findProfileByUserId(userId));
            if(profile != null){
                return Response.ok(profile.getRealName());
            }
        }
        return Response.ok();
    }

    //获取防疫项目名称
    private String getVaccinItemName(Long vaccinItemId) {
        return vaccinItemId == null ? null : RespHelper.or(doctorBasicReadService.findBasicById(vaccinItemId), new DoctorBasic()).getName();
    }

    //获取客户名称
    private String getCustomerName(Long customerId) {
        return customerId == null ? null : RespHelper.or(doctorBasicReadService.findCustomerById(customerId), new DoctorCustomer()).getName();
    }

    //put一些关联字段
    private Map<String, Object> putBasicFields(Map<String, Object> params) {
        params = Params.filterNullOrEmpty(params);

        //手工录入, 记录下创建人
        params.put("isAuto", IsOrNot.NO.getValue());
        params.put("creatorId", UserUtil.getUserId());
        params.put("creatorName", UserUtil.getCurrentUser().getName());
        
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
            throw new ServiceException("group.event.is.auto");
        }
    }

    //检验参数是否为空
    private void checkParam(Map<String, Object> params) {
        if (params.get("changeTypeId") == null || params.get("sowQty") == null
                || params.get("boarQty") == null || params.get("weight") == null) {
            throw new ServiceException("some.param.is.null");
        }
        Boolean isSaleType = Objects.equals(getLong(params, "changeTypeId"), CHANGE_TYPE_SALE);
        Boolean canGetCustomer = getLong(params, "customerId") != null || params.get("customerName") != null;
        if (isSaleType && (!canGetCustomer || getLong(params, "price") == null)) {
            throw new ServiceException("price.or.customer.info.is.null.when.sale");
        }
    }

    /**
     * 猪群事件时间限制
     * @param groupId
     * @param eventType
     * @param eventAt
     * @return
     */
    private void checkEventAt(Long groupId, Integer eventType, Date eventAt){
        if (Objects.equals(eventType, GroupEventType.NEW.getValue())){
            return;
        }
        DoctorGroupEvent lastEvent = RespHelper.or500(doctorGroupReadService.findLastEventByGroupId(groupId));
        if (lastEvent != null  && new DateTime(eventAt).plusDays(1).isAfter(lastEvent.getEventAt().getTime())){
            return;
        }else {
            throw new ServiceException("event.at.illegal");
        }
    }
}
