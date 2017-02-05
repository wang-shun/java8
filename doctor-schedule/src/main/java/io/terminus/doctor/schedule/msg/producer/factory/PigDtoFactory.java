package io.terminus.doctor.schedule.msg.producer.factory;

import com.google.api.client.util.Maps;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;

import java.util.Map;

/**
 * Desc: 猪Dto消息创建
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/3
 */
@Slf4j
public class PigDtoFactory {

    private static PigDtoFactory pigDtoFactory = new PigDtoFactory();

    private PigDtoFactory() {
    }

    public static PigDtoFactory getInstance() {
        return pigDtoFactory;
    }

    /**
     * 创建猪类的消息
     */
    public Map<String, Object> createPigMessage(DoctorPigInfoDto pigDto, Double timeDiff, Double ruleTimeDiff, String url) {
        // 创建消息
        Map<String, Object> jsonData = Maps.newHashMap();
        jsonData.put("pigId", pigDto.getPigId());
        jsonData.put("pigCode", pigDto.getPigCode());
        jsonData.put("updatedAt", pigDto.getUpdatedAt());
        jsonData.put("barnName", pigDto.getBarnName());
        jsonData.put("barnId", pigDto.getBarnId());
        jsonData.put("farmId", pigDto.getFarmId());
        jsonData.put("farmName", pigDto.getFarmName());
        jsonData.put("status", pigDto.getStatus());
        jsonData.put("statusName", pigDto.getStatusName());
        jsonData.put("dateAge", pigDto.getDateAge());
        jsonData.put("weight", pigDto.getWeight());
        jsonData.put("parity", pigDto.getParity());
        jsonData.put("matingDate", getBreedingDate(pigDto));
        //事件发生多少天
        jsonData.put("timeDiff", timeDiff);
        jsonData.put("ruleTimeDiff", ruleTimeDiff);
        jsonData.put("url", url);
        return jsonData;
    }

    /**
     * 获取配种日期
     */
    public String getBreedingDate(DoctorPigInfoDto pigDto) {
        // 获取配种日期
        try {
            // @see DoctorMatingDto
            if (StringUtils.isNotBlank(pigDto.getExtraTrack())) {
                Map<String, Object> map = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().readValue(pigDto.getExtraTrack(), JacksonType.MAP_OF_OBJECT);
                if (map.get("matingDate") != null){
                    return DateTimeFormat.forPattern("yyyy-MM-dd").print((Long) map.get("matingDate"));
                }

            }
        } catch (Exception e) {
            log.error("[PigDtoFactory] get Breeding date failed, pigDto is {}", pigDto);
        }
        return "";
    }
}
