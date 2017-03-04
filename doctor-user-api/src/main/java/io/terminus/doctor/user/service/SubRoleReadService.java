package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.SubRole;
import io.terminus.parana.user.auth.CustomRoleReadService;

import java.util.List;

/**
 * 子账号自定义角色读服务
 *
 * @author houly
 */
public interface SubRoleReadService extends CustomRoleReadService<SubRole> {

    /**
     * 通过 ID 查询
     *
     * @param id 主键 ID
     * @return 自定义角色
     */
    Response<SubRole> findById(Long id);

    /**
     * 通过 IDs 批量查询
     *
     * @param ids 主键 ID 列表
     * @return 自定义角色列表
     */
    Response<List<SubRole>> findByIds(List<Long> ids);

    /**
     * 通过主账号 ID 查询
     *
     * @param appKey 角色使用场景
     * @param userId 主账号 ID
     * @param status 角色状态
     * @return 自定义角色列表
     */
    Response<List<SubRole>> findByUserIdAndStatus(String appKey, Long userId, Integer status);

    /**
     * 通过猪场 ID 查询
     *
     * @param appKey 角色使用场景
     * @param farmId 猪场 ID
     * @param status 角色状态
     * @return 自定义角色列表
     */
    Response<List<SubRole>> findByFarmIdAndStatus(String appKey, Long farmId, Integer status);

    /**
     * 分页查询
     *
     * @param appKey 角色使用场景
     * @param userId 主账号 ID
     * @param status 角色状态
     * @param roleName 角色名称
     * @param pageNo 页码
     * @param size   查询个数
     * @return 自定义角色分页
     */
    Response<Paging<SubRole>> pagination(String appKey, Long userId, Integer status, String roleName, Integer pageNo, Integer size);
}
