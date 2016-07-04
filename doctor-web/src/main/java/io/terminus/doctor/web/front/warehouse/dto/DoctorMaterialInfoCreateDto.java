package io.terminus.doctor.web.front.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-31
 * Email:yaoqj@terminus.io
 * Descirbe: 创建，修改 对应的Material Info 数据信息
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorMaterialInfoCreateDto implements Serializable{

    private static final long serialVersionUID = 746533050208692029L;

    private Long farmId;    //猪场Id

    /**
     * 对应的猪场类型
     * @see io.terminus.doctor.warehouse.enums.WareHouseType
     */
    private Integer type;

    private String materialName;    // 原料名称

    private String inputCode;

    private String mark;    // 标注信息 (非必填信息)

    private Long unitId;    //单位

    private Long unitGroupId; //组单位

    private Long defaultConsumeCount; // 默认消耗数量信息

    private Long price; //价格
}
