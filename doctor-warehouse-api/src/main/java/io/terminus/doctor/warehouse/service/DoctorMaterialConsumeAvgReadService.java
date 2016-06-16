package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeAvgDto;

import java.util.List;

/**
 * Desc: 物料消耗信息统计方式ReadService
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/12
 */
public interface DoctorMaterialConsumeAvgReadService {

    /**
     * 根据farmId查询物料消耗信息统计
     * @param farmId
     * @return
     */
    Response<List<DoctorMaterialConsumeAvgDto>> findMaterialConsumeAvgsByFarmId(Long farmId);

}
