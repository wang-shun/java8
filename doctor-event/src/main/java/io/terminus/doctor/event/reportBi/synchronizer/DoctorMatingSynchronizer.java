package io.terminus.doctor.event.reportBi.synchronizer;

import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.event.dao.reportBi.DoctorReportMatingDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorFiledUrlCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorPigDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorReportMating;
import io.terminus.doctor.event.reportBi.helper.FieldHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

import static io.terminus.doctor.event.reportBi.helper.DateHelper.dateCN;
import static io.terminus.doctor.event.reportBi.helper.DateHelper.withDateStartDay;
import static java.util.Objects.isNull;

/**
 * Created by xjn on 18/1/13.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorMatingSynchronizer {

    private final DoctorReportMatingDao doctorReportMatingDao;
    private final FieldHelper fieldHelper;

    @Autowired
    public DoctorMatingSynchronizer(DoctorReportMatingDao doctorReportMatingDao, FieldHelper fieldHelper) {
        this.doctorReportMatingDao = doctorReportMatingDao;
        this.fieldHelper = fieldHelper;
    }

    public void synchronize(DoctorPigDailyExtend pigDaily,
                            DoctorDimensionCriteria dimensionCriteria){
        DoctorReportMating reportBI;
        if (isNull(dimensionCriteria.getSumAt()) || isNull(reportBI = doctorReportMatingDao.findByDimension(dimensionCriteria))) {
            reportBI= new DoctorReportMating();
            reportBI.setOrzType(dimensionCriteria.getOrzType());
            reportBI.setDateType(dimensionCriteria.getDateType());
        }
        insertOrUpdate(build(pigDaily, reportBI, dimensionCriteria));
    }

    private void insertOrUpdate(DoctorReportMating reportBi){
        if (isNull(reportBi.getId())) {
            doctorReportMatingDao.create(reportBi);
            return;
        }
        doctorReportMatingDao.update(reportBi);
    }

    public DoctorReportMating build(DoctorPigDailyExtend pigDaily, DoctorReportMating reportBi, DoctorDimensionCriteria dimensionCriteria) {
        Integer isRealTime = dimensionCriteria.getIsRealTime();
        if (Objects.equals(reportBi.getOrzType(), OrzDimension.FARM.getValue())) {
            reportBi.setOrzId(pigDaily.getFarmId());
            reportBi.setOrzName(pigDaily.getFarmName());
        } else {
            reportBi.setOrzId(pigDaily.getOrgId());
            reportBi.setOrzName(pigDaily.getOrgName());
        }
        DateDimension dateDimension = DateDimension.from(reportBi.getDateType());
        reportBi.setSumAt(withDateStartDay(pigDaily.getSumAt(), dateDimension));
        reportBi.setSumAtName(dateCN(pigDaily.getSumAt(), dateDimension));
        buildRealTime(pigDaily, reportBi);
        if (!Objects.equals(isRealTime, IsOrNot.YES.getKey())) {
            buildDelay(pigDaily, reportBi,dimensionCriteria);
        }
        return reportBi;
    }

    private void buildRealTime(DoctorPigDailyExtend pigDaily, DoctorReportMating reportBi) {
        DoctorFiledUrlCriteria filedUrlCriteria = new DoctorFiledUrlCriteria();
        fieldHelper.fillPigFiledUrl(filedUrlCriteria, pigDaily, reportBi.getOrzType(), reportBi.getDateType());

        reportBi.setStart(pigDaily.getSowPhStart());
        reportBi.setHoubeiIn(pigDaily.getSowPhReserveIn());
        reportBi.setSowPhWeanIn(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getSowPhWeanIn(), "sowPhWeanIn"));
        reportBi.setOtherIn(otherIn(pigDaily, reportBi.getOrzType()));
        reportBi.setDead(pigDaily.getSowPhDead());
        reportBi.setWeedOut(pigDaily.getSowPhWeedOut());
        reportBi.setSale(pigDaily.getSowPhSale());
        reportBi.setChgFarmOut(pigDaily.getSowPhChgFarm());
        reportBi.setOtherChange(pigDaily.getSowPhOtherOut());
        reportBi.setMatingCount(pigDaily.getMatingCount());
        reportBi.setMatingSowCount(pigDaily.getSowPhMating());
        reportBi.setPregnancySowCount(pigDaily.getSowPhPregnant());
        reportBi.setNoPregnancySowCount(pigDaily.getSowPhKonghuai());
        reportBi.setPregPositive(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getPregPositive(), "pregPositive"));
        reportBi.setPregNegative(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getPregNegative(), "pregNegative"));
        reportBi.setPregFanqing(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getPregFanqing(), "pregFanqing"));
        reportBi.setPregLiuchan(fieldHelper.filedUrl(filedUrlCriteria, pigDaily.getPregLiuchan(), "pregLiuchan"));
        reportBi.setEnd(pigDaily.getSowPhEnd());
    }

    private Integer otherIn(DoctorPigDailyExtend pigDaily, Integer orzType) {
        Integer otherIn = pigDaily.getSowPhEntryIn() - pigDaily.getSowPhReserveIn();
        if (Objects.equals(orzType, OrzDimension.FARM.getValue())) {
            otherIn = otherIn + pigDaily.getSowPhChgFarmIn();
        }
        return otherIn;
    }

    private void buildDelay(DoctorPigDailyExtend pigDaily, DoctorReportMating reportBi,DoctorDimensionCriteria dimensionCriteria) {
        if (DateDimension.YEARLY.contains(reportBi.getDateType())) {
            reportBi.setMatingRate(matingRate(pigDaily,dimensionCriteria));
        }
    }

    private Double matingRate(DoctorPigDailyExtend dailyExtend,DoctorDimensionCriteria dimensionCriteria) {
        Date startAt = dimensionCriteria.getStartAt();
        Date endAt = dimensionCriteria.getEndAt();
        Integer weanMateCount = doctorReportMatingDao.getWeanMateCount(dailyExtend.getFarmId(),startAt,endAt,1);
        Integer wwanCount = doctorReportMatingDao.getWeanMateCount(dailyExtend.getFarmId(),startAt,endAt,0);
        //断奶母猪7日配种数-断奶母猪7日死淘数/断奶窝数-断奶母猪7日死淘数
        //return FieldHelper.get((dailyExtend.getWeanMate()-dailyExtend.getWeanDeadWeedOut()), (dailyExtend.getWeanNest() - dailyExtend.getWeanDeadWeedOut()));
        return FieldHelper.get(weanMateCount, wwanCount);
    }

    public void deleteAll() {
        doctorReportMatingDao.deleteAll();
    }
}
