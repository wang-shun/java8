package io.terminus.doctor.event.listener;

import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.event.dao.reportBi.DoctorReportBoarDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportDeliverDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportFattenDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportMatePregDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportNurseryDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportReverseDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportSowDao;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorPigDaily;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by xjn on 18/1/9.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorReportBiListener implements EventListener{

    private final DoctorReportBoarDao doctorReportBoarDao;
    private final DoctorReportDeliverDao doctorReportDeliverDao;
    private final DoctorReportFattenDao doctorReportFattenDao;
    private final DoctorReportMatePregDao doctorReportMatePregDao;
    private final DoctorReportNurseryDao doctorReportNurseryDao;
    private final DoctorReportReverseDao doctorReportReverseDao;
    private final DoctorReportSowDao doctorReportSowDao;

    @Autowired
    public DoctorReportBiListener(DoctorReportBoarDao doctorReportBoarDao, DoctorReportDeliverDao doctorReportDeliverDao, DoctorReportFattenDao doctorReportFattenDao, DoctorReportMatePregDao doctorReportMatePregDao, DoctorReportNurseryDao doctorReportNurseryDao, DoctorReportReverseDao doctorReportReverseDao, DoctorReportSowDao doctorReportSowDao) {
        this.doctorReportBoarDao = doctorReportBoarDao;
        this.doctorReportDeliverDao = doctorReportDeliverDao;
        this.doctorReportFattenDao = doctorReportFattenDao;
        this.doctorReportMatePregDao = doctorReportMatePregDao;
        this.doctorReportNurseryDao = doctorReportNurseryDao;
        this.doctorReportReverseDao = doctorReportReverseDao;
        this.doctorReportSowDao = doctorReportSowDao;
    }

    /**
     * 同步猪报表数据到bi
     * @param pigDaily 猪日报表
     */
    @Subscribe
    public void synchronizePigData(DoctorPigDaily pigDaily) {
        
    }

    /**
     * 同步猪群报表数据到bi
     * @param groupDaily 猪群日报表
     */
    @Subscribe
    public void synchronizeGroupData(DoctorGroupDaily groupDaily){
        if (Objects.equals(groupDaily.getPigType(), PigType.NURSERY_PIGLET.getValue())) {
            // TODO: 18/1/9  
        } else if (Objects.equals(groupDaily.getPigType(), PigType.FATTEN_PIG.getValue())) {
            // TODO: 18/1/9  
        } else {
            // TODO: 18/1/9  
        }
    }

    /**
     * 同步特殊指标数据到bi
     */
    @Subscribe
    public void synchronizeSpecificData(){}
}
