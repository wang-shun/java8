package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorProfitMaterialOrPig;

import java.util.Map;
import java.util.List;

/**
 * 物料和猪类型的报表数据读出
 * Created by terminus on 2017/4/12.
 */
public interface DoctorProfitMaterOrPigReadServer {

    /**
     *
     * @param farmId
     * @param map
     * @return
     */
    Response<List<DoctorProfitMaterialOrPig>> findProfitMaterialOrPig(Long farmId, Map<String, Object> map);
}
