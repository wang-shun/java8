package io.terminus.doctor.web.init;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.Joiners;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RandomUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.DoctorPigInfoDetailDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorBarnWriteService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticWriteService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorFarmWriteService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorOrgWriteService;
import io.terminus.doctor.user.service.DoctorServiceReviewReadService;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.DoctorServiceStatusReadService;
import io.terminus.doctor.user.service.DoctorServiceStatusWriteService;
import io.terminus.doctor.user.service.DoctorStaffReadService;
import io.terminus.doctor.user.service.DoctorStaffWriteService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionWriteService;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.model.FlowHistoryProcess;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.model.FlowProcessTrack;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isEmpty;
import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.doctor.common.utils.RespHelper.or500;

/**
 * Desc: 初始化猪场 (仅供内测用!)
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/21
 */
@Slf4j
@RestController
@RequestMapping("/api/test/init")
public class InitFarms {

    private static final Long INIT_ID = 0L;
    private static final DateTimeFormatter TIME = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    @Autowired
    private DoctorOrgReadService doctorOrgReadService;
    @Autowired
    private DoctorOrgWriteService doctorOrgWriteService;
    @Autowired
    private UserReadService<User> userReadService;
    @Autowired
    private DoctorFarmReadService doctorFarmReadService;
    @Autowired
    private DoctorFarmWriteService doctorFarmWriteService;
    @Autowired
    private DoctorBasicReadService doctorBasicReadService;
    @Autowired
    private DoctorBasicWriteService doctorBasicWriteService;
    @Autowired
    private DoctorBarnReadService doctorBarnReadService;
    @Autowired
    private DoctorBarnWriteService doctorBarnWriteService;
    @Autowired
    private DoctorStaffReadService doctorStaffReadService;
    @Autowired
    private DoctorStaffWriteService doctorStaffWriteService;
    @Autowired
    private DoctorGroupReadService doctorGroupReadService;
    @Autowired
    private DoctorGroupWriteService doctorGroupWriteService;
    @Autowired
    private DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;
    @Autowired
    private DoctorPigReadService doctorPigReadService;
    @Autowired
    private DoctorPigWriteService doctorPigWriteService;
    @Autowired
    private JdbcAccess jdbcAccess;
    @Autowired
    private DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService;
    @Autowired
    private DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    @Autowired
    private DoctorServiceStatusReadService doctorServiceStatusReadService;
    @Autowired
    private DoctorServiceStatusWriteService doctorServiceStatusWriteService;
    @Autowired
    private DoctorServiceReviewReadService doctorServiceReviewReadService;
    @Autowired
    private DoctorServiceReviewWriteService doctorServiceReviewWriteService;

    /**
     * 根据用户id初始化出所有的猪场相关数据(内测用)
     * @param userId  用户id
     * @return  是否成功
     */
    @RequestMapping(value = "/farm", method = RequestMethod.GET)
    public Boolean initAllDataByUserId(@RequestParam("userId") Long userId,
                                       @RequestParam(value = "orgName", required = false) String orgName,
                                       @RequestParam(value = "farmName", required = false) String farmName) {
        User user = or500(userReadService.findById(userId));
        //判断下,如果staff已经创建了,就不在初始化其他数据
        DoctorStaff staff = or500(doctorStaffReadService.findStaffByUserId(userId));
        if (staff != null) {
            throw new JsonResponseException("staff不为空, 数据已经初始化!");
        }
        init(user, orgName, farmName);
        return Boolean.TRUE;
    }

    /**
     * 仅初始化母猪数据(内测用)
     * @param farmId  猪场id
     * @return 是否成功
     */
    @RequestMapping(value = "/sow", method = RequestMethod.GET)
    public Boolean initSows(@RequestParam("farmId") Long farmId) {
        DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
        initPigs(farm);
        return Boolean.TRUE;
    }

    /**
     * 根据猪场id清理猪场所有数据(内测用)
     * @param farmId  猪场id
     * @return 是否成功
     */
    public Boolean cleanFarm(@RequestParam("farmId") Long farmId) {
        return Boolean.TRUE;
    }

