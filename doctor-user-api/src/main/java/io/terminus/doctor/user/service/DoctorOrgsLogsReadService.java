package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorOrgsLogs;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-06-13 19:41:03
 * Created by [ your name ]
 */
public interface DoctorOrgsLogsReadService {

    /**
     * 鏌ヨ
     * @param id
     * @return doctorOrgsLogs
     */
    Response<DoctorOrgsLogs> findById(Long id);

    /**
     * 鍒嗛〉
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorOrgsLogs>
     */
    Response<Paging<DoctorOrgsLogs>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

   /**
    * 鍒楄〃
    * @param criteria
    * @return List<DoctorOrgsLogs>
    */
    Response<List<DoctorOrgsLogs>> list(Map<String, Object> criteria);
}