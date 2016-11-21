package io.terminus.doctor.basic.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorFarmBasic;

import java.util.List;
import java.util.Map;

/**
 * Desc: 猪场基础数据关联表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-11-21
 */
public interface DoctorFarmBasicReadService {

    /**
     * 根据id查询猪场基础数据关联表
     * @param farmBasicId 主键id
     * @return 猪场基础数据关联表
     */
    Response<DoctorFarmBasic> findFarmBasicById(Long farmBasicId);

    /**
     * 查询所有猪场基础数据关联表
     * @return 所有猪场基础数据关联表
     */
    Response<List<DoctorFarmBasic>> findAllFarmBasics();

    /**
     * 分页查询猪场基础数据关联表
     * @param criteria  查询条件
     * @param pageNo    当前页码
     * @param size      分页大小
     * @return 猪场基础数据关联表分页查询结果
     */
    Response<Paging<DoctorFarmBasic>> pagingFarmBasic(Map<String, Object> criteria, Integer pageNo, Integer size);
}
