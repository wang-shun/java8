package io.terminus.doctor.web.front.event.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.DoctorBreed;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorChangeType;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.basic.model.DoctorDisease;
import io.terminus.doctor.basic.model.DoctorGenetic;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
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
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorStaffReadService;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import io.terminus.doctor.warehouse.service.DoctorMaterialInfoReadService;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.terminus.common.utils.Arguments.isEmpty;

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
    private final DoctorMaterialInfoReadService doctorMaterialInfoReadService;
    private final DoctorStaffReadService doctorStaffReadService;
    private final UserReadService<User> userReadService;

    @Autowired
    public DoctorGroupWebServiceImpl(DoctorGroupWriteService doctorGroupWriteService,
                                     DoctorFarmReadService doctorFarmReadService,
                                     DoctorBasicReadService doctorBasicReadService,
                                     DoctorBarnReadService doctorBarnReadService,
                                     DoctorGroupReadService doctorGroupReadService,
                                     DoctorOrgReadService doctorOrgReadService,
                                     DoctorMaterialInfoReadService doctorMaterialInfoReadService,
                                     DoctorStaffReadService doctorStaffReadService,
                                     UserReadService<User> userReadService) {
        this.doctorGroupWriteService = doctorGroupWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorBasicReadService = doctorBasicReadService;
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorOrgReadService = doctorOrgReadService;
        this.doctorMaterialInfoReadService = doctorMaterialInfoReadService;
        this.doctorStaffReadService = doctorStaffReadService;
        this.userReadService = userReadService;
    }

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

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
        newGroupInput.setBreedName(getBreedName(newGroupInput.getBreedId()));
        newGroupInput.setGeneticName(getGeneticName(newGroupInput.getGeneticId()));

        //事件录入人信息
        newGroupInput.setCreatorId(UserUtil.getUserId());
        newGroupInput.setCreatorName(UserUtil.getCurrentUser().getName());

        DoctorGroup group = BeanMapper.map(newGroupInput, DoctorGroup.class);
        group.setRemark(null);  //dozer不需要转换remark

        //设置猪场公司信息
        DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(group.getFarmId()));
        group.setFarmName(farm.getName());

        DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgById(farm.getOrgId()));
        group.setOrgId(org.getId());
        group.setOrgName(org.getName());
        return group;
    }

    @Override
    public Response<Boolean> createGroupEvent(Long groupId, Integer eventType, String data) {
        try {
            //1.校验猪群是否存在
            DoctorGroupDetail groupDetail = checkGroupExist(groupId);

            //2.校验能否操作此事件 // TODO: 16/6/6 当前没有猪只,不能防疫,疾病,存栏,转群,等等等
            checkEventTypeIllegal(groupId, eventType);

            Map<String, Object> params = JSON_MAPPER.fromJson(data, JSON_MAPPER.createCollectionType(Map.class, String.class, Object.class));

            //3.根据不同的事件类型调用不同的录入接口
            GroupEventType groupEventType = checkNotNull(GroupEventType.from(eventType));
            switch (groupEventType) {
                case MOVE_IN:
                    params.put("inTypeName", DoctorMoveInGroupEvent.InType.from(getInteger(params, "inType")).getDesc());
                    RespHelper.orServEx(doctorGroupWriteService.groupEventMoveIn(groupDetail, BeanMapper.map(putBasicFields(params), DoctorMoveInGroupInput.class)));
                    break;
                case CHANGE:
                    params.put("changeTypeName", getChangeTypeName(getLong(params, "changeTypeId")));
                    params.put("changeReasonName", getChangeReasonName(getLong(params, "changeReasonId")));
                    params.put("customerName", getCustomerName(getLong(params, "customerId")));
                    RespHelper.orServEx(doctorGroupWriteService.groupEventChange(groupDetail, BeanMapper.map(putBasicFields(params), DoctorChangeGroupInput.class)));
                    break;
                case TRANS_GROUP:
                    params.put("toBarnName", getBarnName(getLong(params, "toBarnId")));

                    //如果不新建猪群, 拼上转入猪群号
                    if (Integer.valueOf(String.valueOf(params.get("isCreateGroup"))).equals(IsOrNot.NO.getValue())) {
                        params.put("toGroupCode", getGroupCode(getLong(params, "toGroupId")));
                    }
                    RespHelper.orServEx(doctorGroupWriteService.groupEventTransGroup(groupDetail, BeanMapper.map(putBasicFields(params), DoctorTransGroupInput.class)));
                    break;
                case TURN_SEED:
                    RespHelper.orServEx(doctorGroupWriteService.groupEventTurnSeed(groupDetail, BeanMapper.map(putBasicFields(params), DoctorTurnSeedGroupInput.class)));
                    break;
                case LIVE_STOCK:
                    RespHelper.orServEx(doctorGroupWriteService.groupEventLiveStock(groupDetail, BeanMapper.map(putBasicFields(params), DoctorLiveStockGroupInput.class)));
                    break;
                case DISEASE:
                    params.put("diseaseName", getDiseaseName(getLong(params, "diseaseId")));
                    params.put("doctorName", getStaffUserName(getLong(params, "doctorId")));
                    RespHelper.orServEx(doctorGroupWriteService.groupEventDisease(groupDetail, BeanMapper.map(putBasicFields(params), DoctorDiseaseGroupInput.class)));
                    break;
                case ANTIEPIDEMIC:
                    params.put("vaccinName", getVaccinName(getLong(params, "vaccinId")));
                    params.put("vaccinStaffName", getStaffUserName(getLong(params, "vaccinStaffId")));
                    RespHelper.orServEx(doctorGroupWriteService.groupEventAntiepidemic(groupDetail, BeanMapper.map(putBasicFields(params), DoctorAntiepidemicGroupInput.class)));
                    break;
                case TRANS_FARM:
                    params.put("toFarmName", getFarmName(getLong(params, "toFarmId")));
                    params.put("toBarnName", getBarnName(getLong(params, "toBarnId")));

                    //如果不新建猪群, 拼上转入猪群号
                    if (Integer.valueOf(String.valueOf(params.get("isCreateGroup"))).equals(IsOrNot.NO.getValue())) {
                        params.put("toGroupCode", getGroupCode(getLong(params, "toGroupId")));
                    }
                    RespHelper.orServEx(doctorGroupWriteService.groupEventTransFarm(groupDetail, BeanMapper.map(putBasicFields(params), DoctorTransFarmGroupInput.class)));
                    break;
                case CLOSE:
                    RespHelper.orServEx(doctorGroupWriteService.groupEventClose(groupDetail, BeanMapper.map(putBasicFields(params), DoctorCloseGroupInput.class)));
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
    public Response<String> generateGroupCode(String barnName) {
        if (isEmpty(barnName)) {
            return Response.ok();
        }
        return Response.ok(barnName + "(" +DateUtil.toDateString(new Date()) + ")");
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
    private void checkEventTypeIllegal(Long groupId, Integer eventType) {
        List<Integer> eventTypes = RespHelper.or500(doctorGroupReadService.findEventTypesByGroupIds(Lists.newArrayList(groupId)));
        if (!eventTypes.contains(eventType)) {
            throw new ServiceException("event.type.illegal");
        }
    }

    //获取猪场名称
    private String getFarmName(Long farmId) {
        return farmId == null ? null : RespHelper.or(doctorFarmReadService.findFarmById(farmId), new DoctorFarm()).getName();
    }

    //获取猪群号
    private String getGroupCode(Long groupId) {
        return RespHelper.orServEx(doctorGroupReadService.findGroupById(groupId)).getGroupCode();
    }

    //获取猪舍名称
    private String getBarnName(Long barnId) {
        return barnId == null ? null : RespHelper.or(doctorBarnReadService.findBarnById(barnId), new DoctorBarn()).getName();
    }

    //获取品种名称
    private String getBreedName(Long breedId) {
        return breedId == null ? null : RespHelper.or(doctorBasicReadService.findBreedById(breedId), new DoctorBreed()).getName();
    }

    //获取品系名称
    private String getGeneticName(Long geneticId) {
        return geneticId == null ? null : RespHelper.or(doctorBasicReadService.findGeneticById(geneticId), new DoctorGenetic()).getName();
    }

    //获取疫苗名称
    private String getVaccinName(Long vaccinId) {
        return vaccinId == null ? null : RespHelper.or(doctorMaterialInfoReadService.queryById(vaccinId), new DoctorMaterialInfo()).getMaterialName();
    }

    //获取疾病名称
    private String getDiseaseName(Long diseaseId) {
        return diseaseId == null ? null : RespHelper.or(doctorBasicReadService.findDiseaseById(diseaseId), new DoctorDisease()).getName();
    }

    //获取变动类型名称
    private String getChangeTypeName(Long changeTypeId) {
        return changeTypeId == null ? null : RespHelper.or(doctorBasicReadService.findChangeTypeById(changeTypeId), new DoctorChangeType()).getName();
    }

    //获取变动原因
    private String getChangeReasonName(Long changeReasonId) {
        return changeReasonId == null ? null : RespHelper.or(doctorBasicReadService.findChangeReasonById(changeReasonId), new DoctorChangeReason()).getReason();
    }

    //获取职工用户的名称
    private String getStaffUserName(Long staffId) {
        if (staffId == null) {
            return null;
        }
        DoctorStaff staff = RespHelper.or500(doctorStaffReadService.findStaffById(staffId));
        User user = RespHelper.or500(userReadService.findById(staff.getUserId()));
        return user.getName();
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
        params.put("creatorId", 1L);
        params.put("creatorName", "admin");
//        params.put("creatorId", UserUtil.getUserId());
//        params.put("creatorName", UserUtil.getCurrentUser().getName());
        //id关联字段
        params.put("barnName", getBarnName(getLong(params, "barnId")));
        params.put("breedName", getBreedName(getLong(params, "breedId")));
        params.put("geneticName", getGeneticName(getLong(params, "geneticId")));
        return Params.filterNullOrEmpty(params);
    }

    private Long getLong(Map<String, Object> params, String key) {
        Object o = params.get(key);
        return o == null ? null : Long.valueOf(String.valueOf(o));
    }

    private Integer getInteger(Map<String, Object> params, String key) {
        Object o = params.get(key);
        return o == null ? null : Integer.valueOf(String.valueOf(o));
    }
}
