package io.terminus.doctor.event.dto.event.sow;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.enums.PregCheckResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import javax.validation.constraints.NotNull;
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
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPregChkResultDto extends BasePigEventInputDto implements Serializable{

    private static final long serialVersionUID = 2879901632920960216L;

    @NotNull(message = "event.at.not.null")
    private Date checkDate; //妊娠检查日期
    /**
     * @see io.terminus.doctor.event.enums.PregCheckResult
     */
    @NotNull(message = "check.result.not.null")
    private Integer checkResult;    // 妊娠检查结果

    private Long abortionReasonId;  // 流产原因

    private String abortionReasonName;  //流产原因

    private String checkMark;

//    private String pigCode;
//
//    private Long barnId;
//
//    private String barnName;

    @Override
    public Map<String, String> descMap(){
        Map<String, String> map = new HashMap<>();
        if(checkResult != null){
            PregCheckResult result = PregCheckResult.from(checkResult);
            if(result != null){
                map.put("检查结果", result.getDesc());
            }
            if(abortionReasonName != null){
                map.put("流产原因", abortionReasonName);
            }
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.checkDate;
    }
}
