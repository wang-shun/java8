package io.terminus.doctor.web.init;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.utils.Joiners;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.common.utils.DateUtil;
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
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorFarmWriteService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorOrgWriteService;
import io.terminus.doctor.user.service.DoctorStaffReadService;
import io.terminus.doctor.user.service.DoctorStaffWriteService;
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
import java.util.stream.Collectors;

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

    /**
     * 根据用户id初始化出所有的猪场相关数据(内测用)
     * @param userId  用户id
     * @return  是否成功
     */
    @RequestMapping(value = "/farm", method = RequestMethod.GET)
    public Boolean initAllDataByUserId(@RequestParam("userId") Long userId) {
        User user = or500(userReadService.findById(userId));
        init(user);
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

    private void init(User user) {
        //1. 判断是否创建公司
        DoctorOrg org = initOrg(user);

        //2. 创建猪场
        DoctorFarm farm = initFarm(org);

        //3. 创建staff
        DoctorStaff staff = initStaff(farm, user);

        //4. 创建基础数据
        or500(doctorBasicWriteService.initFarmBasic(farm.getId()));

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

    private DoctorOrg initOrg(User user) {
        DoctorOrg org = or500(doctorOrgReadService.findOrgById(INIT_ID));
        org.setName(getName(org.getName()));
        org.setMobile(user.getMobile());
        Long orgId = or500(doctorOrgWriteService.createOrg(org));
        org.setId(orgId);
        return org;
    }

    private DoctorFarm initFarm(DoctorOrg org) {
        DoctorFarm farm = or500(doctorFarmReadService.findFarmById(INIT_ID));
        farm.setOrgId(org.getId());
        farm.setOrgName(org.getName());
        farm.setName(getName(farm.getName()));
        Long farmId = or500(doctorFarmWriteService.createFarm(farm));
        farm.setId(farmId);
        return farm;
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
        or500(doctorPigReadService.findPigsByFarmId(farm.getId())).forEach(pig -> {
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
            pig.setPigCode(getPigCode(pig.getPigType()));

            Long oldPigId = pig.getId();
            Long newPigId = or500(doctorPigWriteService.createPig(pig));
            pig.setId(newPigId);

            //猪事件
            List<Long> eventIds = events.stream().map(event -> initPigEvent(event, farm, pig)).collect(Collectors.toList());

            //猪跟踪
            pigTrack.setPigId(newPigId);
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
        FlowInstance flowInstance = jdbcAccess.findFlowInstanceSingle(FlowInstance.builder().businessId(oldPigId).build());//获取流程实例
        List<FlowProcess> flowProcesses = jdbcAccess.findFlowProcesses(FlowProcess.builder().flowInstanceId(flowInstance.getId()).build());
        List<FlowProcessTrack> flowProcessTracks = jdbcAccess.findFlowProcessTracks(FlowProcessTrack.builder().flowInstanceId(flowInstance.getId()).build());
        List<FlowHistoryProcess> flowHistoryProcesses = jdbcAccess.findFlowHistoryProcesses(FlowHistoryProcess.builder().flowInstanceId(flowInstance.getId()).build());

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

    private String getPigCode(Integer pigType) {
        return DoctorPig.PIG_TYPE.from(pigType).getDesc() + DateTime.now().toString(TIME);
    }

    private DoctorBarn getPigBarn(DoctorPig pig) {
        DoctorBarn initBarn = or500(doctorBarnReadService.findBarnById(pig.getInitBarnId()));
        List<DoctorBarn> barns = or500(doctorBarnReadService.findBarnsByEnums(pig.getFarmId(), initBarn.getPigType(), null, null));
        return !notEmpty(barns) ? initBarn : barns.get(0);
    }

//    @Override
    public void run(String... strings) throws Exception {
        initAllDataByUserId(23L);
    }
}
