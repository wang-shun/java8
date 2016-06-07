package io.terminus.doctor.msg.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.msg.dao.DoctorMessageRuleDao;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 猪场软件消息规则表读服务实现类
 * Date: 2016-05-31
 * Author: chk@terminus.io
 */
@Slf4j
@Service
public class DoctorMessageRuleReadServiceImpl implements DoctorMessageRuleReadService {

    private final DoctorMessageRuleDao doctorMessageRuleDao;

    @Autowired
    public DoctorMessageRuleReadServiceImpl(DoctorMessageRuleDao doctorMessageRuleDao) {
        this.doctorMessageRuleDao = doctorMessageRuleDao;
    }

    @Override
    public Response<DoctorMessageRule> findMessageRuleById(Long messageRuleId) {
        try {
            return Response.ok(doctorMessageRuleDao.findById(messageRuleId));
        } catch (Exception e) {
            log.error("find messageRule by id failed, messageRuleId:{}, cause:{}", messageRuleId, Throwables.getStackTraceAsString(e));
            return Response.fail("messageRule.find.fail");
        }
    }

    @Override
    public Response<List<DoctorMessageRule>> findMessageRulesByTplId(Long templateId) {
        try{
            return Response.ok(doctorMessageRuleDao.findByTpl(templateId));
        } catch (Exception e) {
            log.error("find messageRule by tplId failed, tplId is {}, cause by {}", templateId, Throwables.getStackTraceAsString(e));
            return Response.fail("messageRule.find.fail");
        }
    }
}
