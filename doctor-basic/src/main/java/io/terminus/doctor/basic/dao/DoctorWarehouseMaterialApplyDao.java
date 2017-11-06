package io.terminus.doctor.basic.dao;

import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 14:05:59
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseMaterialApplyDao extends MyBatisDao<DoctorWarehouseMaterialApply> {


    public List<DoctorWarehouseMaterialApply> listAndOrderByHandleDate(DoctorWarehouseMaterialApply criteria, Integer limit) {

        Map<String, Object> params = Maps.newHashMap();
        if (criteria != null) {
            Map<String, Object> objMap = (Map) JsonMapper.nonDefaultMapper().getMapper().convertValue(criteria, Map.class);
            params.putAll(objMap);
        }
        if (null != limit)
            params.put("limit", limit);

        return sqlSession.selectList(sqlId("listAndOrderByHandleDate"), params);
    }


    public List<DoctorWarehouseMaterialApply> advList(Map<String, Object> criteria) {
        return sqlSession.selectList(sqlId("advList"), criteria);
    }

    public DoctorWarehouseMaterialApply findMaterialHandle(Long materialHandleId) {
        List<DoctorWarehouseMaterialApply> applies = this.list(DoctorWarehouseMaterialApply.builder()
                .materialHandleId(materialHandleId)
                .build());
        if (applies.isEmpty())
            return null;

        return applies.get(0);
    }
}

