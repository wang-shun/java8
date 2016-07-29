package io.terminus.doctor.move.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.handler.DoctorMoveTableEnum;
import io.terminus.doctor.move.model.B_ChangeReason;
import io.terminus.doctor.move.model.B_Customer;
import io.terminus.doctor.move.model.Proc_InventoryGain;
import io.terminus.doctor.move.model.TB_FieldValue;
import io.terminus.doctor.move.model.View_EventListGain;
import io.terminus.doctor.move.model.View_GainCardList;
import io.terminus.doctor.move.model.View_PigLocationList;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.parana.user.impl.dao.UserProfileDao;
import io.terminus.parana.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 迁移数据, TODO: 注意如果一个数据源有多个猪场的情况!!!
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/27
 */
@Slf4j
@Service
public class DoctorMoveDataService implements CommandLineRunner {

    private final DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    private final DoctorBarnDao doctorBarnDao;
    private final DoctorCustomerDao doctorCustomerDao;
    private final DoctorChangeReasonDao doctorChangeReasonDao;
    private final DoctorBasicDao doctorBasicDao;
    private final DoctorStaffDao doctorStaffDao;
    private final UserProfileDao userProfileDao;
    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;

    @Autowired
    public DoctorMoveDataService(DoctorMoveDatasourceHandler doctorMoveDatasourceHandler,
                                 DoctorBarnDao doctorBarnDao,
                                 DoctorCustomerDao doctorCustomerDao,
                                 DoctorChangeReasonDao doctorChangeReasonDao,
                                 DoctorBasicDao doctorBasicDao,
                                 DoctorStaffDao doctorStaffDao,
                                 UserProfileDao userProfileDao,
                                 DoctorGroupDao doctorGroupDao,
                                 DoctorGroupEventDao doctorGroupEventDao,
                                 DoctorGroupTrackDao doctorGroupTrackDao) {
        this.doctorMoveDatasourceHandler = doctorMoveDatasourceHandler;
        this.doctorBarnDao = doctorBarnDao;
        this.doctorCustomerDao = doctorCustomerDao;
        this.doctorChangeReasonDao = doctorChangeReasonDao;
        this.doctorBasicDao = doctorBasicDao;
        this.doctorStaffDao = doctorStaffDao;
        this.userProfileDao = userProfileDao;
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
    }

    /**
     * 迁移基础数据
     */
    @Transactional
    public Response<Boolean> moveBasic(Long moveId) {
        try {
            //基础数据按照类型名称分组
            Map<String, List<DoctorBasic>> basicsMap = doctorBasicDao.listAll().stream().collect(Collectors.groupingBy(DoctorBasic::getTypeName));
            Map<String, List<TB_FieldValue>> fieldsMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, TB_FieldValue.class, "TB_FieldValue")).stream()
                    .collect(Collectors.groupingBy(TB_FieldValue::getTypeId));

