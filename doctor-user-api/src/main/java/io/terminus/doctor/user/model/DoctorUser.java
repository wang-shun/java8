package io.terminus.doctor.user.model;

import io.terminus.parana.common.model.ParanaUser;
import lombok.Data;

/**
 * Desc:
 * Mail: houly@terminus.io
 * Data: 下午6:41 16/5/25
 * Author: houly
 */
@Data
public class DoctorUser extends ParanaUser {
    private static final long serialVersionUID = 6527616850371469846L;

    private String mobile;

    private String auth;

    private Long orgId;

    private Long farmId;

    /**
     * 猪场软件服务的审核状态
     * @see DoctorServiceReview.Status
     */
    private Integer reviewStatusDoctor;
}
