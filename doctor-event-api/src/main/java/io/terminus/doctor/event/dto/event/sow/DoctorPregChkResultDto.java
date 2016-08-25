package io.terminus.doctor.event.dto.event.sow;

import io.terminus.doctor.event.enums.PregCheckResult;
import lombok.AllArgsConstructor;
import lombok.Data;
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
 * Descirbe: 母猪妊娠检查事件
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPregChkResultDto implements Serializable{

    private static final long serialVersionUID = 2879901632920960216L;

    private Date checkDate; //妊娠检查日期

    /**
     * @see io.terminus.doctor.event.enums.PregCheckResult
     */
    private Integer checkResult;    // 妊娠检查结果

    private String checkMark;   // 校验标识

    public Map<String, String> descMap(){
        Map<String, String> map = new HashMap<>();
        PregCheckResult result = PregCheckResult.from(checkResult);
        if(result != null){
            map.put("检查结果", result.getDesc());
        }
        return map;
    }
}
