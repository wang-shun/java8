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
 * Date:2016-06-02
 * Email:yaoqj@terminus.io
 * Descirbe: doctor 流产事件
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorAbortionDto extends AbstractPigEventInputDto implements Serializable{

    private static final long serialVersionUID = -8197476307563535842L;

    private Date abortionDate;  // 流产日期

    private Long abortionReasonId;  // 流产原因

    private String abortionReasonName;  //流产原因

    @Override
    public Map<String, String> descMap(){
        Map<String, String> map = new HashMap<>();
        if(abortionReasonName != null){
            map.put("流产原因", abortionReasonName);
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.abortionDate;
    }
}
