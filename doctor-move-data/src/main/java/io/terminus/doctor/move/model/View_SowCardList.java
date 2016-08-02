package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class View_SowCardList implements Serializable {
    private static final long serialVersionUID = 2952157119962433190L;
    private String pigOutId;         // 猪的OID
    private String status;           // 猪群状态
    private String farmOutId;
    private String pigCode;          // 猪群号
    private String pigFatherCode;
    private String pigMotherCode;
    private Integer source;
    private Date birthDate;
    private Double birthWeight;
    private Date inFarmDate;       // 进场日期
    private Integer inFarmDayAge;     // 进场日龄
    private String initBarnName;
    private String currentBarnOutId; // 当前猪舍outId
    private Integer firstParity;      // 初始胎次
    private Integer currentParity;    // 当前胎次
    private Integer leftCount;        // 左乳头数
    private Integer rightCount;       // 右乳头数
    private String breed;
    private String genetic;
    private Double weight;
    private String remark;
    private String outFarmDate;      // 离场日期
    private String pigType;
}
