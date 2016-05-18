package io.terminus.doctor.open.rest.farm;

import io.terminus.doctor.open.util.OPRespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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

    private final DoctorFarmReadService doctorFarmReadService;

    @Autowired
    private OPFarms(DoctorFarmReadService doctorFarmReadService) {
        this.doctorFarmReadService = doctorFarmReadService;
    }

    /**
     * 查询公司概况
     * @return 公司信息
     */
    @OpenMethod(key = "get.company.info")
    public DoctorOrg getOrgInfo() {
        return OPRespHelper.orOPEx(doctorFarmReadService.findOrgByUserId(UserUtil.getUserId()));
    }

    /**
     * 根据用户id查询所拥有权限的猪场信息
     * @return 猪场信息list
     */
    @OpenMethod(key = "get.farm.info")
    public List<DoctorFarm> getFarmInfo() {
        return OPRespHelper.orOPEx(doctorFarmReadService.findFarmsByUserId(UserUtil.getUserId()));
    }
}
