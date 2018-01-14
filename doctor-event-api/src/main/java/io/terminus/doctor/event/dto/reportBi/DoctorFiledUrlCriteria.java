package io.terminus.doctor.event.dto.reportBi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;
import org.omg.PortableInterceptor.INACTIVE;

import java.util.Date;

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
    private Date start;
    private Date end;
}
