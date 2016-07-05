package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.dto.DoctorMenuDto;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 菜单的读取
 * Mail: houly@terminus.io
 * Data: 下午5:55 16/6/3
 * Author: houly
 */
@Slf4j
@Service
public class DoctorMobileMenuReadServiceImpl implements DoctorMobileMenuReadService {

    private final DoctorUserReadService doctorUserReadService;
    private final DoctorServiceStatusReadService doctorServiceStatusReadService;
    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    private final PrimaryUserReadService primaryUserReadService;

    //下面的几个是首页左侧菜单
    private final DoctorMenuDto userInfo;
    private final DoctorMenuDto staffManage;
    private final DoctorMenuDto farmManageMultiple;

    @Autowired
    public DoctorMobileMenuReadServiceImpl(DoctorUserReadService doctorUserReadService,
                                           DoctorServiceStatusReadService doctorServiceStatusReadService,
                                           DoctorUserDataPermissionReadService doctorUserDataPermissionReadService,
                                           PrimaryUserReadService primaryUserReadService,
                                           @Value("${doctor.url:default}") String url){
        this.doctorUserReadService = doctorUserReadService;
        this.doctorServiceStatusReadService = doctorServiceStatusReadService;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
        this.primaryUserReadService = primaryUserReadService;

        userInfo = DoctorMenuDto.builder().name("个人信息").url(url+"/user-center").iconClass("user_info_icon").level(1).build();
        staffManage = DoctorMenuDto.builder().name("员工管理").url(url+"/authority/manage-select").iconClass("personnel_manager_icon").level(1).build();
        farmManageMultiple = DoctorMenuDto.builder().name("猪场管理").url("pigdoctor://company?homepage_type=1").iconClass("pig_farm_manager_icon").level(1).build();
    }

    @Override
    public Response<List<DoctorMenuDto>> findMenuByUserIdAndLevel(Long userId, Integer level) {
        List<DoctorMenuDto> menus = Lists.newArrayList(userInfo);
        try {
            User user = RespHelper.orServEx(doctorUserReadService.findById(userId));

            //当用户为主账号时
            if(Objects.equals(UserType.FARM_ADMIN_PRIMARY.value(), user.getType())){
                //查询服务状态
                DoctorServiceStatus serviceStatus = RespHelper.orServEx(doctorServiceStatusReadService.findByUserId(userId));
                //如果已开通猪场软件
                if(Objects.equals(DoctorServiceStatus.Status.OPENED.value(), serviceStatus.getPigdoctorStatus())){
                    //添加菜单"员工管理"
                    menus.add(staffManage);
                    //添加猪场管理菜单
                    this.addFarmManageMenu(menus, userId);
                }
            }

            //当用户为子账号时
            if(Objects.equals(UserType.FARM_SUB.value(), user.getType())){
                //当前用户是子账号, 找出他的主账号
                Long primaryUserId = RespHelper.orServEx(primaryUserReadService.findSubByUserId(userId)).getParentUserId();
                //查询服务状态
                DoctorServiceStatus serviceStatus = RespHelper.orServEx(doctorServiceStatusReadService.findByUserId(primaryUserId));
                //如果已开通猪场软件
                if(Objects.equals(DoctorServiceStatus.Status.OPENED.value(), serviceStatus.getPigdoctorStatus())) {
                    //添加猪场管理菜单
                    this.addFarmManageMenu(menus, userId);
                }
            }

            return Response.ok(menus);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("find menu role by user={} failed, cause:{}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.menu.fail");
        }
    }

    private DoctorMenuDto getSingleFarmMenu(Long farmId){
        return DoctorMenuDto.builder()
                .name("猪场管理")
                .url("pigdoctor://pigfarm?homepage_type=2&pig_farm_id=" + farmId)
                .iconClass("pig_farm_manager_icon")
                .level(1)
                .build();
    }

    private void addFarmManageMenu(List<DoctorMenuDto> menus, Long userId){
        //查询关联猪场
        DoctorUserDataPermission permission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
        if(permission != null && permission.getFarmIdsList() != null){
            //只有一个猪场
            if(permission.getFarmIdsList().size() == 1){
                menus.add(this.getSingleFarmMenu(permission.getFarmIdsList().get(0)));
            }
            //有多个猪场
            if(permission.getFarmIdsList().size() > 1){
                menus.add(farmManageMultiple);
            }
        }
    }

}
