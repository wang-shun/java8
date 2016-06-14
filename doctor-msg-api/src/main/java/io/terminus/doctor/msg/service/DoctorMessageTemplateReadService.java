package io.terminus.doctor.msg.service;

import io.terminus.common.model.Response;
import io.terminus.parana.msg.dto.MessageInfo;
import io.terminus.parana.msg.service.MessageTemplateReadService;

import java.io.Serializable;
import java.util.Map;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/14
 */

public interface DoctorMessageTemplateReadService extends MessageTemplateReadService {

    /**
     * 应用消息模板,以生成消息的标题, 缓存模板
     *
     * @param templateName 消息模板名称
     * @param context 消息模板的上下文, 包含模板中的变量信息
     * @return 应用消息模板后生成的标题
     */
    Response<String> getMessageTitleWithCache(String templateName, Map<String, Serializable> context);

    /**
     * 应用消息模板,以生成消息内容, 缓存模板
     *
     * @param templateName 消息模板名称
     * @param context 消息模板的上下文, 包含模板中的变量信息
     * @return 应用消息模板后生成的消息内容
     */
    Response<String> getMessageContentWithCache(String templateName, Map<String, Serializable> context);

    /**
     * 应用消息模板,以生成消息标题和内容, 缓存模板
     * @param templateName 消息模板名称
     * @param context 消息模板的上下文, 包含模板中的变量信息
     * @return 应用消息模板后生成的标题和消息内容
     */
    Response<MessageInfo> getMessageInfoWithCache(String templateName, Map<String, Serializable> context);
}
