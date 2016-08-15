package io.terminus.doctor.front.pigevent;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.dto.event.sow.DoctorAbortionDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPartWeanDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorDiseaseDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.dto.event.usual.DoctorVaccinationDto;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.enums.FarrowingType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.MatingType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.utils.HttpGetRequest;
import io.terminus.doctor.utils.HttpPostRequest;
import io.terminus.doctor.workflow.core.WorkFlowService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by yaoqijun.
 * Date:2016-06-06
 * Email:yaoqj@terminus.io
 * Descirbe: 测试对应的创建事件录入
 */
@SpringApplicationConfiguration(FrontWebConfiguration.class)
public class DoctorPigCreateEventsTest extends BaseFrontWebTest {

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

//    @Autowired
//    private Subscriber subscriber;

    @Before
    public void before() throws Exception {
        basicUrl = "http://localhost:" + this.port + "/api/doctor/events/create";

        // init node xml
        File f = new File(getClass().getResource("/flow/sow.xml").getFile());
        workFlowService.getFlowDefinitionService().deploy(new FileInputStream(f));
    }

    /**
     * 测试创建猪信息
     */
    @Test
    public void testCreateMockData() {
        // 公猪信息创建方式
//        for(int i=0; i<10; i++){
//            boarEntryEventCreate();
//        }

        Long pigBoarId = boarEntryEventCreate();

        // 母猪信息创建方式
        for (int i = 0; i < 1; i++) {
            Long pigId = sowEntryEventCreate();
            sowMatingEventCreate(pigId, pigBoarId); // 配种事件信息录入

            testPregCheckResultEventCreate(pigId, PregCheckResult.YANG);

            testToPregEventCreate(pigId);

//            testAbortionEventCreate(pigId);

            testToFarrowing(pigId);

            // 测试 分娩数量200
            testFarrowingEventCreate(pigId);

            // 全部断奶
            testWeanMethod(pigId, 200);

            // 部分断奶事件信息
//            testWeanMethod(pigId, 100);

            // 测试 拼窝事件信息
            testFostersEventCreate(pigId, 100);

            // 回 配种 舍
//            testToMating(pigId);

        }

    }

