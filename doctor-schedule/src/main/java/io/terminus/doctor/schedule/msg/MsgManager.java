package io.terminus.doctor.schedule.msg;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.producer.IProducer;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.PrimaryUserReadService;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import io.terminus.parana.web.msg.MsgWebService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService;

    @Autowired
    private DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

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

  //  @Value("${message.app.domain}")
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
                SubUser subUser = SubUser.builder()
                        .userId(sub.getUserId())
                        .parentUserId(sub.getParentUserId())
                        .roleId(sub.getRoleId())
                        .farmIds(Lists.newArrayList())
                        .barnIds(Lists.newArrayList())
                        .build();
                // 获取猪场权限
                DoctorUserDataPermission dataPermission = RespHelper.orServEx(
                        doctorUserDataPermissionReadService.findDataPermissionByUserId(sub.getUserId()));
                if (dataPermission != null) {
                    dataPermission.setFarmIds(dataPermission.getFarmIds());
                    subUser.getFarmIds().addAll(dataPermission.getFarmIdsList());
                    dataPermission.setBarnIds(dataPermission.getBarnIds());
                    subUser.getBarnIds().addAll(dataPermission.getBarnIdsList());
                }
                subUsers.add(subUser);
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
      //  User user = (User) RespHelper.orServEx(userReadService.findById(message.getUserId()));
        for (int i = 0; msgMessages != null && i < msgMessages.size(); i++) {
            DoctorMessage message = msgMessages.get(i);
            Map<String, Serializable> map = null;
            try{
                // 获取用户信息
                User user = (User) RespHelper.orServEx(userReadService.findById(message.getUserId()));
                if (StringUtils.isNotBlank(user.getMobile())) {
                    map = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(message.getData(), Map.class);
                    // 发送短信 // TODO: 临时取消短信发送
                    // smsWebService.send(user.getMobile(), message.getMessageTemplate(), map, null);
                    message.setSendedAt(new Date());
                    message.setStatus(DoctorMessage.Status.SENDED.getValue());
                }
            } catch (Exception e) {
                log.error("msg message send error, cause by {}", Throwables.getStackTraceAsString(e));
                message.setFailedBy("msg message send error, context is " + map + ", cause by " + e.getMessage());
                message.setStatus(DoctorMessage.Status.FAILED.getValue());
            }
            //doctorMessageWriteService.updateMessage(message);
        }
      //  smsWebService.send(user.getMobile(), message.getMessageTemplate(), map, null);
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
                    // 发送邮件 // TODO 临时取消
                   // emailWebService.send(user.getEmail(), message.getMessageTemplate(), map, null);
                    message.setSendedAt(new Date());
                    message.setStatus(DoctorMessage.Status.SENDED.getValue());
                }
            } catch (Exception e) {
                log.error("email message send error, cause by {}", Throwables.getStackTraceAsString(e));
                message.setFailedBy("email message send error, context is " + map + ", cause by " + e.getMessage());
                message.setStatus(DoctorMessage.Status.FAILED.getValue());
            }
           // doctorMessageWriteService.updateMessage(message);
        }
    }

    /**
     * app push 消息消费
     */
    public void consumeAppPush() {
        List<DoctorMessage> emailMessages = RespHelper.orServEx(doctorMessageReadService.findAppPushMessage());
        for (int i = 0; emailMessages != null && i < emailMessages.size(); i++) {
            DoctorMessage message = emailMessages.get(i);
            Map<String, Serializable> map = null;
            try{
                // 获取用户信息
                User user = (User) RespHelper.orServEx(userReadService.findById(message.getUserId()));
                if (StringUtils.isNotBlank(user.getEmail())) {
                    map = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(message.getData(), Map.class);
                    // app push // TODO 临时取消
                    // appPushWebService.send("[" + userId+ "]", doctorMessageRuleTemplate.getMessageTemplate(), map, null);
                    message.setSendedAt(new Date());
                    message.setStatus(DoctorMessage.Status.SENDED.getValue());
                }
            } catch (Exception e) {
                log.error("app push message send error, cause by {}", Throwables.getStackTraceAsString(e));
                message.setFailedBy("app push message send error, context is " + map + ", cause by " + e.getMessage());
                message.setStatus(DoctorMessage.Status.FAILED.getValue());
            }
            // doctorMessageWriteService.updateMessage(message);
        }
    }
