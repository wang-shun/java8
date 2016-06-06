package io.terminus.doctor.web.front.msg.controller;

import com.google.common.base.Preconditions;
import io.terminus.common.model.Paging;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/6
 */
@RestController
@Slf4j
@RequestMapping("/api/doctor/msg")
public class DoctorMessages {

    private final DoctorMessageReadService doctorMessageReadService;

    private final DoctorMessageWriteService doctorMessageWriteService;

    @Autowired
    public DoctorMessages(DoctorMessageReadService doctorMessageReadService,
                          DoctorMessageWriteService doctorMessageWriteService) {
        this.doctorMessageReadService = doctorMessageReadService;
        this.doctorMessageWriteService = doctorMessageWriteService;
    }

    /**
     * 查询预警消息分页
     * @param pageNo    页码
     * @param pageSize  页大小
     * @param criteria  参数
     * @return
     */
    @RequestMapping(value = "/warn/messages", method = RequestMethod.GET)
    public Paging<DoctorMessage> pagingWarnDoctorMessages(@RequestParam("pageNo") Integer pageNo,
                                                      @RequestParam("pageSize") Integer pageSize,
                                                      @RequestParam Map<String, Object> criteria) {
        criteria.put("userId", UserUtil.getUserId());
        return RespHelper.or500(doctorMessageReadService.pagingWarnMessages(criteria, pageNo, pageSize));
    }

    /**
     * 查询系统消息分页
     * @param pageNo    页码
     * @param pageSize  页大小
     * @param criteria  参数
     * @return
     */
    @RequestMapping(value = "/sys/messages", method = RequestMethod.GET)
    public Paging<DoctorMessage> pagingSysDoctorMessages(@RequestParam("pageNo") Integer pageNo,
                                                          @RequestParam("pageSize") Integer pageSize,
                                                          @RequestParam Map<String, Object> criteria) {
        criteria.put("userId", UserUtil.getUserId());
        return RespHelper.or500(doctorMessageReadService.pagingSysMessages(criteria, pageNo, pageSize));
    }

    /**
     * 查询未读消息数量
     * @return
     */
    @RequestMapping(value = "/noReadCount", method = RequestMethod.GET)
    public Long findNoReadCount() {
        return RespHelper.or500(doctorMessageReadService.findNoReadCount(UserUtil.getUserId()));
    }

    /**
     * 查询消息详情
     * @param id    消息id
     * @return
     */
    @RequestMapping(value = "/message/detail", method = RequestMethod.GET)
    public DoctorMessage findMessageDetail(@RequestParam("id") Long id) {
        // 将消息设置为已读
        DoctorMessage message = RespHelper.or500(doctorMessageReadService.findMessageById(id));
        if (message != null) {
            message.setStatus(DoctorMessage.Status.READED.getValue());
            doctorMessageWriteService.updateMessage(message);
        }
        return message;
    }

    /**
     * 更新消息
     * @param doctorMessage
     * @return
     */
    @RequestMapping(value = "/message", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOrUpdateMessgae(@RequestBody DoctorMessage doctorMessage) {
        Preconditions.checkNotNull(doctorMessage, "messgae.not.null");
        if (doctorMessage.getId() == null) {
            doctorMessage.setCreatedBy(UserUtil.getUserId());
            RespHelper.or500(doctorMessageWriteService.createMessage(doctorMessage));
        }else {
            RespHelper.or500(doctorMessageWriteService.updateMessage(doctorMessage));
        }
        return Boolean.TRUE;
    }

    /**
     * 删除消息
     * @param id    消息id
     * @return
     */
    @RequestMapping(value  = "/message", method = RequestMethod.DELETE)
    public Boolean deleteMessage(@RequestParam Long id) {
        return RespHelper.or500(doctorMessageWriteService.deleteMessageById(id));
    }
}
