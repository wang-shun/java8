package io.terminus.doctor.web.front.warehouse.dto;

import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chenzenghui on 16/9/27.
 */
@Data
public class MaterialReport implements Serializable {
    private static final long serialVersionUID = -4312015092552964456L;

    private List<DoctorMaterialConsumeProvider> events;
    /**
     * 合计报表
     */
    private WarehouseReport.Report totalReport = new WarehouseReport.Report();
}
