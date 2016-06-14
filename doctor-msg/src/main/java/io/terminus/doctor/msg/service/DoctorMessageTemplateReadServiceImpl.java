package io.terminus.doctor.msg.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.parana.msg.dto.MessageInfo;
import io.terminus.parana.msg.impl.dao.mysql.MessageTemplateDao;
import io.terminus.parana.msg.impl.service.MessageTemplateReadServiceImpl;
import io.terminus.parana.msg.model.MessageTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/14
 */
@Slf4j
@Service
public class DoctorMessageTemplateReadServiceImpl extends MessageTemplateReadServiceImpl implements DoctorMessageTemplateReadService {

    private final MessageTemplateDao messageTemplateDao;

    @Autowired
    public DoctorMessageTemplateReadServiceImpl(MessageTemplateDao messageTemplateDao) {
        super(messageTemplateDao);
        this.messageTemplateDao = messageTemplateDao;
    }

    @Override
    public Response<String> getMessageTitle(String templateName, Map<String, Serializable> context) {
        try {
            MessageTemplate template = messageTemplateDao.findByName(templateName);
            return Response.ok(template.getTitle());
        } catch (Exception e) {
            log.error("get message title failed, templateName:{}, context:{}, cause:{}", templateName, context, Throwables.getStackTraceAsString(e));
            return Response.fail("get.message.title.failed");
        }
    }

    @Override
    public Response<String> getMessageContent(String templateName, Map<String, Serializable> context) {
        try {
            MessageTemplate template = messageTemplateDao.findByName(templateName);
            return Response.ok(template.getContent());
        } catch (Exception e) {
            log.error("get message content failed, templateName:{}, context:{}, cause:{}", templateName, context, Throwables.getStackTraceAsString(e));
            return Response.fail("get.message.content.failed");
        }
    }

    @Override
    public Response<MessageInfo> getMessageInfo(String templateName, Map<String, Serializable> context) {
        try {
            MessageTemplate template = messageTemplateDao.findByName(templateName);
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.setMessageTitle(template.getTitle());
            messageInfo.setMessageContent(template.getContent());
            return Response.ok(messageInfo);
        } catch (Exception e) {
            log.error("get message info failed, templateName:{}, context:{}, cause:{}", templateName, context, Throwables.getStackTraceAsString(e));
            return Response.fail("get.message.info.failed");
        }
    }

    @Override
    public Response<String> getMessageTitleWithCache(String templateName, Map<String, Serializable> context) {
        return getMessageTitle(templateName, context);
    }

    @Override
    public Response<String> getMessageContentWithCache(String templateName, Map<String, Serializable> context) {
        return getMessageContent(templateName, context);
    }

    @Override
    public Response<MessageInfo> getMessageInfoWithCache(String templateName, Map<String, Serializable> context) {
        return getMessageInfo(templateName, context);
    }
}
