package io.terminus.doctor.web.front.auth;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import io.terminus.doctor.basic.service.DoctorFarmBasicReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        DoctorFarmBasic farmBasic = RespHelper.orServEx(doctorFarmBasicReadService.findFarmBasicByFarmId(farmId));
        return canBasic(farmBasic, basicId);
    }

    private static boolean canBasic(DoctorFarmBasic farmBasic, Long basicId) {
        return !(farmBasic == null || !notEmpty(farmBasic.getBasicIdList()))
                && farmBasic.getBasicIdList().contains(basicId);
    }

    /**
     * 校验此猪场是否有查看变动原因权限
     * @param farmId    猪场id
     * @param reasonId  变动原因id
     * @return true 有，false 没有
     */
    public boolean checkFarmBasicReasonAuth(Long farmId, Long reasonId) {
        DoctorFarmBasic farmBasic = RespHelper.orServEx(doctorFarmBasicReadService.findFarmBasicByFarmId(farmId));
        return canReason(farmBasic, reasonId);
    }

    private static boolean canReason(DoctorFarmBasic farmBasic, Long reasonId) {
        return !(farmBasic == null || !notEmpty(farmBasic.getReasonIdList()))
                && farmBasic.getReasonIdList().contains(reasonId);
    }

    /**
     * 根据权限过滤一把基础数据
     * @param farmId    猪场id
     * @param basics    基础数据
     * @return 过滤后的结果
     */
    public List<DoctorBasic> filterBasicByFarmAuth(Long farmId, List<DoctorBasic> basics) {
        if (!notEmpty(basics)) {
            return Collections.emptyList();
        }
        DoctorFarmBasic farmBasic = RespHelper.orServEx(doctorFarmBasicReadService.findFarmBasicByFarmId(farmId));
        return basics.stream()
                .filter(basic -> canBasic(farmBasic, basic.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 根据权限过滤一把基础数据
     * @param farmId    猪场id
     * @param reasons   基础数据
     * @return 过滤后的结果
     */
    public List<DoctorChangeReason> filterReasonByFarmAuth(Long farmId, List<DoctorChangeReason> reasons) {
        if (!notEmpty(reasons)) {
            return Collections.emptyList();
        }
        DoctorFarmBasic farmBasic = RespHelper.orServEx(doctorFarmBasicReadService.findFarmBasicByFarmId(farmId));
        return reasons.stream()
                .filter(reason -> canReason(farmBasic, reason.getId()))
                .collect(Collectors.toList());
    }
}
