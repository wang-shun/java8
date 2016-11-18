package io.terminus.doctor.event.dto.report.common;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 18:42 16/9/21
 */
@Data
public class DoctorStockStructureCommonReport implements Serializable{
    private static final long serialVersionUID = -4518103155605236994L;

    private String sumAt;              //统计月份
    private String type;               //1:胎次; 2:品种;
    private long businessId;           //胎次、品种id
    private String businessName;       //具体胎次、品种
    private Integer count;        //头数

}
