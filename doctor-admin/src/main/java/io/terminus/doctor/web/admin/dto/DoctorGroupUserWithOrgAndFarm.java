package io.terminus.doctor.web.admin.dto;

import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.parana.user.model.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 附带公司和猪场信息的用户
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/2/28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorGroupUserWithOrgAndFarm extends User implements Serializable {
    private static final long serialVersionUID = -5087611170784168032L;

    /**
     * 有权限的公司
     */
    private List<DoctorOrg> orgs;

    /**
     * 有权限的猪场
     */
    private List<DoctorFarm> farms;
}
