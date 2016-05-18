package io.terminus.doctor.user.dto;

import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.parana.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Desc: 用户基础信息dto
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorUserInfoDto implements Serializable {
    private static final long serialVersionUID = 7144766865272567461L;

    private User user;

    private DoctorStaff staff;  //职员信息
}
