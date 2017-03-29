package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorGroupInfoCheck;

import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 猪群数据校验表写服务
 * Date: 2017-03-25
 */

public interface DoctorGroupInfoCheckWriteService {

    /**
     * 创建DoctorGroupInfoCheck
     * @param doctorGroupInfoCheck
     * @return 主键id
     */
    Response<Long> createDoctorGroupInfoCheck(DoctorGroupInfoCheck doctorGroupInfoCheck);

    /**
     * 更新DoctorGroupInfoCheck
     * @param doctorGroupInfoCheck
     * @return 是否成功
     */
    Response<Boolean> updateDoctorGroupInfoCheck(DoctorGroupInfoCheck doctorGroupInfoCheck);

    /**
     * 根据主键id删除DoctorGroupInfoCheck
     * @param doctorGroupInfoCheckId
     * @return 是否成功
     */
    Response<Boolean> deleteDoctorGroupInfoCheckById(Long doctorGroupInfoCheckId);

    /**
     * 生成猪群校验数据
     * @return
     */
    Response<Boolean> generateGroupCheckDatas(List<Long> farmIds);
}