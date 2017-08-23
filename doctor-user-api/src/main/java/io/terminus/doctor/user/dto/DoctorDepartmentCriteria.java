package io.terminus.doctor.user.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xjn on 17/7/19.
 * 部门查询条件
 */
@Data
public class DoctorDepartmentCriteria implements Serializable{
    private static final long serialVersionUID = 5902955018922558955L;

    private Integer pageNo;

    private Integer pageSize;

    private String name;

    private List<Long> ids;
}
