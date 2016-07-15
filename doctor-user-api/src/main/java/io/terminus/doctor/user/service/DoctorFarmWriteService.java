package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorFarm;

import java.util.List;

/**
 * Desc: 猪场信息写接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public interface DoctorFarmWriteService {

    /**
     * 创建猪场
     * @param farm
     * @return
     */
    Response<Long> createFarm(DoctorFarm farm);

    /**
     * 批量创建猪场
     * @param farms
     * @return
     */
    Response<Integer> createFarms(List<DoctorFarm> farms);

    /**
     * 更新猪场
     * @param farm
     * @return
     */
    Response<Boolean> updateFarm(DoctorFarm farm);

    /**
     * 删除猪场
     * @param farmId
     * @return
     */
    Response<Boolean> deleteFarm(Long farmId);

    /**
     * 给已开通猪场软件的用户添加猪场, 内含事务控制
     * @param userId
     * @param farms
     * @return
     */
    Response<Boolean> addFarms4PrimaryUser(Long userId, List<DoctorFarm> farms);
}
