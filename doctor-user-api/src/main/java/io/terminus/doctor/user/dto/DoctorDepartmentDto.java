package io.terminus.doctor.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xjn on 17/7/19.
 * 部门
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorDepartmentDto implements Serializable{

    private static final long serialVersionUID = -7438154880602109253L;

    /**
     * 部门id(farmId、orgId)
     */
    private Long id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 所在部门树的层数 集团为1
     */
    private Integer level;

    /**
     * 子节点列表
     */
    private List<DoctorDepartmentDto> childrenList;
}
