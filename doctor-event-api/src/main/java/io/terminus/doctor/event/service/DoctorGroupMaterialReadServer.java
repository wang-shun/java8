package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorMasterialDatailsGroup;

import java.util.Map;

/**
 * Created by terminus on 2017/4/18.
 */
public interface DoctorGroupMaterialReadServer {
    /**
     * 分页查询 DoctorMasterialDatailsGroup
     * @param map
     * @param pageNo
     * @param size
     * @return
     */
    public Response<Paging<DoctorMasterialDatailsGroup>> findMasterialDatailsGroup(Map<String, Object> map, Integer pageNo, Integer size);

}
