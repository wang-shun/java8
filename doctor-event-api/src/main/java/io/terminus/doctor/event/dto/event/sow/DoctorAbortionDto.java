package io.terminus.doctor.event.dto.event.sow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

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

    private String abortionReason;  //流产原因
}
