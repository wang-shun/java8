package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xjn on 16/12/21.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorFarmEntryDtoList implements Serializable {
    private static final long serialVersionUID = -5405285433515914820L;
    List<DoctorFarmEntryDto> doctorFarmEntryDtos;
}
