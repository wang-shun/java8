package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorTrackSnapshot;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-03-01 17:01:25
 * Created by [ your name ]
 */
public interface DoctorTrackSnapshotWriteService {

    /**
     * 创建
     * @param doctorTrackSnapshot
     * @return Boolean
     */
    Response<Long> create(DoctorTrackSnapshot doctorTrackSnapshot);

    /**
     * 更新
     * @param doctorTrackSnapshot
     * @return Boolean
     */
    Response<Boolean> update(DoctorTrackSnapshot doctorTrackSnapshot);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}