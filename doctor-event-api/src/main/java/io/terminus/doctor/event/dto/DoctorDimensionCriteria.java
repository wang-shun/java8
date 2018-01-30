package io.terminus.doctor.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 18/1/11.
 * email:xiaojiannan@terminus.io
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorDimensionCriteria implements Serializable{
    private static final long serialVersionUID = -2475954192377211983L;
    /**
     * 组织id
     */
    private Long orzId;
    /**
     * 组织维度
     * @see io.terminus.doctor.event.enums.OrzDimension
     */
    private Integer orzType;

    private String orzDimensionName;

    /**
     * 统计时间
     */
    private Date sumAt;

    /**
     * 时间维度
     * @see io.terminus.doctor.event.enums.DateDimension
     */
    private Integer dateType;

    private String dateDimensionName;

    private Integer pigType;

    private Date startAt;

    private Date endAt;

    public DoctorDimensionCriteria(Long orzId, Integer orzType, Date sumAt, Integer dateType, Integer pigType) {
        this.orzId = orzId;
        this.orzType = orzType;
        this.sumAt = sumAt;
        this.dateType = dateType;
        this.pigType = pigType;
    }

    public DoctorDimensionCriteria(Long orzId, Integer orzType, Date sumAt, Integer dateType) {
        this(orzId, orzType, sumAt, dateType, null);
    }
}