    private void init(User user, String orgName, String farmName) {
        //0. 服务审核
        initServiceReview(user);

        //1. 判断是否创建公司
        DoctorOrg org = initOrg(user, orgName);

        //2. 创建猪场
        DoctorFarm farm = initFarm(org, farmName);
        initDataPermission(farm, user);

        //3. 创建staff
        DoctorStaff staff = initStaff(farm, user);

        //4. 初始化客户与变动原因
        initCustomer(farm, user);
        initChangeReason(farm);

        //5. 创建猪舍
        initBarns(farm, staff);

        //6. 创建猪相关信息
        initPigs(farm);

        //7. 创建猪群相关信息
        initGroups(farm, staff);

        //8. 创建猪统计信息
        or500(doctorPigTypeStatisticWriteService.statisticGroup(farm.getOrgId(), farm.getId()));
        or500(doctorPigTypeStatisticWriteService.statisticPig(farm.getOrgId(), farm.getId(), DoctorPig.PIG_TYPE.BOAR.getKey()));
        or500(doctorPigTypeStatisticWriteService.statisticPig(farm.getOrgId(), farm.getId(), DoctorPig.PIG_TYPE.SOW.getKey()));

        //9. 创建物料相关信息
        // TODO: 16/6/21
    }

    private String getName(String name) {
        return name + DateTime.now().toString(TIME);
    }

    private DoctorOrg initOrg(User user, String orgName) {
        DoctorOrg org = or500(doctorOrgReadService.findOrgById(INIT_ID));
        org.setName(isEmpty(orgName) ? getName(org.getName()) : orgName);
        org.setMobile(user.getMobile());
        Long orgId = or500(doctorOrgWriteService.createOrg(org));
        org.setId(orgId);
        return org;
    }

    private DoctorFarm initFarm(DoctorOrg org, String farmName) {
        DoctorFarm farm = or500(doctorFarmReadService.findFarmById(INIT_ID));
        farm.setOrgId(org.getId());
        farm.setOrgName(org.getName());
        farm.setName(isEmpty(farmName) ? getName(farm.getName()) : farmName);
        Long farmId = or500(doctorFarmWriteService.createFarm(farm));
        farm.setId(farmId);
        return farm;
    }

