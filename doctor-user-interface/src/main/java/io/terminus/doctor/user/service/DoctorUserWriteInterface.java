package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;

/**
 * 陈增辉 16/5/23.
 * 对外提供与用户相关的dubbo接口
 */
public interface DoctorUserWriteInterface {

    Response<String> write(Integer test);
}
