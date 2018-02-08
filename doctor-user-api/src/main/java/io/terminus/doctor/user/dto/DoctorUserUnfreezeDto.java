package io.terminus.doctor.user.dto;

import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.parana.user.model.UserProfile;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 18/2/7.
 * email:xiaojiannan@terminus.io
 */
@Data
public class DoctorUserUnfreezeDto implements Serializable{
    private static final long serialVersionUID = -7062559291837285740L;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户类型
     * @see io.terminus.doctor.common.enums.UserType
     */
    private Integer userType;

    /**
     * 猪场主账户信息（当用户是主账户是）
     */
    private PrimaryUser primaryUser;

    /**
     * 猪场子账户信息（当用户是子账户时）
     */
    private Sub sub;

    /**
     * 用户权限
     */
    private DoctorUserDataPermission permission;

    private UserProfile userProfile;

}
