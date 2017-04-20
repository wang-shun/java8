package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 09:42 2017/4/20
 */
@Data
public class DoctorGroupStock implements Serializable {

    private static final long serialVersionUID = 6890790845464828321L;

    private Integer farrowEnd;

    private Integer nurseryEnd;

    private Integer fattenEnd;

    private Integer houbeiEnd;
}
