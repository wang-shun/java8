package io.terminus.doctor.event.dao;

import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.model.DoctorMasterialDatailsGroup;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by terminus on 2017/4/18.
 */
@Repository
public class DoctorGroupMaterialDao extends MyBatisDao<DoctorMasterialDatailsGroup>{
    /**
     * 批量插入DoctorMasterialDatailsGroup数据
     */
    public void insterDoctorGroupMaterials(List<DoctorMasterialDatailsGroup> doctorMasterialDatailsGroups) {
        getSqlSession().insert(sqlId("creates"), doctorMasterialDatailsGroups);
    }
    /**
     * 删除数据
     */
    public void deleteDoctorGroupMaterials() {
        getSqlSession().delete(sqlId("delete"));
    }
    /**
     * 插查询DoctorMasterialDatailsGroup数据
     */
    public Paging<DoctorMasterialDatailsGroup> findMasterialDatails(Map<String, Object> map,Integer offset, Integer limit){
        map = Params.filterNullOrEmpty(map);
        map.put("offset", offset);
        map.put("limit", limit);
        Long total = getSqlSession().selectOne(sqlId("count"), map);
        if (total <= 0){
            return new Paging<>(0L, Collections.<DoctorMasterialDatailsGroup>emptyList());
        }
        List<DoctorMasterialDatailsGroup> doctorMasterialDatailsGroups = getSqlSession().selectList(sqlId("find"), map);
        return new Paging<>(total, doctorMasterialDatailsGroups);
    }
}
