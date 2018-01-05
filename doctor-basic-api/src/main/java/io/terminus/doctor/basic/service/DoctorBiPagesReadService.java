package io.terminus.doctor.basic.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBiPages;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-05 13:13:41
 * Created by [ your name ]
 */
public interface DoctorBiPagesReadService {

    /**
     * 查询
     *
     * @param id
     * @return doctorBiPages
     */
    Response<DoctorBiPages> findById(Long id);

    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorBiPages>
     */
    Response<Paging<DoctorBiPages>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return List<DoctorBiPages>
     */
    Response<List<DoctorBiPages>> list(Map<String, Object> criteria);


    Response<DoctorBiPages> findByName(String name);
}