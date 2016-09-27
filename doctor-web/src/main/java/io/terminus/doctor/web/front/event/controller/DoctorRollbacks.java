package io.terminus.doctor.web.front.event.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.service.DoctorRollbackService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/9/27
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/rollback")
public class DoctorRollbacks {

    @RpcConsumer
    private DoctorRollbackService doctorRollbackService;

    /**
     * 回滚猪群事件
     * @param eventId 猪群事件
     * @return 是否成功
     */
    @RequestMapping(value = "/group", method = RequestMethod.GET)
    public Boolean rollbackGroupEvent(@RequestParam("eventId") Long eventId) {
        return RespHelper.or500(doctorRollbackService.rollbackGroupEvent(eventId, UserUtil.getUserId(), UserUtil.getCurrentUser().getName()));
    }

    /**
     * 回滚猪事件
     * @param eventId 猪事件
     * @return 是否成功
     */
    @RequestMapping(value = "/pig", method = RequestMethod.GET)
    public Boolean rollbackPigEvent(@RequestParam("eventId") Long eventId) {
        return RespHelper.or500(doctorRollbackService.rollbackPigEvent(eventId, UserUtil.getUserId(), UserUtil.getCurrentUser().getName()));
    }
}
