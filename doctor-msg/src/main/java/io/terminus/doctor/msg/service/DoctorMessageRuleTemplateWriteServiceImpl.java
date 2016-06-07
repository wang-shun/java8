package io.terminus.doctor.msg.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.msg.dao.DoctorMessageRuleDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleRoleDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleTemplateDao;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 猪场软件消息规则模板表写服务实现类
 * Date: 2016-05-31
 * Author: chk@terminus.io
 */
@Slf4j
@Service
public class DoctorMessageRuleTemplateWriteServiceImpl implements DoctorMessageRuleTemplateWriteService {

    private final DoctorMessageRuleTemplateDao doctorMessageRuleTemplateDao;
    private final DoctorMessageRuleDao doctorMessageRuleDao;
    private final DoctorMessageRuleRoleDao doctorMessageRuleRoleDao;

    @Autowired
    public DoctorMessageRuleTemplateWriteServiceImpl(DoctorMessageRuleTemplateDao doctorMessageRuleTemplateDao,
                                                     DoctorMessageRuleDao doctorMessageRuleDao,
                                                     DoctorMessageRuleRoleDao doctorMessageRuleRoleDao) {
        this.doctorMessageRuleTemplateDao = doctorMessageRuleTemplateDao;
        this.doctorMessageRuleDao = doctorMessageRuleDao;
        this.doctorMessageRuleRoleDao = doctorMessageRuleRoleDao;
    }

    @Override
    public Response<Long> createMessageRuleTemplate(DoctorMessageRuleTemplate messageRuleTemplate) {
        try {
            doctorMessageRuleTemplateDao.create(messageRuleTemplate);
            return Response.ok(messageRuleTemplate.getId());
        } catch (Exception e) {
            log.error("create messageRuleTemplate failed, messageRuleTemplate:{}, cause:{}", messageRuleTemplate, Throwables.getStackTraceAsString(e));
            return Response.fail("messageRuleTemplate.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateMessageRuleTemplate(DoctorMessageRuleTemplate messageRuleTemplate) {
        try {
            // 1. 查询出于模板相管理的猪场, 并更新默认的rulevalue值
            List<DoctorMessageRule> farmTpls = doctorMessageRuleDao.findByTpl(messageRuleTemplate.getId());
            for (int i = 0; farmTpls != null && i < farmTpls.size(); i++) {
                DoctorMessageRule farmTpl = farmTpls.get(i);
                if (farmTpl.getUseDefault() != null && 1 == farmTpl.getUseDefault()) {
                    farmTpl.setRuleValue(messageRuleTemplate.getRuleValue());
                    doctorMessageRuleDao.update(farmTpl);
                    // 2. 找出与角色相关的信息, 并更新默认rulevalue值
                    List<DoctorMessageRuleRole> roles = doctorMessageRuleRoleDao.findByTplAndFarmId(messageRuleTemplate.getId(), farmTpl.getFarmId());
                    for (int j = 0; roles != null && j < roles.size(); j++) {
                        DoctorMessageRuleRole role = roles.get(j);
                        if (role.getUseDefault() != null && 1 == role.getUseDefault()) {
                            role.setRuleValue(messageRuleTemplate.getRuleValue());
                            doctorMessageRuleRoleDao.update(role);
                        }
                    }
                }
            }
            return Response.ok(doctorMessageRuleTemplateDao.update(messageRuleTemplate));
        } catch (Exception e) {
            log.error("update messageRuleTemplate failed, messageRuleTemplate:{}, cause:{}", messageRuleTemplate, Throwables.getStackTraceAsString(e));
            return Response.fail("messageRuleTemplate.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteMessageRuleTemplateById(Long messageRuleTemplateId) {
        try {
            // 逻辑删除
            DoctorMessageRuleTemplate ruleTemplate = doctorMessageRuleTemplateDao.findById(messageRuleTemplateId);
            if (ruleTemplate != null) {
                ruleTemplate.setStatus(DoctorMessageRuleTemplate.Status.DELETE.getValue());
            }
            return Response.ok(doctorMessageRuleTemplateDao.update(ruleTemplate));
        } catch (Exception e) {
            log.error("delete messageRuleTemplate failed, messageRuleTemplateId:{}, cause:{}", messageRuleTemplateId, Throwables.getStackTraceAsString(e));
            return Response.fail("messageRuleTemplate.delete.fail");
        }
    }
}
