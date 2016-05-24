package io.terminus.doctor.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-23
 * Email:yaoqj@terminus.io
 * Descirbe: 用户输入基本操作信息方式
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWareHouseBasicDto implements Serializable{

    private static final long serialVersionUID = 681021478404669941L;

    private Long farmId;

    private String farmName;

    private Long wareHouseId;

    private String wareHouseName;

    private Long materialId;

    private String materialName;

    private Long barnId;

    private String barnName;

    private Long staffId;

    private String staffName;
}
