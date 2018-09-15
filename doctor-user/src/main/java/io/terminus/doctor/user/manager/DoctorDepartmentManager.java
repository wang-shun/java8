package io.terminus.doctor.user.manager;

import com.google.common.collect.Lists;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dto.DoctorDepartmentDto;
import io.terminus.doctor.user.dto.DoctorDepartmentLinerDto;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isNull;

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
//        List<DoctorOrg> orgList = doctorOrgDao.findOrgByParentId(departmentId);
//        if (Arguments.isNullOrEmpty(orgList)) {
//            return doctorFarmDao.findByOrgId(departmentId);
//        }
//        return doctorFarmDao.findByOrgIds(orgList.stream().map(DoctorOrg::getId)
//                .collect(Collectors.toList()));
        List<DoctorFarm> farmList = Lists.newArrayList();
        findFarm(departmentId, farmList);
        return farmList;
    }

    private void findFarm(Long departmentId, List<DoctorFarm> farmList) {
        List<DoctorOrg> orgList = doctorOrgDao.findOrgByParentId(departmentId);
        if (Arguments.isNullOrEmpty(orgList)) {
            farmList.addAll(doctorFarmDao.findByOrgId(departmentId));
            return;
        }
        orgList.forEach(doctorOrg -> findFarm(doctorOrg.getId(), farmList));
    }


    public DoctorDepartmentDto findCliqueTree(Long departmentId) {
        return findCliqueTreeImp(departmentId, 1);
    }

    private DoctorDepartmentDto findCliqueTreeImp(Long departmentId, Integer level) {
        DoctorOrg doctorOrg = doctorOrgDao.findById(departmentId);
        DoctorDepartmentDto departmentDto = new DoctorDepartmentDto();
        departmentDto.setId(doctorOrg.getId());
        departmentDto.setName(doctorOrg.getName());
        departmentDto.setType(doctorOrg.getType());
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

    //关联子公司得到公司（陈娟 2018-08-29）
    public List<DoctorDepartmentDto> availableBindDepartment(Long departmentId,String name) {
//        List<DoctorOrg> orgList = doctorOrgDao.findExcludeIds(upAndIncludeSelfNodeId(departmentId));
        List<DoctorOrg> company = doctorOrgDao.getCompanyByName(name);
        return company.stream().map(doctorOrg -> new DoctorDepartmentDto(doctorOrg.getId(), doctorOrg.getName(), null,doctorOrg.getType(),null))
                .collect(Collectors.toList());
    }

    private List<Long> upAndIncludeSelfNodeId(Long departmentId) {
        List<Long> departmentIdList = Lists.newArrayList();
        DoctorOrg doctorOrg = doctorOrgDao.findById(departmentId);
        if (isNull(doctorOrg)) {
            return Lists.newArrayList();
        }
        departmentIdList.add(doctorOrg.getId());
        while (!Objects.equals(doctorOrg.getType(), DoctorOrg.Type.CLIQUE.getValue())) {
            doctorOrg = doctorOrgDao.findById(doctorOrg.getParentId());
            departmentIdList.add(doctorOrg.getId());
        }
        return departmentIdList;
    }

    public Paging<DoctorDepartmentDto> pagingCliqueTree(Map<String, Object> criteria, Integer pageSize, Integer pageNo) {
        PageInfo pageInfo = PageInfo.of(pageNo, pageSize);
        //集团，公司的数据展示（陈娟 2018-8-29）
        Paging<DoctorOrg> pagingOrg = doctorOrgDao.pagingCompany(pageInfo.getOffset(), pageInfo.getLimit(), criteria);
        List<DoctorDepartmentDto> departmentDtoList = pagingOrg.getData().stream()
                .map(doctorOrg -> findCliqueTree(doctorOrg.getId())).collect(Collectors.toList());
        //可以模糊查询集团下面的公司，此时查出公司和他上面的集团
        Integer type=Integer.valueOf(criteria.get("type").toString());
        String fuzzyName = (String)criteria.get("fuzzyName");
        if(type==2){
            //先根据条件得到集团
            List<DoctorOrg> parentId = doctorOrgDao.getParentId(criteria);
            //根据集团得到符合条件的子公司
            List<DoctorDepartmentDto> parentList = parentId.stream()
                    .map(parent -> findCliqueTree2(parent.getParentId(),fuzzyName)).collect(Collectors.toList());
            departmentDtoList.addAll(parentList);

        }

        return new Paging<>(pagingOrg.getTotal(), departmentDtoList);
    }

    public DoctorDepartmentDto findCliqueTree2(Long departmentId,String fuzzyName) {
        return findCliqueTreeImp2(departmentId, fuzzyName,1);
    }

    private DoctorDepartmentDto findCliqueTreeImp2(Long departmentId,String fuzzyName, Integer level) {
        DoctorOrg doctorOrg = doctorOrgDao.findById(departmentId);
        DoctorDepartmentDto departmentDto = new DoctorDepartmentDto();
        departmentDto.setId(doctorOrg.getId());
        departmentDto.setName(doctorOrg.getName());
        departmentDto.setType(doctorOrg.getType());
        departmentDto.setLevel(level);
        level ++;
        if(fuzzyName==null||isNull(fuzzyName)){
            fuzzyName=new String("");
        }
        List<DoctorOrg> children = doctorOrgDao.findOrgByParentIdAndName(departmentId,fuzzyName);
        if (Arguments.isNullOrEmpty(children)) {
            return departmentDto;
        }
        List<DoctorDepartmentDto> childrenDepartmentList = Lists.newArrayList();
        for (DoctorOrg childOrg : children) {
            childrenDepartmentList.add(findCliqueTreeImp2(childOrg.getId(),fuzzyName, level));
        }
        departmentDto.setChildren(childrenDepartmentList);
        return departmentDto;
    }

    public DoctorDepartmentDto findClique(Long departmentId, Boolean isFarm) {
        if (isFarm) {
            DoctorFarm doctorFarm = doctorFarmDao.findById(departmentId);
            departmentId = doctorFarm.getOrgId();
        }
        return findClique(departmentId);
    }

    private DoctorDepartmentDto findClique(Long departmentId) {
        DoctorOrg doctorOrg = doctorOrgDao.findById(departmentId);
        if (Objects.equals(doctorOrg.getType(), DoctorOrg.Type.CLIQUE.getValue())) {
            return new DoctorDepartmentDto(doctorOrg.getId(), doctorOrg.getName(), 1,null, null);
        }
        return findClique(doctorOrg.getParentId());
    }

    public DoctorDepartmentLinerDto findLinerBy(Long farmId) {
        DoctorFarm farm = doctorFarmDao.findById(farmId);
        DoctorDepartmentLinerDto linerDto = new DoctorDepartmentLinerDto();
        linerDto.setFarmId(farmId);
        linerDto.setFarmName(farm.getName());
        DoctorOrg doctorOrg = doctorOrgDao.findById(farm.getOrgId());
        linerDto.setOrgId(doctorOrg.getId());
        linerDto.setOrgName(doctorOrg.getName());
        if (Objects.equals(doctorOrg.getType(), DoctorOrg.Type.CLIQUE.getValue())){
            linerDto.setCliqueId(doctorOrg.getId());
            linerDto.setCliqueName(doctorOrg.getName());
        } else {
            //此处判断还需测试，正式系统没有判断不报错，但是测试系统报错（孔景军）
            if(doctorOrg.getParentId() != 0L) {
                DoctorOrg clique = doctorOrgDao.findById(doctorOrg.getParentId());
                linerDto.setCliqueId(clique.getId());
                linerDto.setCliqueName(clique.getName());
            }
        }
        return linerDto;
    }
}
