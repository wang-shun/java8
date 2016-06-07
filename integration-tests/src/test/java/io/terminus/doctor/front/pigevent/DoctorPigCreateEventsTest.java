package io.terminus.doctor.front.pigevent;

import com.google.api.client.util.Lists;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.enums.MatingType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.handler.sow.DoctorSowChgLocationHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.workflow.core.WorkFlowService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import utils.HttpPostRequest;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-06-06
 * Email:yaoqj@terminus.io
 * Descirbe: 测试对应的创建事件录入
 */
@SpringApplicationConfiguration(FrontWebConfiguration.class)
public class DoctorPigCreateEventsTest extends BaseFrontWebTest{

    private String basicUrl = null;

    @Autowired
    private WorkFlowService workFlowService;

    @Autowired
    private DoctorPigDao doctorPigDao;

    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;

    @Autowired
    private DoctorPigSnapshotDao doctorPigSnapshotDao;

    @Autowired
    private DoctorPigEventDao doctorPigEventDao;

    @Autowired
    private DoctorSowChgLocationHandler doctorSowChgLocationHandler;

    @Before
    public void before() throws Exception{
        basicUrl = "http://localhost:" + this.port + "/api/doctor/events/create";

        // init node xml
        File f = new File(getClass().getResource("/flow/sow.xml").getFile());
        workFlowService.getFlowDefinitionService().deploy(new FileInputStream(f));
   }

    /**
     * 测试母猪进厂事件信息
     */
    @Test
    public void sowEntryEventCreateTest(){

        Long pigId = sowEntryEventCreate();

        sowMatingEventCreate(pigId);

        testToPregEventCreate(pigId);

        // 显示 state
        printCurrentState();

        // 获取下一个事件信息
        testCurrentSowInputStatus(pigId);
    }

    /**
     * 去妊娠舍
     */
    private void testToPregEventCreate(Long pigId){
        String url = basicUrl + "/createSowEvent";
        HttpEntity httpEntity = HttpPostRequest.formRequest().param("farmId", 12345l)
                .param("pigId", pigId).param("eventType", PigEvent.TO_PREG.getKey())
                .param("sowInfoDtoJson", JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(DoctorChgLocationDto.builder()
                        .changeLocationDate(new Date()).chgLocationFromBarnId(5l).chgLocationFromBarnName("fromBarnName")
                        .chgLocationToBarnId(6l).chgLocationToBarnName("toBarnName")
                        .build()))
                .httpEntity();
        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);
    }

    /**
     * 当前 母猪 可录入事件信息
     */
    private void testCurrentSowInputStatus(Long pigId){
        String url = "http://localhost:" + this.port + "/api/doctor/events/pig/queryPigEvents";

        List<Long> ids = Lists.newArrayList();
        ids.add(pigId);
        HttpEntity httpEntity = HttpPostRequest.bodyRequest().params(ids);

        ResponseEntity entity = this.restTemplate.postForEntity(url, httpEntity, Object.class);
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(entity.getBody()));
    }

    /**
     * 母猪配种事件测试
     * @param pigId
     */
    private void sowMatingEventCreate(Long pigId){
        String url = basicUrl + "/createSowEvent";
        HttpEntity httpEntity = HttpPostRequest.formRequest().param("farmId", 12345l)
                .param("pigId", pigId).param("eventType", PigEvent.MATING.getKey())
                .param("sowInfoDtoJson", JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(DoctorMatingDto.builder()
                        .matingDate(new Date()).judgePregDate(DateTime.now().plusDays(100).toDate()).matingType(MatingType.MANUAL.getKey())
                        .matingStaff("staff").mattingMark("matingMark")
                        .build())).httpEntity();

        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);

    }

    private Long sowEntryEventCreate(){
        String url = basicUrl + "/createEntryInfo";
        DoctorFarmEntryDto doctorFarmEntryDto = buildFarmEntryDto();
        doctorFarmEntryDto.setPigType(DoctorPig.PIG_TYPE.SOW.getKey());

        HttpEntity httpEntity = HttpPostRequest.formRequest().param("farmId", 12345l).
                param("doctorFarmEntryJson",
                        JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorFarmEntryDto))
                .httpEntity();
        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        return result;
    }

    /**
     * 打印当前的信息
     */
    private void printCurrentState(){
        System.out.println("doctor pig ******************************************************");
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigDao.listAll()));
        System.out.println("doctor pig track******************************************************");
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigTrackDao.listAll()));
        System.out.println("doctor pig snap shot content******************************************************");
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigSnapshotDao.listAll()));
        System.out.println("doctor pig event dao ******************************************************");
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigEventDao.listAll()));
        return;
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
