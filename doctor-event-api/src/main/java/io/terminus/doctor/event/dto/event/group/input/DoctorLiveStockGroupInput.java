package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Desc: 猪只存栏事件录入信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorLiveStockGroupInput extends BaseGroupInput implements Serializable {
    private static final long serialVersionUID = 510561556213800405L;

    /**
     * 平均体重(单位: 千克)
     */
    @NotNull(message = "avgWeight.not.null")
    private Double avgWeight;
}
