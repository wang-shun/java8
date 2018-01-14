package io.terminus.doctor.event.reportBi;

import com.google.common.collect.Lists;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportReserveDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorGroupDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.reportBi.synchronizer.DoctorReserveSynchronizer;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static java.util.Objects.isNull;

/**
 * Created by xjn on 18/1/9.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorReportBiDataSynchronize {
    private final DoctorPigDailyDao doctorPigDailyDao;
    private final DoctorGroupDailyDao doctorGroupDailyDao;
    private final DoctorReserveSynchronizer reserveSynchronizer;
    private final DoctorReportReserveDao doctorReportReserveDao;

    private final Integer DELTA_INTERVAL = 1440;
    private final Integer REAL_TIME_INTERVAL = 1000000;

    @Autowired
    public DoctorReportBiDataSynchronize(DoctorPigDailyDao doctorPigDailyDao,
                                         DoctorGroupDailyDao doctorGroupDailyDao,
                                         DoctorReserveSynchronizer reserveSynchronizer,
                                         DoctorReportReserveDao doctorReportReserveDao) {
        this.doctorPigDailyDao = doctorPigDailyDao;
        this.doctorGroupDailyDao = doctorGroupDailyDao;
        this.reserveSynchronizer = reserveSynchronizer;
        this.doctorReportReserveDao = doctorReportReserveDao;
    }

    /**
     * 全量同步数据
     */
    public void synchronizeFullBiData() {
        cleanFullBiData();
        DoctorDimensionCriteria dimensionCriteria = new DoctorDimensionCriteria();
        synchronizeBiDataImpl(dimensionCriteria);
    }

    /**
     * 增量同步数据
     */
    public void synchronizeDeltaBiData(){
        DoctorDimensionCriteria dimensionCriteria = new DoctorDimensionCriteria();
        dimensionCriteria.setSumAt(DateTime.now().minusMinutes(DELTA_INTERVAL).toDate());
        synchronizeBiDataImpl(dimensionCriteria);
    }

    /**
     * 同步实时数据
     */
    public void synchronizeRealTimeBiData(){
        Date date = DateTime.now().minusMinutes(REAL_TIME_INTERVAL).toDate();
        List<DoctorGroupDaily> groupDailyList = doctorGroupDailyDao.findByAfter(date);
        if (Arguments.isNullOrEmpty(groupDailyList)) {
            return;
        }
        synchronizeForDay(groupDailyList);
        List<DoctorDimensionCriteria> dimensionCriteriaList = Lists.newArrayList();
        dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.WEEK.getValue(), OrzDimension.FARM.getValue()));
        dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.MONTH.getValue(), OrzDimension.FARM.getValue()));
        dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.QUARTER.getValue(), OrzDimension.FARM.getValue()));
        dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.YEAR.getValue(), OrzDimension.FARM.getValue()));
        dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.WEEK.getValue(), OrzDimension.ORG.getValue()));
        dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.MONTH.getValue(), OrzDimension.ORG.getValue()));
        dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.QUARTER.getValue(), OrzDimension.ORG.getValue()));
        dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.YEAR.getValue(), OrzDimension.ORG.getValue()));
        List<DoctorDimensionCriteria> orgList = groupDailyList.stream().map(groupDaily ->
            new DoctorDimensionCriteria(groupDaily.getOrgId(), OrzDimension.ORG.getValue(), groupDaily.getSumAt(), DateDimension.DAY.getValue(), groupDaily.getPigType())
        ).collect(Collectors.toList());
        dimensionCriteriaList.addAll(orgList);
        dimensionCriteriaList.parallelStream().forEach(dimensionCriteria ->
                synchronizeGroupBiData(doctorGroupDailyDao.selectOneSumForDimension(dimensionCriteria), dimensionCriteria));
        // TODO: 18/1/13 pigdaily
    }

    private void synchronizeForDay(List<DoctorGroupDaily> groupDailyList) {
        if (Arguments.isNullOrEmpty(groupDailyList)) {
            return;
        }
        DoctorGroupDailyExtend extend = new DoctorGroupDailyExtend();
        DoctorDimensionCriteria doctorDimensionCriteria = new DoctorDimensionCriteria();
        doctorDimensionCriteria.setOrzType(OrzDimension.FARM.getValue());
        doctorDimensionCriteria.setDateType(DateDimension.DAY.getValue());
        groupDailyList.forEach(groupDaily -> {
            BeanMapper.copy(groupDaily, extend);
            extend.setDailyLivestockOnHand(groupDaily.getEnd().doubleValue());
            synchronizeGroupBiData(extend, doctorDimensionCriteria);
        });
    }
    private List<DoctorDimensionCriteria> necessarySynchronizeDimension(Date date) {

        return null;
    }

    /**
     * 清空bi相关表的所有数据
     */
    private void cleanFullBiData(){

    }

    private void synchronizeBiDataImpl(DoctorDimensionCriteria dimensionCriteria) {
        //同步猪场日
        synchronizeFullBiDataForDay();
        //同步猪场周
        dimensionCriteria.setOrzType(OrzDimension.FARM.getValue());
        dimensionCriteria.setDateType(DateDimension.WEEK.getValue());
        synchronizeFullBiDataForDimension(dimensionCriteria);
//        //同步猪场月
//        dimensionCriteria.setOrzType(OrzDimension.FARM.getValue());
//        dimensionCriteria.setDateType(DateDimension.MONTH.getValue());
//        synchronizeFullBiDataForDimension(dimensionCriteria);
//        //同步猪场季
//        dimensionCriteria.setOrzType(OrzDimension.FARM.getValue());
//        dimensionCriteria.setDateType(DateDimension.QUARTER.getValue());
//        synchronizeFullBiDataForDimension(dimensionCriteria);
//        //同步猪场年
//        dimensionCriteria.setOrzType(OrzDimension.FARM.getValue());
//        dimensionCriteria.setDateType(DateDimension.YEAR.getValue());
//        synchronizeFullBiDataForDimension(dimensionCriteria);
//        //同步公司日
        dimensionCriteria.setOrzType(OrzDimension.ORG.getValue());
        dimensionCriteria.setDateType(DateDimension.DAY.getValue());
        synchronizeFullBiDataForDimension(dimensionCriteria);
        //同步公司周
        dimensionCriteria.setOrzType(OrzDimension.ORG.getValue());
        dimensionCriteria.setDateType(DateDimension.WEEK.getValue());
        synchronizeFullBiDataForDimension(dimensionCriteria);
//        //同步公司月
//        dimensionCriteria.setOrzType(OrzDimension.ORG.getValue());
//        dimensionCriteria.setDateType(DateDimension.MONTH.getValue());
//        synchronizeFullBiDataForDimension(dimensionCriteria);
//        //同步公司季
//        dimensionCriteria.setOrzType(OrzDimension.ORG.getValue());
//        dimensionCriteria.setDateType(DateDimension.QUARTER.getValue());
//        synchronizeFullBiDataForDimension(dimensionCriteria);
//        //同步公司年
//        dimensionCriteria.setOrzType(OrzDimension.ORG.getValue());
//        dimensionCriteria.setDateType(DateDimension.YEAR.getValue());
//        synchronizeFullBiDataForDimension(dimensionCriteria);
    }

    /**
     * 全量同步不同维度的数据
     * @param dimensionCriteria
     */
    private void synchronizeFullBiDataForDimension(DoctorDimensionCriteria dimensionCriteria) {
        List<DoctorGroupDailyExtend> groupDailyList = doctorGroupDailyDao.sumForDimension(dimensionCriteria);
        groupDailyList.parallelStream().forEach(groupDaily -> synchronizeGroupBiData(groupDaily, dimensionCriteria));
//        List<DoctorPigDaily> pigDailyList = doctorPigDailyDao.sumForDimension(dimensionCriteria);
//        pigDailyList.parallelStream().forEach(this::synchronizePigBiData);
    }

    private void synchronizeFullBiDataForDay() {
        List<DoctorGroupDaily> groupDailyList;
        Integer pageSize = 5000;
        Integer pageNo = 0;
        while (true) {
            groupDailyList = doctorGroupDailyDao.paging(pageNo, pageSize).getData();
            synchronizeForDay(groupDailyList);
            if (groupDailyList.size() < 5000) break;
            pageNo += 5000;
        }
    }

    private void synchronizeGroupBiData(DoctorGroupDailyExtend groupDaily, DoctorDimensionCriteria dimensionCriteria) {
        PigType pigType = expectNotNull(PigType.from(groupDaily.getPigType()), "pigType.is.illegal");
        if (isNull(dimensionCriteria.getOrzId())) {
            if (Objects.equals(dimensionCriteria.getOrzType(), OrzDimension.ORG.getValue())) {
                dimensionCriteria.setOrzId(groupDaily.getOrgId());
            } else {
                dimensionCriteria.setOrzId(groupDaily.getFarmId());
            }
        }
        dimensionCriteria.setSumAt(groupDaily.getSumAt());
        dimensionCriteria.setPigType(groupDaily.getPigType());
        groupDaily.setStart(doctorGroupDailyDao.start(dimensionCriteria));
        groupDaily.setEnd(doctorGroupDailyDao.end(dimensionCriteria));
        switch (pigType) {
            case DELIVER_SOW:
                ;
                break;
            case NURSERY_PIGLET:
                ;
                break;
            case FATTEN_PIG:
                ;
                break;
            case RESERVE:
                reserveSynchronizer.synchronize(groupDaily, dimensionCriteria);
                break;
        }
    }

    private void synchronizePigBiData(DoctorPigDaily pigDaily){

    }
}
