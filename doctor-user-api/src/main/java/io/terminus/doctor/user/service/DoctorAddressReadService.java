package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.DoctorAddressDto;
import io.terminus.parana.user.address.model.Address;

import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/14
 */

public interface DoctorAddressReadService {

    /**
     * 查询所有地址
     *
     * @return 地址list
     */
    Response<List<DoctorAddressDto>> findAllAddress();

    Response<Address> findByNameAndPid(String name, Integer pid);
}
