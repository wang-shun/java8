package io.terminus.doctor.user.dto;

import lombok.Data;

/**
 * Created by xjn on 18/1/10.
 * email:xiaojiannan@terminus.io
 * 组织维度线性化
 */
@Data
public class DoctorDepartmentLinerDto {

    /**
     * 集团id
     */
    private Long cliqueId;

    /**
     * 集团名称
     */
    private String cliqueName;

    /**
     * 公司id
     */
    private Long orgId;

    /**
     * 公司名称
     */
    private String orgName;

    /**
     * 猪场id
     */
    private Long farmId;

    /**
     * 猪场名称
     */
    private String farmName;
}
