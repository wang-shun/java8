package io.terminus.doctor.web.core.msg.email;

import lombok.Data;
import lombok.experimental.Builder;

/**
 * Desc: 普通短信
 * Mail: houly@terminus.io
 * Data: 下午2:04 16/5/30
 * Author: houly
 */
@Data
@Builder
public class CommonMailToken {
    private String host;
    private Integer port;
    private String account;
    private String password;
    private Integer protocol;        //1: SSL; 2: TLS
}

