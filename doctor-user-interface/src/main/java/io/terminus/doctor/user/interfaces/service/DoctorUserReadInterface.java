package io.terminus.doctor.user.interfaces.service;

import io.terminus.doctor.user.interfaces.model.Paging;
import io.terminus.doctor.user.interfaces.model.Response;
import io.terminus.doctor.user.interfaces.model.User;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 陈增辉 16/5/23.
 * 对外提供与用户相关的dubbo接口
 */
public interface DoctorUserReadInterface {

    Response<User> findByNick(String nickname);
    Response<User> findByEmail(String email);
    Response<User> findByMobile(String mobile);
    Response<User> load(Integer id);
    Response<User> load(Long id);
    Response<List<User>> loads(List<Long> ids);
    Response<List<User>> loads(Long id0, Long id1, Long... idn);

    /**
     * 多条件查询表 parana_users
     * @param criteria Map中可以放以下key[type]:
     *                 <br>1. name[String], 模糊匹配字段 name
     *                 <br>2. mobile[String], 精确匹配字段 mobile
     *                 <br>3. email[String], 精确匹配字段 email
     *                 <br>4. type[Integer], 通过枚举  转换成名称后精确匹配字段 type TODO
     *                 <br>5. types[Array], 同上, 但仅在 type=null 时才有效
     *                 <br>6. status[Integer], 精确匹配字段 status
     *                 <br>7. login 或 displayNameExact [String], 精确匹配字段 name
     *                 <br>8. displayName[String], 匹配字段 name 以此值开头的
     *                 <br>9. haveMobile[Object], 只要此值不为空则匹配字段 mobile 不为空
     *                 <br>10.searchValue[String], email\mobile\name 任一字段与此值相等
     * @return
     */
    Response<List<User>> loadsBy(Map<String, Object> criteria);

    /**
     * 查询表中最大的id
     * @return
     */
    Response<Long> maxId();

    /**
     * 查询表中最小的 updated_at
     * @return
     */
    Response<Date> minDate();

    Response<Paging<User>> paging(Integer offset, Integer limit);

    /**
     * 分页查询用户基本信息
     * @param offset
     * @param limit
     * @param criteria 关于字段的匹配规则,参见loadsBy方法
     * @return
     */
    Response<Paging<User>> paging(Integer offset, Integer limit, User criteria);

    /**
     * 分页查询用户基本信息
     * @param offset
     * @param limit
     * @param criteria 关于字段的匹配规则,参见loadsBy方法
     * @return
     */
    Response<Paging<User>> paging(Integer offset, Integer limit, Map<String, Object> criteria);

    /**
     * 分页查询用户基本信息
     * @param criteria 关于字段的匹配规则,参见loadsBy方法
     * @return
     */
    Response<Paging<User>> paging(Map<String, Object> criteria);
}
