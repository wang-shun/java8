package io.terminus.doctor.web.front.auth;

import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import io.terminus.doctor.basic.service.DoctorFarmBasicReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 猪群权限中心
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/20
 */
@Slf4j
@Component
public class DoctorFarmAuthCenter {

    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorFarmBasicReadService doctorFarmBasicReadService;

    @Autowired
    public DoctorFarmAuthCenter(DoctorFarmReadService doctorFarmReadService, DoctorFarmBasicReadService doctorFarmBasicReadService) {
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorFarmBasicReadService = doctorFarmBasicReadService;
    }

    /**
     * 根据登录用户, 校验是否有操作此猪场的权限
     * @param farmId 猪场id
     */
    public BaseUser checkFarmAuth(Long farmId) {
        Long userId = UserUtil.getUserId();
        if (userId == null) {
            throw new ServiceException("user.not.login");
        }
        List<Long> farmIds = RespHelper.orServEx(doctorFarmReadService.findFarmIdsByUserId(userId));
        if (!farmIds.contains(farmId)) {
            throw new ServiceException("user.not.auth.farm");
        }
        return UserUtil.getCurrentUser();
    }

    /**
     * 校验此猪场是否有查看基础数据权限
     * @param farmId    猪场id
     * @param basicId   基础数据id
     * @return true 有，false 没有
     */
    public boolean checkFarmBasicAuth(Long farmId, Long basicId) {
        try {
            DoctorFarmBasic farmBasic = RespHelper.orServEx(doctorFarmBasicReadService.findFarmBasicByFarmId(farmId));
            return !(farmBasic == null || !notEmpty(farmBasic.getBasicIdList()))
                    && farmBasic.getBasicIdList().contains(basicId);
        } catch (Exception e) {
            log.error("check farm basic auth failed, farmId:{}, basicId:{}, cause:{}", farmId, basicId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 校验此猪场是否有查看变动原因权限
     * @param farmId    猪场id
     * @param reasonId  变动原因id
     * @return true 有，false 没有
     */
    public boolean checkFarmBasicReasonAuth(Long farmId, Long reasonId) {
        try {
            DoctorFarmBasic farmBasic = RespHelper.orServEx(doctorFarmBasicReadService.findFarmBasicByFarmId(farmId));
            return !(farmBasic == null || !notEmpty(farmBasic.getReasonIdList()))
                    && farmBasic.getReasonIdList().contains(reasonId);
        } catch (Exception e) {
            log.error("check farm basic reason auth failed, farmId:{}, reasonId:{}, cause:{}", farmId, reasonId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }
}
