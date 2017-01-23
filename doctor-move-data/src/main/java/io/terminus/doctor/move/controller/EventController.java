package io.terminus.doctor.move.controller;

import com.google.common.base.Throwables;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.basic.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by chenzenghui on 16/8/31.
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/move/data/event")
public class EventController {

    private final DoctorPigEventDao doctorPigEventDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;
    private final DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;

    @Autowired
    public EventController(DoctorPigEventDao doctorPigEventDao,
                           DoctorGroupEventDao doctorGroupEventDao,
                           DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService,
                           DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao) {
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorMaterialConsumeProviderReadService = doctorMaterialConsumeProviderReadService;
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
    }

    @RequestMapping(value = "/refreshDesc", method = RequestMethod.GET)
    public String refreshDesc(){
        Date beginDate = DateUtil.toDate("2016-08-15");
        DoctorBasicInputInfoDto basicPigInput = new DoctorBasicInputInfoDto();
        try{
            List<DoctorPigEvent> pigEvents = doctorPigEventDao.findByDateRange(beginDate, new Date());
            pigEvents.forEach(pigEvent -> {
                if(pigEvent.getExtraMap() != null){
                    pigEvent.setDesc(basicPigInput.generateEventDescFromExtra(BeanMapper.map(pigEvent.getExtraMap(), BasePigEventInputDto.class)));
                    doctorPigEventDao.update(pigEvent);
                }
            });

            List<DoctorGroupEvent> groupEvents= doctorGroupEventDao.findByDateRange(beginDate, new Date());
            for(DoctorGroupEvent groupEvent : groupEvents) {
                if(groupEvent.getExtra() != null){
                    BaseGroupInput baseGroupInput = BaseGroupInput.generateBaseGroupInputFromTypeAndExtra(JsonMapper.JSON_NON_EMPTY_MAPPER.getMapper().readValue(groupEvent.getExtra(), JacksonType.MAP_OF_OBJECT), GroupEventType.from(groupEvent.getType()));
                    baseGroupInput.setIsAuto(groupEvent.getIsAuto());
                    groupEvent.setDesc(baseGroupInput.generateEventDesc());
                    doctorGroupEventDao.update(groupEvent);
                }
            }
            return "ok";
        }catch(Exception e) {
            log.error("refreshDesc failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Throwables.getStackTraceAsString(e);
        }
    }


    /**
     * 将表 doctor_material_consume_providers 的extra 中的barn拆出来
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/extractBarn", method = RequestMethod.GET)
    public String extractBarn(@RequestParam("farmId") Long farmId){
        int pageNo = 1;
        try{
            while(true){
                Paging<DoctorMaterialConsumeProvider> paging = RespHelper.or500(doctorMaterialConsumeProviderReadService.page(
                        farmId, null, null, DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue(), null,
                        null, null, null, null, pageNo++, 1000));
                if(paging.isEmpty()){
                    break;
                }
                for(DoctorMaterialConsumeProvider cp : paging.getData()){
                    Map<String, Object> extraMap = cp.getExtraMap();
                    if(extraMap != null && extraMap.get("barnId") != null && extraMap.get("barnName") != null){
                        cp.setBarnId(Long.valueOf(extraMap.get("barnId").toString()));
                        cp.setBarnName(extraMap.get("barnName").toString());
                        doctorMaterialConsumeProviderDao.update(cp);
                    }
                }
            }
            return "ok";
        }catch(Exception e){
            return Throwables.getStackTraceAsString(e);
        }
    }
}
