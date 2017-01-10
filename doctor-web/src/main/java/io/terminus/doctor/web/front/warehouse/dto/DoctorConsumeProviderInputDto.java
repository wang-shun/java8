package io.terminus.doctor.web.front.warehouse.dto;

import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaoqijun.
 * Date:2016-05-31
 * Email:yaoqj@terminus.io
 * Descirbe: 对应的物料领用消耗录入信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorConsumeProviderInputDto implements Serializable{

    private static final long serialVersionUID = 3717428182117868904L;

    private Long farmId;       // 对应的猪场Id

    private Long wareHouseId;   //对应的仓库Id

    private Long materialId;    // 对应的物料Id

    private Double count;  //对应的数量

    private Long unitPrice; // 单价, 单位为"分"

    private Long unitId; // 单位

    private Integer consumeDays;    // 对应的消耗日期

    private Long barnId;    //对应的消耗 猪舍Id

    private String barnName;    //对应的猪舍名称

    private Long groupId; // 消耗物资的猪群
    private String groupCode;

    /**
     * 物资入库时可能会填写的供货厂家
     */
    private Long factoryId;
    private String factoryName;

    /**
     * 事件类型
     * @see DoctorMaterialConsumeProvider.EVENT_TYPE
     */
    @NotNull(message = "eventType.not.null")
    private Integer eventType;

    /**
     * 员工
     */
    private Long staffId;

    private Date eventAt;
}
