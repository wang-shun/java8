package io.terminus.doctor.msg.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;

import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 猪场软件消息规则与角色表写服务
 * Date: 2016-05-31
 * Author: chk@terminus.io
 */

public interface DoctorMessageRuleRoleWriteService {

    /**
     * 创建DoctorMessageRuleRole
     * @param messageRuleRole
     * @return 主键id
     */
    Response<Long> createMessageRuleRole(DoctorMessageRuleRole messageRuleRole);

    /**
     * 更新DoctorMessageRuleRole
     * @param messageRuleRole
     * @return 是否成功
     */
    Response<Boolean> updateMessageRuleRole(DoctorMessageRuleRole messageRuleRole);

    /**
     * 根据主键id删除DoctorMessageRuleRole
     * @param messageRuleRoleId
     * @return 是否成功
     */
    Response<Boolean> deleteMessageRuleRoleById(Long messageRuleRoleId);

    /**
     * 将rule与role进行绑定
     * @param ruleId    规则id
     * @param roleIds   角色ids
     * @return
     */
    Response<Boolean> relateRuleRolesByRuleId(Long ruleId, List<Long> roleIds);

    /**
     * 将rule与role进行绑定
     * @param roleId    角色id
     * @param ruleIds   规则ids
     * @return
     */
    Response<Boolean> relateRuleRolesByRoleId(Long roleId, List<Long> ruleIds);
}