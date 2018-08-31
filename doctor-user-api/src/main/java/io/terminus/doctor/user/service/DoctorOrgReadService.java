package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorOrg;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface DoctorOrgReadService {

    /**
     * 根据公司id查询公司信息
     * @param orgId 公司id
     * @return 公司信息
     */
    Response<DoctorOrg> findOrgById(Long orgId);

    /**
     * 根据公司id查询公司信息
     * @param orgIds 公司ids
     * @return 公司信息
     */
    Response<List<DoctorOrg>> findOrgByIds(List<Long> orgIds);

    /**
     * 根据用户id查询有权限的公司
     * @param userId 用户id
     * @return 公司列表
     */
    Response<List<DoctorOrg>> findOrgsByUserId(@NotNull(message = "userId.not.null") Long userId);

    /**
     * 查询所有公司
     * @return 所有公司
     */
    Response<List<DoctorOrg>> findAllOrgs();

    /**
     * 查询父公司下所有子公司
     * @param parentId 父公司id
     * @return 子公司列表
     */
    Response<List<DoctorOrg>> findOrgByParentId(Long parentId);

    Response<DoctorOrg>  findGroupcompanyByOrgId(Long orgId);

    /**
     * 根据公司名字模糊搜索公司
     * @param fuzzyName
     * @param type 公司类型
     * @return
     */
    Response<List<DoctorOrg>> suggestOrg(String fuzzyName, Integer type);

    /**
     * 分页查询
     * @param criteria 查询条件
     * @param pageSize 分页大小
     * @param pageNo 页码
     * @return 分页结果
     */
    Response<Paging<DoctorOrg>> paging(Map<String, Object> criteria, Integer pageSize, Integer pageNo);

    /**
     * 根据公司名称查询公司
     * @param name
     * @return
     */
    Response<DoctorOrg> findByName(String name);

    /**
     * 用户审核通过后把公司的parent_id置为0、type置为2(此方法已作废)
     * @param id
     * @return
     */
    Response<Boolean> updateOrgPidTpye(Long id);

    /**
     * 通过集团查公司(孔景军)
     * @param orgIds
     * @param orgId
     * @return
     */
    Response<List<DoctorOrg>>  findOrgByGroup(List<Long> orgIds,Long groupId);

    /**
     * 通过用户id查用户类型
     * @param userId
     * @return
     */
    Integer  findUserTypeById(Long userId);
    List<Map<String,Object>> getOrgcunlan(Long groupId,List<Long> orgIds);
    Map<Object,String> getGroupcunlan(Long groupId);

    /**
     * 员工查询（yusq）1
     */
    Response staffQuery(Map<String, Object> params);

}