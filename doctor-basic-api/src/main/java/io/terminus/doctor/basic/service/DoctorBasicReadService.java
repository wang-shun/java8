package io.terminus.doctor.basic.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Desc: 基础数据读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */

public interface DoctorBasicReadService {

    /////////////////////////// 基础数据表 ///////////////////////////
    /**
     * 查询全部基础数据表
     * @return 基础数据表
     */
    Response<List<DoctorBasic>> findAllBasics();

    /**
     * 查询全部使用状态的基础数据表
     * @return 基础数据表
     */
    Response<List<DoctorBasic>> findAllValidBasics();

    /**
     * 根据id查询基础数据表
     * @param basicId 主键id
     * @return 基础数据表
     */
    Response<DoctorBasic> findBasicById(@NotNull(message = "basicId.not.null") Long basicId);

    /**
     * 根据ids查询基础数据表
     * @param basicIds 主键ids
     * @return 基础数据表
     */
    Response<List<DoctorBasic>> findBasicByIds(@NotNull(message = "basicId.not.null") List<Long> basicIds);

    /**
     * 根据基础数据类型和输入码查询(非缓存)
     * @param type  类型
     * @see io.terminus.doctor.basic.model.DoctorBasic.Type
     * @param srm   输入码
     * @return 基础数据信息
     */
    Response<List<DoctorBasic>> findBasicByTypeAndSrm(@NotNull(message = "type.not.null") Integer type,
                                                      @Nullable String srm);

    /**
     * 根据基础数据类型和输入码查询(非缓存)
     * @param type  类型
     * @see io.terminus.doctor.basic.model.DoctorBasic.Type
     * @param srm   输入码
     * @return 基础数据信息
     */
    Response<List<DoctorBasic>> findValidBasicByTypeAndSrm(@NotNull(message = "type.not.null") Integer type,
                                                      @Nullable String srm);

    /**
     * 根据基础数据类型和输入码查询(缓存)
     * @param type  类型
     * @see io.terminus.doctor.basic.model.DoctorBasic.Type
     * @param srm   输入码
     * @return 基础数据信息
     */
    Response<List<DoctorBasic>> findBasicByTypeAndSrmWithCache(@NotNull(message = "type.not.null") Integer type,
                                                               @Nullable String srm);

    /**
     * 根据id查询基础数据表 根据猪场id过滤
     * @param basicId 主键id
     * @return 基础数据表
     */
    Response<DoctorBasic> findBasicByIdFilterByFarmId(@NotNull(message = "farmId.not.null") Long farmId,
                                                      @NotNull(message = "basicId.not.null") Long basicId);

    /**
     * 根据基础数据类型和输入码查询(非缓存)
     * @param type  类型
     * @see io.terminus.doctor.basic.model.DoctorBasic.Type
     * @param srm   输入码
     * @return 基础数据信息
     */
    Response<List<DoctorBasic>> findBasicByTypeAndSrmFilterByFarmId(@NotNull(message = "farmId.not.null") Long farmId,
                                                                    @NotNull(message = "type.not.null") Integer type,
                                                                    @Nullable String srm);

    /**
     * 根据基础数据类型和输入码查询(缓存)
     * @param type  类型
     * @see io.terminus.doctor.basic.model.DoctorBasic.Type
     * @param srm   输入码
     * @return 基础数据信息
     */
    Response<List<DoctorBasic>> findBasicByTypeAndSrmWithCacheFilterByFarmId(@NotNull(message = "farmId.not.null") Long farmId,
                                                                             @NotNull(message = "type.not.null") Integer type,
                                                                             @Nullable String srm);

    //////////////////////////// 猪群变动相关 ////////////////////////////
    /**
     * 根据id查询变动原因表
     * @param changeReasonId 主键id
     * @return 变动类型表
     */
    Response<DoctorChangeReason> findChangeReasonById(@NotNull(message = "changeReasonId.not.null") Long changeReasonId);

    /**
     * 根据ids查询变动原因表
     * @param changeReasonIds 主键ids
     * @return 变动类型表
     */
    Response<List<DoctorChangeReason>> findChangeReasonByIds(@NotNull(message = "changeReasonId.not.null") List<Long> changeReasonIds);

    /**
     * 查询全部变动原因表
     * @return 变动类型表
     */
    Response<List<DoctorChangeReason>> findAllChangeReasons();

    /**
     * 根据变动类型和输入码查询
     * @param changeTypeId 变动类型id
     * @param srm 不区分大小写模糊匹配
     * @return 变动原因列表
     */
    Response<List<DoctorChangeReason>> findChangeReasonByChangeTypeIdAndSrm(@NotNull(message = "changeTypeId.not.null") Long changeTypeId, @Nullable String srm);

    /**
     * 根据id查询变动原因表
     * @param changeReasonId 主键id
     * @return 变动类型表
     */
    Response<DoctorChangeReason> findChangeReasonByIdFilterByFarmId(@NotNull(message = "farmId.not.null") Long farmId,
                                                                    @NotNull(message = "changeReasonId.not.null") Long changeReasonId);

    /**
     * 根据变动类型和输入码查询
     * @param changeTypeId 变动类型id
     * @param srm 不区分大小写模糊匹配
     * @return 变动原因列表
     */
    Response<List<DoctorChangeReason>> findChangeReasonByChangeTypeIdAndSrmFilterByFarmId(@NotNull(message = "farmId.not.null") Long farmId,
                                                                                          @NotNull(message = "changeTypeId.not.null") Long changeTypeId,
                                                                                          @Nullable String srm);

    //////////////////////////// 猪场客户相关 ////////////////////////////
    /**
     * 根据id查询客户表
     * @param customerId 主键id
     * @return 客户表
     */
    Response<DoctorCustomer> findCustomerById(@NotNull(message = "customerId.not.null") Long customerId);

    /**
     * 根据farmId查询客户表
     * @param farmId 猪场id
     * @return 客户表
     */
    Response<List<DoctorCustomer>> findCustomersByFarmId(@NotNull(message = "farmId.not.null") Long farmId);

    // 客户数据分页（陈娟 2018-10-24）
    Response<Paging<DoctorCustomer>> pagingCustomers(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

    /**
     * 分页查询变动原因
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return
     */
    Response<Paging<DoctorChangeReason>> pagingChangeReason(Integer pageNo, Integer pageSize, Map<String, Object> criteria);
}
