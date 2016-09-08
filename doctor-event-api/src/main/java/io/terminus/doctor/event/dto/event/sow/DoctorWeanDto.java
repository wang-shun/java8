package io.terminus.doctor.event.dto.event.sow;

import io.terminus.doctor.event.dto.event.AbstractPigEventInputDto;
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
 * Descirbe: 断奶事件
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWeanDto extends AbstractPigEventInputDto implements Serializable {

    private static final long serialVersionUID = -8375069125388951376L;

    private Date weanDate;  // 断奶日期

    private Integer pigletsCount;   // 断奶数量

    private Double avgWeight;   //平均断奶重量

    private String weanRemark;  //断奶标识

    private Integer qualifiedCount; // 合格数量

    private Integer notQualifiedCount; //不合格的数量

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        return map;
    }

    @Override
    public Date eventAt() {
        return this.weanDate;
    }
}
