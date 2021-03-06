package io.terminus.doctor.event.dto.search;

import com.google.common.collect.Maps;
import io.terminus.doctor.event.model.DoctorPig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Desc: 猪查询信息
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
     * @see DoctorPig.PigSex
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
    private Integer pregCheckResult;

    /**
     * 品种
     */
    private Long breedId;
    private String breedName;

    /**
     * 品系
     */
    private Long geneticId;
    private String geneticName;

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

    /**
     * 猪类型
     */
    private Integer boarType;

    /**
     * 猪rfid
     */
    private String rfid;

    /**
     * 状态天数,母猪出于当前状态的天数
     */
    private Integer statusDay;

    /**
     * 母猪体重
     */
    private Double pigWeight;

    /**
     * 扩展字段:
     *      1. Date checkDate : 妊娠检查的时间
     */
    private Map<String, Object> extra = Maps.newHashMap();
}
