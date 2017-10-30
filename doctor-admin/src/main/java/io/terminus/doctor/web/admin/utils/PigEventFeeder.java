package io.terminus.doctor.web.admin.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorPigReadService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2017/9/13.
 */

@Setter
@Component
public class PigEventFeeder {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();


    @Autowired
    private DoctorBasicReadService doctorBasicReadService;
    @Autowired
    private DoctorPigReadService doctorPigReadService;


    public Map<String, Object> feedIfChanged(String eventDtoJson, DoctorPigEvent pigEvent) throws IOException {

        Map<String, Object> map = OBJECT_MAPPER.readValue(eventDtoJson, Map.class);

        if (map.containsKey("breed") && !map.get("breed").equals(pigEvent.getBreedId()))//品种
            map.put("breedName", RespHelper.orServEx(doctorBasicReadService.findBasicById(Long.parseLong(map.get("breed").toString()))).getName());

        if (map.containsKey("breedType") && !map.get("breedType").equals(pigEvent.getBreedTypeId()))//品系
            map.put("breedTypeName", RespHelper.orServEx(doctorBasicReadService.findBasicById(Long.parseLong(map.get("breed").toString()))).getName());

        if (map.containsKey("matingBoarPigId"))//公猪号
            map.put("matingBoarPigCode", RespHelper.or500(doctorPigReadService.findPigById(Long.parseLong(map.get("matingBoarPigId").toString()))));




        return map;
    }


}