    /**
     * 创建Mock 所有的数据内容
     */
    public void testCreateSingleMockData() {

        // 创建公猪 进厂事件信息
        for (int i = 0; i < 5; i++) {
            boarEntryEventCreate();
        }

        // 创建母猪进程事件信息
        for (int i = 0; i < 2; i++) {
            sowEntryEventCreate();
        }

        // 创建配种事件
        for (int i = 0; i < 2; i++) {
            Long pigId = sowEntryEventCreate();
            Long pigBoarId = boarEntryEventCreate();
            sowMatingEventCreate(pigId, pigBoarId);
        }

        // 没有转舍 ， 妊娠检查 阴 性
        for (int i = 0; i < 2; i++) {
            Long pigId = sowEntryEventCreate();
            Long pigBoarId = boarEntryEventCreate();
            sowMatingEventCreate(pigId, pigBoarId);
            testPregCheckResultEventCreate(pigId, PregCheckResult.YING);
        }

        // 没有转舍 妊娠检查 不确定
        for (int i = 0; i < 2; i++) {
            Long pigId = sowEntryEventCreate();
            Long pigBoarId = boarEntryEventCreate();
            sowMatingEventCreate(pigId, pigBoarId);
            testPregCheckResultEventCreate(pigId, PregCheckResult.UNSURE);
        }

        // 没有转舍 妊娠检查 阳性
        for (int i = 0; i < 2; i++) {
            Long pigId = sowEntryEventCreate();
            Long pigBoarId = boarEntryEventCreate();
            sowMatingEventCreate(pigId, pigBoarId);
            testPregCheckResultEventCreate(pigId, PregCheckResult.YANG);
        }

        // 配种后 -> 转舍
        for (int i = 0; i < 2; i++) {
            Long pigId = sowEntryEventCreate();
            Long pigBoarId = boarEntryEventCreate();
            sowMatingEventCreate(pigId, pigBoarId);
            testToPregEventCreate(pigId);
        }

        // 配种 -> 转舍 -> 妊娠 阴
        for (int i = 0; i < 2; i++) {
            Long pigId = sowEntryEventCreate();
            Long pigBoarId = boarEntryEventCreate();
            sowMatingEventCreate(pigId, pigBoarId);
            testToPregEventCreate(pigId);
            testPregCheckResultEventCreate(pigId, PregCheckResult.YING);
        }

        // 配种 -> 转舍 -> 妊娠 阳性
        for (int i = 0; i < 2; i++) {
            Long pigId = sowEntryEventCreate();
            Long pigBoarId = boarEntryEventCreate();
            sowMatingEventCreate(pigId, pigBoarId);
            testToPregEventCreate(pigId);
            testPregCheckResultEventCreate(pigId, PregCheckResult.YANG);
        }

        // 配种 -> 转舍 -> 妊娠 不确定
        for (int i = 0; i < 2; i++) {
            Long pigId = sowEntryEventCreate();
            Long pigBoarId = boarEntryEventCreate();
            sowMatingEventCreate(pigId, pigBoarId);
            testToPregEventCreate(pigId);
            testPregCheckResultEventCreate(pigId, PregCheckResult.UNSURE);
        }

        // 配种 -> 转舍 -> 妊娠 -> 流产
        for (int i = 0; i < 2; i++) {
            Long pigId = sowEntryEventCreate();
            Long pigBoarId = boarEntryEventCreate();
            sowMatingEventCreate(pigId, pigBoarId);
            testToPregEventCreate(pigId);
            testPregCheckResultEventCreate(pigId, PregCheckResult.YANG);
            testAbortionEventCreate(pigId);
        }

        // 配种 -> 转舍 -> 妊娠 -> 去分娩
        for (int i = 0; i < 2; i++) {
            Long pigId = sowEntryEventCreate();
            Long pigBoarId = boarEntryEventCreate();
            sowMatingEventCreate(pigId, pigBoarId);
            testToPregEventCreate(pigId);
            testPregCheckResultEventCreate(pigId, PregCheckResult.YANG);
            testToFarrowing(pigId);
        }

        // 配种 -> 转舍 -> 妊娠 -> 去分娩 -> 分娩
        for (int i = 0; i < 2; i++) {
            Long pigId = sowEntryEventCreate();
            Long pigBoarId = boarEntryEventCreate();
            sowMatingEventCreate(pigId, pigBoarId);
            testToPregEventCreate(pigId);
            testPregCheckResultEventCreate(pigId, PregCheckResult.YANG);
            testToFarrowing(pigId);
            testFarrowingEventCreate(pigId);
        }

        // 配种 -> 转舍 -> 妊娠 -》去分娩 -》分娩 -》 完全断奶
        for (int i = 0; i < 4; i++) {
            Long pigId = sowEntryEventCreate();
            Long pigBoarId = boarEntryEventCreate();
            sowMatingEventCreate(pigId, pigBoarId);
            testToPregEventCreate(pigId);
            testPregCheckResultEventCreate(pigId, PregCheckResult.YANG);
            testToFarrowing(pigId);
            testFarrowingEventCreate(pigId);
            if (i % 2 == 0) {
                testWeanMethod(pigId, 200);
            } else {
                testWeanMethod(pigId, 100);
            }
        }
    }


