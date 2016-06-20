package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.dto.DoctorMenuDto;
import io.terminus.doctor.user.enums.RoleType;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

    private Map<RoleType, List<DoctorMenuDto>> menuMap = Maps.newHashMap();


    @Autowired
    public DoctorMobileMenuReadServiceImpl(DoctorUserReadService doctorUserReadService,
                                           DoctorServiceStatusReadService doctorServiceStatusReadService,
                                           @Value("${doctor.url:default}")String url){
        this.doctorUserReadService = doctorUserReadService;
        this.doctorServiceStatusReadService = doctorServiceStatusReadService;
        DoctorMenuDto userInfo = DoctorMenuDto.builder().name("个人信息").url(url+"/user/index").iconClass("icon_myfill").level(1).build();
        DoctorMenuDto staffManage = DoctorMenuDto.builder().name("员工管理").url(url+"/authority/manage-select").iconClass("icon_pengyoufill").level(1).build();
        DoctorMenuDto farmManage = DoctorMenuDto.builder().name("猪场管理").url(url+"/entry/mall").iconClass("icon_susheguanli").level(1).build();
        menuMap.put(
                RoleType.MAIN, Lists.newArrayList(farmManage, staffManage, userInfo)
        );
        menuMap.put(
                RoleType.MAIN_CLOSED, Lists.newArrayList(userInfo)
        );
        menuMap.put(
                RoleType.SUB_SINGLE, Lists.newArrayList(userInfo)
        );
        menuMap.put(
                RoleType.SUB_MULTI, Lists.newArrayList(farmManage, userInfo)
        );
    }

    @Override
    public Response<List<DoctorMenuDto>> findMenuByUserIdAndLevel(Long userId, Integer level) {

        try {
            RoleType roleType = RoleType.from(RespHelper.orServEx(doctorUserReadService.findUserRoleTypeByUserId(userId)));
            if(Objects.equals(RoleType.MAIN, roleType)){
                DoctorServiceStatus serviceStatus = RespHelper.orServEx(doctorServiceStatusReadService.findByUserId(userId));
                if(!Objects.equals(DoctorServiceStatus.Status.OPENED.value(), serviceStatus.getPigdoctorStatus())){
                    return Response.ok(menuMap.get(RoleType.MAIN_CLOSED));
                }
            }
            return Response.ok(menuMap.get(roleType));
        } catch (Exception e) {
            log.error("find menu role by user={} failed, cause:{}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.menu.fail");
        }
    }
}
