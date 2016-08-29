package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigTypeStatisticDao;
import io.terminus.doctor.event.dto.DoctorGroupCount;
import io.terminus.doctor.event.enums.DataRange;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService;
    private final DoctorPigReadService doctorPigReadService;

    @Autowired
    public DoctorPigTypeStatisticWriteServiceImpl(DoctorPigTypeStatisticDao doctorPigTypeStatisticDao,
                                                  DoctorGroupReadService doctorGroupReadService,
                                                  DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService,
                                                  DoctorPigReadService doctorPigReadService) {
        this.doctorPigTypeStatisticDao = doctorPigTypeStatisticDao;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorPigTypeStatisticReadService = doctorPigTypeStatisticReadService;
        this.doctorPigReadService = doctorPigReadService;
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
            DoctorGroupCount groupCount = RespHelper.orServEx(doctorGroupReadService.countFarmGroups(orgId, farmId));
            DoctorPigTypeStatistic statistic = RespHelper.orServEx(doctorPigTypeStatisticReadService.findPigTypeStatisticByFarmId(farmId));

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
            Long pigCount = RespHelper.orServEx(doctorPigReadService.queryPigCount(DataRange.FARM.getKey(), farmId, pigType));

            Integer pigCountInt = Integer.valueOf(pigCount.toString());

            DoctorPigTypeStatistic statistic = RespHelper.orServEx(doctorPigTypeStatisticReadService.findPigTypeStatisticByFarmId(farmId));

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
        statistic.setHoubei((int) groupCount.getHoubeiSowCount() + (int) groupCount.getHoubeiBoarCount());
    }
}
