package io.terminus.doctor.event.event;

import io.terminus.doctor.event.dto.DoctorRollbackDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xjn on 16/11/9.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListenedRollbackEvent implements Serializable {
    private static final long serialVersionUID = -4078579039992328341L;
    private List<DoctorRollbackDto> doctorRollbackDtos;
}
