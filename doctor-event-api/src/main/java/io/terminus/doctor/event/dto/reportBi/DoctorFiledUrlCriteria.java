package io.terminus.doctor.event.dto.reportBi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

/**
 * Created by xjn on 18/1/14.
 * email:xiaojiannan@terminus.io
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorFiledUrlCriteria {
    private Integer value;
    private String filedName;
    private Long farmId;
    private Integer pigType;
    private String start;
    private String end;
}
