package io.terminus.doctor.open.rest.farm;

import io.terminus.common.model.Response;
import io.terminus.doctor.open.dto.DoctorBasicDto;
import io.terminus.doctor.open.dto.DoctorFarmBasicDto;
import io.terminus.doctor.open.rest.farm.service.DoctorStatisticReadService;
import io.terminus.doctor.open.util.OPRespHelper;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import io.terminus.pampas.openplatform.exceptions.OPClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@OpenBean
@SuppressWarnings("unused")
public class OPFarms {

    private final DoctorStatisticReadService doctorStatisticReadService;
    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    @Autowired
    private OPFarms(DoctorStatisticReadService doctorStatisticReadService, DoctorUserDataPermissionReadService doctorUserDataPermissionReadService) {
        this.doctorStatisticReadService = doctorStatisticReadService;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
    }

    /**
     * 查询单个猪场信息
     * 猪场id
     * @return 猪场信息
     * @see DoctorStatisticReadService#getFarmStatistic(java.lang.Long) 正式接口
     */
    @OpenMethod(key = "get.farm.info", paramNames = "farmId")
    public DoctorFarmBasicDto getFarmInfo(@NotNull(message = "farmId.not.null") Long farmId) {
        return OPRespHelper.orOPEx(doctorStatisticReadService.getFarmStatistic(farmId));
    }

    /**
     * 根据用户id查询所拥有权限的猪场信息
     * @return 猪场信息list
     * @see DoctorStatisticReadService#getOrgStatistic(java.lang.Long) 正式接口
     */
    @OpenMethod(key = "get.company.info")
    public DoctorBasicDto getCompanyInfo() {
        return OPRespHelper.orOPEx(doctorStatisticReadService.getOrgStatistic(UserUtil.getUserId()));
    }
    /**
     * 根据用户id查询所拥有权限的猪场信息
     * @return 猪场信息list
     * @see DoctorStatisticReadService#getOrgStatistic(java.lang.Long) 正式接口
     */
    @OpenMethod(key = "get.company.info.org",paramNames = "orgId")
    public DoctorBasicDto getCompanyInfoByOrg(Long orgId) {
        return OPRespHelper.orOPEx(doctorStatisticReadService.getOrgStatisticByOrg(UserUtil.getUserId(),orgId));
    }
}
