package io.terminus.doctor.web.init;

import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorBarnWriteService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorFarmWriteService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorOrgWriteService;
import io.terminus.doctor.user.service.DoctorStaffReadService;
import io.terminus.doctor.user.service.DoctorStaffWriteService;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.terminus.doctor.common.utils.RespHelper.or500;

/**
 * Desc: 初始化猪场 (仅供内测用!)
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/21
 */
@Slf4j
@RestController
@RequestMapping("/api/test/init")
public class InitFarms {

    private static final Long INIT_ID = 0L;
    private static final DateTimeFormatter TIME = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    @Autowired
    private DoctorOrgReadService doctorOrgReadService;
    @Autowired
    private DoctorOrgWriteService doctorOrgWriteService;
    @Autowired
    private UserReadService<User> userReadService;
    @Autowired
    private DoctorFarmReadService doctorFarmReadService;
    @Autowired
    private DoctorFarmWriteService doctorFarmWriteService;
    @Autowired
    private DoctorBasicReadService doctorBasicReadService;
    @Autowired
    private DoctorBasicWriteService doctorBasicWriteService;
    @Autowired
    private DoctorBarnReadService doctorBarnReadService;
    @Autowired
    private DoctorBarnWriteService doctorBarnWriteService;
    @Autowired
    private DoctorStaffReadService doctorStaffReadService;
    @Autowired
    private DoctorStaffWriteService doctorStaffWriteService;


    /**
     * 根据用户id初始化出所有的猪场相关数据(内测用)
     * @param userId  用户id
     * @return  是否成功
     */
    @RequestMapping(value = "/farm", method = RequestMethod.GET)
    public Boolean initFarm(@RequestParam("userId") Long userId) {
        User user = or500(userReadService.findById(userId));
        init(user);
        return Boolean.TRUE;
    }

    /**
     * 根据猪场id清理猪场所有数据(内测用)
     * @param farmId  猪场id
     * @return 是否成功
     */
    public Boolean cleanFarm(@RequestParam("farmId") Long farmId) {
        return Boolean.TRUE;
    }

    @Transactional
    private void init(User user) {
        //1. 判断是否创建公司
        DoctorOrg org = initOrg(user);

        //2. 创建猪场
        DoctorFarm farm = initFarm(org);

        //3. 创建staff
        DoctorStaff staff = initStaff(farm, user);

        //4. 创建基础数据
        or500(doctorBasicWriteService.initFarmBasic(farm.getId()));

        //5. 创建猪舍
        initBarns(farm, staff);

        //6. 创建猪相关信息
        //7. 创建猪群相关信息
        //8. 创建物料相关信息

    }

    private String getName(String name) {
        return name + DateTime.now().toString(TIME);
    }

    private DoctorOrg initOrg(User user) {
        DoctorOrg org = or500(doctorOrgReadService.findOrgByUserId(user.getId()));
        if (org == null) {
            org = or500(doctorOrgReadService.findOrgById(INIT_ID));
            org.setName(getName(org.getName()));
            org.setMobile(user.getMobile());
            or500(doctorOrgWriteService.createOrg(org));
        }
        return org;
    }

    private DoctorFarm initFarm(DoctorOrg org) {
        DoctorFarm farm = or500(doctorFarmReadService.findFarmById(INIT_ID));
        farm.setOrgId(org.getId());
        farm.setOrgName(org.getName());
        farm.setName(getName(farm.getName()));
        or500(doctorFarmWriteService.createFarm(farm));
        return farm;
    }

    private DoctorStaff initStaff(DoctorFarm farm, User user) {
        DoctorStaff staff = or500(doctorStaffReadService.findStaffById(INIT_ID));
        staff.setUserId(user.getId());
        staff.setOrgId(farm.getOrgId());
        staff.setOrgName(farm.getOrgName());
        return staff;
    }

    private void initBarns(DoctorFarm farm, DoctorStaff staff) {
        List<DoctorBarn> barns = or500(doctorBarnReadService.findBarnsByFarmId(farm.getId()));
        barns.forEach(barn -> {
            barn.setFarmId(farm.getId());
            barn.setFarmName(farm.getName());
            barn.setOrgId(farm.getOrgId());
            barn.setOrgName(farm.getOrgName());
            barn.setStaffId(staff.getId());
            or500(doctorBarnWriteService.createBarn(barn));
        });
    }
}
