package io.terminus.doctor.event.search.pig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪查询信息
 *      @see IndexedPig
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchedPig implements Serializable {
    private static final long serialVersionUID = 7185851187242120950L;
    /**
     * id
     */
    private Long id;

    /**
     * 猪号
     */
    private String pigCode;

    /**
     * 猪类型
     * @see io.terminus.doctor.event.model.DoctorPig.PIG_TYPE
     */
    private Integer pigType;
    private String pigTypeName;

    /**
     * 猪场信息
     */
    private Long farmId;
    private String farmName;

    /**
     * 进场日期
     */
    private Date inFarmDate;

    /**
     * 出场日期
     */
    private Date outFarmDate;

    /**
     * 状态
     */
    private Integer status;
    private String statusName;

    /**
     * 当前猪舍
     */
    private Long currentBarnId;
    private String currentBarnName;

    /**
     * 当前体重
     */
    private Date birthDate;
    private Double weight;

    /**
     * 当前日龄
     */
    private Integer dayAge;

    /**
     * 当前胎次
     */
    private Integer currentParity;
}
