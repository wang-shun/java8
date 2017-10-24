package io.terminus.doctor.event.concurrent;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.enums.MatingType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.helper.DoctorConcurrentControl;
import io.terminus.doctor.event.manager.DoctorDailyReportManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by xjn on 17/10/24.
 */
public class DoctorConcurrentTest extends BaseServiceTest {
    @Autowired
    private DoctorDailyReportManager manager;
    @Autowired
    private DoctorDailyReportDao doctorDailyReportDao;
    @Autowired
    private DoctorConcurrentControl doctorConcurrentControl;
    @Autowired
    private DoctorPigEventManager doctorPigEventManager;


    @Test
    public void dailyReportConcurrentTest() throws InterruptedException {
        DoctorDailyReport dailyReport = doctorDailyReportDao.findById(1);
        for (int i = 0; i < 2; i++) {
            new Thread(()-> manager.createOrUpdateDailyPig(dailyReport)).start();
        }
        Thread.sleep(2000);
    }

    @Test
    public void setNxTest() throws InterruptedException {
        System.out.println(doctorConcurrentControl.setKey("9240"));
        System.out.println(doctorConcurrentControl.setKey("9329"));
    }

    @Test
    public void eventConcurrentTest() throws InterruptedException {
        DoctorMatingDto matingDto = DoctorMatingDto.builder()
                .matingBoarPigCode("D8718")
                .matingBoarPigId(8729L)
                .matingDate(DateUtil.toDate("2017-10-20"))
                .matingType(MatingType.MANUAL.getKey())
                .judgePregDate(new Date())
                .build();
        matingDto.setPigId(9240L);
        matingDto.setPigCode("LY320");
        matingDto.setEventType(PigEvent.MATING.getKey());
        matingDto.setEventName(PigEvent.MATING.getName());
        matingDto.setBarnId(21L);
        matingDto.setBarnType(5);
        matingDto.setBarnName("空怀3D1");
        DoctorBasicInputInfoDto inputInfoDto = DoctorBasicInputInfoDto.builder()
                .orgId(1L)
                .orgName("灵宝融利实业有限公司")
                .farmId(1L)
                .farmName("融利实业种猪场")
                .staffId(10L)
                .staffName("xrnm")
                .build();

        DoctorMatingDto matingDto1 = BeanMapper.map(matingDto, DoctorMatingDto.class);
        DoctorMatingDto matingDto2 = BeanMapper.map(matingDto, DoctorMatingDto.class);
        for (int i = 0; i < 3; i++) {
            if (i == 0) {
                new Thread(() -> {
                    doctorPigEventManager.eventHandle(matingDto, inputInfoDto);
                }).start();
            }
            if (i== 1) {
                matingDto1.setPigId(9329L);
                new Thread(() ->{
                    doctorPigEventManager.eventHandle(matingDto1, inputInfoDto);
                }).start();
            }
            if (i== 2) {
                matingDto2.setPigId(10506L);
                matingDto1.setPigId(9329L);
                new Thread(() ->{
                    doctorPigEventManager.eventHandle(matingDto2, inputInfoDto);
                }).start();
            }

        }
        Thread.sleep(10000);
    }

}
