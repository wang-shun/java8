package io.terminus.doctor.web.front.warehouse.dto;

import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chenzenghui on 16/9/27.
 */
@Data
public class MaterialReport implements Serializable {
    private static final long serialVersionUID = -4312015092552964456L;

    private List<MaterialConsumeProviderDto> events;
    /**
     * 合计报表
     */
    private WarehouseReport.Report totalReport = new WarehouseReport.Report();

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class MaterialConsumeProviderDto extends DoctorMaterialConsumeProvider{
        private static final long serialVersionUID = -3953581304211676850L;

        private Long diaoboWarehouseId;
        private String diaoboWarehouseName;
    }
}
