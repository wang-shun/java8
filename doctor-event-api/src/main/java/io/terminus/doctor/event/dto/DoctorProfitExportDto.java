package io.terminus.doctor.event.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by terminus on 2017/4/12.
 */
@Data
public class DoctorProfitExportDto implements Serializable{

    private static final long serialVersionUID = -2304363052266950425L;

    /**
     * 猪的类型
     */
    private String pigType;
    /**
     * 猪场Id
     */
    private Long farmId;
    /**
     * 猪的类型名
     */
    private String pigTypeName;
    /**
     * 猪舍的ID
     */
    private Long barnId;
    /**
     * 金额
     */
    private Double amount = 0.0;
}
