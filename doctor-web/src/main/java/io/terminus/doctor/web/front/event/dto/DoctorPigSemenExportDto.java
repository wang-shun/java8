package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/14.
 */
@Data
public class DoctorPigSemenExportDto implements Serializable{

    private static final long serialVersionUID = -7169753118655610384L;
    /**
     * 猪代码
     */
    private String pigCode;
    /**
     * 猪舍
     */
    private String barnName;
    /**
     * 采精日期
     */
    private Date semenDate;
    /**
     * 采精重量采精重量
     */
    private Double semenWeight;
    /**
     * 稀释倍数
     */
    private Double dilutionRatio;
    /**
     * 释后重量
     */
    private Double dilutionWeight;
    /**
     * 精液密度
     */
    private Double semenDensity;
    /**
     * 精液活力
     */
    private Double semenActive;
    /**
     * PH值
     */
    private  Double semenPh;
    /**
     * 总评
     *
     */
    private Double semenTotal;
    /**
     * 畸形率
     */
    private Double semenJxRatio;
    /**
     * 备注
     */
    private String semenRemark;
    /**
     * 操作人
     */
    private String updatorName;
}
