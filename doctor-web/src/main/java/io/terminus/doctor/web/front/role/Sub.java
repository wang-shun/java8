package io.terminus.doctor.web.front.role;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Desc: 子账号
 * Mail: houly@terminus.io
 * Data: 下午5:24 16/5/25
 * Author: houly
 */
@Data
public class Sub {

    private Long id;

    private Long staffId;

    private String username; //用户名

    private String password;

    private String contact; //联系方式

    private String realName; //真实姓名

    private Long roleId;    //角色ID

    private String roleName;  //角色名称

    private Date createdAt; //创建时间

    private List<Long> farmIds;
}
