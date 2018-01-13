package io.terminus.doctor.event.reportBi;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportReserveDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorGroupDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorReportReserve;
import io.terminus.doctor.event.reportBi.synchronizer.DoctorReserveSynchronizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.terminus.doctor.common.utils.Checks.expectNotNull;

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

    }

    /**
     * 同步实时数据
     */
    public void synchronizeRealTimeBiData(){

    }

    /**
     * 同步延时数据
     */
    public void synchronizeDelayBiData(){

    }

    /**
     * 清空bi相关表的所有数据
     */
    private void cleanFullBiData(){

    }

    private void synchronizeBiDataImpl(DoctorDimensionCriteria dimensionCriteria) {
        //同步猪场日
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

    private void synchronizeGroupBiData(DoctorGroupDailyExtend groupDaily, DoctorDimensionCriteria dimensionCriteria){
        OrzDimension orzDimension = expectNotNull(OrzDimension.from(dimensionCriteria.getOrzType()), "orzType.is.illegal");
        DateDimension dateDimension = expectNotNull(DateDimension.from(dimensionCriteria.getDateType()), "dateType.is.illegal");
        PigType pigType = expectNotNull(PigType.from(groupDaily.getPigType()), "pigType.is.illegal");
        
        
        switch (pigType) {
            case DELIVER_SOW: ; break;
            case NURSERY_PIGLET: ; break;
            case FATTEN_PIG: ; break;
            case RESERVE:
                DoctorReportReserve reserve = new DoctorReportReserve();
                reserve.setOrzType(orzDimension.getName());
                reserve.setDateType(dateDimension.getName());
                groupDaily.setStart(11);
                groupDaily.setEnd(100);
                reserveSynchronizer.synchronize(groupDaily, reserve); break;
        }
    }

    private void synchronizePigBiData(DoctorPigDaily pigDaily){

    }
}
