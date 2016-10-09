package io.terminus.doctor.web.front.event.dto;

import io.terminus.common.model.Paging;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by xiao on 16/10/9.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorGroupEventPagingDto implements Serializable {
    private static final long serialVersionUID = 3212146298819685552L;
    private Paging<DoctorGroupEvent> paging;
    private Long canRollback;
}
