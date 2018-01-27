package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorPigDaily;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-12 17:33:52
 * Created by [ your name ]
 */
public interface DoctorPigDailyReadService {

    /**
     * 查询
     * @param id
     * @return doctorPigDaily
     */
    Response<DoctorPigDaily> findById(Long id);

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorPigDaily>
     */
    Response<Paging<DoctorPigDaily>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

   /**
    * 列表
    * @param criteria
    * @return List<DoctorPigDaily>
    */
    Response<List<DoctorPigDaily>> list(Map<String, Object> criteria);
}