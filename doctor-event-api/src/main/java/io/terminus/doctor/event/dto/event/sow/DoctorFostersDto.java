package io.terminus.doctor.event.dto.event.sow;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 仔猪拼窝事件(自动生成代哺的事件)
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorFostersDto extends BasePigEventInputDto implements Serializable{

    private static final long serialVersionUID = 2998287596879859648L;

    @NotNull(message = "event.at.not.null")
    private String fostersDate;   // 拼窝日期

    @Min(value = 0, message = "fosters.count.not.less.zero")
    @NotNull(message = "fosters.count.not.null")
    private Integer fostersCount;   //  拼窝数量

    private Integer sowFostersCount;    // 拼窝母猪数量

    private Integer boarFostersCount;   // 拼窝公猪数量

    private Double fosterTotalWeight;   //拼窝总重量

    private Long fosterReason;  //寄养原因id

    private String fosterReasonName;  //寄养原因名称

    @NotNull(message = "fosters.sow.id.not.null")
    private Long fosterSowId;   // 拼窝母猪Id

    @NotEmpty(message = "fosters.sow.code.not.empty")
    private String fosterSowCode; // 拼窝母猪code

    private String fosterSowOutId; // 拼窝母猪outId(做关联用)

    private String fosterRemark;    // 拼窝标识

    @Override
    public Map<String, String> descMap(){
        Map<String, String> map = new HashMap<>();

        if(fosterSowCode != null){
            map.put("被拼窝母猪", fosterSowCode);
        }
        if(fostersCount != null){
            map.put("拼窝仔猪数", fostersCount.toString()+"头");
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return DateUtil.toDate(fostersDate);
    }

    @Override
    public String changeRemark() {
        return this.fosterRemark;
    }
}