    private void initDataPermission(DoctorFarm farm, User user) {
        if (or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(user.getId())) == null) {
            DoctorUserDataPermission permission = new DoctorUserDataPermission();
            permission.setFarmIds(farm.getId().toString());
            permission.setUserId(user.getId());
            or500(doctorUserDataPermissionWriteService.createDataPermission(permission));
        }
    }

    private void initServiceReview(User user) {
        DoctorServiceStatus status = or500(doctorServiceStatusReadService.findByUserId(user.getId()));
        if (status == null) {
            status = or500(doctorServiceStatusReadService.findById(INIT_ID));
            status.setUserId(user.getId());
            or500(doctorServiceStatusWriteService.createServiceStatus(status));
        }
        List<DoctorServiceReview> reviews = or500(doctorServiceReviewReadService.findServiceReviewsByUserId(user.getId()));
        if (!notEmpty(reviews)) {
            reviews = or500(doctorServiceReviewReadService.findServiceReviewsByUserId(INIT_ID));
            reviews.forEach(review -> {
                review.setUserId(user.getId());
                review.setUserMobile(user.getMobile());
                or500(doctorServiceReviewWriteService.createReview(review));
            });
        }
    }

    private DoctorStaff initStaff(DoctorFarm farm, User user) {
        DoctorStaff staff = or500(doctorStaffReadService.findStaffById(INIT_ID));
        staff.setUserId(user.getId());
        staff.setOrgId(farm.getOrgId());
        staff.setOrgName(farm.getOrgName());
        Long staffId = or500(doctorStaffWriteService.createDoctorStaff(staff));
        staff.setId(staffId);
        return staff;
    }

    private void initChangeReason(DoctorFarm farm) {
        or500(doctorBasicReadService.findChangeReasonByFarmId(INIT_ID)).forEach(reason -> {
            reason.setFarmId(farm.getId());
            or500(doctorBasicWriteService.createChangeReason(reason));
        });
    }

    private void initCustomer(DoctorFarm farm, User user) {
        or500(doctorBasicReadService.findCustomersByFarmId(INIT_ID)).forEach(customer -> {
            customer.setName(customer.getName() + RandomUtil.random(0, 10) + UUID.randomUUID().toString().substring(0, 2));
            customer.setFarmId(farm.getId());
            customer.setFarmName(farm.getName());
            customer.setCreatorId(user.getId());
            customer.setCreatorName(user.getName());
            doctorBasicWriteService.createCustomer(customer);
        });
    }

    private void initBarns(DoctorFarm farm, DoctorStaff staff) {
        List<DoctorBarn> barns = or500(doctorBarnReadService.findBarnsByFarmId(INIT_ID));
        barns.forEach(barn -> {
            barn.setFarmId(farm.getId());
            barn.setFarmName(farm.getName());
            barn.setOrgId(farm.getOrgId());
            barn.setOrgName(farm.getOrgName());
            barn.setStaffId(staff.getId());
            or500(doctorBarnWriteService.createBarn(barn));
        });
    }

    private void initGroups(DoctorFarm farm, DoctorStaff staff) {
        List<DoctorGroup> groups = or500(doctorGroupReadService.findGroupsByFarmId(INIT_ID));
        groups.forEach(group -> {
            DoctorGroupDetail groupDetail = or500(doctorGroupReadService.findGroupDetailByGroupId(group.getId()));
            List<DoctorGroupEvent> groupEvents = or500(doctorGroupReadService.pagingGroupEvent(group.getFarmId(), group.getId(), null, null, Integer.MAX_VALUE)).getData();

            //copy 猪群
            group.setOrgId(farm.getOrgId());
            group.setOrgName(farm.getOrgName());
            group.setFarmId(farm.getId());
            group.setFarmName(farm.getName());

            DoctorBarn barn = or500(doctorBarnReadService.findBarnsByEnums(farm.getId(), group.getPigType(), null, null)).get(0);
            group.setGroupCode(barn.getName() + "(" + DateUtil.toDateString(new Date()) + ")");
            group.setOpenAt(new Date());

            group.setInitBarnId(barn.getId());
            group.setInitBarnName(barn.getName());
            group.setCurrentBarnId(barn.getId());
            group.setCurrentBarnName(barn.getName());
            group.setStaffId(staff.getId());
            Long groupId = or500(doctorGroupWriteService.createGroup(group));
            group.setId(groupId);

            //copy 猪群事件(模板里只有两个事件:新建猪群和转入猪群)
            Map<Integer, DoctorGroupEvent> eventMap = Maps.newHashMap();
            groupEvents.forEach(event -> {
                event.setOrgId(group.getOrgId());
                event.setOrgName(group.getOrgName());
                event.setFarmId(group.getFarmId());
                event.setFarmName(group.getFarmName());
                event.setEventAt(new Date());
                event.setGroupId(groupId);
                event.setGroupCode(group.getGroupCode());
                event.setBarnId(barn.getId());
                event.setBarnName(barn.getName());
                Long eventId = or500(doctorGroupWriteService.createGroupEvent(event));
                event.setId(eventId);
                eventMap.put(event.getType(), event);
            });

            //copy 猪群跟踪
            DoctorGroupEvent event = MoreObjects.firstNonNull(eventMap.get(GroupEventType.MOVE_IN.getValue()), eventMap.get(GroupEventType.NEW.getValue()));
            DoctorGroupTrack groupTrack = groupDetail.getGroupTrack();
            groupTrack.setGroupId(groupId);
            groupTrack.setRelEventId(event.getId());
            Long groupTrackId = or500(doctorGroupWriteService.createGroupTrack(groupTrack));
            groupTrack.setId(groupTrackId);

            //copy 猪群镜像
            DoctorGroupSnapshot groupSnapshot = new DoctorGroupSnapshot();
            groupSnapshot.setEventType(GroupEventType.MOVE_IN.getValue());  //猪群事件类型
            groupSnapshot.setToGroupId(group.getId());
            groupSnapshot.setToEventId(event.getId());
            groupSnapshot.setToInfo(JsonMapper.nonEmptyMapper().toJson(DoctorGroupSnapShotInfo.builder()
                    .group(group)
                    .groupEvent(event)
                    .groupTrack(groupTrack)
                    .build()));
            or500(doctorGroupWriteService.createGroupSnapShot(groupSnapshot));
        });
    }

    //初始化猪
    private void initPigs(DoctorFarm farm) {
        or500(doctorPigReadService.findPigsByFarmId(INIT_ID)).forEach(pig -> {
            DoctorPigInfoDetailDto pigDetail = or500(doctorPigReadService.queryPigDetailInfoByPigId(pig.getId(), Integer.MAX_VALUE));
            DoctorPigTrack pigTrack = pigDetail.getDoctorPigTrack();
            List<DoctorPigEvent> events = pigDetail.getDoctorPigEvents();

            //猪
            pig.setOrgId(farm.getOrgId());
            pig.setOrgName(farm.getOrgName());
            pig.setFarmId(farm.getId());
            pig.setFarmName(farm.getName());
            DoctorBarn barn = getPigBarn(pig);
            pig.setInitBarnId(barn.getId());
            pig.setInitBarnName(barn.getName());

            Long oldPigId = pig.getId();
            Long newPigId = or500(doctorPigWriteService.createPig(pig));
            pig.setId(newPigId);

            //猪事件
            List<Long> eventIds = events.stream().map(event -> initPigEvent(event, farm, pig)).collect(Collectors.toList());

            //猪跟踪
            pigTrack.setPigId(newPigId);
            pigTrack.setPigType(pig.getPigType());
            pigTrack.setFarmId(farm.getId());
            pigTrack.setCurrentBarnId(pig.getInitBarnId());
            pigTrack.setCurrentBarnName(pig.getInitBarnName());
            pigTrack.setRelEventIds(getRelEventIds(eventIds, pig, pigTrack));
            Long pigTrackId = or500(doctorPigWriteService.createPigTrack(pigTrack));
            pigTrack.setId(pigTrackId);

            //猪镜像
            DoctorPigSnapshot pigSnapshot = DoctorPigSnapshot.builder()
                    .pigId(pig.getId()).farmId(pig.getFarmId()).orgId(pig.getOrgId()).eventId(Lists.reverse(eventIds).get(0)).build();
            pigSnapshot.setPigInfoMap(ImmutableMap.of("doctorPigTrack", JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(pigTrack)));
            or500(doctorPigWriteService.createPigSnapShot(pigSnapshot));

            initWorkFlow(oldPigId, pig);
        });
    }

    private Long initPigEvent(DoctorPigEvent event, DoctorFarm farm, DoctorPig pig) {
        event.setOrgId(farm.getOrgId());
        event.setOrgName(farm.getOrgName());
        event.setFarmId(farm.getId());
        event.setFarmName(farm.getName());
        event.setPigId(pig.getId());
        event.setPigCode(pig.getPigCode());
        event.setEventAt(new Date());
        event.setBarnId(pig.getInitBarnId());
        event.setBarnName(pig.getInitBarnName());
        return or500(doctorPigWriteService.createPigEvent(event));
    }

    //初始化工作流相关数据
    private void initWorkFlow(Long oldPigId, DoctorPig pig) {
        FlowInstance flowInstance;
        try {
            flowInstance = jdbcAccess.findFlowInstanceSingle(MapBuilder.of().put("businessId", oldPigId).map());//获取流程实例
            if (flowInstance == null) {
                log.warn("flow instance not found, businessId:{}", oldPigId);
                return;
            }
        } catch (Exception e) {
            log.error("find flow instance fail, businessId:{}, cause:{}", oldPigId, Throwables.getStackTraceAsString(e));
            return;
        }
        List<FlowProcess> flowProcesses = jdbcAccess.findFlowProcesses(MapBuilder.of().put("flowInstanceId", flowInstance.getId()).map());
        List<FlowProcessTrack> flowProcessTracks = jdbcAccess.findFlowProcessTracks(MapBuilder.of().put("flowInstanceId", flowInstance.getId()).map());
        List<FlowHistoryProcess> flowHistoryProcesses = jdbcAccess.findFlowHistoryProcesses(MapBuilder.of().put("flowInstanceId", flowInstance.getId()).map());

        //流程实例
        flowInstance.setBusinessId(pig.getId());
        Long flowInstanceId = jdbcAccess.createFlowInstance(flowInstance);
        flowInstance.setId(flowInstanceId);

        //流程活动节点
        flowProcesses.forEach(flowProcess -> {
            flowProcess.setFlowInstanceId(flowInstanceId);
            jdbcAccess.createFlowProcess(flowProcess);
        });

        //流程活动节点跟踪
        flowProcessTracks.forEach(flowProcessTrack -> {
            flowProcessTrack.setFlowInstanceId(flowInstanceId);
            jdbcAccess.createFlowProcessTrack(flowProcessTrack);
        });

        //流程历史活动节点
        flowHistoryProcesses.forEach(flowHistoryProcess -> {
            flowHistoryProcess.setFlowInstanceId(flowInstanceId);
            jdbcAccess.createFlowHistoryProcess(flowHistoryProcess);
        });
    }

    private String getRelEventIds(List<Long> eventIds, DoctorPig pig, DoctorPigTrack pigTrack) {
        if (DoctorPig.PIG_TYPE.SOW.getKey().equals(pig.getPigType())) {
            return JsonMapper.nonEmptyMapper().toJson(ImmutableMap.of(pigTrack.getCurrentParity(), Joiners.COMMA.join(eventIds)));
        }
        return Joiners.COMMA.join(eventIds);
    }

    private DoctorBarn getPigBarn(DoctorPig pig) {
        DoctorBarn initBarn = or500(doctorBarnReadService.findBarnById(pig.getInitBarnId()));
        List<DoctorBarn> barns = or500(doctorBarnReadService.findBarnsByEnums(pig.getFarmId(), initBarn.getPigType(), null, null));
        return !notEmpty(barns) ? initBarn : barns.get(0);
    }
}
