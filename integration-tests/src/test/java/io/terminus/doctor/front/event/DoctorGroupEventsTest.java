package io.terminus.doctor.front.event;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorAntiepidemicGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.web.front.event.controller.DoctorGroupEvents;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Desc: 猪群事件controller测试类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/7
 */
@Slf4j
@SpringApplicationConfiguration(FrontWebConfiguration.class)
public class DoctorGroupEventsTest extends BaseFrontWebTest {

    private static final ToJsonMapper JSON_MAPPER = ToJsonMapper.JSON_NON_EMPTY_MAPPER;

    /**
     * 新建猪群测试
     * @see DoctorGroupEvents#createNewGroup(io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput)
     */
    @Test
    public void createNewGroupTest() {
        String url = "/api/doctor/events/group/new";
        ResponseEntity<Long> result = postForEntity(url, mockNewGroupInput(), Long.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));

        //查一把, 看是否插入成功
        DoctorGroupDetail groupDetail = findGroupDetailByGroupId(result.getBody());
        assertNotNull(groupDetail);
        log.info("createNewGroupTest result:{}", groupDetail);
    }

    /**
     * 录入猪群事件测试
     * @see DoctorGroupEvents#createGroupEvent(java.lang.Long, java.lang.Integer, java.lang.String)
     */
    @Test
    public void createGroupEventTest() {
        String url = "/api/doctor/events/group/other";
        Long groupId = 5L;

        //存栏事件
        ResponseEntity<Boolean> liveStockResult = postFormForEntity(url, ImmutableMap.of("groupId", groupId, "eventType", 6, "data", mockLiveStockGroupInput()), Boolean.class);
        assertEvent(groupId, 6, liveStockResult);

        //疾病事件
        ResponseEntity<Boolean> diseaseResult = postFormForEntity(url, ImmutableMap.of("groupId", groupId, "eventType", 7, "data", mockDiseaseGroupInput()), Boolean.class);
        assertEvent(groupId, 7, diseaseResult);

        //防疫事件
        ResponseEntity<Boolean> antiResult = postFormForEntity(url, ImmutableMap.of("groupId", groupId, "eventType", 8, "data", mockAntiepidemicGroupInput()), Boolean.class);
        assertEvent(groupId, 8, antiResult);

        //转入猪群事件
        ResponseEntity<Boolean> moveInResult = postFormForEntity(url, ImmutableMap.of("groupId", groupId, "eventType", 2, "data", mockMoveInGroupInput()), Boolean.class);
        assertEvent(groupId, 2, moveInResult);

        //猪群变动事件
        ResponseEntity<Boolean> changeResult = postFormForEntity(url, ImmutableMap.of("groupId", groupId, "eventType", 3, "data", mockChangeGroupInput()), Boolean.class);
        assertEvent(groupId, 3, changeResult);

        //猪群转群事件
        ResponseEntity<Boolean> transGroupResult = postFormForEntity(url, ImmutableMap.of("groupId", groupId, "eventType", 4, "data", mockTransGroupInput()), Boolean.class);
        assertEvent(groupId, 4, transGroupResult);

        //转场事件
        ResponseEntity<Boolean> transFarmResult = postFormForEntity(url, ImmutableMap.of("groupId", groupId, "eventType", 9, "data", mockTransFarmGroupInput()), Boolean.class);
        assertEvent(groupId, 9, transFarmResult);

        //关闭猪群事件
        ResponseEntity<Boolean> closeResult = postFormForEntity(url, ImmutableMap.of("groupId", groupId, "eventType", 10, "data", mockCloseGroupInput()), Boolean.class);
        assertEvent(groupId, 10, closeResult);
    }

    private void assertEvent(Long groupId, Integer eventType, ResponseEntity<Boolean> result) {
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        DoctorGroupSnapShotInfo diseaseSnapShot = findGroupSnapShotByGroupId(groupId);
        assertThat(diseaseSnapShot.getGroupEvent().getType(), is(eventType));
    }

    /**
     * 根据猪群id查询可以操作的事件类型测试
     * @see DoctorGroupEvents#findEventTypesByGroupIds(java.lang.Long[])
     */
    @Test
    public void findEventTypesByGroupIdsTest() {
        String url = "/api/doctor/events/group/types";
        ResponseEntity<List> result = postFormForEntity(url, ImmutableMap.of("groupIds[]", "1,2"), List.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().size(), not(0));
        log.info("findEventTypesByGroupIdsTest result:{}", result.getBody());
    }

    /**
     * 生成猪群号 猪舍名(yyyy-MM-dd)测试
     * @see DoctorGroupEvents#generateGroupCode(java.lang.String)
     */
    @Test
    public void generateGroupCodeTest() {
        String url = "/api/doctor/events/group/code";
        ResponseEntity<String> result = getForEntity(url, ImmutableMap.of("barnName", "育肥舍"), String.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertNotNull(result.getBody());
        log.info("generateGroupCodeTest result:{}", result.getBody());
    }

    /**
     * 查询猪群详情测试
     * @see DoctorGroupEvents#findGroupDetailByGroupId(java.lang.Long)
     */
    @Test
    public void findGroupDetailByGroupIdTest() {
        DoctorGroupDetail groupDetail = findGroupDetailByGroupId(5L);
        assertNotNull(groupDetail);
        log.info("findGroupDetailByGroupIdTest result:{}", groupDetail);
    }

    private DoctorGroupDetail findGroupDetailByGroupId(Long groupId) {
        String url = "/api/doctor/events/group/detail";
        ResponseEntity<DoctorGroupDetail> result = getForEntity(url, ImmutableMap.of("groupId", groupId), DoctorGroupDetail.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        return result.getBody();
    }

    /**
     * 分页查询猪群历史事件测试
     * @see DoctorGroupEvents#pagingGroupEvent(java.lang.Long, java.lang.Long, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    @Test
    public void pagingGroupEventTest() {
        String url = "/api/doctor/events/group/paging";
        ResponseEntity<Paging> result = getForEntity(url, ImmutableMap.of("farmId", 0), Paging.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().getTotal(), not(0L));
        log.info("pagingGroupEventTest result:{}", result.getBody().getData());
    }

    /**
     * 查询猪群镜像信息(猪群, 猪群跟踪, 最新event)
     * @see DoctorGroupEvents#findGroupSnapShotByGroupId(java.lang.Long)
     */
    @Test
    public void findGroupSnapShotByGroupIdTest() {
        DoctorGroupSnapShotInfo snapshot = findGroupSnapShotByGroupId(5L);
        assertNotNull(snapshot);
        log.info("findGroupSnapShotByGroupIdTest result:{}", snapshot);
    }

    private DoctorGroupSnapShotInfo findGroupSnapShotByGroupId(Long groupId) {
        String url = "/api/doctor/events/group/snapshot";
        ResponseEntity<DoctorGroupSnapShotInfo> result = getForEntity(url, ImmutableMap.of("groupId", groupId), DoctorGroupSnapShotInfo.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        return result.getBody();
    }

    //新建猪群
    private DoctorNewGroupInput mockNewGroupInput() {
        DoctorNewGroupInput input = new DoctorNewGroupInput();
        input.setFarmId(0L);
        input.setGroupCode("育肥舍(111)");
        input.setBarnId(3L);
        input.setPigType(3);
        input.setSex(2);
        input.setBreedId(3L);
        input.setGeneticId(2L);
        input.setSource(1);
        input.setEventAt("2016-06-11");
        return input;
    }

    //防疫事件
    private String mockAntiepidemicGroupInput() {
        DoctorAntiepidemicGroupInput input = new DoctorAntiepidemicGroupInput();
        input.setEventAt("2016-06-11");
        input.setQuantity(1);
        input.setVaccinId(9L);
        input.setVaccinResult(DoctorAntiepidemicGroupEvent.VaccinResult.POSITIVE.getValue());
        return JSON_MAPPER.toJson(input);
    }

    //猪群变动事件
    private String mockChangeGroupInput() {
        DoctorChangeGroupInput input = new DoctorChangeGroupInput();
        input.setEventAt("2016-06-11");
        input.setChangeTypeId(3L);
        input.setChangeReasonId(1L);
        input.setQuantity(5);
        input.setBoarQty(2);
        input.setSowQty(3);
        input.setWeight(10D);
        input.setPrice(100L);
        return JSON_MAPPER.toJson(input);
    }

    //关闭猪群事件
    private String mockCloseGroupInput() {
        DoctorCloseGroupInput input = new DoctorCloseGroupInput();
        input.setEventAt("2016-06-11");
        return JSON_MAPPER.toJson(input);
    }

    //疾病事件
    private String mockDiseaseGroupInput() {
        DoctorDiseaseGroupInput input = new DoctorDiseaseGroupInput();
        input.setEventAt("2016-06-11");
        input.setQuantity(1);
        input.setDiseaseId(1L);
        return JSON_MAPPER.toJson(input);
    }

    //存栏事件
    private String mockLiveStockGroupInput() {
        DoctorLiveStockGroupInput input = new DoctorLiveStockGroupInput();
        input.setEventAt("2016-06-11");
        input.setAvgWeight(100D);
        return JSON_MAPPER.toJson(input);
    }

    //转入猪群事件
    private String mockMoveInGroupInput() {
        DoctorMoveInGroupInput input = new DoctorMoveInGroupInput();
        input.setEventAt("2016-06-11");
        input.setInType(DoctorMoveInGroupEvent.InType.PIGLET.getValue());
        input.setSource(PigSource.LOCAL.getKey());
        input.setSex(DoctorGroupTrack.Sex.MIX.getValue());
        input.setQuantity(5);
        input.setBoarQty(2);
        input.setSowQty(3);
        input.setAvgDayAge(15);
        input.setAvgWeight(300D);
        return JSON_MAPPER.toJson(input);
    }

    //转场事件
    private String mockTransFarmGroupInput() {
        DoctorTransFarmGroupInput input = new DoctorTransFarmGroupInput();
        input.setEventAt("2016-06-11");
        input.setToFarmId(1L);
        input.setToBarnId(4L);
        input.setIsCreateGroup(1);
        input.setToGroupCode("保育北-1舍(2222)");
        input.setQuantity(2);
        input.setBoarQty(1);
        input.setSowQty(1);
        input.setWeight(300D);
        return JSON_MAPPER.toJson(input);
    }

    //转群事件
    private String mockTransGroupInput() {
        DoctorTransGroupInput input = new DoctorTransGroupInput();
        input.setEventAt("2016-06-11");
        input.setToBarnId(3L);
        input.setIsCreateGroup(1);
        input.setToGroupCode("保育3舍(2222)");
        input.setQuantity(2);
        input.setBoarQty(1);
        input.setSowQty(1);
        input.setWeight(300D);
        return JSON_MAPPER.toJson(input);
    }
}
