package io.terminus.doctor.schedule.msg;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.service.DoctorMessageJob;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.service.PrimaryUserReadService;
import io.terminus.lib.email.EmailService;
import io.terminus.lib.sms.SmsService;
import io.terminus.parana.msg.service.MessageTemplateReadService;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import io.terminus.parana.web.msg.MsgWebService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/2
 */
@Service
@Slf4j
public class MsgManager {

    @Autowired
    private DoctorMessageJob doctorMessageJob;

    @Autowired
    private PrimaryUserReadService primaryUserReadService;

    @Autowired
    private DoctorMessageReadService doctorMessageReadService;

    @Autowired
    private DoctorMessageWriteService doctorMessageWriteService;

    @Autowired
    private MessageTemplateReadService messageTemplateReadService;

    @Autowired
    private UserReadService userReadService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MsgWebService appPushWebService;

    /**
     * 产生消息
     */
    public void produce() {
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
        doctorMessageJob.produce(subUsers);
    }

    /**
     * 发出短信消息
     */
    public void consumeMsg() {
        List<DoctorMessage> msgMessages = RespHelper.orServEx(doctorMessageReadService.findMsgMessage());
        for (int i = 0; msgMessages != null && i < msgMessages.size(); i++) {
            DoctorMessage message = msgMessages.get(i);
            try{
                // 获取用户信息
                User user = (User) RespHelper.orServEx(userReadService.findById(message.getUserId()));
                if (StringUtils.isNotBlank(user.getMobile())) {
                    Map<String, Serializable> map = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(message.getData(), Map.class);
                    smsService.send(
                            "新融农牧",
                            user.getMobile(),
                            // 短信模板内容
                            RespHelper.orServEx(
                                    messageTemplateReadService.getMessageContent(
                                            message.getMessageTemplate(),
                                            map
                                    )
                            )
                    );
                    message.setSendedAt(new Date());
                    message.setStatus(DoctorMessage.Status.SENDED.getValue());
                }
            } catch (Exception e) {
                log.error("msg message send error, cause by {}", Throwables.getStackTraceAsString(e));
                message.setFailedBy(Throwables.getStackTraceAsString(e));
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
            try{
                // 获取用户信息
                User user = (User) RespHelper.orServEx(userReadService.findById(message.getUserId()));
                if (StringUtils.isNotBlank(user.getEmail())) {
                    Map<String, Serializable> map = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(message.getData(), Map.class);
                    emailService.send(
                            "[新融农牧]" + Category.from(message.getCategory()).getDescribe(),
                            // 邮件模板内容
                            RespHelper.orServEx(
                                    messageTemplateReadService.getMessageContent(
                                            message.getMessageTemplate(),
                                            map
                                    )
                            ),
                            user.getEmail(),
                            null
                    );
                }
                message.setSendedAt(new Date());
                message.setStatus(DoctorMessage.Status.SENDED.getValue());
            } catch (Exception e) {
                log.error("email message send error, cause by {}", Throwables.getStackTraceAsString(e));
                message.setFailedBy(Throwables.getStackTraceAsString(e));
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
            try{
                // 获取用户信息
                Map<String, Serializable> map = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(message.getData(), Map.class);
                // 推送消息
                if (message.getUserId() != null) {
                    appPushWebService.send("[" + message.getUserId() + "]", message.getMessageTemplate(), map, null);
                }

                message.setSendedAt(new Date());
                message.setStatus(DoctorMessage.Status.SENDED.getValue());
            } catch (Exception e) {
                log.error("email message send error, cause by {}", Throwables.getStackTraceAsString(e));
                message.setFailedBy(Throwables.getStackTraceAsString(e));
                message.setStatus(DoctorMessage.Status.FAILED.getValue());
            }
            doctorMessageWriteService.updateMessage(message);
        }
    }
}
