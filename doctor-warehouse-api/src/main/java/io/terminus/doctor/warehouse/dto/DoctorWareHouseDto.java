package io.terminus.doctor.warehouse.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 仓库列表信息
 */
@Data
public class DoctorWareHouseDto implements Serializable{

    private static final long serialVersionUID = 317867907937075936L;

    private String warehouseName;

    private String manager;

    private Integer materialId;

    private String materialName;

    private Long remainder;

    private String unitName;

    private Date recentlyConsume;   //最精领用
}
