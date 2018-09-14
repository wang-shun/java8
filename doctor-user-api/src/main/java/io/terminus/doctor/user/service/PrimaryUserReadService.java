package io.terminus.doctor.user.service;

import com.google.common.base.Optional;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.parana.user.model.User;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

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
     * @param farmId 猪场id
     * @param roleId 角色ID ID
     * @param status 子账户绑定状态
     * @param pageNo 页号
     * @param size   查询数量
     * @return 子账户分页
     */
    Response<Paging<Sub>> subPagination(Long farmId, Long roleId, String roleName, String userName,
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

    /**
     * 根据id 获取子账号
     * @param subId 子账号id
     * @return 子账号
     */
    Response<Sub> findSubById(@NotNull(message = "subId.not.null") Long subId);

    /**
     * 更新sub
     * @param sub 需要更新的sub
     * @return
     */
    Response<Boolean> updateSub(Sub sub);

    /**
     * 根据猪场id获取子账号列表
     * @param farmId 猪场id
     * @return 子账号列表
     */
    Response<List<Sub>> findSubsByFarmId(@NotNull(message = "farm.id.not.null") Long farmId);

    /**
     * 根据猪场id和员工状态查询
     * @param farmId 猪场id
     * @param status 员工状态
     * @return 员工列表
     */
    Response<List<Sub>> findSubsByFarmIdAndStatus(@NotNull(message = "farm.id.not.null") Long farmId, Integer status, Long userId);

    // 软件登陆人员是谁，仓库单据操作人就默认是谁，并支持修改 （陈娟 2018-09-13）
    Response<Sub> findSubsByFarmIdAndStatusAndUserId(@NotNull(message = "farm.id.not.null") Long farmId, Integer status, Long userId);

    /**
     * 获取关联猪场主账号列表
     * @param farmId 猪场id
     * @return 主账号
     */
    Response<PrimaryUser> findPrimaryByFarmId(@NotNull(message = "farm.id.not.null") Long farmId);

    Response<PrimaryUser> findPrimaryByFarmIdAndStatus(@NotNull(message = "farm.id.not.null") Long farmId, Integer status);

    /**
     * 获取所有猪场id与username映射
     * @return 映射map
     */
    Response<Map<Long, String>> findFarmIdToUserName();

    Response<PrimaryUser> findByUserId(@NotNull(message = "user.id.not.null") Long userId);

    /**
     * 分页查询开通猪博士服务的用户
     * @param id
     * @param name
     * @param email
     * @param mobile
     * @param status
     * @param type
     * @param pageNo
     * @param pageSize
     * @return
     */
    Response<Paging<User>> pagingOpenDoctorServiceUser(Long id, String name, String email, String mobile, Integer status, Integer type, Integer pageNo, Integer pageSize);

}
