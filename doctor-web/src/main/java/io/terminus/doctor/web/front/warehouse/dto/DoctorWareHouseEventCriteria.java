package io.terminus.doctor.web.front.warehouse.dto;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import io.terminus.common.utils.Splitters;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by xjn on 17/2/7.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorWareHouseEventCriteria implements Serializable {
    private static final long serialVersionUID = -4023389820142109692L;

    @NotNull(message = "farmId.not.null")
    private Long farmId;
    private Long warehouseId;
    private Long materialId;
    private Integer eventType;
    private String eventTypes;
    private Integer materilaType;
    private Long staffId;
    private String startAt;
    private String endAt;

    private List<Integer> types;

    public void setEventTypes(String eventTypes) {
        if (!Strings.isNullOrEmpty(eventTypes)) {
            this.eventTypes = eventTypes;
            types = Splitters.splitToInteger(eventTypes, Splitters.UNDERSCORE);
        }
    }
}
