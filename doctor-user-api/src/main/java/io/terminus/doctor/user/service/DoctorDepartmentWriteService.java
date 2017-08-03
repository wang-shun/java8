package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;

import java.util.List;

/**
 * Created by xjn on 17/7/19.
 * 部门关系写入
 */
public interface DoctorDepartmentWriteService {

    /**
     * 绑定部门关系
     * @param parentId 父节点id
     * @param orgIds 子节点列表
     * @return
     */
    Response<Boolean> bindDepartment(Long parentId, List<Long> orgIds);

    /**
     * 解绑部门关系
     * @param orgId 节点id
     * @return
     */
    Response<Boolean> unbindDepartment(Long orgId);
}
