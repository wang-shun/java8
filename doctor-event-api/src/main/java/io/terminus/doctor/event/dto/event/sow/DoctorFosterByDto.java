package io.terminus.doctor.event.dto.event.sow;

import com.google.common.collect.Maps;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by xjn on 17/1/8.
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorFosterByDto extends BasePigEventInputDto implements Serializable {
    private static final long serialVersionUID = 7364302744766671168L;
    /**
     * 被拼窝时间
     */
    @NotNull(message = "event.at.not.null")
    private Date fosterByDate;

    /**
     * 被拼窝数量
     */
    @Min(value = 0, message = "fosters.count.not.less.zero")
    @NotNull(message = "fosters.count.not.null")
    private Integer fosterByCount;

    private Integer sowFostersByCount;    // 拼窝母猪数量

    private Integer boarFostersByCount;   // 拼窝公猪数量

    private Double fosterByTotalWeight;   //拼窝总重量

    /**
     * 原母猪id
     */
    private Long fromSowId;

    /**
     * 原母猪号
     */
    private String fromSowCode;

    /**
     * 源猪群id
     */
    private Long fromGroupId;

    @Override
    public Map<String, String> descMap() {
        return Maps.newLinkedHashMap();
    }

    @Override
    public Date eventAt() {
        return fosterByDate;
    }

    @Override
    public String changeRemark() {
        return null;
    }
}
