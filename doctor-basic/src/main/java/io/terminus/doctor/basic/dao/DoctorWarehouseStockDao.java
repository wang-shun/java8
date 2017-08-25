package io.terminus.doctor.basic.dao;

import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseStock;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-11 11:31:44
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseStockDao extends MyBatisDao<DoctorWarehouseStock> {


    public Paging<DoctorWarehouseStock> pagingMergeVendor(Integer offset, Integer limit, DoctorWarehouseStock criteria) {
        Map<String, Object> params = Maps.newHashMap();
        if (criteria != null) {
            Map<String, Object> objMap = (Map) JsonMapper.nonDefaultMapper().getMapper().convertValue(criteria, Map.class);
            params.putAll(objMap);
        }

        Long total = (Long) this.sqlSession.selectOne(this.sqlId("countMergeVendor"), criteria);
        if (total.longValue() <= 0L) {
            return new Paging(0L, Collections.emptyList());
        } else {
            params.put("offset", offset);
            params.put("limit", limit);
            List<DoctorWarehouseStock> datas = this.sqlSession.selectList(this.sqlId("pagingMergeVendor"), params);
            return new Paging(total, datas);
        }
    }

    public Paging<DoctorWarehouseStock> pagingMergeVendor(Integer offset, Integer limit, Map<String, Object> criteria) {
        if (criteria == null) {
            criteria = Maps.newHashMap();
        }

        Long total = (Long) this.sqlSession.selectOne(this.sqlId("countMergeVendor"), criteria);
        if (total.longValue() <= 0L) {
            return new Paging(0L, Collections.emptyList());
        } else {
            ((Map) criteria).put("offset", offset);
            ((Map) criteria).put("limit", limit);
            List<DoctorWarehouseStock> datas = this.sqlSession.selectList(this.sqlId("pagingMergeVendor"), criteria);
            return new Paging(total, datas);
        }
    }

    public List<DoctorWarehouseStock> listMergeVendor(DoctorWarehouseStock criteria) {
        return this.sqlSession.selectList(this.sqlId("listMergeVendor"), criteria);
    }

}
