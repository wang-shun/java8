package io.terminus.doctor.basic.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.DoctorReportFieldTypeDto;
import io.terminus.doctor.basic.model.DoctorReportFieldCustomizes;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-27 17:11:01
 * Created by [ your name ]
 */
public interface DoctorReportFieldCustomizesReadService {

    /**
     * 查询
     *
     * @param id
     * @return doctorReportFieldCustomizes
     */
    Response<DoctorReportFieldCustomizes> findById(Long id);

    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorReportFieldCustomizes>
     */
    Response<Paging<DoctorReportFieldCustomizes>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return List<DoctorReportFieldCustomizes>
     */
    Response<List<DoctorReportFieldCustomizes>> list(Map<String, Object> criteria);


    Response<List<Long>> getSelected(Long typeId,Long farmId);

    Response<List<DoctorReportFieldTypeDto>> getSelected(Long farmId);

    Response<List<DoctorReportFieldTypeDto>> getAllWithSelected(Long farmId,Integer type);
}