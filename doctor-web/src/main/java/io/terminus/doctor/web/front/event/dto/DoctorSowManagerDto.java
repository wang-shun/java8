package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DoctorSowManagerDto implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 猪号
     */
    private String pigCode;

    /**
     * 猪rfid
     */
    private String rfid;

    /**
     * 状态
     */
    private Integer status;
    private String statusName;

    /**
     * 状态天数,母猪出于当前状态的天数
     */
    private Integer statusDay;

    /**
     * 母猪体重
     */
    private Double pigWeight;

    /**
     * 当前胎次
     */
    private Integer currentParity;

    /**
     * 品种
     */
    private String breedName;

    /**
     * 当前猪舍
     */
    private Long currentBarnId;
    private String currentBarnName;
}