//    public void consumeAppPush() {
//        List<DoctorMessage> updateMessages = Lists.newArrayList();
//        try {
//            List<DoctorMessageRuleTemplate> templates = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findAllWarnMessageTpl());
//            templates.forEach(doctorMessageRuleTemplate -> {
//                List<Long> userIdList = RespHelper.orServEx(doctorMessageReadService.findUserIdList(DoctorMessage.builder().templateId(doctorMessageRuleTemplate.getId()).build()));
//                userIdList.forEach(userId -> {
//                    StringBuilder sb = new StringBuilder();
//                    List<DoctorMessage> appMessages = RespHelper.orServEx(doctorMessageReadService.findAppPushMessage(doctorMessageRuleTemplate.getId(), userId));
//                    appMessages.forEach(doctorMessage -> {
//                        Map<String, Serializable> dataMap = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorMessage.getData(), Map.class);
//                        if (Objects.equals(doctorMessageRuleTemplate.getCategory(), Category.FATTEN_PIG_REMOVE.getKey())){
//                            sb.append(dataMap.get("groupCode")+",");
//                        }else if (Objects.equals(doctorMessageRuleTemplate.getCategory(), Category.FATTEN_PIG_REMOVE.getKey())){
//
//                        }else {
//                            sb.append(dataMap.get("pigCode")+",");
//                        }
//                        doctorMessage.setSendedAt(new Date());
//                        doctorMessage.setStatus(DoctorMessage.Status.SENDED.getValue());
//                        updateMessages.add(doctorMessage);
//                    });
//                    Map<String, Serializable> map = Maps.newHashMap();
//                    map.put("code", sb);
//                    map.put("after_open", "go_url");
//                    map.put("title", doctorMessageRuleTemplate.getName());
//                    map.put("ticker", doctorMessageRuleTemplate.getName());
//                   // appPushWebService.send("[" + userId+ "]", doctorMessageRuleTemplate.getMessageTemplate(), map, null);
//                });
//            });
//
//        }  catch (Exception e) {
//            log.error("app push message send error, cause by {}", Throwables.getStackTraceAsString(e));
//            updateMessages.forEach(doctorMessage -> {
//                doctorMessageWriteService.updateMessage(doctorMessage);
//                doctorMessage.setFailedBy("app push message send error, cause by " + e.getMessage());
//                doctorMessage.setStatus(DoctorMessage.Status.FAILED.getValue());
//            });
//        }
////        updateMessages.forEach(doctorMessage -> {
////            doctorMessageWriteService.updateMessage(doctorMessage);
////        });
//    }

    // 获取url
    private String getAppUrl(DoctorMessage doctorMessage) {
        StringBuilder sb = new StringBuilder();
        String urlPart;
        if (Objects.equals(doctorMessage.getCategory(), Category.FATTEN_PIG_REMOVE.getKey())){
            urlPart = "?groupId=";
        }else if (Objects.equals(doctorMessage.getCategory(), Category.STORAGE_SHORTAGE.getKey())){
            urlPart = "?materialId=";
        }else {
            urlPart = "?pigId=";
        }
        if (StringUtils.isNotBlank(domain)) {
            sb//.append("http://")
                    .append(domain)
                    .append(doctorMessage.getUrl())
                    .append(urlPart)
                    .append(doctorMessage.getBusinessId());
        } else {
            sb.append(doctorMessage.getUrl()).append(urlPart).append(doctorMessage.getBusinessId());
        }
        return sb.toString();
    }
}
