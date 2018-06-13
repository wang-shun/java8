package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorOrgsLogs;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-06-13 19:41:03
 * Created by [ your name ]
 */
public interface DoctorOrgsLogsWriteService {

    /**
     * 鍒涘缓
     * @param doctorOrgsLogs
     * @return Boolean
     */
    Response<Long> create(DoctorOrgsLogs doctorOrgsLogs);

    /**
     * 鏇存柊
     * @param doctorOrgsLogs
     * @return Boolean
     */
    Response<Boolean> update(DoctorOrgsLogs doctorOrgsLogs);

    /**
     * 鍒犻櫎
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}