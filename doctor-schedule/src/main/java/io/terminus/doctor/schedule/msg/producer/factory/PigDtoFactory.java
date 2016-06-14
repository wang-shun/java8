package io.terminus.doctor.schedule.msg.producer.factory;

import com.google.api.client.util.Maps;
import com.google.common.base.Throwables;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.util.Date;
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
    public Map<String, Object> createPigMessage(DoctorPigInfoDto pigDto, Double timeDiff, String url) {
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
        jsonData.put("parity", pigDto.getParity());
        jsonData.put("matingDate", getBreedingDate(pigDto));
        jsonData.put("judgePregDate", getBirthDate(pigDto));
        jsonData.put("timeDiff", timeDiff);
        jsonData.put("url", url);
        return jsonData;
    }

    /**
     * 获取配种日期
     */
    public Date getBreedingDate(DoctorPigInfoDto pigDto) {
        // 获取配种日期
        try {
            // @see DoctorMatingDto
            return new Date((Long) JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper()
                    .readValue(pigDto.getExtraTrack(), Map.class).get("matingDate"));
        } catch (Exception e) {
            log.error("[SowBirthDateProducer] get birth date failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    /**
     * 获取预产期
     */
    public Date getBirthDate(DoctorPigInfoDto pigDto) {
        // 获取预产期
        try{
            Map map = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().readValue(pigDto.getExtraTrack(), Map.class);
            // @see DoctorMatingDto
            Date date = new Date((Long) map.get("judgePregDate"));
            if (date != null) {
                return date;
            } else {
                // 获取配种日期
                date = new Date((Long) map.get("matingDate"));
                if (date != null) {
                    // 配种日期 + 3 个月返回
                    return new DateTime(date).plusMonths(3).toDate();
                }
            }
        } catch (Exception e) {
            log.error("[SowBirthDateProducer] get birth date failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }
}
