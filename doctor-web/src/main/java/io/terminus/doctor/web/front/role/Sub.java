package io.terminus.doctor.web.front.role;

import lombok.Data;

/**
 * Desc: 子账号
 * Mail: houly@terminus.io
 * Data: 下午5:24 16/5/25
 * Author: houly
 */
@Data
public class Sub {

    private Long id;

    private String username;

    private String password;

    private Long roleId;
}
