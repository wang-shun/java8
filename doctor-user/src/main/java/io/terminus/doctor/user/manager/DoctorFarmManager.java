package io.terminus.doctor.user.manager;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionWriteService;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.address.service.AddressReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by chenzenghui on 16/7/15.
 */

@Slf4j
@Component
public class DoctorFarmManager {
    private final DoctorFarmDao doctorFarmDao;
    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    private final DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService;
    private final AddressReadService addressReadService;
    private final DoctorOrgReadService doctorOrgReadService;
    
    @Autowired
    public DoctorFarmManager(DoctorFarmDao doctorFarmDao,
                             DoctorUserDataPermissionDao doctorUserDataPermissionDao,
                             DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService,
                             AddressReadService addressReadService,
                             DoctorOrgReadService doctorOrgReadService){
        this.doctorFarmDao = doctorFarmDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
        this.doctorUserDataPermissionWriteService = doctorUserDataPermissionWriteService;
        this.addressReadService = addressReadService;
        this.doctorOrgReadService = doctorOrgReadService;
    }

    /**
     * 给已开通猪场软件的用户添加猪场
     * @param userId
     * @param farms
     */
    @Transactional
    public void addFarms4PrimaryUser(Long userId, List<DoctorFarm> farms){
        DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgByUserId(userId));
        List<Long> newFarmIds = Lists.newArrayList(); //将被保存下来的新猪场
        farms.stream().forEach(farm -> {
            farm.setOrgName(org.getName());
            farm.setOrgId(org.getId());
            if(farm.getProvinceId() != null){
                farm.setProvinceName(RespHelper.orServEx(addressReadService.findById(farm.getProvinceId())).getName());
            }
            if(farm.getCityId() != null){
                farm.setCityName(RespHelper.orServEx(addressReadService.findById(farm.getCityId())).getName());
            }
            if(farm.getDistrictId() != null){
                farm.setDistrictName(RespHelper.orServEx(addressReadService.findById(farm.getDistrictId())).getName());
            }
            doctorFarmDao.create(farm);
            newFarmIds.add(farm.getId());
        });

        //更新下数据权限
        DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(userId);
        permission.setFarmIds(permission.getFarmIds() + "," + Joiner.on(",").join(newFarmIds));
        RespHelper.orServEx(doctorUserDataPermissionWriteService.updateDataPermission(permission));
    }
}
