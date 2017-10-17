package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.IotRole;
import io.terminus.doctor.user.model.IotUser;

import java.util.List;

/**
 * Created by xjn on 17/10/11.
 */
public interface IotUserRoleReadService {

    /**
     * 分页查询物联网用户
     * @param realName 真实姓名
     * @param statuses 用户状态
     * @param pageNo 页码
     * @param pageSize 页尺寸
     * @return
     */
    Response<Paging<IotUser>> paging(String realName, List<Integer> statuses,
                                     Integer pageNo, Integer pageSize);

    /**
     * 获取所有有效物联网角色
     * @return 所有物联网角色
     */
    Response<List<IotRole>> listEffected();

    /**
     * 根据角色id查询物联网角色
     * @param id 角色id
     * @return 物联网角色
     */
    Response<IotRole> findIotRoleById(Long id);

    /**
     * 根据关联关系id查询用户与角色关联关系"
     * @param id 关联关系id
     * @return 关联关系
     */
    Response<IotUser> findIotUserRoleById(Long id);

}
