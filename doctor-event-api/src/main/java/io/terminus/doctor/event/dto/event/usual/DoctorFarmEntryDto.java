package io.terminus.doctor.event.dto.event.usual;

import io.terminus.doctor.event.dto.event.AbstractPigEventInputDto;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.model.DoctorPig;
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
 * Date:2016-05-20
 * Email:yaoqj@terminus.io
 * Descirbe: 猪进厂事件dto
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorFarmEntryDto extends AbstractPigEventInputDto implements Serializable{

    private static final long serialVersionUID = -3221757737932679045L;

    /**
     * 公猪 & 母猪
     * @see io.terminus.doctor.event.model.DoctorPig.PIG_TYPE
     */
    private Integer pigType;

    private String pigCode; // pig code 猪 编号

    private Date birthday; // 猪生日

    private Date inFarmDate; // 进厂时间

    private Long barnId;    // 进仓猪舍Id

    private String barnName;    // 进仓猪舍名称

    /**
     * 不同的数据源方式
     * @see io.terminus.doctor.event.enums.PigSource
     */
    private Integer source;

    private Long breed; //品种Id （basic Info）

    private String breedName;   //品种名称

    private Long breedType;     //品系Id  (basic info)

    private String breedTypeName; //品系名称

    private String fatherCode;  // 父类Code （非必填）

    private String motherCode;  // 母Code （非必填）

    private String entryMark;   // 非必填

    // boar (公猪进厂字段)
    /**
     *
     * @see io.terminus.doctor.event.enums.BoarEntryType
     */
    private Integer boarTypeId;

    private String boarTypeName;

    // sow
    private String earCode; // 耳缺号

    private Integer parity; // 当前胎次

    private Integer left;   //左乳头的数量

    private Integer right;  //右乳头数量

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(pigType != null){
            DoctorPig.PIG_TYPE pig = DoctorPig.PIG_TYPE.from(pigType);
            if(pig != null){
                map.put("猪类型", pig.getDesc());
            }
        }
        if(pigCode != null){
            map.put("猪编号", pigCode);
        }
        if(barnName != null){
            map.put("进场猪舍", barnName);
        }
        if(source != null){
            PigSource pigSource = PigSource.from(source);
            if(pigSource != null){
                map.put("来源", pigSource.getDesc());
            }
        }
        if(breedName != null){
            map.put("品种", breedName);
        }
        if(breedTypeName != null){
            map.put("品系", breedTypeName);
        }
        if(boarTypeName != null){
            map.put("公猪进场类型", boarTypeName);
        }
        if(earCode != null){
            map.put("耳缺号", earCode);
        }
        if(parity != null){
            map.put("胎次", parity.toString());
        }
        if(left != null){
            map.put("左乳头数", left.toString());
        }
        if(right != null){
            map.put("右乳头数", right.toString());
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.inFarmDate;
    }

}
