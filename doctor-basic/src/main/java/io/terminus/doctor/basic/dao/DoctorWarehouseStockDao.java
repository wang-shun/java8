package io.terminus.doctor.basic.dao;

import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.common.mysql.dao.MyBatisDao;

import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-13 17:14:31
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseStockDao extends MyBatisDao<DoctorWarehouseStock> {

    public List<DoctorWarehouseStock> listMergeVendor(DoctorWarehouseStock criteria) {
        return this.sqlSession.selectList(this.sqlId("listMergeVendor"), criteria);
    }


    public Paging<DoctorWarehouseStock> advPaging(Integer offset, Integer limit, Map<String, Object> criteria) {
        if (criteria == null) {
            criteria = Maps.newHashMap();
        }

        Long total = (Long) this.sqlSession.selectOne(this.sqlId("advCount"), criteria);
        if (total.longValue() <= 0L) {
            return new Paging(0L, Collections.emptyList());
        } else {
            ((Map) criteria).put("offset", offset);
            ((Map) criteria).put("limit", limit);
            List<DoctorWarehouseStock> datas = this.sqlSession.selectList(this.sqlId("advPaging"), criteria);
            return new Paging(total, datas);
        }
    }


    public List<DoctorWarehouseStock> advList(Map<String, Object> criteria) {
        return this.sqlSession.selectList(this.sqlId("advList"), criteria);
    }


    public Optional<DoctorWarehouseStock> findBySkuIdAndWarehouseId(Long skuId, Long warehouseId) {
        List<DoctorWarehouseStock> stocks = this.list(DoctorWarehouseStock.builder()
                .warehouseId(warehouseId)
                .skuId(skuId)
                .build());
        if (stocks.isEmpty())
            return Optional.empty();

        return Optional.ofNullable(stocks.get(0));
    }

    public List<Long> findSkuIds(Long warehouseId) {
        return this.sqlSession.selectList(this.sqlId("findSkuIds"), warehouseId);
    }


    public Long advCount(Map<String, Object> criteria) {
        return (Long) this.sqlSession.selectOne(this.sqlId("advCount"), criteria);
    }
}
