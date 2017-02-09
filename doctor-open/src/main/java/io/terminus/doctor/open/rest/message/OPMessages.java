package io.terminus.doctor.open.rest.message;

import io.terminus.doctor.event.service.DoctorMessageUserReadService;
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

    private final DoctorMessageUserReadService doctorMessageUserReadService;

    @Autowired
    public OPMessages(DoctorMessageUserReadService doctorMessageUserReadService) {
        this.doctorMessageUserReadService = doctorMessageUserReadService;
    }

    /**
     * 获取未读消息数量
     * @return 未读消息数量
     */
    @OpenMethod(key = "get.unread.message")
    public Integer getUnReadMessages() {
        return OPRespHelper.orOPEx(doctorMessageUserReadService.findNoReadCount(UserUtil.getUserId())).intValue();
    }
}
