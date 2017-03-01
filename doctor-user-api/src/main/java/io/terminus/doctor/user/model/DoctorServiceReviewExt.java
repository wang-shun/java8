package io.terminus.doctor.user.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Desc: 用户服务审批扩展数据类
 */
public class DoctorServiceReviewExt extends DoctorServiceReview {
    private static final long serialVersionUID = -1415834293194710625L;

    @Getter @Setter
    private String orgName;

    @Getter @Setter
    private List<DoctorOrg> orgs;

}
