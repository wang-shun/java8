package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.Data;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorChangeGroupExportDto extends DoctorGroupEvent{
    /**
     * 猪群变动类型id
     */
    private Long changeTypeId;

    /**
     * 猪群变动类型name
     */
    private String changeTypeName;

    /**
     * 变动原因id
     */
    private Long changeReasonId;

    /**
     * 变动原因
     */
    private String changeReasonName;

    private Long breedId;

    private String breedName;

    /**
     * 单价(分)
     */
    private Long price;

    /**
     * 金额(分)
     */
    private Long amount;

    /**
     * 客户id
     */
    private Long customerId;

    private String customerName;

    /**
     * 其中:公猪数
     */
    private Integer boarQty;

    /**
     * 其中:母猪数
     */
    private Integer sowQty;

    /**
     * 基础重量
     * @see io.terminus.doctor.event.enums.SaleBaseWeight
     */
    private Integer baseWeight;

    /**
     * 超出价格(分/kg)
     */
    private Long overPrice;

}
