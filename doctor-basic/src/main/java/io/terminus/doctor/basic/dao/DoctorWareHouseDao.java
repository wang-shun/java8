package io.terminus.doctor.basic.dao;

import com.alibaba.dubbo.container.page.Page;
import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorWareHouseDao extends MyBatisDao<DoctorWareHouse>{

    public List<DoctorWareHouse> findByFarmId(Long farmId){
        return this.getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }
    public List<DoctorWareHouse> getWarehouseByType(DoctorWareHouse criteria,Integer pageNum, Integer pageSize){
        Map<String, Object> params = new HashMap<>();
        params.put("criteria", criteria);
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        return this.getSqlSession().selectList(sqlId("getWarehouseByType"),params);
    }

    /**
     * 按照仓库类型进行tab分页筛选，仓库按照创建时间进行排列
     * @param farmId
     * @param type
     * @return
     */
    public List<Map<String, Object>> listTypeMap(Long farmId, Integer type,Date date) {
        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmId);
        params.put("type", type);
        params.put("settlementDate",date);
        return this.getSqlSession().selectList(sqlId("listTypeMap"),params);
    }

    /**
     * 按照仓库类型进行tab分页筛选，仓库按照创建时间进行排列
     * @return
     */
    public Paging<Map<String, Object>> listDetailTypeMap(
            Integer offerSet,
            Integer limit,
            Map<String, Object> params) {
        Long total = this.sqlSession.selectOne(this.sqlId("listDetailTypeCount"), params);
        if (total.longValue() <= 0L) {
            return new Paging(0L, Collections.emptyList());
        } else {
            params.put("offset", offerSet);
            params.put("limit", limit);
            List<Map<String, Object>> datas = this.sqlSession.selectList(this.sqlId("listDetailTypeMap"), params);
            return new Paging(total, datas);
        }
    }

    public List<Map<String,Object>> findMapByFarmId(Long farmId) {
        return this.sqlSession.selectList(this.sqlId("findMapByFarmId"), farmId);
    }
}
