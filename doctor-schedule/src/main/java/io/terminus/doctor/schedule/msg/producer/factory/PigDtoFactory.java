package io.terminus.doctor.schedule.msg.producer.factory;

import com.google.api.client.util.Maps;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;

import java.util.Map;

/**
 * Desc: 猪Dto消息创建
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/3
 */
public class PigDtoFactory {

    private static PigDtoFactory pigDtoFactory = new PigDtoFactory();

    private PigDtoFactory(){}

    public static PigDtoFactory getInstance() {
        return pigDtoFactory;
    }

    public Map<String, Object> createPigMessage(DoctorPigInfoDto pigDto, Double timeDiff, String url) {
        // 创建消息
        Map<String, Object> jsonData = Maps.newHashMap();
        jsonData.put("pigId", pigDto.getPigId());
        jsonData.put("pigCode", pigDto.getPigCode());
        jsonData.put("updatedAt",pigDto.getUpdatedAt());
        jsonData.put("barnName",pigDto.getBarnName());
        jsonData.put("barnId",pigDto.getBarnId());
        jsonData.put("farmId",pigDto.getFarmId());
        jsonData.put("farmName",pigDto.getFarmName());
        jsonData.put("status",pigDto.getStatus());
        jsonData.put("statusName",pigDto.getStatusName());
        jsonData.put("timeDiff", timeDiff);
        jsonData.put("url", url);
        return jsonData;
    }
}
