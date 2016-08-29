package io.terminus.doctor.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Desc: 猪只数统计表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-06-03
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPigTypeStatistic implements Serializable {
    private static final long serialVersionUID = 3053973085443889095L;

    private Long id;
    
    /**
     * 公司id
     */
    private Long orgId;
    
    /**
     * 猪场id
     */
    private Long farmId;
    
    /**
     * 公猪数
     */
    private Integer boar;
    
    /**
     * 母猪数
     */
    private Integer sow;
    
    /**
     * 产房仔猪数
     */
    private Integer farrow;
    
    /**
     * 保育猪数
     */
    private Integer nursery;
    
    /**
     * 育肥猪数
     */
    private Integer fatten;

    /**
     * 后备猪数
     */
    private Integer houbei;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;

    /**
     * 设置 猪数量
     * @param pigType
     * @see io.terminus.doctor.event.model.DoctorPig.PIG_TYPE
     * @param pigCount
     */
    public void putPigTypeCount(Integer pigType, Integer pigCount){
        if(Objects.equals(pigType, DoctorPig.PIG_TYPE.BOAR.getKey())){
            this.boar = pigCount;
        }else if(Objects.equals(pigType, DoctorPig.PIG_TYPE.SOW.getKey())){
            this.sow = pigCount;
        }else {
            throw new IllegalArgumentException("input.pigType.error");
        }
    }
}
