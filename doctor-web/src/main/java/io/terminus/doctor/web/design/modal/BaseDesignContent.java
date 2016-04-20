package io.terminus.doctor.web.design.modal;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Author:cp
 * Created on 11/5/15.
 */
public abstract class BaseDesignContent implements Serializable {
    @Setter
    @Getter
    private String app;
}
