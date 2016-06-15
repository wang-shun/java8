package io.terminus.doctor.schedule.msg.producer.factory;

import com.google.api.client.util.Maps;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;

import java.util.Map;

/**
 * Desc: 猪群详情制造类
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/14
 */
public class GroupDetailFactory {

    private static GroupDetailFactory groupDetailFactory = new GroupDetailFactory();

    private GroupDetailFactory() {}

    public static GroupDetailFactory getInstance() {
        return groupDetailFactory;
    }

    public Map<String, Object> createPigMessage(DoctorGroupDetail detail, Double timeDiff, String url) {
        DoctorGroup group = detail.getGroup();
        DoctorGroupTrack track = detail.getGroupTrack();
        // 创建数据
        Map<String, Object> jsonData = Maps.newHashMap();
        jsonData.put("groupId", group.getId());
        jsonData.put("farmId", group.getFarmId());
        jsonData.put("farmName", group.getFarmName());
        jsonData.put("groupCode", group.getGroupCode());
        jsonData.put("pigType", group.getPigType());
        jsonData.put("quantity", track.getQuantity());
        jsonData.put("avgDayAge", track.getAvgDayAge());
        jsonData.put("avgWeight", track.getAvgWeight());
        jsonData.put("url", url);
        return jsonData;
    }
}
