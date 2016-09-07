package io.terminus.doctor.schedule.msg.producer.factory;

import com.google.api.client.util.Maps;
import com.google.common.base.Throwables;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
        jsonData.put("weight", pigDto.getWeight());
        jsonData.put("parity", pigDto.getParity());
        jsonData.put("matingDate", getBreedingDate(pigDto));
        jsonData.put("timeDiff", timeDiff);
        jsonData.put("url", url);
        if (pigDto.getOperatorName() != null) {
            jsonData.put("operatorName", pigDto.getOperatorName());
        }
        if (pigDto.getEventDate() != null){
            jsonData.put("eventTime", pigDto.getEventDate());
        }
        if (pigDto.getReason() != null){
            jsonData.put("reason", pigDto.getReason());
        }
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

//    /**
//     * 获取预产期
//     */
//    public String getBirthDate(DoctorPigInfoDto pigDto) {
//        // 获取预产期
//        try{
//            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
//            if (StringUtils.isNotBlank(pigDto.getExtraTrack())) {
//                Map map = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().readValue(pigDto.getExtraTrack(), Map.class);
//                // @see DoctorMatingDto
//                Long dateMillis = (Long) map.get("judgePregDate");
//                if (dateMillis != null) {
//                    return formatter.print(dateMillis);
//                } else {
//                    // 获取配种日期
//                    dateMillis = (Long) map.get("matingDate");
//                    if (dateMillis != null) {
//                        // 配种日期 + 3 个月返回
//                        return formatter.print(new DateTime(dateMillis).plusMonths(3));
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("[PigDtoFactory] get birth date failed, pigDto is {}", pigDto);
//        }
//        return "";
//    }
}
