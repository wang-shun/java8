package io.terminus.doctor.front.pigevent;

import configuration.front.FrontWebConfiguration;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.front.BaseFrontWebTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;

import java.util.Date;

/**
 * Created by yaoqijun.
 * Date:2016-06-06
 * Email:yaoqj@terminus.io
 * Descirbe: 测试对应的创建事件录入
 */
@SpringApplicationConfiguration(FrontWebConfiguration.class)
public class DoctorPigCreateEventsTest extends BaseFrontWebTest{

    private String basicUrl = null;

    @Before
    public void before(){
        basicUrl = "http://localhost:" + this.port + "/api/doctor/events/create";
    }

    /**
     * 测试母猪进厂事件信息
     */
    @Test
    public void sowEntryEventCreateTest(){
        String url = basicUrl + "/createEntryInfo";
//
//        DoctorFarmEntryDto doctorFarmEntryDto = buildFarmEntryDto();
//        doctorFarmEntryDto.setPigType(DoctorPig.PIG_TYPE.SOW.getKey());
//
//        HttpEntity httpEntity = HttpPostRequest.formRequest().param("farmId", 12345l).param("doctorFarmEntry",buildFarmEntryDto()).httpEntity();
//        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
//        System.out.println(result);
    }

    /**
     * 构建Pig 进厂信息 pigType 不构建
     * @return
     */
    private DoctorFarmEntryDto buildFarmEntryDto(){

        return DoctorFarmEntryDto.builder()
                .pigCode("pigCode").birthday(new Date()).inFarmDate(new Date()).barnId(1l).barnName("barnName")
                .source(PigSource.LOCAL.getKey()).breed(1l).breedName("breedName").breedType(1l).breedTypeName("breedTypeName")
                .fatherCode("fatherCode").motherCode("motherCode").entryMark("entryMark")
                .boarTypeId(BoarEntryType.HGZ.getKey()).boarTypeName(BoarEntryType.HGZ.getDesc())
                .earCode("earCode").parity(1).left(100).right(100)
                .build();

    }
}
