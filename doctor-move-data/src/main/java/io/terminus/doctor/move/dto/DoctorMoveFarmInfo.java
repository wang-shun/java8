package io.terminus.doctor.move.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

/**
 * Created by xjn on 17/3/31.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class DoctorMoveFarmInfo {

    private String oID;
    /**
     * 公司名称
     */
    private String orgName;
    /**
     * 源猪场名称
     */
    private String oldFarmName;
    /**
     * 新猪场名称
     */
    private String newFarmName;
    /**
     * 登陆名
     */
    private String loginName;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 真实姓名
     */
    private String realName;

    private String province;
    private String city;
    private String region;
    private String address;

}
