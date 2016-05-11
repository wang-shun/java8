package io.terminus.doctor.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 用户可以通过本系统登录其他系统，表doctor_other_system用于维护可从本系统登录的其他系统
 * Mail: zenghui@terminus.io
 * author: 陈增辉
 * Date: 2016年05月11日
 */
@Data
public class DoctorOtherSystem implements Serializable {
    private static final long serialVersionUID = 7314838931540007646L;

    private Long id;

    private String systemCode;

    private String password;

    private Long corpId;

    private String domain;

    private Date createdAt;

    private Date updatedAt;
}
