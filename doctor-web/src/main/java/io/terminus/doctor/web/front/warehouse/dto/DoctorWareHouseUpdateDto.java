package io.terminus.doctor.web.front.warehouse.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-07-02
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Data
public class DoctorWareHouseUpdateDto implements Serializable{

    private static final long serialVersionUID = 5856516410957509148L;

    private Long managerId; //管理员Id

    private String managerName; // 管理员姓名

    private String address; // 管理员地址

    private String warehouseName; //仓库名称
}
