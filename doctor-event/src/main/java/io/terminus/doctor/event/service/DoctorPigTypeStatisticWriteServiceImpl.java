package io.terminus.doctor.event.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.CountUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigTypeStatisticDao;
import io.terminus.doctor.event.dto.DoctorGroupCount;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * Desc: 猪只数统计表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-06-03
 */
@Slf4j
@Service
@RpcProvider
public class DoctorPigTypeStatisticWriteServiceImpl implements DoctorPigTypeStatisticWriteService {

    private final DoctorPigTypeStatisticDao doctorPigTypeStatisticDao;
    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorPigDao doctorPigDao;

    @Autowired
    public DoctorPigTypeStatisticWriteServiceImpl(DoctorPigTypeStatisticDao doctorPigTypeStatisticDao,
                                                  DoctorGroupDao doctorGroupDao,
                                                  DoctorGroupTrackDao doctorGroupTrackDao,
                                                  DoctorPigDao doctorPigDao) {
        this.doctorPigTypeStatisticDao = doctorPigTypeStatisticDao;
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorPigDao = doctorPigDao;
    }

    @Override
    public Response<Long> createPigTypeStatistic(DoctorPigTypeStatistic pigTypeStatistic) {
        try {
            doctorPigTypeStatisticDao.create(pigTypeStatistic);
            return Response.ok(pigTypeStatistic.getId());
        } catch (Exception e) {
            log.error("create pigTypeStatistic failed, pigTypeStatistic:{}, cause:{}", pigTypeStatistic, Throwables.getStackTraceAsString(e));
            return Response.fail("pigTypeStatistic.create.fail");
        }
    }

    @Override
    public Response<Boolean> updatePigTypeStatistic(DoctorPigTypeStatistic pigTypeStatistic) {
        try {
            return Response.ok(doctorPigTypeStatisticDao.update(pigTypeStatistic));
        } catch (Exception e) {
            log.error("update pigTypeStatistic failed, pigTypeStatistic:{}, cause:{}", pigTypeStatistic, Throwables.getStackTraceAsString(e));
            return Response.fail("pigTypeStatistic.update.fail");
        }
    }

    @Override
    public Response<Boolean> deletePigTypeStatisticById(Long pigTypeStatisticId) {
        try {
            return Response.ok(doctorPigTypeStatisticDao.delete(pigTypeStatisticId));
        } catch (Exception e) {
            log.error("delete pigTypeStatistic failed, pigTypeStatisticId:{}, cause:{}", pigTypeStatisticId, Throwables.getStackTraceAsString(e));
            return Response.fail("pigTypeStatistic.delete.fail");
        }
    }

    @Override
    public Response<Boolean> statisticGroup(Long orgId, Long farmId) {
        try {
            DoctorGroupCount groupCount = getGroupCount(farmId);
            DoctorPigTypeStatistic statistic = doctorPigTypeStatisticDao.findByFarmId(farmId);

            //如果不存在, 就新建统计数据
            if (statistic == null) {
                statistic = DoctorPigTypeStatistic.builder().farmId(farmId).orgId(orgId).build();
                setGroupStatistic(statistic, groupCount);
                RespHelper.orServEx(createPigTypeStatistic(statistic));
            } else {
                setGroupStatistic(statistic, groupCount);
                RespHelper.orServEx(updatePigTypeStatistic(statistic));
            }
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("statistic group failed, orgId:{}, farmId:{}, cause:{}", orgId, farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("statistic.group.fail");
        }
    }

    @Override
    public Response<Boolean> statisticPig(Long orgId, Long farmId, Integer pigType) {
        try {
            Long pigCount = doctorPigDao.getPigCount(farmId, DoctorPig.PigSex.from(pigType));

            Integer pigCountInt = Integer.valueOf(pigCount.toString());

            DoctorPigTypeStatistic statistic = doctorPigTypeStatisticDao.findByFarmId(farmId);

            if(isNull(statistic)){
                statistic = DoctorPigTypeStatistic.builder().farmId(farmId).orgId(orgId).build();
                statistic.putPigTypeCount(pigType, pigCountInt);
                RespHelper.orServEx(createPigTypeStatistic(statistic));
            } else {
                statistic.putPigTypeCount(pigType, pigCountInt);
                RespHelper.orServEx(updatePigTypeStatistic(statistic));
            }
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("statistic pig failed, orgId:{}, farmId:{}, pigType:{}, cause:{}",
                    orgId, farmId, pigType, Throwables.getStackTraceAsString(e));
            return Response.fail("statistic.pig.fail");
        }
    }

    //拼接猪群统计
    private void setGroupStatistic(DoctorPigTypeStatistic statistic, DoctorGroupCount groupCount) {
        statistic.setFarrow((int) groupCount.getFarrowCount());
        statistic.setNursery((int) groupCount.getNurseryCount());
        statistic.setFatten((int) groupCount.getFattenCount());
        statistic.setHoubei((int) groupCount.getHoubeiCount());
    }

    private DoctorGroupCount getGroupCount(Long farmId) {
        DoctorGroupSearchDto searchDto = new DoctorGroupSearchDto();
        searchDto.setFarmId(farmId);
        searchDto.setStatus(DoctorGroup.Status.CREATED.getValue());

        List<DoctorGroupDetail> groupDetails = doctorGroupDao.findBySearchDto(searchDto).stream()
                .map(group -> new DoctorGroupDetail(group, doctorGroupTrackDao.findByGroupId(group.getId())))
                .collect(Collectors.toList());

        //过滤猪群类型, 然后按照类型分组
        Map<Integer, List<DoctorGroupDetail>> groupMap = groupDetails.stream()
                .filter(pt -> pt.getGroup().getPigType() != null)
                .collect(Collectors.groupingBy(gd -> gd.getGroup().getPigType()));

        List<DoctorGroupDetail> farrows = MoreObjects.firstNonNull(groupMap.get(PigType.DELIVER_SOW.getValue()), Lists.<DoctorGroupDetail>newArrayList());

        List<DoctorGroupDetail> nurseries = MoreObjects.firstNonNull(groupMap.get(PigType.NURSERY_PIGLET.getValue()), Lists.<DoctorGroupDetail>newArrayList());
        List<DoctorGroupDetail> fattens = MoreObjects.firstNonNull(groupMap.get(PigType.FATTEN_PIG.getValue()), Lists.<DoctorGroupDetail>newArrayList());
        List<DoctorGroupDetail> houbei = MoreObjects.firstNonNull(groupMap.get(PigType.RESERVE.getValue()), Lists.<DoctorGroupDetail>newArrayList());


        //根据猪类统计
        DoctorGroupCount count = new DoctorGroupCount();
        count.setFarmId(farmId);
        count.setFarrowCount(CountUtil.sumInt(farrows, g -> g.getGroupTrack().getQuantity()));
        count.setNurseryCount(CountUtil.sumInt(nurseries, g -> g.getGroupTrack().getQuantity()));
        count.setFattenCount(CountUtil.sumInt(fattens, g -> g.getGroupTrack().getQuantity()));
        count.setHoubeiCount(CountUtil.sumInt(houbei, g -> g.getGroupTrack().getQuantity()));
        return count;
    }
}
