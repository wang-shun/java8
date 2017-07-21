package io.terminus.doctor.user.manager;

import com.google.common.collect.Lists;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dto.DoctorDepartmentDto;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public List<DoctorFarm> findAllFarmsByOrgId(Long departmentId) {
        List<DoctorOrg> orgList = doctorOrgDao.findOrgByParentId(departmentId);
        if (Arguments.isNullOrEmpty(orgList)) {
            return doctorFarmDao.findByOrgId(departmentId);
        }
        return doctorFarmDao.findByOrgIds(orgList.stream().map(DoctorOrg::getId)
                .collect(Collectors.toList()));
    }

    public DoctorDepartmentDto findCliqueTree(Long departmentId) {
        return findCliqueTreeImp(departmentId, 1);
    }

    private DoctorDepartmentDto findCliqueTreeImp(Long departmentId, Integer level) {
        DoctorOrg doctorOrg = doctorOrgDao.findById(departmentId);
        DoctorDepartmentDto departmentDto = new DoctorDepartmentDto();
        departmentDto.setId(doctorOrg.getId());
        departmentDto.setName(doctorOrg.getName());
        departmentDto.setLevel(level);
        level ++;
        List<DoctorOrg> children = doctorOrgDao.findOrgByParentId(departmentId);
        if (Arguments.isNullOrEmpty(children)) {
            return departmentDto;
        }
        List<DoctorDepartmentDto> childrenDepartmentList = Lists.newArrayList();
        for (DoctorOrg childOrg : children) {
            childrenDepartmentList.add(findCliqueTreeImp(childOrg.getId(), level));
        }
        departmentDto.setChildren(childrenDepartmentList);
        return departmentDto;
    }

    public Boolean bindDepartment(Long parentId, List<Long> orgIds) {
        if (Arguments.isNullOrEmpty(orgIds)) {
            return Boolean.FALSE;
        }

        List<DoctorOrg> hasOrgList= doctorOrgDao.findOrgByParentId(parentId);
        if (!hasOrgList.isEmpty()) {
            doctorOrgDao.unbindDepartment(hasOrgList.stream().map(DoctorOrg::getId)
                    .collect(Collectors.toList()));
        }
        doctorOrgDao.bindDepartment(orgIds, parentId);
        return Boolean.TRUE;
    }

    public Boolean unbindDepartment(Long departmentId) {
        return doctorOrgDao.unbindDepartment(Lists.newArrayList(departmentId));
    }

    public List<DoctorDepartmentDto> availableBindDepartment(Long departmentId) {
        List<DoctorOrg> orgList = doctorOrgDao.findExcludeIds(upAndIncludeSelfNodeId(departmentId));
        return orgList.stream().map(doctorOrg -> new DoctorDepartmentDto(doctorOrg.getId(), doctorOrg.getName(), null, null))
                .collect(Collectors.toList());
    }

    private List<Long> upAndIncludeSelfNodeId(Long departmentId) {
        List<Long> departmentIdList = Lists.newArrayList();
        DoctorOrg doctorOrg = doctorOrgDao.findById(departmentId);
        departmentIdList.add(doctorOrg.getId());
        while (!Objects.equals(doctorOrg.getType(), DoctorOrg.Type.CLIQUE.getValue())) {
            departmentIdList.add(doctorOrg.getId());
            doctorOrg = doctorOrgDao.findById(doctorOrg.getParentId());
        }
        return departmentIdList;
    }

    public Paging<DoctorDepartmentDto> pagingCliqueTree(Map<String, Object> criteria, Integer pageSize, Integer pageNo) {
        PageInfo pageInfo = PageInfo.of(pageNo, pageSize);
        Paging<DoctorOrg> pagingOrg = doctorOrgDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria);
        List<DoctorDepartmentDto> departmentDtoList = pagingOrg.getData().stream()
                .map(doctorOrg -> findCliqueTree(doctorOrg.getId())).collect(Collectors.toList());
        return new Paging<>(pagingOrg.getTotal(), departmentDtoList);
    }
}
