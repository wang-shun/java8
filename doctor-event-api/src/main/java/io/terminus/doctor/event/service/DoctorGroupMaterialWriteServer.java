package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorMasterialDatailsGroup;

import java.util.List;

/**
 * Created by terminus on 2017/4/18.
 */
public interface DoctorGroupMaterialWriteServer {
    /**
     * 批量插入DoctorMasterialDatailsGroup数据
     * @return
     */
    public Response<Boolean> insterDoctorGroupMaterialWareDetails(List<DoctorMasterialDatailsGroup> doctorMasterialDatailsGroups);
    /**
     * 根据flag标志来进行删除
     * 删除
     * @param flag
     */
    public Response<Boolean> deleteDoctorGroupMaterial(Integer flag);
}
