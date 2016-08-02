package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class View_BoarCardList implements Serializable{
    private static final long serialVersionUID = -8085241787217620564L;
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
    private String breed;
    private String genetic;
    private Double weight;
    private String remark;
    private String outFarmDate;      // 离场日期
    private String pigType;
    private String boarType;         // 公猪类型 活公猪, 新鲜精液, 冷冻精液
}
