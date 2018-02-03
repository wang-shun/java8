package io.terminus.doctor.event.concurrent;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.handler.group.DoctorTransGroupEventHandler;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.manager.DoctorRollbackManager;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.test.BaseServiceTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sun.awt.windows.ThemeReader;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 */
@Slf4j
public class ConcurrentControllerTest extends BaseServiceTest{

    @Autowired
    private DoctorPigEventManager doctorPigEventManager;
    @Autowired
    private DoctorGroupEventManager doctorGroupEventManager;
    @Autowired
    private DoctorGroupManager doctorGroupManager;
    @Autowired
    private DoctorRollbackManager doctorRollbackManager;

    @Autowired
    private DoctorPigDao doctorPigDao;
    @Autowired
    private DoctorGroupReadService doctorGroupReadService;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;

    private JsonMapper jsonMapper = JsonMapper.nonEmptyMapper();

    @Test
    public void pigEventHandleTest() {
        //并发进场
        String input = "{\"isAuto\":0,\"pigType\":1,\"pigCode\":\"concurrent3\",\"barnId\":10284,\"barnName\":\"配种1舍张三\",\"barnType\":5,\"eventType\":7,\"eventName\":\"进场\",\"eventDesc\":\"进场事件\",\"eventSource\":1,\"origin\":2000,\"birthday\":1516924800000,\"inFarmDate\":1516924800000,\"source\":1,\"breed\":1,\"breedName\":\"长白\",\"parity\":1}";
        String basic = "{\"farmId\":404,\"farmName\":\"演示猪场一区\",\"orgId\":188,\"orgName\":\"新融农牧演示集团\",\"staffId\":91,\"staffName\":\"lyy\"}";

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    doctorPigEventManager.eventHandle(jsonMapper.fromJson(input, DoctorFarmEntryDto.class), jsonMapper.fromJson(basic, DoctorBasicInputInfoDto.class));
                    System.out.println("-------------successful");
                }catch (InvalidException e) {
                    log.error("---------------error:{}",e.getError());
                }
            }).start();
        }

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void pigBatchEventHandleTest() {
        //批量并发进场
        String input = "{\"inputJsonList\":[{\"farmId\":\"404\",\"pigCode\":\"cc5\",\"barnId\":\"10284\",\"birthday\":\"2018-01-26\",\"inFarmDate\":\"2018-01-26\",\"source\":\"1\",\"breed\":\"1\",\"breedType\":\"\",\"parity\":\"1\",\"fatherCode\":\"\",\"motherCode\":\"\",\"earCode\":\"\",\"left\":\"\",\"right\":\"\",\"origin\":200,\"pigType\":1,\"barnName\":\"配种1舍张三\",\"breedName\":\"长白\"}" +
                ",{\"farmId\":\"404\",\"pigCode\":\"cc6\",\"barnId\":\"10284\",\"birthday\":\"2018-01-26\",\"inFarmDate\":\"2018-01-26\",\"source\":\"1\",\"breed\":\"1\",\"breedType\":\"\",\"parity\":\"1\",\"fatherCode\":\"\",\"motherCode\":\"\",\"earCode\":\"\",\"left\":\"\",\"right\":\"\",\"origin\":200,\"pigType\":1,\"barnName\":\"配种1舍张三\",\"breedName\":\"长白\"}," +
                "{\"farmId\":\"404\",\"pigCode\":\"cc7\",\"barnId\":\"10284\",\"birthday\":\"2018-01-26\",\"inFarmDate\":\"2018-01-26\",\"source\":\"1\",\"breed\":\"1\",\"breedType\":\"\",\"parity\":\"1\",\"fatherCode\":\"\",\"motherCode\":\"\",\"earCode\":\"\",\"left\":\"\",\"right\":\"\",\"origin\":200,\"pigType\":1,\"barnName\":\"配种1舍张三\",\"breedName\":\"长白\"}," +
                "{\"farmId\":\"404\",\"pigCode\":\"cc8\",\"barnId\":\"10284\",\"birthday\":\"2018-01-26\",\"inFarmDate\":\"2018-01-26\",\"source\":\"1\",\"breed\":\"1\",\"breedType\":\"\",\"parity\":\"1\",\"fatherCode\":\"\",\"motherCode\":\"\",\"earCode\":\"\",\"left\":\"\",\"right\":\"\",\"origin\":200,\"pigType\":1,\"barnName\":\"配种1舍张三\",\"breedName\":\"长白\"}]}";
        String basic = "{\"farmId\":404,\"farmName\":\"演示猪场一区\",\"orgId\":188,\"orgName\":\"新融农牧演示集团\",\"staffId\":91,\"staffName\":\"lyy\"}";
        List<DoctorFarmEntryDto> list =
                ((List<Map>)jsonMapper.fromJson(input, Map.class).get("inputJsonList")).stream().map(map -> jsonMapper.getMapper().convertValue(map, DoctorFarmEntryDto.class)).collect(Collectors.toList());
        List<BasePigEventInputDto> list1 =
                list.stream().map(v -> {
                    BasePigEventInputDto base  = v;
                    base.setEventType(7);
                    return base;
                }).collect(Collectors.toList());

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    doctorPigEventManager.batchEventsHandle(list1, jsonMapper.fromJson(basic, DoctorBasicInputInfoDto.class));
                    log.info("------------------successful");
                }catch (InvalidException e) {
                    log.error("-----------------error：{}", e.getError());
                }
            }).start();
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void rollbackEvent(){
        DoctorPigEvent pigEvent = doctorPigEventDao.findEventById(681976L);

        for (int i = 0; i < 10; i++) {
            new Thread(() ->{
                try {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    doctorRollbackManager.rollbackPig(pigEvent, 1L, "xx");
                    log.info("------------------successful");
                } catch (InvalidException e) {
                    log.error("-----------------error：{}", e.getError());
                }
            }).start();
        }

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void modifyEvent(){
        DoctorPigEvent pigEvent = doctorPigEventDao.findEventById(681977L);
        BasePigEventInputDto dto = jsonMapper.fromJson(pigEvent.getExtra(), DoctorConditionDto.class);
        for (int i = 0; i < 10; i++) {
            new Thread(() ->{
                try {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    doctorPigEventManager.modifyPigEventHandle(dto, 681977L, PigEvent.CONDITION.getKey());
                    log.info("------------------successful");
                } catch (InvalidException e) {
                    log.error("-----------------error：{}", e.getError());
                }
            }).start();
        }

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void groupNewGroup(){
        for (int i = 0; i < 10; i++) {
//            new Thread(() -> doctorGroupManager.createNewGroup(Lists.newArrayList(), jsonMapper.fromJson(basic, DoctorBasicInputInfoDto.class))).start();
        }
    }

    @Test
    public void groupBatchNewGroup(){

    }

    @Test
    public void groupEvent(){
        String input = "{\"eventAt\":\"2018-01-27\",\"toBarnId\":\"10290\",\"isCreateGroup\":\"1\",\"toGroupId\":\"7077\",\"toGroupCode\":\"后备舍(2018-01-27)\",\"quantity\":\"2\",\"avgWeight\":\"2\",\"boarQty\":\"\",\"sowQty\":\"\",\"remark\":\"\",\"weight\":\"4.000\"}";
        DoctorTransGroupInput transGroupInput = jsonMapper.fromJson(input, DoctorTransGroupInput.class);
        DoctorGroupDetail doctorGroupDetail  = RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(7054L));

        for (int i = 0; i < 10; i++) {
            new Thread(() ->{
                try {
                    doctorGroupEventManager.handleEvent(doctorGroupDetail, transGroupInput, DoctorTransGroupEventHandler.class);
                    log.info("------------------successful");
                } catch (InvalidException e) {
                    log.error("-----------------error：{}", e.getError());
                }
            }).start();
        }

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void groupBatchEvent(){

    }
}
