package io.terminus.doctor.web.front.warehouseV2.vo;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import lombok.Data;

/**
 * Created by sunbo@terminus.io on 2017/10/17.
 */
@Data
public class WarehouseMaterialApplyVo extends DoctorWarehouseMaterialApply {

    private String code;

    private String vendorName;

    private String specification;
}
