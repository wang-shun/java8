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
     * 给已开通猪场软件的用户添加猪场, 内含事务控制
     * @param userId 用户id
     * @param orgId  给公司添加猪场
     * @param farms  猪场
     * @return
     */
    Response<List<DoctorFarm>> addFarms4PrimaryUser(Long userId, Long orgId, List<DoctorFarm> farms);
}
