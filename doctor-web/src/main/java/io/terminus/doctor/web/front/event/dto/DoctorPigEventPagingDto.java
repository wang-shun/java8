package io.terminus.doctor.web.front.event.dto;

import io.terminus.common.model.Paging;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by xiao on 16/10/9.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPigEventPagingDto implements Serializable{
    private static final long serialVersionUID = 4841965830148105226L;
    private Paging<DoctorPigEvent> paging;
    private Long canRollback;
}
