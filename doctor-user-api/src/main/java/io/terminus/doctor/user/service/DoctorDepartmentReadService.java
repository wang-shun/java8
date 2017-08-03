package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.DoctorDepartmentDto;
import io.terminus.doctor.user.model.DoctorFarm;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Created by xjn on 17/7/19.
 * 部门关系读取
 */
public interface DoctorDepartmentReadService {

    /**
     * 根据公司id查询所有猪场信息列表(子公司或者父公司)
     * @param orgId 公司id
     * @return 猪场信息列表
     */
    Response<List<DoctorFarm>> findAllFarmsByOrgId(@NotNull(message = "orgId.not.null") Long orgId);

    /**
     * 获取部门结构树
     * @param orgId 根节点
     * @return 集团数
     */
    Response<DoctorDepartmentDto> findCliqueTree(@NotNull(message = "orgId.not.null") Long orgId);

    /**
     * 可绑定的在次公司下的公司列表
     * @param orgId 父公司id
     * @return 公司列表
     */
    Response<List<DoctorDepartmentDto>> availableBindDepartment(@NotNull(message = "orgId.not.null") Long orgId);

    /**
     * 分页获取集团树
     * @param criteria 查询条件
     * @param pageSize 分页大小
     * @param pageNo 页码
     * @return 分页数据
     */
    Response<Paging<DoctorDepartmentDto>> pagingCliqueTree(Map<String, Object> criteria, Integer pageSize, Integer pageNo);

    /**
     * 获取部门所在集团
     * @param departmentId 部门id
     * @param isFarm 是否是猪场
     * @return 集团
     */
    Response<DoctorDepartmentDto> findClique(Long departmentId, Boolean isFarm);
}
