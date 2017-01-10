package io.terminus.doctor.event.service;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by yaoqijun.
 * Date:2016-05-26
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public class DoctorPigEventWriteServiceTest extends BaseServiceTest{

    @Autowired
    private DoctorPigEventWriteService doctorPigEventWriteService;

    @Autowired
    private DoctorPigEventDao doctorPigEventDao;

    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;

    @Autowired
    private DoctorPigSnapshotDao doctorPigSnapshotDao;

    @Autowired
    private DoctorPigDao doctorPigDao;

    /**
     * 测试对应的回滚方式
     */
    @Test
    public void testRollBackService(){

    }

    @Test
    public void testEventWriteService(){

    }

    @Test
    public void testPigEntryEvent(){

//        Response<Long> response = doctorPigEventWriteService.pigEntryEvent(buildEntryInputInfo(),buildFarmEntryDto(),DoctorPig.PigSex.SOW.getKey());
//
////        printInfo(doctorPigDao.findById(6l));
//
//        // validate table
//        printInfo(doctorPigEventDao.findById(response.getResult()));
//
//        // pig track
//        printInfo(doctorPigTrackDao.findByPigId(1l));
//
//        // snap shot
//        printInfo(doctorPigSnapshotDao.queryByEventId(response.getResult()));

    }



//    private DoctorFarmEntryDto buildFarmEntryDto(){
//        DoctorFarmEntryDto doctorFarmEntryDto = DoctorFarmEntryDto.builder()
//                .pigCode("pigcode").birthday(new Date()).inFarmDate(new Date()).barnId(1l).barnName("barnName")
//                .source(1).breed(1l).breedName("breedNAme").breedType(1l).breedTypeName("typeName")
//                .fatherId(1l).motherId(1l).mark("mark").earCode("earCoe").parity(100).left(1).right(2)
//                .build();
//        return doctorFarmEntryDto;
//    }

    public DoctorBasicInputInfoDto buildEntryInputInfo(){
        return DoctorBasicInputInfoDto.builder()
                .farmId(1l).farmName("farmName").orgId(1l).orgName("orgName")
                .staffId(1l).staffName("staffName")
                //.relEventId(66666l)
                .build();
    }

    public DoctorBasicInputInfoDto buildBasicInputInfo(){

        return DoctorBasicInputInfoDto.builder()
                .farmId(1l).farmName("farmName").orgId(1l).orgName("orgName")
                .staffId(1l).staffName("staffName").eventType(1).eventName("eventName").eventDesc("eventDesc")
                //.relEventId(66666l)
                .build();
    }

    private <T> void printInfo(T t){
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(t));
    }

}
