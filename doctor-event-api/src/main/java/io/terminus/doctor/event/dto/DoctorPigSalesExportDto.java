package io.terminus.doctor.event.dto;

import lombok.Data;
import java.util.Date;

import java.io.Serializable;

/**
 * Created by terminus on 2017/4/11.
 * 猪的销售月报表
 */
@Data
public class DoctorPigSalesExportDto implements Serializable{

    private static final long serialVersionUID = -6251193009963881402L;
    /**
     * 批次
     */
    private String batch;
    /**
     * 猪舍名
     */
    private String barnName;
    /**
     * 销售时间
     */
    private Date salesDate;
    /**
     * 猪的日龄
     */
    private Integer ages;
    /**
     * 猪的类型
     */
    private Integer pigType;
    /**
     * 猪的类型名
     */
    private String pigTypeName;
    /**
     * 猪的数量
     */
    private Long pigNumber;
    /**
     * 猪的重量
     */
    private Double pigWeight;
    /**
     * 猪的单价
     */
    private Double pigPrice;
    /**
     * 猪的金额
     */
    private Double pigAmount;
    /**
     * 客户
     */
    private String client;
}
