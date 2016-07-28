package io.terminus.doctor.move.controller;

import com.google.common.base.Throwables;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by chenzenghui on 16/7/28.
 */

@Slf4j
@RestController
@RequestMapping("/api/data/warehouse")
public class WareHouseInit {
    @Autowired
    private DoctorUserReadService doctorUserReadService;
    @Autowired
    private DoctorOrgDao doctorOrgDao;
    @Autowired
    private DoctorStaffDao doctorStaffDao;
    @Autowired
    private DoctorFarmDao doctorFarmDao;
    @Autowired
    private DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    @Autowired
    private DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;

    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public String initWareHouse(@RequestParam String mobile, @RequestParam Long dataSourceId){
        log.warn("start to init warehouse data, mobile={}, dataSourceId = {}", mobile, dataSourceId);
        try{
            this.init(mobile, dataSourceId);
            log.warn("init warehouse succeed, mobile={}, dataSourceId = {}", mobile, dataSourceId);
            return "ok";
        }catch(Exception e){
            log.error("init warehouse data, mobile={}, dataSourceId={}, error:{}", mobile, dataSourceId, Throwables.getStackTraceAsString(e));
            return "error";
        }
    }

    private void init(String mobile, Long dataSourceId){
        User user = RespHelper.or500(doctorUserReadService.findBy(mobile, LoginType.MOBILE));
        Long userId = user.getId();
        DoctorStaff staff = doctorStaffDao.findByUserId(userId);
        DoctorOrg org = doctorOrgDao.findById(staff.getOrgId());
        DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(userId);
        List<Long> farmIds = permission.getFarmIdsList();

    }
}
