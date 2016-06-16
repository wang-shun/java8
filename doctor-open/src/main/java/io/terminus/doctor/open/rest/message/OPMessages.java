package io.terminus.doctor.open.rest.message;

import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.open.util.OPRespHelper;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@OpenBean
@SuppressWarnings("unused")
public class OPMessages {

    private final DoctorMessageReadService doctorMessageReadService;

    @Autowired
    public OPMessages(DoctorMessageReadService doctorMessageReadService) {
        this.doctorMessageReadService = doctorMessageReadService;
    }

    /**
     * 获取未读消息数量
     * @return 未读消息数量
     */
    @OpenMethod(key = "get.unread.message")
    public Integer getUnReadMessages() {
        return OPRespHelper.orOPEx(doctorMessageReadService.findNoReadCount(UserUtil.getUserId())).intValue();
    }
}
