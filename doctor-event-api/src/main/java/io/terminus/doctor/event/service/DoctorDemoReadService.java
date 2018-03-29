package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorDemo;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-03-29 10:49:19
 * Created by [ your name ]
 */
public interface DoctorDemoReadService {

    /**
     * 查询
     * @param id
     * @return doctorDemo
     */
    Response<DoctorDemo> findById(Long id);

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorDemo>
     */
    Response<Paging<DoctorDemo>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

   /**
    * 列表
    * @param criteria
    * @return List<DoctorDemo>
    */
    Response<List<DoctorDemo>> list(Map<String, Object> criteria);


    Response<DoctorDemo> findByName(String name);


    Response<Boolean> createDemo(DoctorDemo doctorDemo);
}