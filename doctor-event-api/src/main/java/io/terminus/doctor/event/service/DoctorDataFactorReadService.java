package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorDataFactor;

import java.util.List;
import java.util.Map;

/**
 * Desc: 信用模型计算因子读服务接口
 * Mail: hehaiyang@terminus.io
 * Date: 2017/3/17
 */
public interface DoctorDataFactorReadService {

    /**
     * 查询
     * @param id
     * @return doctorDataFactor
     */
    Response<DoctorDataFactor> findById(Long id);

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorDataFactor>
     */
    Response<Paging<DoctorDataFactor>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

   /**
    * 列表
    * @param criteria
    * @return List<DoctorDataFactor>
    */
    Response<List<DoctorDataFactor>> list(Map<String, Object> criteria);
}