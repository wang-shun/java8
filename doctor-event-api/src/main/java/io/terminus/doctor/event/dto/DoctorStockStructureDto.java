package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.dto.report.common.DoctorStockStructureCommonReport;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 17:01 2017/4/20
 */
@Data
public class DoctorStockStructureDto implements Serializable{
    private static final long serialVersionUID = -6069218646856432236L;

    /**
     * 胎次分布
     */
    private List<DoctorStockStructureCommonReport> parityStockList;

    /**
     * 品类分布
     */
    private List<DoctorStockStructureCommonReport> breedStockList;
}
