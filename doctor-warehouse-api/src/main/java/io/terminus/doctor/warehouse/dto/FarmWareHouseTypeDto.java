package io.terminus.doctor.warehouse.dto;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 猪场仓库类型统计
 */
public class FarmWareHouseTypeDto implements Serializable {

    private static final long serialVersionUID = 8660869818248687526L;

    private Long remainder; //剩余数量

    private Integer materialType;

    private String materialName;    //品类名称
}
