package io.terminus.doctor.user.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class TargetSystemModel implements Serializable{
    private static final long serialVersionUID = -3919105221196587178L;

    @Setter @Getter
    private String domain;
    @Setter @Getter
    private String password;
    @Setter @Getter
    private Long corpId;
}
