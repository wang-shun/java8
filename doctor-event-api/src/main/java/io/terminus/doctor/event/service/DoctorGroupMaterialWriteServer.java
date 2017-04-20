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
    public void insterDoctorGroupMaterialWareHouse( List<Long> farmIds, Integer flag);
    /**
     * 删除
     */
    public Response<Boolean> deleteDoctorGroupMaterial(Integer flag);

    /**
     *
     * @param farmIds
     * @param flag
     * @return
     */
    public void insterDoctorGroupMaterialWareDetails( List<Long> farmIds, Integer flag);

    /**
     *
     * @param farmIds
     * @param flag
     * @return
     */
    public void insterDoctorGroupMaterialWare( List<Long> farmIds, Integer flag);
}
