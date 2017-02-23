package io.terminus.doctor.event.dto.event.usual;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.model.DoctorPig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
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
public class DoctorFarmEntryDto extends BasePigEventInputDto implements Serializable{

    private static final long serialVersionUID = -3221757737932679045L;


    @NotNull(message = "birthday.not.null")
    private Date birthday; // 猪生日

    @NotNull(message = "event.at.not.null")
    private Date inFarmDate; // 进厂时间

    /**
     * 不同的数据源方式
     * @see io.terminus.doctor.event.enums.PigSource
     */
    @NotNull(message = "source.not.null")
    private Integer source;

    @NotNull(message = "breed.not.null")
    private Long breed; //品种Id （basic Info）

    private String breedName;   //品种名称

    private Long breedType;     //品系Id  (basic info)

    private String breedTypeName; //品系名称

    private String fatherCode;  // 父类Code （非必填）

    private String motherCode;  // 母Code （非必填）

    private String entryMark;   // 非必填

    /**
     * 公猪进场事件类型
     * @see io.terminus.doctor.event.enums.BoarEntryType
     */
    private Integer boarType;

    private String boarTypeName;

    private Double weight;

    private String earCode; // 耳缺号

    private Integer parity; // 当前胎次

    private Integer left;   //左乳头的数量

    private Integer right;  //右乳头数量

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new LinkedHashMap<>();
        if(breedName != null){
            map.put("品种", breedName);
        }
        if(breedTypeName != null){
            map.put("品系", breedTypeName);
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.inFarmDate;
    }

}
