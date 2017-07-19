package io.terminus.doctor.user.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xjn on 17/7/19.
 * 部门
 */
@Data
public class DoctorDepartmentDto implements Serializable{

    private static final long serialVersionUID = -7438154880602109253L;

    /**
     * 部门id(farmId、orgId)
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 子节点列表
     */
    private List<DoctorDepartmentDto> childrenList;
}
