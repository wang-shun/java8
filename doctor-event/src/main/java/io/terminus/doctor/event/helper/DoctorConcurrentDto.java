package io.terminus.doctor.event.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by xjn on 17/10/30.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorConcurrentDto {

    private String value;
    private Integer count;
}
