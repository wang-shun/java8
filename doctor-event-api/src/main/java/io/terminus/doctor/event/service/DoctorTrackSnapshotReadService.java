package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorTrackSnapshot;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-03-01 17:01:25
 * Created by [ your name ]
 */
public interface DoctorTrackSnapshotReadService {

    /**
     * 查询
     * @param id
     * @return doctorTrackSnapshot
     */
    Response<DoctorTrackSnapshot> findById(Long id);

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorTrackSnapshot>
     */
    Response<Paging<DoctorTrackSnapshot>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

   /**
    * 列表
    * @param criteria
    * @return List<DoctorTrackSnapshot>
    */
    Response<List<DoctorTrackSnapshot>> list(Map<String, Object> criteria);
}