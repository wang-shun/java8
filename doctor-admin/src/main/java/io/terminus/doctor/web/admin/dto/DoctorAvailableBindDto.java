package io.terminus.doctor.web.admin.dto;

import io.terminus.doctor.user.dto.DoctorDepartmentDto;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xjn on 17/7/20.
 */
@Data
public class DoctorAvailableBindDto implements Serializable{

    private static final long serialVersionUID = 3193407423047813491L;

    private DoctorDepartmentDto doctorDepartmentDto;

    private List<DoctorDepartmentDto> departmentDtoList;
}
