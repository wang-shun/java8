package io.terminus.doctor.web.admin.dto;

import io.terminus.doctor.user.model.DoctorOrg;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

public class UserApplyServiceDetailDto implements Serializable{
    private static final long serialVersionUID = 3590493478487803972L;

    @Setter @Getter
    private Long userId;

    /**
     * 用户提交的公司信息
     * 运营人员审核时从后台查询得到, 提交审核结果时再传回给后台
     */
    @Setter @Getter
    private DoctorOrg org;

    /**
     * 猪场名称
     */
    @Setter @Getter
    private List<String> farms;
}
