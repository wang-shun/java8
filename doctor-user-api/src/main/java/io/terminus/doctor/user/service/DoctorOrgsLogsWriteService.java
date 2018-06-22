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
     * 创建
     * @param doctorOrgsLogs
     * @return Boolean
     */
    Response<Long> create(DoctorOrgsLogs doctorOrgsLogs);

    /**
     * 更新
     * @param doctorOrgsLogs
     * @return Boolean
     */
    Response<Boolean> update(DoctorOrgsLogs doctorOrgsLogs);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);


    /**
     * 修改公司名称后就往日志里表里加一条记录
     * @param doctorOrgsLogs
     * @return Boolean
     */
    Response<Long> createLog(DoctorOrgsLogs doctorOrgsLogs);
}