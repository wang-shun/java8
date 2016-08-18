package io.terminus.doctor.event.handler.sow;

import com.google.common.base.Throwables;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.workflow.event.ITacker;
import io.terminus.doctor.workflow.utils.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by xiao on 16/8/18.
 */
@Slf4j
@Component
public class DoctorSowBreedCountCheckTacker implements ITacker {
    @Override
    public Boolean tacker(String flowData) {
        if (StringUtils.isNotBlank(flowData)){
            try {
                Map<String, Object> flowDataMap = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().readValue(flowData, JacksonType.MAP_OF_OBJECT);
                String doctorPigEvent = (String) flowDataMap.get("event");
                if (StringHelper.isNotBlank(doctorPigEvent)){
                    Map<String, Object> doctorPigEventMap = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().readValue(doctorPigEvent, JacksonType.MAP_OF_OBJECT);
                    String currentMatingCountStr = (String) doctorPigEventMap.get("currentMatingCount");
                    if (StringUtils.isNotBlank(currentMatingCountStr)){
                        Integer currentMatingCount = Integer.parseInt(currentMatingCountStr);
                        if (currentMatingCount < 3){
                            return true;
                        }
                    }
                }
            }catch (Exception e){
                log.error("getTaskEvents  failed cause by {}", Throwables.getStackTraceAsString(e));
            }

        }
        return false;
    }
}
