package io.terminus.doctor.event.dto.event.sow;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 窝重测量事件
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorLitterWeightDto extends BasePigEventInputDto implements Serializable {

    private static final long serialVersionUID = -3360271781401622417L;

    private Long pigId;

    private Date measureDate;

    private Integer pigletsCount;   // !! 不可变的数量， 校验

    private Double nestWeight;

    private String remark;

    private String pigCode;

    private Long barnId;

    private String barnName;

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        return map;
    }

    @Override
    public Date eventAt() {
        return this.measureDate;
    }
}
