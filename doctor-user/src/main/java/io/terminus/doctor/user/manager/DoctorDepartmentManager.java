package io.terminus.doctor.user.manager;

import io.terminus.common.utils.Arguments;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dto.DoctorDepartmentDto;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xjn on 17/7/19.
 * 部门管理,暂时不添加缓存,从数据库查询
 */
@Component
public class DoctorDepartmentManager {
    @Autowired
    private DoctorFarmDao doctorFarmDao;
    @Autowired
    private DoctorOrgDao doctorOrgDao;

    public List<DoctorFarm> findAllFarmsByOrgId(Long orgId) {
        List<DoctorOrg> orgList = doctorOrgDao.findOrgByParentId(orgId);
        if (Arguments.isNullOrEmpty(orgList)) {
            return doctorFarmDao.findByOrgId(orgId);
        }
        return doctorFarmDao.findByOrgIds(orgList.stream().map(DoctorOrg::getId).collect(Collectors.toList()));
    }

    public DoctorDepartmentDto findCliqueTree(Long orgId) {
        DoctorOrg doctorOrg = doctorOrgDao.findById(orgId);
        DoctorDepartmentDto departmentDto = new DoctorDepartmentDto();
        departmentDto.setDepartmentId(doctorOrg.getId());
        departmentDto.setDepartmentName(doctorOrg.getName());
        List<DoctorOrg> children = doctorOrgDao.findOrgByParentId(orgId);
        if (Arguments.isNullOrEmpty(children)) {
            return departmentDto;
        }
        departmentDto.setChildrenList(children.stream().map(childOrg ->
                findCliqueTree(childOrg.getId())).collect(Collectors.toList()));
        return departmentDto;
    }

    public Boolean bindDepartment(Long parentId, List<Long> orgIds) {
        orgIds.forEach(orgId -> {
            DoctorOrg doctorOrg = doctorOrgDao.findById(orgId);
            DoctorOrg updateOrg = new DoctorOrg();
            updateOrg.setId(doctorOrg.getId());
            updateOrg.setParentId(parentId);
            updateOrg.setType(DoctorOrg.Type.ORG.getValue());
            doctorOrgDao.update(updateOrg);
        });
        return Boolean.TRUE;
    }

    public Boolean unbindDepartment(Long orgId) {
        DoctorOrg doctorOrg = doctorOrgDao.findById(orgId);
        DoctorOrg updateOrg = new DoctorOrg();
        updateOrg.setId(doctorOrg.getId());
        updateOrg.setParentId(0L);
        updateOrg.setType(DoctorOrg.Type.CLIQUE.getValue());
        return doctorOrgDao.update(updateOrg);
    }
}
