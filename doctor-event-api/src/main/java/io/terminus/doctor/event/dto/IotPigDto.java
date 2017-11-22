package io.terminus.doctor.event.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/10/31.
 */
@Data
public class IotPigDto implements Serializable{

    private static final long serialVersionUID = -412635789073290156L;

    private Long pigId;
    private String pigCode;
    private String rfid;
    private Integer statusDay;
    private Integer status;
    private String statusName;
}