    /**
     * 测试猪 事件信息
     */
    @Test
    public void testPigPagingInfo() {
        // 录入母猪事件信息
        for (int i = 0; i < 30; i++) {
//            sowEntryEventCreate();
            boarEntryEventCreate();
        }
//        HttpEntity httpEntity = HttpPostRequest.formRequest()
//                .param("farmId", 12345l).param("status", PigStatus.Entry.getKey()).param("pageNo", 1).param("pageSize", 10)
//                .httpEntity();
//
//        ResponseEntity responseEntity = this.restTemplate.postForEntity("http://localhost:" + this.port + "/api/doctor/pigs/queryByStatus", httpEntity, Object.class);
//        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(responseEntity.getBody()));

//        HttpEntity httpEntityDetail = HttpPostRequest.formRequest()
//                .param("farmId", 12345l).param("pigId", 10l)
//                .httpEntity();
//        ResponseEntity responseEntityDetail = this.restTemplate.postForEntity("http://localhost:" + this.port + "/api/doctor/pigs/getPigDetail", httpEntityDetail, Object.class);
//        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(responseEntityDetail.getBody()));

//        HttpEntity httpEntityDetail = HttpPostRequest.formRequest()
//                .param("farmId", 12345l).param("pigId", 10l)
//                .httpEntity();
//        ResponseEntity responseEntityDetail = this.restTemplate.postForEntity(
//                "http://localhost:" + this.port + "/api/doctor/pigs/getSowPigDetail",
//                httpEntityDetail, Object.class);
//        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(responseEntityDetail.getBody()));

        String url = HttpGetRequest.url("http://localhost:" + this.port + "/api/doctor/pigs/getBoarPigDetail")
                .params("farmId", 12345l).params("pigId", 10l).params("eventSize", 3).build();
        ResponseEntity responseEntity = this.restTemplate.getForEntity(url, Object.class);
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(responseEntity.getBody()));
    }

    @Test
    public void testZkClientCondition() throws Exception {
//        subscriber.subscribe(data->{
//            System.out.println(String.valueOf(data));
//        });

        boarEntryEventCreate();
    }

    /**
     * 测试母猪普通事件信息
     */
    @Test
    public void casualCasualEventCreateTest() {
        Long pigId = 1l;

        // 录入母猪信息测试内容
        boarEntryEventCreate();
        boarEntryEventCreate();
        boarEntryEventCreate();
        boarEntryEventCreate();

//        chgLocationCasualEventCreate(pigId);

//        createRemovalPigEventContent(pigId);
        createRemovalPigEventsContent(Arrays.asList(1l, 2l, 3l, 4l));

//        createDiseaseVaccinationEvent(pigId);

//        createConditionEvent(pigId);
//        createSemseEvent(pigId);

        printCurrentState();
//        testCurrentSowInputStatus(pigId);
    }

    /**
     * 批量猪
     *
     * @param pigIds
     */
    public void createRemovalPigEventsContent(List<Long> pigIds) {
        String url = basicUrl + "/createCasualEvents";

        HttpEntity httpEntity = HttpPostRequest.formRequest()
                .param("pigIds", pigIds).param("farmId", 12345l)
                .param("pigEvent", PigEvent.REMOVAL.getKey())
                .param("doctorRemovalDtoJson", DoctorRemovalDto.builder()
                        .chgTypeId(1l).chgTypeName("chgTypeName").chgReasonId(1l).chgReasonName("chgReasonName").toBarnId(1l)
                        .weight(100d).sum(100L).price(100L).customerId(1l).remark("remark")
                        .build())
                .httpEntity();
        Boolean result = this.restTemplate.postForObject(url, httpEntity, Boolean.class);
        System.out.println(result);
    }

    /**
     * 测试母猪进厂事件信息
     */
    @Test
    public void sowEntryEventCreateTest() {

        Long pigId = 1l;
        sowEntryEventCreate();

        Long boarId = 2l;
        sowEntryEventCreate();

//        sowMatingEventCreate(pigId, boarId);

//        testPregCheckResultEventCreate(pigId, PregCheckResult.YANG);

//        testToPregEventCreate(pigId);

        // test
//        testAbortionEventCreate(pigId);

//        testToFarrowing(pigId);

//        testFarrowingEventCreate(pigId);

//        testWeanMethod(pigId, 200);

        // 测试凭我事件信息test
//        testFostersEventCreate(pigId, 200);

        //  录入转场事件信息
//        testToMating(pigId);

        // 显示 state
        printCurrentState();

        // 获取下一个事件信息
        testCurrentSowInputStatus(pigId);
    }

    /**
     * 测试母猪回滚事件信息
     */
    @Test
    public void testRollBackEventCreateTest() {

        String url = "http://localhost:" + this.port + "/api/doctor/events/pig/rollBackPigEvent";

        Long pigId = 1l;
        sowEntryEventCreate();
        Long boarId = 2l;
        sowEntryEventCreate();
        sowMatingEventCreate(pigId, boarId);
        testPregCheckResultEventCreate(pigId, PregCheckResult.YANG);
        testToPregEventCreate(pigId);

        // test rollback info
        HttpEntity httpEntity = HttpPostRequest.formRequest()
                .param("pigEventId", 5l).param("revertPigType", 1)
                .httpEntity();

        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);

        printCurrentState();
    }

    private void createSemenEvent(Long pigId) {

        String url = basicUrl + "/createSemen";

        HttpEntity httpEntity = HttpPostRequest.formRequest()
                .param("pigId", pigId).param("farmId", 12345l).param("doctorSemenDtoJson", DoctorSemenDto.builder()
                        .semenDate(new Date()).semenWeight(100d).dilutionWeight(100d).dilutionRatio(10d)
                        .semenActive(100d).semenDensity(100d).semenJxRatio(100d).semenPh(100d).semenRemark("remark").semenTotal(100d)
                        .build())
                .httpEntity();

        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);
    }

    private void createConditionEvent(Long pigId) {
        String url = basicUrl + "/createConditionEvent";

        HttpEntity httpEntity = HttpPostRequest.formRequest()
                .param("pigId", pigId).param("farmId", 12345l).param("doctorConditionDtoJson", DoctorConditionDto.builder()
                        .conditionDate(new Date()).conditionJudgeScore(100d)
                        .conditionWeight(100d).conditionBackWeight(100d)
                        .conditionRemark("remark")
                        .build())
                .httpEntity();
        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);

        System.out.println(result);
    }

    /**
     * 创建 疾病 免疫事件信息
     */
    private void createDiseaseVaccinationEvent(Long pigId) {
        String url = basicUrl + "/createDiseaseEvent";

        HttpEntity httpEntity = HttpPostRequest.formRequest()
                .param("pigId", pigId).param("farmId", 12345l).param("doctorDiseaseDtoJson", DoctorDiseaseDto.builder()
                        .diseaseDate(new Date()).diseaseName("diseaseName").diseaseStaff("staff").diseaseRemark("remark")
                        .build())
                .httpEntity();

        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);

        url = basicUrl + "/createVaccinationEvent";
        HttpEntity httpEntityVa = HttpPostRequest.formRequest()
                .param("pigId", pigId).param("farmId", 12345l).param("doctorVaccinationDtoJson", DoctorVaccinationDto.builder()
                        .vaccinationDate(new Date()).vaccinationId(1l).vaccinationName("vaccinationName")
                        .vaccinationStaffId(1l).vaccinationStaffName("vaccinationName").vaccinationRemark("remark")
                        .build())
                .httpEntity();
        result = this.restTemplate.postForObject(url, httpEntityVa, Long.class);
        System.out.println(result);
    }

    /**
     * 离场母猪创建事件
     */
    private void createRemovalPigEventContent(Long pigId) {
        String url = basicUrl + "/createRemovalEvent";

        HttpEntity httpEntity = HttpPostRequest.formRequest()
                .param("pigId", pigId).param("farmId", 12345l).param("doctorRemovalDtoJson", DoctorRemovalDto.builder()
                        .chgTypeId(1l).chgTypeName("chgTypeName").chgReasonId(1l).chgReasonName("chgReasonName").toBarnId(1l)
                        .weight(100d).sum(100L).price(100L).customerId(1l).remark("remark")
                        .build())
                .httpEntity();

        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);
    }

    /**
     * 录入转舍事件信息
     */
    private void chgLocationCasualEventCreate(Long pigId) {
        String url = basicUrl + "/createChgLocation";

        HttpEntity httpEntity = HttpPostRequest.formRequest()
                .param("pigId", pigId).param("farmId", 12345l).param("doctorChgLocationDtoJson", DoctorChgLocationDto.builder()
                        .changeLocationDate(new Date()).chgLocationFromBarnId(1l).chgLocationFromBarnName("fromBarnName")
                        .chgLocationToBarnId(2l).chgLocationToBarnName("toBarnName")
                        .build())
                .httpEntity();

        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);
    }

    private void testFostersEventCreate(Long pigId, Integer fosterCount) {
        // 创建一个可以被拼窝的母猪
        Long pigFosterId = sowEntryEventCreate();

        Long pigBoarId = boarEntryEventCreate();

        sowMatingEventCreate(pigFosterId, pigBoarId);
        testPregCheckResultEventCreate(pigFosterId, PregCheckResult.YANG);
        testToPregEventCreate(pigFosterId);
        testToFarrowing(pigFosterId);
        testFarrowingEventCreate(pigFosterId);

        DoctorFostersDto doctorFostersDto = DoctorFostersDto.builder()
                .fostersDate(new Date()).fostersCount(fosterCount).sowFostersCount(fosterCount / 2).boarFostersCount(fosterCount / 2)
                .fosterSowId(pigFosterId).fosterReason(1l).fosterRemark("testFostersReMark")
                .build();

        String url = basicUrl + "/createSowEvent";
        HttpEntity httpEntity = HttpPostRequest.formRequest().param("farmId", 12345l)
                .param("pigId", pigId).param("eventType", PigEvent.FOSTERS.getKey())
                .param("sowInfoDtoJson", JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorFostersDto))
                .httpEntity();

        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);
    }


    private void testWeanMethod(Long pigId, Integer count) {
        String url = basicUrl + "/createSowEvent";
        HttpEntity httpEntity = HttpPostRequest.formRequest().param("farmId", 12345l)
                .param("pigId", pigId).param("eventType", PigEvent.WEAN.getKey())
                .param("sowInfoDtoJson", DoctorPartWeanDto.builder()
                        .partWeanDate(new Date()).partWeanPigletsCount(count).partWeanAvgWeight(12345.123).partWeanRemark("partWeanRemark")
                        .build())
                .httpEntity();
        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);
    }

    /**
     * 分娩事件测试
     *
     * @param pigId
     */
    private void testFarrowingEventCreate(Long pigId) {
        String url = basicUrl + "/createSowEvent";
        HttpEntity httpEntity = (HttpPostRequest.formRequest().param("farmId", 12345l)
                .param("pigId", pigId).param("eventType", PigEvent.FARROWING.getKey())
                .param("sowInfoDtoJson", DoctorFarrowingDto.builder()
                        .farrowingDate(new Date()).nestCode("12345").barnId(7l).barnName("farrowingBarnName").bedCode("bedCode")
                        .farrowingType(FarrowingType.HELP.getKey()).isHelp(IsOrNot.YES.getValue())
                        .birthNestAvg(1234.123).farrowingLiveCount(200).liveSowCount(100).liveBoarCount(100).healthCount(100)
                        .weakCount(100).mnyCount(0).deadCount(0).blackCount(0).jxCount(0).toBarnId(-1l).toBarnName("notKnow")
                        .farrowStaff1("staff").farrowStaff2("staff2").farrowRemark("farrowingReMark")
                        .build()).httpEntity());
        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);
    }

    /**
     * 测试母猪转舍事件信息
     *
     * @param pigId
     */
    private void testToMating(Long pigId) {
        String url = basicUrl + "/createSowEvent";
        HttpEntity httpEntity = HttpPostRequest.formRequest()
                .param("farmId", 12345l).param("pigId", pigId).param("eventType", PigEvent.TO_MATING.getKey())
                .param("sowInfoDtoJson", DoctorChgLocationDto.builder()
                        .changeLocationDate(new Date()).chgLocationFromBarnId(7l).chgLocationFromBarnName("fromWeanBarnName")
                        .chgLocationToBarnId(5l).chgLocationToBarnName("matingBarnName")
                        .build()).httpEntity();

        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);
    }

    /**
     * 测试对应的去分娩
     */
    private void testToFarrowing(Long pigId) {
        String url = basicUrl + "/createSowEvent";
        HttpEntity httpEntity = (HttpPostRequest.formRequest().param("farmId", 12345l)
                .param("pigId", pigId).param("eventType", PigEvent.TO_FARROWING.getKey())
                .param("sowInfoDtoJson", DoctorChgLocationDto.builder()
                        .changeLocationDate(new Date()).chgLocationFromBarnId(6l).chgLocationFromBarnName("matingBarnName")
                        .chgLocationToBarnId(7l).chgLocationToBarnName("farrowingBarnName")
                        .build()).httpEntity());
        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);
    }

    /**
     * 创建对应的流产事件信息
     */
    private void testAbortionEventCreate(Long pigId) {
        String url = basicUrl + "/createSowEvent";
        HttpEntity httpEntity = HttpPostRequest.formRequest().param("farmId", 12345l)
                .param("pigId", pigId).param("eventType", PigEvent.ABORTION.getKey())
                .param("sowInfoDtoJson", JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(
                        DoctorAbortionDto.builder()
                                .abortionDate(new Date()).abortionReasonName("abortionReason")
                                .build()
                ))
                .httpEntity();
        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);
    }

    /**
     * 创建妊娠检查方式
     */
    private void testPregCheckResultEventCreate(Long pigId, PregCheckResult pregCheckResult) {
        String url = basicUrl + "/createSowEvent";
        HttpEntity httpEntity = HttpPostRequest.formRequest().param("farmId", 12345l)
                .param("pigId", pigId).param("eventType", PigEvent.PREG_CHECK.getKey())
                .param("sowInfoDtoJson", JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(
                        DoctorPregChkResultDto.builder()
                                .checkDate(new Date()).checkResult(pregCheckResult.getKey()).checkMark("checkMarkResult")
                                .build()
                ))
                .httpEntity();
        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);
    }


    /**
     * 去妊娠舍
     */
    private void testToPregEventCreate(Long pigId) {
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
    private void testCurrentSowInputStatus(Long pigId) {
        String url = "http://localhost:" + this.port + "/api/doctor/events/pig/queryPigEvents";

        List<Long> ids = Lists.newArrayList();
        ids.add(pigId);
        HttpEntity httpEntity = HttpPostRequest.bodyRequest().params(ids);

        ResponseEntity entity = this.restTemplate.postForEntity(url, httpEntity, Object.class);
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(entity.getBody()));
    }

    /**
     * 母猪配种事件测试
     *
     * @param pigId
     */
    private void sowMatingEventCreate(Long pigId, Long boarId) {
        String url = basicUrl + "/createSowEvent";
        HttpEntity httpEntity = HttpPostRequest.formRequest().param("farmId", 12345l)
                .param("pigId", pigId).param("eventType", PigEvent.MATING.getKey())
                .param("sowInfoDtoJson", JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(DoctorMatingDto.builder()
                        .matingDate(DateTime.now().toDate()).judgePregDate(DateTime.now().plusDays(114).toDate()).matingType(MatingType.MANUAL.getKey())
                        .matingStaff("staff").mattingMark("matingMark").matingBoarPigId(boarId)
                        .build())).httpEntity();

        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        System.out.println(result);

    }

    private Long boarEntryEventCreate() {
        String url = basicUrl + "/createEntryInfo";
        DoctorFarmEntryDto doctorFarmEntryDto = buildFarmEntryDto();
        doctorFarmEntryDto.setPigType(DoctorPig.PIG_TYPE.BOAR.getKey());

        HttpEntity httpEntity = HttpPostRequest.formRequest().param("farmId", 12345l).
                param("doctorFarmEntryJson",
                        JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorFarmEntryDto))
                .httpEntity();
        Long result = this.restTemplate.postForObject(url, httpEntity, Long.class);
        return result;
    }

    private Long sowEntryEventCreate() {
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
    private void printCurrentState() {
        System.out.println("doctor pig ******************************************************");
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigDao.list(ImmutableMap.of("isRemoval", 1))));
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigDao.list(ImmutableMap.of("isRemoval", 0))));
        System.out.println("doctor pig track******************************************************");
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigTrackDao.list(ImmutableMap.of("isRemoval", 1))));
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigTrackDao.list(ImmutableMap.of("isRemoval", 0))));
        System.out.println("doctor pig snap shot content******************************************************");
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigSnapshotDao.listAll()));
        System.out.println("doctor pig event dao ******************************************************");
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorPigEventDao.listAll()));
        return;
    }

    /**
     * 构建Pig 进厂信息 pigType 不构建
     *
     * @return
     */
    private DoctorFarmEntryDto buildFarmEntryDto() {

        return DoctorFarmEntryDto.builder()
                .pigCode(UUID.randomUUID().toString()).birthday(new Date()).inFarmDate(new Date()).barnId(1l).barnName("barnName")
                .source(PigSource.LOCAL.getKey()).breed(1l).breedName("breedName").breedType(1l).breedTypeName("breedTypeName")
                .fatherCode("fatherCode").motherCode("motherCode").entryMark("entryMark")
                .boarTypeId(BoarEntryType.HGZ.getKey()).boarTypeName(BoarEntryType.HGZ.getDesc())
                .earCode("earCode").parity(1).left(100).right(100)
                .build();

    }
}
