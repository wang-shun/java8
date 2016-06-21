package io.terminus.doctor.schedule.msg;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.producer.IProducer;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.service.PrimaryUserReadService;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import io.terminus.parana.web.msg.MsgWebService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Desc: 消息管理manager
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/2
 */
@Component
@Slf4j
public class MsgManager {

    @Autowired
    private PrimaryUserReadService primaryUserReadService;

    @Autowired
    private DoctorMessageReadService doctorMessageReadService;

    @Autowired
    private DoctorMessageWriteService doctorMessageWriteService;

    @Autowired
    private UserReadService userReadService;

    @Autowired
    private MsgWebService smsWebService;

    @Autowired
    private MsgWebService emailWebService;

    @Autowired
    private MsgWebService appPushWebService;

    @Autowired
    private ApplicationContext applicationContext;

    private Map<String, IProducer> producerMap;

    @Value("${message.app.domain}")
    private String domain;

    @PostConstruct
    public void init() {
        producerMap = applicationContext.getBeansOfType(IProducer.class);
        if (producerMap == null) {
            producerMap = Maps.newHashMap();
        }
    }

    /**
     * 产生消息
     */
    public void produce() {
        try{
            List<SubUser> subUsers = Lists.newArrayList();
            List<Sub> subs = RespHelper.orServEx(primaryUserReadService.findAllActiveSubs());
            for (int i = 0; subs!= null && i < subs.size(); i++) {
                Sub sub = subs.get(i);
                subUsers.add(SubUser.builder()
                        .userId(sub.getUserId())
                        .parentUserId(sub.getParentUserId())
                        .roleId(sub.getRoleId())
                        .build());
            }
            // 执行
            producerMap.forEach((beanName, producer) -> {
                try{
                    producer.produce(subUsers);
                } catch (Exception e) {
                    log.error("produce message error -> {} 执行失败, cause by {}",
                            producer.getClass().getSimpleName(), Throwables.getStackTraceAsString(e));
                }
            });
        } catch (Exception e) {
            log.error("[produce message] -> message produce error, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 发出短信消息
     */
    public void consumeMsg() {
        List<DoctorMessage> msgMessages = RespHelper.orServEx(doctorMessageReadService.findMsgMessage());
        for (int i = 0; msgMessages != null && i < msgMessages.size(); i++) {
            DoctorMessage message = msgMessages.get(i);
            Map<String, Serializable> map = null;
            try{
                // 获取用户信息
                User user = (User) RespHelper.orServEx(userReadService.findById(message.getUserId()));
                if (StringUtils.isNotBlank(user.getMobile())) {
                    map = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(message.getData(), Map.class);
                    // 发送短信
                    smsWebService.send(user.getMobile(), message.getMessageTemplate(), map, null);
                    message.setSendedAt(new Date());
                    message.setStatus(DoctorMessage.Status.SENDED.getValue());
                }
            } catch (Exception e) {
                log.error("msg message send error, cause by {}", Throwables.getStackTraceAsString(e));
                message.setFailedBy("msg message send error, context is " + map + ", cause by " + e.getMessage());
                message.setStatus(DoctorMessage.Status.FAILED.getValue());
            }
            doctorMessageWriteService.updateMessage(message);
        }
    }

    /**
     * 发出邮件消息
     */
    public void consumeEmail() {
        List<DoctorMessage> emailMessages = RespHelper.orServEx(doctorMessageReadService.findEmailMessage());
        for (int i = 0; emailMessages != null && i < emailMessages.size(); i++) {
            DoctorMessage message = emailMessages.get(i);
            Map<String, Serializable> map = null;
            try{
                // 获取用户信息
                User user = (User) RespHelper.orServEx(userReadService.findById(message.getUserId()));
                if (StringUtils.isNotBlank(user.getEmail())) {
                    map = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(message.getData(), Map.class);
                    // 发送邮件
                    emailWebService.send(user.getEmail(), message.getMessageTemplate(), map, null);
                    message.setSendedAt(new Date());
                    message.setStatus(DoctorMessage.Status.SENDED.getValue());
                }
            } catch (Exception e) {
                log.error("email message send error, cause by {}", Throwables.getStackTraceAsString(e));
                message.setFailedBy("email message send error, context is " + map + ", cause by " + e.getMessage());
                message.setStatus(DoctorMessage.Status.FAILED.getValue());
            }
            doctorMessageWriteService.updateMessage(message);
        }
    }

    /**
     * app push 消息消费
     */
    public void consumeAppPush() {
        List<DoctorMessage> appMessages = RespHelper.orServEx(doctorMessageReadService.findAppPushMessage());
        for (int i = 0; appMessages != null && i < appMessages.size(); i++) {
            DoctorMessage message = appMessages.get(i);
            Map<String, Serializable> map = null;
            try{
                // 获取用户信息
                map = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(message.getData(), Map.class);
                map.put("url", getAppUrl(message.getUrl(), message.getId())); // 设置回调url
                // 推送消息
                if (message.getUserId() != null) {
                    appPushWebService.send("[" + message.getUserId() + "]", message.getMessageTemplate(), map, null);
                    message.setSendedAt(new Date());
                    message.setStatus(DoctorMessage.Status.SENDED.getValue());
                }
            } catch (Exception e) {
                log.error("app push message send error, context is {}, cause by {}", map, Throwables.getStackTraceAsString(e));
                message.setFailedBy("app push message send error, context is " + map + ", cause by " + e.getMessage());
                message.setStatus(DoctorMessage.Status.FAILED.getValue());
            }
            doctorMessageWriteService.updateMessage(message);
        }
    }

    // 获取url
    private String getAppUrl(String url, Long id) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(domain)) {
            sb//.append("http://")
                    .append(domain)
                    .append(url)
                    .append("?id=")
                    .append(id);
        } else {
            sb.append(url).append("?id=").append(id);
        }
        return sb.toString();
    }
}
