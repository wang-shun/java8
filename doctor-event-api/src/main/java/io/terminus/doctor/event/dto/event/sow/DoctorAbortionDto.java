package io.terminus.doctor.event.dto.event.sow;

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
 * Date:2016-06-02
 * Email:yaoqj@terminus.io
 * Descirbe: doctor 流产事件
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorAbortionDto implements Serializable{

    private static final long serialVersionUID = -8197476307563535842L;

    private Date abortionDate;  // 流产日期

    private Long abortionReasonId;  // 流产原因

    private String abortionReasonName;  //流产原因

    public Map<String, String> descMap(){
        Map<String, String> map = new HashMap<>();
        if(abortionReasonName != null){
            map.put("流产原因", abortionReasonName);
        }
        return map;
    }
}
