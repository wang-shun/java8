package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorDailyGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.model.DoctorDailyGroup;
import io.terminus.doctor.event.model.DoctorGroup;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Code generated by terminus code gen
 * Desc: 猪群数量每天记录表写服务实现类
 * Date: 2017-04-17
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDailyGroupWriteServiceImpl implements DoctorDailyGroupWriteService {

    private final DoctorDailyGroupDao doctorDailyGroupDao;
    private final DoctorGroupDao doctorGroupDao;
    private final DoctorKpiDao doctorKpiDao;

    @Autowired
    public DoctorDailyGroupWriteServiceImpl(DoctorDailyGroupDao doctorDailyGroupDao, DoctorGroupDao doctorGroupDao, DoctorKpiDao doctorKpiDao) {
        this.doctorDailyGroupDao = doctorDailyGroupDao;
        this.doctorGroupDao = doctorGroupDao;
        this.doctorKpiDao = doctorKpiDao;
    }

    @Override
    public Response<Long> createDoctorDailyGroup(DoctorDailyGroup doctorDailyGroup) {
        try {
            doctorDailyGroupDao.create(doctorDailyGroup);
            return Response.ok(doctorDailyGroup.getId());
        } catch (Exception e) {
            log.error("create doctorDailyGroup failed, doctorDailyGroup:{}, cause:{}", doctorDailyGroup, Throwables.getStackTraceAsString(e));
            return Response.fail("doctorDailyGroup.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateDoctorDailyGroup(DoctorDailyGroup doctorDailyGroup) {
        try {
            return Response.ok(doctorDailyGroupDao.update(doctorDailyGroup));
        } catch (Exception e) {
            log.error("update doctorDailyGroup failed, doctorDailyGroup:{}, cause:{}", doctorDailyGroup, Throwables.getStackTraceAsString(e));
            return Response.fail("doctorDailyGroup.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteDoctorDailyGroupById(Long doctorDailyGroupId) {
        try {
            return Response.ok(doctorDailyGroupDao.delete(doctorDailyGroupId));
        } catch (Exception e) {
            log.error("delete doctorDailyGroup failed, doctorDailyGroupId:{}, cause:{}", doctorDailyGroupId, Throwables.getStackTraceAsString(e));
            return Response.fail("doctorDailyGroup.delete.fail");
        }
    }

    @Override
    public Response<Boolean> generateYesterdayAndToday(List<Long> farmIds, Date date) {
        try{
            Date today = new DateTime(date).plus(1).toDate();
            createDailyGroups(farmIds, date);
            createDailyGroups(farmIds, today);
        }catch(Exception e){
            log.info("generate yesterday and today failed, cause: {}", Throwables.getStackTraceAsString(e));
            return Response.ok(Boolean.FALSE);
        }

        return Response.ok(Boolean.TRUE);
    }

    @Override
    public Response<Boolean> createDailyGroups(List<Long> farmIds, Date date) {
        try{
            doctorDailyGroupDao.deleteByFarmIdAndSumAt(date);
            farmIds.forEach(farmId -> {
                log.info("create group daily start, farmId: {}, now is: {}", farmId, DateUtil.toDateTimeString(new Date()));
                List<DoctorGroup> groups= doctorGroupDao.findByFarmIdAndDate(farmId, date);
                List<DoctorDailyGroup> dailyGroups = Lists.newArrayList();
                groups.forEach(group -> {
                    DoctorDailyGroup doctorDailyGroup = getDoctorDailyGroup(group, date, DateUtil.getDateEnd(new DateTime(date)).toDate());
                    dailyGroups.add(doctorDailyGroup);
                });
                if(!Arguments.isNullOrEmpty(dailyGroups)){
                    doctorDailyGroupDao.creates(dailyGroups);
                }
            });
        }catch (Exception e){
            log.error("create group daily failed, cause: {}", Throwables.getStackTraceAsString(e));
            return Response.fail("create group daily failed");
        }
        return Response.ok(Boolean.TRUE);
    }

    @Override
    public Response<Boolean> createDailyGroups(Long farmId, Date date) {
        try{
            log.info("create group daily start, farmId: {}, date: {}, now is: {}", farmId, DateUtil.toDateString(date), DateUtil.toDateTimeString(new Date()));
            doctorDailyGroupDao.deleteByFarmIdAndSumAt(farmId, date);
            List<DoctorGroup> groups= doctorGroupDao.findByFarmIdAndDate(farmId, date);
            List<DoctorDailyGroup> dailyGroups = Lists.newArrayList();
            groups.forEach(group -> {
                DoctorDailyGroup doctorDailyGroup = getDoctorDailyGroup(group, date, DateUtil.getDateEnd(new DateTime(date)).toDate());
                dailyGroups.add(doctorDailyGroup);
            });
            if(!Arguments.isNullOrEmpty(dailyGroups)){
                doctorDailyGroupDao.creates(dailyGroups);
            }
        }catch (Exception e){
            log.error("create group daily failed, cause: {}", Throwables.getStackTraceAsString(e));
            return Response.fail("create group daily failed");
        }
        return Response.ok(Boolean.TRUE);
    }

    private DoctorDailyGroup getDoctorDailyGroup(DoctorGroup group, Date startAt, Date endAt) {
        Long groupId = group.getId();
        DoctorDailyGroup doctorDailyGroup = new DoctorDailyGroup();
        doctorDailyGroup.setFarmId(group.getFarmId());
        doctorDailyGroup.setGroupId(groupId);
        doctorDailyGroup.setType(group.getPigType());
        doctorDailyGroup.setSumAt(startAt);
        doctorDailyGroup.setStart(doctorKpiDao.realTimeLivetockGroup(groupId, new DateTime(startAt).minusDays(1).toDate()));
        if(Objects.equals(PigType.DELIVER_SOW.getValue(), group.getPigType())){
            doctorDailyGroup.setUnweanCount(doctorKpiDao.getGroupUnWean(groupId, endAt));
            doctorDailyGroup.setWeanCount(doctorKpiDao.getGroupWean(groupId, endAt));
        }
        doctorDailyGroup.setInnerIn(doctorKpiDao.getGroupInnerIn(groupId, startAt, endAt));
        doctorDailyGroup.setOuterIn(doctorKpiDao.getGroupOuterIn(groupId, startAt, endAt));
        doctorDailyGroup.setSale(doctorKpiDao.getGroupSale(groupId, startAt, endAt));
        doctorDailyGroup.setDead(doctorKpiDao.getGroupDead(groupId, startAt, endAt));
        doctorDailyGroup.setWeedOut(doctorKpiDao.getGroupWeedOut(groupId, startAt, endAt));
        doctorDailyGroup.setOtherChange(doctorKpiDao.getGroupOtherChange(groupId, startAt, endAt));
        doctorDailyGroup.setChgFarm(doctorKpiDao.getGroupChgFarm(groupId, startAt, endAt));
        doctorDailyGroup.setInnerOut(doctorKpiDao.getGroupInnerOut(groupId, startAt, endAt));
        if(Objects.equals(PigType.RESERVE.getValue(), group.getPigType())){
            doctorDailyGroup.setOuterOut(doctorKpiDao.getGroupTrunSeed(groupId, startAt, endAt));
        }else {
            doctorDailyGroup.setOuterOut(doctorKpiDao.getGroupOuterOut(groupId, startAt, endAt));
        }
        doctorDailyGroup.setEnd(doctorKpiDao.realTimeLivetockGroup(groupId, startAt));
        return doctorDailyGroup;
    }
}