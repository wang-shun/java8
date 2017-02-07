package io.terminus.doctor.msg.dto;

import com.google.common.base.Strings;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.msg.model.DoctorMessage;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xiao on 16/9/18.
 */
@Data
public class DoctorMessageSearchDto extends DoctorMessage {
    private static final long serialVersionUID = 1616433046730343439L;

    /**
     * 消息状态列表
     */
    private String statuses;

    private List<Integer> statusList;

    /**
     * 消息类型列表
     */
    private List<Integer> types;

    /**
     * 消息ids
     */
    private List<Long> ids;

    private String eventStartAt;

    private String eventEndAt;

    private String otherStartAt;

    private String otherEndAt;

    private String sortBy;

    private String desc;

    public void setStatuses(String statuses) {
        if (!Strings.isNullOrEmpty(statuses)) {
            this.statuses = statuses;
            this.statusList = Splitters.UNDERSCORE.splitToList(statuses)
                    .stream().map(Integer::parseInt).collect(Collectors.toList());
        }
    }
}
