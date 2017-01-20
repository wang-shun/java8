package io.terminus.doctor.event.dto;

import com.google.common.base.Joiner;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.enums.PigEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 基本的用户录入信息内容(信息录入基本字段信息)
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorBasicInputInfoDto implements Serializable{

    private static final long serialVersionUID = 3753583575280390916L;

    // usual farm info
    private Long farmId;

    private String farmName;

    private Long orgId;

    private String orgName;

    private Long staffId;

    private String staffName;

    /**
     * 断奶事件时，母猪关联的猪群id
     */
    private Long weanGroupId;

    /**
     * @see io.terminus.doctor.event.enums.IsOrNot
     */

    private Boolean need; //逻辑判断字段

    /**
     * 相同宿舍, 不同pig构建
     * @param pigId
     * @param pigType
     * @param pigCode
     * @return
     */
    public DoctorBasicInputInfoDto buildSameBarnPigInfo(Long pigId, Integer pigType, String pigCode){
        return DoctorBasicInputInfoDto.builder()
                .farmId(this.farmId).farmName(this.farmName).orgId(this.orgId).orgName(this.orgName).staffId(this.staffId).staffName(this.staffName)
                .build();
    }

    public String generateEventDescFromExtra(BasePigEventInputDto inputDto){
        if(inputDto.getEventType() == null){
            return inputDto.getEventDesc();
        }
        PigEvent pigEvent = PigEvent.from(inputDto.getEventType());
        if(pigEvent == null){
            return inputDto.getEventDesc();
        }

        //BasePigEventInputDto baseDto = transFromPigEventAndExtra(pigEvent, inputDto);
        Map<String, String> fieldMap = inputDto.descMap();
        if(Objects.equals(pigEvent, PigEvent.FOSTERS_BY)){
            // 被拼窝的母猪的描述中按理说应当带上"拼窝来源母猪", 但是 dto 中没有这个字段, 那就把"被拼窝母猪"这个字段去掉, 别让客户注意到...嘻嘻~~~
            fieldMap.remove("被拼窝母猪");
        }
        if(inputDto.getOperatorName() != null){
            fieldMap.put("操作人", inputDto.getOperatorName());
        }
        return Joiner.on("#").withKeyValueSeparator("：").join(fieldMap);
    }
}
