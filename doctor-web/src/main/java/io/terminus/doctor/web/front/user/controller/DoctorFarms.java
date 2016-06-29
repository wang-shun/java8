package io.terminus.doctor.web.front.user.controller;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorStaffReadService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/29
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/farm")
public class DoctorFarms {

    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorStaffReadService doctorStaffReadService;

    @Autowired
    public DoctorFarms(DoctorFarmReadService doctorFarmReadService,
                       DoctorStaffReadService doctorStaffReadService) {
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorStaffReadService = doctorStaffReadService;
    }

    /**
     * 根据当前登录用户公司,查询猪场(非数据权限, 查询当前用户所属公司下的所有猪场!)
     * @return 猪场list
     */
    @RequestMapping(value = "/loginUser", method = RequestMethod.GET)
    public List<DoctorFarm> findFarmsByLoginUser() {
        if (UserUtil.getUserId() == null) {
            throw new JsonResponseException("user.not.login");
        }
        DoctorStaff staff = RespHelper.or500(doctorStaffReadService.findStaffByUserId(UserUtil.getUserId()));
        return RespHelper.or500(doctorFarmReadService.findFarmsByOrgId(staff.getOrgId()));
    }
}
