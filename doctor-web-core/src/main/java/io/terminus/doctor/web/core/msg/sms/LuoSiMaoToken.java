package io.terminus.doctor.web.core.msg.sms;

import lombok.Data;
import lombok.experimental.Builder;

/**
 * Created by yaoqijun.
 * Date:2015-08-03
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Data
@Builder
public class LuoSiMaoToken {

    private String sendUrl;

    private String statusUrl;

    private String apiKey;

    private String companyName;

}
