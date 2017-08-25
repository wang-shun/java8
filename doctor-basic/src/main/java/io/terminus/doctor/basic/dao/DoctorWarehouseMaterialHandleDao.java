package io.terminus.doctor.basic.dao;

import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialHandle;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 08:56:13
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseMaterialHandleDao extends MyBatisDao<DoctorWarehouseMaterialHandle> {


    public Paging<DoctorWarehouseMaterialHandle> advPaging(Integer offset, Integer limit, Map<String, Object> criteria) {

        if (criteria == null) {
            criteria = Maps.newHashMap();
        }
        Long total = (Long) this.sqlSession.selectOne(this.sqlId("advCount"), criteria);
        if (total.longValue() <= 0L) {
            return new Paging(0L, Collections.emptyList());
        } else {
            ((Map) criteria).put("offset", offset);
            ((Map) criteria).put("limit", limit);
            List<DoctorWarehouseMaterialHandle> datas = this.sqlSession.selectList(this.sqlId("advPaging"), criteria);
            return new Paging(total, datas);
        }
    }


    public List<DoctorWarehouseMaterialHandle> advList(Map<?, ?> criteria) {

        return this.sqlSession.selectList(this.sqlId("advList"), criteria);
    }

}
