package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/13.
 * 用于猪事件的多报表生成
 */
@Data
public class DoctorPigBoarInFarmExportDto implements Serializable{

    private static final long serialVersionUID = -5594568389939010764L;

    /**
     * 进场胎次
     */
    private Integer parity;
    /**
     * 猪代码
     */
    private String pigCode;
    /**
     * 品种
     */
    private String breedName;
    /**
     * 品系
     */
    private String breedTypeName;
    /**
     * 进场日期
     */
    private Date inFarmDate;
    /**
     * 出生日期
     */
    private Date birthday;
    /**
     * 公猪类型
     */
    private String boarTypeName;
    /**
     * 进场猪舍
     */
    private String initBarnName;
    /**
     * 来源
     */
    private Integer source;
    /**
     * 来源名
     */
    private String sourceName;
    /**
     * 猪的父亲代码
     */
    private String fatherCode;
    /**
     * 猪的母亲代码
     */
    private String motherCode;
    /**
     * 备注
     */
    private String entryMark;
    /**
     * 录入人员
     */
    private String creatorName;
    /**
     * 进场状态
     */
    private Integer pigStatusAfter;
    private String pigStatusAfterName;
    /**
     * 耳缺号
     */
    private String earCode;
    /**
     * 左乳头数
     */
    private Integer left;
    /**
     * 右乳头数
     */
    private Integer right;
}
