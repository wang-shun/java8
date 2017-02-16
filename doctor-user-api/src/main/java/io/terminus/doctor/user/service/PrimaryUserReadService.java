package io.terminus.doctor.user.service;

import com.google.common.base.Optional;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;

import java.util.List;

/**
 * 主账号读服务
 *
 * @author Effet
 */
public interface PrimaryUserReadService {


    /**
     * 通过主账号 ID 和 子账户用户 ID 查询子账号关联
     *
     * @param parentUserId 主账号用户 ID
     * @param userId 子账户用户 ID
     * @return 子账号信息
     */
    Response<Optional<Sub>> findSubSellerByParentUserIdAndUserId(Long parentUserId, Long userId);

    /**
     * 分页子账户信息
     *
     * @param parentUserId 主账号用户 ID
     * @param roleId 角色ID ID
     * @param status 子账户绑定状态
     * @param pageNo 页号
     * @param size   查询数量
     * @return 子账户分页
     */
    Response<Paging<Sub>> subPagination(Long parentUserId, Long roleId, String roleName, String userName,
                                        String realName, Integer status, Integer pageNo, Integer size);

    /**
     * 多条件筛选, 相当于分页查询去掉了分页参数, 所有参数都可以为空
     * @param parentUserId 主账号用户 ID
     * @param roleId 角色ID ID
     * @param roleName
     * @param userName
     * @param realName
     * @param status 子账户绑定状态
     * @param limit 限制数量, 可为空
     * @return
     */
    Response<List<Sub>> findByConditions(Long parentUserId, Long roleId, String roleName, String userName,
                                         String realName, Integer status, Integer limit);

    /**
     * 获取所有审核通过的子账号
     * @return  子账户
     */
    Response<List<Sub>> findAllActiveSubs();

    /**
     * 根据子账号userId查询 主子账号关联信息
     * @param subUserId 子账号的userId
     * @return 主子账号关联信息,即表doctor_user_subs中的一行数据
     */
    Response<Sub> findSubByUserId(Long subUserId);

    /**
     * 获取所有主账号
     * @return
     */
    Response<List<PrimaryUser>> findAllPrimaryUser();

}
