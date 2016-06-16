package io.terminus.doctor.msg.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.msg.dao.DoctorMessageRuleTemplateDao;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Code generated by terminus code gen
 * Desc: 猪场软件消息规则模板表读服务实现类
 * Date: 2016-05-31
 * Author: chk@terminus.io
 */
@Slf4j
@Service
public class DoctorMessageRuleTemplateReadServiceImpl implements DoctorMessageRuleTemplateReadService {

    private final DoctorMessageRuleTemplateDao doctorMessageRuleTemplateDao;

    @Autowired
    public DoctorMessageRuleTemplateReadServiceImpl(DoctorMessageRuleTemplateDao doctorMessageRuleTemplateDao) {
        this.doctorMessageRuleTemplateDao = doctorMessageRuleTemplateDao;
    }

    @Override
    public Response<DoctorMessageRuleTemplate> findMessageRuleTemplateById(Long messageRuleTemplateId) {
        try {
            return Response.ok(doctorMessageRuleTemplateDao.findById(messageRuleTemplateId));
        } catch (Exception e) {
            log.error("find messageRuleTemplate by id failed, messageRuleTemplateId:{}, cause:{}", messageRuleTemplateId, Throwables.getStackTraceAsString(e));
            return Response.fail("messageRuleTemplate.find.fail");
        }
    }

    @Override
    public Response<List<DoctorMessageRuleTemplate>> findByCategory(Integer category) {
        try{
            return Response.ok(doctorMessageRuleTemplateDao.findByCategory(category));
        } catch (Exception e) {
            log.error("find messageRuleTemplate by category failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("messageRuleTemplate.find.fail");
        }
    }

    @Override
    public Response<List<DoctorMessageRuleTemplate>> findAllWarnMessageTpl() {
        try{
            return Response.ok(doctorMessageRuleTemplateDao.findAllWarnMessageTpl());
        } catch (Exception e) {
            log.error("find all warn messageRuleTemplate failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("messageRuleTemplate.find.fail");
        }
    }

    @Override
    public Response<List<DoctorMessageRuleTemplate>> findTemplatesByCriteria(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorMessageRuleTemplateDao.listNotDelete(criteria));
        } catch (Exception e) {
            log.error("find msg templates by criteria failed, criteria is {}, causu by {}", criteria, Throwables.getStackTraceAsString(e));
            return Response.fail("find.msg.template.fail");
        }
    }
}
