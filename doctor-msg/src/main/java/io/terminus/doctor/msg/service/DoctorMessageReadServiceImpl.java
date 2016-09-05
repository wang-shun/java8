package io.terminus.doctor.msg.service;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.msg.dao.DoctorMessageDao;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Code generated by terminus code gen
 * Desc: 猪场软件消息表读服务实现类
 * Date: 2016-05-30
 * Author: chk@terminus.io
 */
@Slf4j
@Service
@RpcProvider
public class DoctorMessageReadServiceImpl implements DoctorMessageReadService {

    private final DoctorMessageDao doctorMessageDao;

    @Autowired
    public DoctorMessageReadServiceImpl(DoctorMessageDao doctorMessageDao) {
        this.doctorMessageDao = doctorMessageDao;
    }

    @Override
    public Response<DoctorMessage> findMessageById(Long messageId) {
        try {
            return Response.ok(doctorMessageDao.findById(messageId));
        } catch (Exception e) {
            log.error("find message by id failed, messageId:{}, cause:{}", messageId, Throwables.getStackTraceAsString(e));
            return Response.fail("message.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorMessage>> pagingWarnMessages(Map<String, Object> criteria, Integer pageNo, Integer pageSize) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            // 获取预警消息
            criteria.put("types", ImmutableList.of(
                    DoctorMessageRuleTemplate.Type.WARNING.getValue(), DoctorMessageRuleTemplate.Type.ERROR.getValue()));
            criteria.put("channel", Rule.Channel.SYSTEM.getValue());
            if (criteria.get("status") == null) {
                criteria.put("statuses", ImmutableList.of(DoctorMessage.Status.NORMAL.getValue(), DoctorMessage.Status.READED.getValue()));
            }
            return Response.ok(doctorMessageDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("paging message by criteria failed, criteria is {}, cause by {}", criteria, Throwables.getStackTraceAsString(e));
            return Response.fail("message.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorMessage>> pagingSysMessages(Map<String, Object> criteria, Integer pageNo, Integer pageSize) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            // 获取系统消息
            criteria.put("type", DoctorMessageRuleTemplate.Type.SYSTEM.getValue());
            criteria.put("channel", Rule.Channel.SYSTEM.getValue());
            if (criteria.get("status") == null) {
                criteria.put("statuses", ImmutableList.of(DoctorMessage.Status.NORMAL.getValue(), DoctorMessage.Status.READED.getValue()));
            }
            return Response.ok(doctorMessageDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("paging message by criteria failed, criteria is {}, cause by {}", criteria, Throwables.getStackTraceAsString(e));
            return Response.fail("message.find.fail");
        }
    }

    @Override
    public Response<List<DoctorMessage>> findMessageByCriteria(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorMessageDao.list(criteria));
        } catch (Exception e) {
            log.error("find message by criteria failed, criteria:{}, cause:{}", criteria, Throwables.getStackTraceAsString(e));
            return Response.fail("message.find.fail");
        }
    }

    @Override
    public Response<DoctorMessage> findLatestSysMessage(Long templateId) {
        try{
            return Response.ok(doctorMessageDao.findLatestSysMessage(templateId));
        } catch (Exception e) {
            log.error("find latest sys message failed, templateId:{}, cause by {}", templateId, Throwables.getStackTraceAsString(e));
            return Response.fail("latest.sys.msg.fail");
        }
    }

    @Override
    public Response<DoctorMessage> findLatestWarnMessage(Long templateId, Long farmId, Long roleId) {
        try{
            return Response.ok(doctorMessageDao.findLatestWarnMessage(templateId, farmId, roleId));
        } catch (Exception e) {
            log.error("find latest warn message failed, templateId:{}, farmId:{}, roleId:{}, cause by {}",
                    templateId, farmId, roleId, Throwables.getStackTraceAsString(e));
            return Response.fail("latest.warn.msg.fail");
        }
    }

    @Override
    public Response<DoctorMessage> findLatestWarnMessage(Long templateId, Long farmId) {
        try{
            return Response.ok(doctorMessageDao.findLatestWarnMessage(templateId, farmId));
        } catch (Exception e) {
            log.error("find latest warn message failed, templateId:{}, farmId:{}, cause by {}",
                    templateId, farmId,  Throwables.getStackTraceAsString(e));
            return Response.fail("latest.warn.msg.fail");
        }
    }

    @Override
    public Response<Long> findNoReadCount(Long userId) {
        try{
            return Response.ok(doctorMessageDao.findNoReadCount(userId));
        } catch (Exception e) {
            log.error("find no read message count failed, user id is {}, cause by {}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("message.find.fail");
        }
    }

    @Override
    public Response<List<DoctorMessage>> findMsgMessage() {
        try{
            return Response.ok(doctorMessageDao.list(
                    ImmutableMap.of("channel", Rule.Channel.MESSAGE.getValue(), "status", DoctorMessage.Status.NORMAL.getValue())));
        } catch (Exception e) {
            log.error("", Throwables.getStackTraceAsString(e));
            return Response.fail("msg.message.find.fail");
        }
    }

    @Override
    public Response<List<DoctorMessage>> findEmailMessage() {
        try{
            return Response.ok(doctorMessageDao.list(
                    ImmutableMap.of("channel", Rule.Channel.EMAIL.getValue(), "status", DoctorMessage.Status.NORMAL.getValue())));
        } catch (Exception e) {
            log.error("", Throwables.getStackTraceAsString(e));
            return Response.fail("email.message.find.fail");
        }
    }

    @Override
    public Response<List<DoctorMessage>> findAppPushMessage() {
        try{
            return Response.ok(doctorMessageDao.list(
                    ImmutableMap.of("channel", Rule.Channel.APPPUSH.getValue(), "status", DoctorMessage.Status.NORMAL.getValue())));
        } catch (Exception e) {
            log.error("", Throwables.getStackTraceAsString(e));
            return Response.fail("app.message.find.fail");
        }
    }

    @Override
    public Response<Long> findMessageCountByCriteria(Map criteria) {
        try {
            return Response.ok(doctorMessageDao.findMessageCountByCriteria(criteria));
        } catch (Exception e) {
            log.error("find.message.count.by.criteria.failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("find.message.count.by.criteria.failed");
        }
    }
}
