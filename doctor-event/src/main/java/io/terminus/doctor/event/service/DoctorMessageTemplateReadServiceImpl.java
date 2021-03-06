package io.terminus.doctor.event.service;

import com.github.jknack.handlebars.Template;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.helper.DoctorHandleBarsHelper;
import io.terminus.doctor.event.helper.DoctorMessageTemplateCacher;
import io.terminus.parana.msg.dto.MessageInfo;
import io.terminus.parana.msg.impl.dao.mysql.MessageTemplateDao;
import io.terminus.parana.msg.model.MessageTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Desc: 消息模板解析服务类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/14
 */
@Slf4j
@Service
@RpcProvider
public class DoctorMessageTemplateReadServiceImpl implements DoctorMessageTemplateReadService {

    private final MessageTemplateDao messageTemplateDao;
    private final DoctorMessageTemplateCacher doctorMessageTemplateCacher;
    private final DoctorHandleBarsHelper doctorHandleBarsHelper;

    @Autowired
    public DoctorMessageTemplateReadServiceImpl(MessageTemplateDao messageTemplateDao,
                                                DoctorMessageTemplateCacher doctorMessageTemplateCacher,
                                                DoctorHandleBarsHelper doctorHandleBarsHelper) {
        this.messageTemplateDao = messageTemplateDao;
        this.doctorMessageTemplateCacher = doctorMessageTemplateCacher;
        this.doctorHandleBarsHelper = doctorHandleBarsHelper;
    }

    @Override
    public Response<String> getMessageTitle(String templateName, Map<String, Serializable> context) {
        try {
            MessageTemplate template = messageTemplateDao.findByName(templateName);
            return Response.ok(template != null ? doctorHandleBarsHelper.compileInline(template.getTitle()).apply(context) : null);
        } catch (Exception e) {
            log.error("get message title failed, templateName:{}, context:{}, cause:{}",
                    templateName, context, Throwables.getStackTraceAsString(e));
            return Response.fail("get.message.title.failed");
        }
    }

    @Override
    public Response<String> getMessageContent(String templateName, Map<String, Serializable> context) {
        try {
            MessageTemplate template = messageTemplateDao.findByName(templateName);
            return Response.ok(template != null ? doctorHandleBarsHelper.compileInline(template.getContent()).apply(context) : null);
        } catch (Exception e) {
            log.error("get message content failed, templateName:{}, context:{}, cause:{}", templateName, context, Throwables.getStackTraceAsString(e));
            return Response.fail("get.message.content.failed");
        }
    }

    @Override
    public Response<MessageInfo> getMessageInfo(String templateName, Map<String, Serializable> context) {
        try {
            MessageTemplate template = messageTemplateDao.findByName(templateName);
            MessageInfo messageInfo = null;
            if (template != null) {
                messageInfo = new MessageInfo();
                messageInfo.setMessageTitle(template.getTitle());
                messageInfo.setMessageContent(template.getContent());
            }
            messageInfo.setMessageTitle(doctorHandleBarsHelper.compileInline(template.getTitle()).apply(context));
            messageInfo.setMessageContent(doctorHandleBarsHelper.compileInline(template.getContent()).apply(context));
            return Response.ok(messageInfo);
        } catch (Exception e) {
            log.error("get message info failed, templateName:{}, context:{}, cause:{}", templateName, context, Throwables.getStackTraceAsString(e));
            return Response.fail("get.message.info.failed");
        }
    }

    @Override
    public Response<String> getMessageTitleWithCache(String templateName, Map<String, Serializable> context) {
        try {
            return Response.ok(getTitleTemlate(templateName).apply(context));
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("get message title with cache failed, templateName:{}, context:{}, cause:{}", templateName, context, Throwables.getStackTraceAsString(e));
            return Response.fail("get.message.title.failed");
        }
    }

    @Override
    public Response<String> getMessageContentWithCache(String templateName, Map<String, Serializable> context) {
        try {
            return Response.ok(getContentTemlate(templateName).apply(context));
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("get message content with cache failed, templateName:{}, context:{}, cause:{}", templateName, context, Throwables.getStackTraceAsString(e));
            return Response.fail("get.message.content.failed");
        }
    }

    @Override
    public Response<MessageInfo> getMessageInfoWithCache(String templateName, Map<String, Serializable> context) {
        try {
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.setMessageTitle(getTitleTemlate(templateName).apply(context));
            messageInfo.setMessageContent(getContentTemlate(templateName).apply(context));
            return Response.ok(messageInfo);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("get message info with cache failed, templateName:{}, context:{}, cause:{}", templateName, context, Throwables.getStackTraceAsString(e));
            return Response.fail("get.message.info.failed");
        }
    }

    @Override
    public Response<Boolean> cleanAllMessageTemplateCache() {
        try {
            doctorMessageTemplateCacher.cleanAll();
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("clean all message template cache failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("message.template.cache.clean.fail");
        }
    }

    @Override
    public Response<Boolean> reloadAllMessageTemplateCacheByName(String templateName) {
        try {
            doctorMessageTemplateCacher.refreshByName(templateName);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("reload all message template cache by name failed, templateName:{}, cause:{}",
                    templateName, Throwables.getStackTraceAsString(e));
            return Response.fail("message.template.cache.reload.fail");
        }
    }

    private Template getTitleTemlate(String name) throws ExecutionException {
        Optional<Template> template = doctorMessageTemplateCacher.getTemplateTitleCache().get(name);
        if (template.isPresent()) {
            return template.get();
        }
        throw new ServiceException("message.title.template.not.found");
    }

    private Template getContentTemlate(String name) throws ExecutionException {
        Optional<Template> template = doctorMessageTemplateCacher.getTemplateContentCache().get(name);
        if (template.isPresent()) {
            return template.get();
        }
        throw new ServiceException("message.content.template.not.found");
    }
}
