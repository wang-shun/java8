package io.terminus.doctor.user.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class TargetSystemModel implements Serializable{
    private static final long serialVersionUID = -3919105221196587178L;

    private String domain;
    private String password;
    private Long corpId;
}
