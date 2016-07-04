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
public class DoctorMaterialInfoUpdateDto implements Serializable{

    private static final long serialVersionUID = -7230693940510550551L;

    private Long materialInfoId;

    private String materialName;    // 原料名称

    private String inputCode;

    private String mark;    // 标注信息 (非必填信息)

    private Long unitId;    //单位

    private Long unitGroupId; //组单位

    private Long defaultConsumeCount; // 默认消耗数量信息

    private Long price; //价格
}
