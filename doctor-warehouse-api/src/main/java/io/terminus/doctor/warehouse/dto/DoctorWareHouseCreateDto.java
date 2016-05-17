package io.terminus.doctor.warehouse.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 仓库信息创建
 */
@Data
public class DoctorWareHouseCreateDto implements Serializable{

    private Integer materialId;

    private String materialName;

}
