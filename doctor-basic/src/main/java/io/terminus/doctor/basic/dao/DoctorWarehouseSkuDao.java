package io.terminus.doctor.basic.dao;

import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-26 17:04:11
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseSkuDao extends MyBatisDao<DoctorWarehouseSku> {


    public String findLastCode(Long orgId, Integer type) {

        Map<String, Object> params = new HashMap<>(2);
        params.put("orgId", orgId);
        params.put("type", type);
        return this.sqlSession.selectOne(this.sqlId("findLastCode"), params);
    }

    public List<DoctorWarehouseSku> findWarehouseSkuByOrgAndName(Long orgId, String name) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("orgId", orgId);
        params.put("name", name);
        return getSqlSession().selectList("findWarehouseSkuByOrgAndName", params);
    }

    public Paging<DoctorWarehouseSku> pagingWarehouseSku(Integer offset, Integer limit, Map<String, Object> criteria) {

        if (criteria == null) {
            criteria = Maps.newHashMap();
        }
        Long total = (Long) this.sqlSession.selectOne(this.sqlId("warehouseSkuCount"), criteria);
        if (total.longValue() <= 0L) {
            return new Paging(0L, Collections.emptyList());
        } else {
            ((Map) criteria).put("offset", offset);
            ((Map) criteria).put("limit", limit);
            List<DoctorWarehouseMaterialHandle> datas = this.sqlSession.selectList(this.sqlId("pagingWarehouseSku"), criteria);
            return new Paging(total, datas);
        }
    }
}
