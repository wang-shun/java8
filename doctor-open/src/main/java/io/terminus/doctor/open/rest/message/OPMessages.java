package io.terminus.doctor.open.rest.message;

import io.terminus.doctor.common.utils.RandomUtil;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@OpenBean
@SuppressWarnings("unused")
public class OPMessages {

    /**
     * 获取未读消息数量
     * @return 未读消息数量
     */
    @OpenMethod(key = "get.unread.message")
    public Integer getUnReadMessages() {
        return RandomUtil.random(0, 10);
    }
}
