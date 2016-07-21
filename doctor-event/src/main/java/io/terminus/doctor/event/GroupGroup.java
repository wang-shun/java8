package io.terminus.doctor.event;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/21
 */
@Data
public class FuckGroup implements Serializable {
    private static final long serialVersionUID = -6399348639152659020L;

    private String outId;
    private String groupId;
    private String openAt;
    private String closeAt;
    private String status;
    private String sex;
    private String breed;
    private String genetic;
    private String barn;
    private String weight;

}
