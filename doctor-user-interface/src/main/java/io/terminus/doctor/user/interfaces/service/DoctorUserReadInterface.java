package io.terminus.doctor.user.interfaces.service;

import io.terminus.doctor.user.interfaces.model.PagingDto;
import io.terminus.doctor.user.interfaces.model.RespDto;
import io.terminus.doctor.user.interfaces.model.UserDto;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 陈增辉 16/5/23.
 * 对外提供与用户相关的dubbo接口
 */
public interface DoctorUserReadInterface {

    /**
     * 按用户名查询
     * @param nickname 按用户名
     * @return 如果查询不到不会返回异常, 而是在RespDto中放null
     */
    RespDto<UserDto> findByNick(String nickname);

    /**
     * 按邮箱查询
     * @param email 邮箱
     * @return 如果查询不到不会返回异常, 而是在RespDto中放null
     */
    RespDto<UserDto> findByEmail(String email);

    /**
     * 按手机号查询
     * @param mobile 手机号
     * @return 如果查询不到不会返回异常, 而是在RespDto中放null
     */
    RespDto<UserDto> findByMobile(String mobile);

    /**
     * 按id查询用户
     * @param id 用户id
     * @return 如果查询不到将会返回异常 user.not.found
     */
    RespDto<UserDto> load(Integer id);

    /**
     * 按id查询用户
     * @param id 用户id
     * @return 如果查询不到将会返回异常 user.not.found
     */
    RespDto<UserDto> load(Long id);

    /**
     * 批量查询用户
     * @param ids  用户id
     * @return 如果查询不到不会返回异常
     */
    RespDto<List<UserDto>> loads(List<Long> ids);

    /**
     * 批量查询用户
     * @param id0  用户id
     * @param id1  用户id
     * @param idn  用户id
     * @return 如果查询不到不会返回异常
     */
    RespDto<List<UserDto>> loads(Long id0, Long id1, Long... idn);

    /**
     * 多条件查询表 parana_users
     * @param criteria Map中可以放以下key[type]:
     *                 <br>1. name[String], 模糊匹配字段 name
     *                 <br>2. mobile[String], 精确匹配字段 mobile
     *                 <br>3. email[String], 精确匹配字段 email
     *                 <br>4. type[String], 参数为 rolesJson 字段的子字符串
     *                 <br>5. types[Array[String]], 同上, 但仅在 type=null 时才有效
     *                 <br>6. status[Integer], 精确匹配字段 status
     *                 <br>7. login 或 displayNameExact [String], 精确匹配字段 name
     *                 <br>8. displayName[String], 匹配字段 name 以此值开头的
     *                 <br>9. haveMobile[Object], 只要此值不为空则匹配字段 mobile 不为空
     *                 <br>10.searchValue[String], email\mobile\name 任一字段与此值相等
     * @return 如果查询不到不会返回异常
     */
    RespDto<List<UserDto>> loadsBy(Map<String, Object> criteria);

    /**
     * 查询表中最大的id
     * @return
     */
    RespDto<Long> maxId();

    /**
     * 查询表中最小的 updated_at
     * @return
     */
    RespDto<Date> minDate();

    /**
     * 查询id小于lastId内的limit个用戶
     * @param lastId 最大的用戶id
     * @param limit 用戶个数
     * @return id小于lastId内的pageSize个用戶
     */
    RespDto<List<UserDto>> listTo(Long lastId, int limit);
    /**
     * 查询id小于lastId内且更新时间大于since的limit个用戶
     * @param lastId lastId 最大的用戶id
     * @param since 起始更新时间
     * @param limit 用戶个数
     * @return id小于lastId内且更新时间大于since的limit个用戶
     */
    RespDto<List<UserDto>> listSince(Long lastId, String since, int limit);

    /**
     * 分页查询
     * @param offset 起始值
     * @param limit 数量
     * @return
     */
    RespDto<PagingDto<UserDto>> paging(Integer offset, Integer limit);

    /**
     * 分页查询用户基本信息
     * @param offset 起始值
     * @param limit 数量
     * @param criteria 关于字段的匹配规则,参见loadsBy方法
     * @return
     */
    RespDto<PagingDto<UserDto>> paging(Integer offset, Integer limit, UserDto criteria);

    /**
     * 分页查询用户基本信息
     * @param offset 起始值
     * @param limit 数量
     * @param criteria 关于字段的匹配规则,参见loadsBy方法
     * @return
     */
    RespDto<PagingDto<UserDto>> paging(Integer offset, Integer limit, Map<String, Object> criteria);

    /**
     * 分页查询用户基本信息
     * @param criteria 关于字段的匹配规则,参见loadsBy方法
     * @return
     */
    RespDto<PagingDto<UserDto>> paging(Map<String, Object> criteria);

    /**
     * 查询所有的用户邮箱
     * @param userTypeName 用户类型(类型的英文名称)
     * @return
     */
    RespDto<List<String>> listAllUserEmails(String userTypeName);

    /**
     * 查询所有的用户手机号
     * @param userTypeName 用户类型(类型的英文名称)
     * @return
     */
    RespDto<List<String>> listAllUserMobiles(String userTypeName);
}
