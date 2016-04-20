package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.Operator;

/**
 * 运营信息读服务
 *
 * @author Effet
 */
public interface OperatorReadService {

    /**
     * 通过用户 ID 查询运营信息
     *
     * @param userId 用户 ID
     * @return 运营信息
     */
    Response<Operator> findByUserId(Long userId);

    /**
     * 运营分页
     *
     * @param roleId 运营角色 ID
     * @param status 运营用户状态
     * @param pageNo 页码
     * @param size   查询数量
     * @return 运营分页列表
     */
    Response<Paging<Operator>> pagination(Long roleId, Integer status, Integer pageNo, Integer size);
}
