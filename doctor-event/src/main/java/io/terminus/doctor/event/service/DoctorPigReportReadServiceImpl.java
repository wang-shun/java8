package io.terminus.doctor.event.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.DoctorPigReport;
import io.terminus.doctor.event.model.DoctorPigDaily;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/12/13.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorPigReportReadServiceImpl implements DoctorPigReportReadService {

    @Autowired
    private DoctorPigDailyDao doctorPigDailyDao;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;

    @Override
    public DoctorPigReport farmReport(Long farmId, Date start, Date end, ReportTime reportTime) {

        DoctorPigReport doctorPigReport = new DoctorPigReport();
        if (reportTime == ReportTime.DAY) {
            if (!DateUtils.isSameDay(start, end)) {
                throw new ServiceException("start.day.not.equals.end.day");
            }
            DoctorPigDaily pigDaily = doctorPigDailyDao.findByFarm(farmId, start, end);
            BeanUtils.copyProperties(pigDaily, doctorPigReport);


            //母猪区=配怀+产房
            //期初
            Integer sowStart = (pigDaily.getSowCfStart() + pigDaily.getSowPhStart());
            //转入=后备进场+后备转入+配怀转场转入+产房转场转入
            Integer sowTransferIn = pigDaily.getSowPhReserveIn() + pigDaily.getSowPhEntryIn() + pigDaily.getSowPhChgFarmIn() + pigDaily.getSowCfInFarmIn();
            //母猪死亡
            Integer sowDead = pigDaily.getSowCfDead() + pigDaily.getSowPhDead();
            //母猪淘汰
            Integer sowWeedOut = pigDaily.getSowCfWeedOut() + pigDaily.getSowPhWeedOut();
            //死淘率=死亡+淘汰/期初+转入
            doctorPigReport.setBasicSowDeadAndWeedOutRate((sowDead + sowWeedOut) / (sowStart + sowTransferIn) * 100);

            //转入到配怀的量+装入到产房的量
            doctorPigReport.setSowOtherIn(pigDaily.getSowCfIn() + pigDaily.getSowPhReserveIn());

            doctorPigReport.setSowEnd(pigDaily.getSowCfEnd() + pigDaily.getSowPhEnd());
            doctorPigReport.setSowDailyQuantity(doctorPigReport.getSowEnd() / 1);

            //前推114天配种数量
            Date before = DateUtils.addDays(start, -114);
            DoctorPigDaily beforePigDaily = doctorPigDailyDao.findByFarm(farmId, before, before);
            doctorPigReport.setEarlyMating(null == beforePigDaily ? 0 : beforePigDaily.getMatingCount());

            //todo 前期分娩窝数
//            doctorPigReport.setEarlyFarrowNest();
            doctorPigReport.setEarlyFarrowRate(pigDaily.getFarrowNest() / doctorPigReport.getEarlyMating() * 100);
            Date after = DateUtils.addDays(start, 114);
            DoctorPigDaily afterPigDaily = doctorPigDailyDao.findByFarm(farmId, after, after);
            doctorPigReport.setLateFarrowNest(null == afterPigDaily ? 0 : afterPigDaily.getFarrowNest());

            doctorPigReport.setLateFarrowRate(doctorPigReport.getLateFarrowNest() / pigDaily.getMatingCount() * 100);
            //产仔总数=尖子数+弱子数+
            Integer farrowAll = pigDaily.getFarrowHealth() + pigDaily.getFarrowWeak() + pigDaily.getFarrowjmh() + pigDaily.getFarrowDead();
            doctorPigReport.setAvgFarrow(farrowAll / pigDaily.getFarrowNest() * 100);
            //产仔活子数=
            //TODO 活子数
            Integer farrowLive = 0;
            doctorPigReport.setAvgFarrowLive(farrowLive / pigDaily.getFarrowNest() * 100);
            doctorPigReport.setAvgFarrowHealth(pigDaily.getFarrowHealth() / pigDaily.getFarrowNest() * 100);
            doctorPigReport.setAvgFarrowWeak(pigDaily.getFarrowWeak() / pigDaily.getFarrowNest() * 100);
            doctorPigReport.setAvgWeight(pigDaily.getWeight() / pigDaily.getFarrowNest() * 100);
            doctorPigReport.setFirstWeight(pigDaily.getWeight() / farrowLive * 100);


        } else if (reportTime == ReportTime.WEEK) {

        } else if (reportTime == ReportTime.MONTH) {

        } else if (reportTime == ReportTime.SEASON) {

        } else if (reportTime == ReportTime.YEAR) {

        }

        return doctorPigReport;
    }

    @Override
    public DoctorPigReport companyReport(Long orgId, Date start, Date end, ReportTime reportTime) {

        //TODO 转场转入无意义
        return null;
    }

    @Override
    public DoctorPigReport orgReport(Long orgId, Date start, Date end, ReportTime reportTime) {
        return null;
    }


//    public DoctorPigReport count(List<DoctorPigDaily> pigDailies) {
//            pigDailies.stream().flatMap()
//    }
}