            //按照遍历doctor里的基础数据, 如果有缺失的, 就补充进来
            for (Map.Entry<String, List<DoctorBasic>> basic : basicsMap.entrySet()) {
                //取出基础字段名称
                List<String> basicNames = basic.getValue().stream().map(DoctorBasic::getName).collect(Collectors.toList());

                List<TB_FieldValue> fieldValues = fieldsMap.get(basic.getKey());
                if (!notEmpty(fieldValues)) {
                    continue;
                }

                //把过滤的结果放到doctor_basics里
                fieldValues.stream()
                        .filter(field -> !basicNames.contains(field.getFieldText()))
                        .forEach(fn -> doctorBasicDao.create(getBasic(fn)));
            }
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("move basic failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.basic.fail");
        }
    }

    //拼接基础数据
    private DoctorBasic getBasic(TB_FieldValue field) {
        DoctorBasic.Type type = DoctorBasic.Type.from(field.getTypeId());
        DoctorBasic basic = new DoctorBasic();
        basic.setName(field.getFieldText());
        basic.setType(type == null ? null : type.getValue());
        basic.setTypeName(field.getTypeId());
        basic.setContext(field.getRemark());
        basic.setOutId(field.getOID());
        basic.setSrm(field.getSrm());
        basic.setIsValid(IsOrNot.YES.getValue());
        return basic;
    }

    /**
     * 迁移Barn
     */
    @Transactional
    public Response<Boolean> moveBarn(Long moveId) {
        try {
            List<DoctorBarn> barns = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findAllData(moveId, View_PigLocationList.class, DoctorMoveTableEnum.view_PigLocationList)).stream()
                    .filter(loc -> isFarm(loc.getFarmOID(), mockFarm().getOutId()))     //这一步很重要, 如果一个公司有多个猪场, 猪场id必须匹配!
                    .filter(f -> true) // TODO: 16/7/28 多个猪场注意过滤outId
                    .map(location -> getBarn(mockOrg(), mockFarm(), mockUser(), location))
                    .collect(Collectors.toList());

            if (notEmpty(barns)) {
                doctorBarnDao.creates(barns);
            }
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("move barn failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.barn.fail");
        }
    }

    /**
     * 迁移客户
     */
    @Transactional
    public Response<Boolean> moveCustomer(Long moveId) {
        try {
            List<DoctorCustomer> customers = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findAllData(moveId, B_Customer.class, DoctorMoveTableEnum.B_Customer)).stream()
                    .filter(f -> true) // TODO: 16/7/28 多个猪场注意过滤outId
                    .map(cus -> getCustomer(mockFarm(), mockUser(), cus))
                    .collect(Collectors.toList());

            if (notEmpty(customers)) {
                doctorCustomerDao.creates(customers);
            }
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("move customer failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.customer.fail");
        }
    }

    /**
     * 迁移变动原因
     */
    @Transactional
    public Response<Boolean> moveChangeReason(Long moveId) {
        try {
            //查出所有的变动
            List<DoctorBasic> changeTypes = doctorBasicDao.findByType(DoctorBasic.Type.CHANGE_TYPE.getValue());

            //查出每个变动下的变动原因, 组装成map
            Map<DoctorBasic, List<DoctorChangeReason>> changeTypeMap = Maps.newHashMap();
            changeTypes.forEach(type -> changeTypeMap.put(type, doctorChangeReasonDao.findByChangeTypeIdAndSrm(type.getId(), null)));

            //查出猪场软件里的所有变动原因, 并按照变动类型 group by
            Map<String, List<B_ChangeReason>> reasonMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, B_ChangeReason.class, "changeReason")).stream()
                    .collect(Collectors.groupingBy(B_ChangeReason::getChangeType));

            //遍历每个变动类型的变动原因, 过滤掉重复的插入
            for (Map.Entry<DoctorBasic, List<DoctorChangeReason>> changeType : changeTypeMap.entrySet()) {
                //当前doctor里存在的reason名称
                List<String> changeReasons = changeType.getValue().stream().map(DoctorChangeReason::getReason).collect(Collectors.toList());
                List<B_ChangeReason> reasons = reasonMap.get(changeType.getKey().getName());

                if (!notEmpty(reasons)) {
                    continue;
                }

                //过滤掉重复的原因, 插入doctor_change_reasons 表
                reasons.stream()
                        .filter(r -> !changeReasons.contains(r.getReasonName()))
                        .forEach(reason -> doctorChangeReasonDao.create(getReason(reason, changeType.getKey().getId())));
            }

            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("move customer failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.customer.fail");
        }
    }

    /**
     * 迁移猪群
     */
    @Transactional
    public Response<Boolean> moveGroup(Long moveId) {
        try {
            //0. 基础数据准备: barn, basic, staff
            Map<String, DoctorBarn> barnMap = doctorBarnDao.findByFarmId(mockFarm().getId()).stream()
                    .collect(Collectors.toMap(DoctorBarn::getOutId, v -> v));
            Map<Integer, Map<String, DoctorBasic>> basicMap = getBasicMap();
            Map<String, Long> staffMap = getStaffName(mockOrg().getId());

            //1. 迁移DoctorGroup
            List<DoctorGroup> groups = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, View_GainCardList.class, "DoctorGroup-GainCardList")).stream()
                    .filter(f -> true) // TODO: 16/7/28 多个猪场注意过滤outId
                    .map(gain -> getGroup(mockOrg(), mockFarm(), gain, barnMap, basicMap, staffMap)).collect(Collectors.toList());

            //2. 迁移DoctorGroupEvent
            Map<Long, List<DoctorGroupEvent>> eventMap = Maps.newHashMap();

            List<View_EventListGain> gainEvents = RespHelper.orServEx(doctorMoveDatasourceHandler.findByHbsSql(moveId, View_EventListGain.class, "DoctorGroupEvent-EventListGain"));

            //3. 迁移DoctorTrack
            //统计结果转换成map
            Map<String, Proc_InventoryGain> gainMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, Proc_InventoryGain.class, "DoctorGroupTrack-Proc_InventoryGain", ImmutableMap.of("date", new Date()))).stream()
                    .filter(f -> true) // TODO: 16/7/28 多个猪场注意过滤outId
                    .collect(Collectors.toMap(Proc_InventoryGain::getGroupOutId, v -> v));

            List<DoctorGroupTrack> groupTracks = groups.stream()
                    .map(group -> getGroupTrack(group, gainMap.get(group.getOutId()), eventMap.get(group.getId())))
                    .collect(Collectors.toList());



            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("move group failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.group.fail");
        }
    }

    //拼接猪群跟踪
    private DoctorGroupTrack getGroupTrack(DoctorGroup group, Proc_InventoryGain gain, List<DoctorGroupEvent> events) {
        DoctorGroupTrack groupTrack = new DoctorGroupTrack();
        groupTrack.setGroupId(group.getId());
        groupTrack.setSex(DoctorGroupTrack.Sex.MIX.getValue());

        //如果猪群已经关闭, 大部分的统计值可以置成0
        if (Objects.equals(group.getStatus(), DoctorGroup.Status.CLOSED.getValue())) {
            return getCloseGroupTrack(groupTrack, events);
        }

        //未关闭的猪群, 拼接
        groupTrack.setQuantity(MoreObjects.firstNonNull(gain.getQuantity(), 0));
        groupTrack.setBoarQty(gain.getQuantity() / 2);
        groupTrack.setSowQty(groupTrack.getQuantity() - groupTrack.getBoarQty());
        groupTrack.setAvgDayAge(gain.getAvgDayAge());
        groupTrack.setBirthDate(DateTime.now().minusDays(groupTrack.getAvgDayAge()).toDate());
        groupTrack.setAvgWeight(MoreObjects.firstNonNull(gain.getAvgWeight(), 0D));
        groupTrack.setWeight(groupTrack.getAvgWeight() * groupTrack.getQuantity());
//        groupTrack.setRelEventId();
//        groupTrack.setPrice();
//        groupTrack.setAmount();
//        groupTrack.setCustomerId();
//        groupTrack.setCustomerName();
//        groupTrack.setSaleQty();
//        groupTrack.setExtra();
//        groupTrack.setExtraEntity();
        return groupTrack;
    }

    //关闭猪群的猪群跟踪
    private DoctorGroupTrack getCloseGroupTrack(DoctorGroupTrack groupTrack, List<DoctorGroupEvent> events) {
        DoctorGroupEvent closeEvent = events.stream().filter(e -> Objects.equals(GroupEventType.CLOSE.getValue(), e.getType())).findFirst().orElse(null);
        if (closeEvent != null) {
            groupTrack.setRelEventId(closeEvent.getId());
        }
        groupTrack.setQuantity(0);
        groupTrack.setBoarQty(0);
        groupTrack.setSowQty(0);
        groupTrack.setAvgDayAge(0);
        groupTrack.setBirthDate(DateTime.now().toDate());
        groupTrack.setAvgWeight(0D);
        groupTrack.setWeight(0D);
        groupTrack.setPrice(0L);
        groupTrack.setAmount(0L);
        groupTrack.setSaleQty(0);
        return groupTrack;
    }

    //拼接猪群
    private DoctorGroup getGroup(DoctorOrg org, DoctorFarm farm, View_GainCardList gain,
                                 Map<String, DoctorBarn> barnMap, Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, Long> staffMap) {
        DoctorGroup group = BeanMapper.map(gain, DoctorGroup.class);

        group.setOrgId(org.getId());
        group.setOrgName(org.getName());
        group.setFarmId(farm.getId());
        group.setFarmName(farm.getName());

        //猪舍
        DoctorBarn barn = barnMap.get(gain.getBarnOutId());
        if (barn != null) {
            group.setInitBarnId(barn.getId());
            group.setInitBarnName(barn.getName());
            group.setCurrentBarnId(barn.getId());
            group.setCurrentBarnName(barn.getName());
            group.setPigType(barn.getPigType());
        }
        //品种
        if (notEmpty(gain.getBreed())) {
            DoctorBasic breed = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(gain.getBreed());
            group.setBreedId(breed.getId());
            group.setBreedName(breed.getName());
        }
        //品系
        if (notEmpty(gain.getGenetic())) {
            DoctorBasic gene = basicMap.get(DoctorBasic.Type.GENETICS.getValue()).get(gain.getGenetic());
            group.setGeneticId(gene.getId());
            group.setGeneticName(gene.getName());
        }
        if (notEmpty(gain.getStaffName())) {
            group.setStaffId(staffMap.get(gain.getStaffName()));
        }
        return group;
    }

    //分别是 Map<DoctorBasic.TypeEnum, Map<DoctorBasic.name, DoctorBasic>>
    private Map<Integer, Map<String, DoctorBasic>> getBasicMap() {
        Map<Integer, Map<String, DoctorBasic>> basicMap = Maps.newHashMap();
        doctorBasicDao.listAll().stream()
                .collect(Collectors.groupingBy(DoctorBasic::getType)).entrySet()
                .forEach(basic -> basicMap.put(basic.getKey(),
                        basic.getValue().stream().collect(Collectors.toMap(DoctorBasic::getName, v -> v))));
        return basicMap;
    }

    //拼接staff,  Map<真实姓名, DoctorStaff>
    public Map<String, Long> getStaffName(Long orgId) {
        Map<String, Long> staffMap = Maps.newHashMap();
        doctorStaffDao.findByOrgId(orgId).forEach(staff -> {
            UserProfile profile = userProfileDao.findByUserId(staff.getUserId());
            if (profile != null && notEmpty(profile.getRealName())) {
                staffMap.put(profile.getRealName(), staff.getId());
            }
        });
        return staffMap;
    }

    //拼接变动原因
    private DoctorChangeReason getReason(B_ChangeReason reason, Long changeTypeId) {
        DoctorChangeReason changeReason = new DoctorChangeReason();
        changeReason.setChangeTypeId(changeTypeId);
        changeReason.setReason(reason.getReasonName());
        changeReason.setOutId(reason.getOID());
        return changeReason;
    }

    //拼接客户
    private DoctorCustomer getCustomer(DoctorFarm farm, DoctorUser user, B_Customer cus) {
        DoctorCustomer customer = new DoctorCustomer();
        customer.setName(cus.getCustomerName());
        customer.setFarmId(farm.getId());
        customer.setFarmName(farm.getName());
        customer.setMobile(cus.getMobilePhone());
        customer.setEmail(cus.getEMail());
        customer.setOutId(cus.getOID());
        customer.setCreatorId(user.getId());
        customer.setCreatorName(user.getName());
        return customer;
    }

    //拼接barn
    private DoctorBarn getBarn(DoctorOrg org, DoctorFarm farm, DoctorUser user, View_PigLocationList location) {
        //转换pigtype
        PigType pigType = PigType.from(location.getTypeName());

        DoctorBarn barn = new DoctorBarn();
        barn.setName(location.getBarn());
        barn.setOrgId(org.getId());
        barn.setOrgName(org.getName());
        barn.setFarmId(farm.getId());
        barn.setFarmName(farm.getName());
        barn.setPigType(pigType == null ? 0: pigType.getValue());
        barn.setCanOpenGroup("可以".equals(location.getCanOpenGroupText()) ? 1 : -1);
        barn.setStatus("在用".equals(location.getIsStopUseText()) ? 1 : 0);
        barn.setStaffId(user.getId());
        barn.setStaffName(user.getName());
        barn.setOutId(location.getOID());
        return barn;
    }

    //判断猪场id是否相同
    private static boolean isFarm(String farmOID, String outId) {
        return Objects.equals(farmOID, outId);
    }

    private static DoctorFarm mockFarm() {
        DoctorFarm farm = new DoctorFarm();
        farm.setId(9999L);
        farm.setName("测试迁移猪场");
        return farm;
    }

    private static DoctorOrg mockOrg() {
        DoctorOrg org = new DoctorOrg();
        org.setId(9999L);
        org.setName("测试迁移公司");
        return org;
    }

    private static DoctorUser mockUser() {
        DoctorUser user = new DoctorUser();
        user.setId(9999L);
        user.setName("测试迁移管理员");
        return user;
    }

    @Override
    public void run(String... strings) throws Exception {
        // Just for test!

    }
}
