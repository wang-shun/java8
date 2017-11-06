package io.terminus.doctor.basic.dao;

import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;

import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-31 13:22:27
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseMaterialHandleDao extends MyBatisDao<DoctorWarehouseMaterialHandle> {

    /**
     * 支持bigType参数，对多个type的or查询
     * 支持startDate和endDate，对handle_date的范围查询
     *
     * @param offset
     * @param limit
     * @param criteria
     * @return
     */
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


    /**
     * 支持bigType参数，对多个type的or查询
     * 支持startDate和endDate，对handle_date的范围查询
     *
     * @param criteria
     * @return
     */
    public List<DoctorWarehouseMaterialHandle> advList(Map<?, ?> criteria) {

        return this.sqlSession.selectList(this.sqlId("advList"), criteria);
    }

    public List<DoctorWarehouseMaterialHandle> findByStockHandle(Long stockHandleId) {
        return this.list(DoctorWarehouseMaterialHandle.builder()
                .deleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue())
                .stockHandleId(stockHandleId)
                .build());
    }

}
