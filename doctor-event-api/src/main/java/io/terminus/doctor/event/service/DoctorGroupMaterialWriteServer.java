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
     * @param doctorMasterialDatailsGroups
     * @return
     */
    public Response<Boolean> insterDoctorGroupMaterial(List<DoctorMasterialDatailsGroup> doctorMasterialDatailsGroups);
    /**
     * 删除
     */
    public Response<Boolean> deleteDoctorGroupMaterial();
}
