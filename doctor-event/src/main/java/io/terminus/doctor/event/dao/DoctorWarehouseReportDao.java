package io.terminus.doctor.event.dao;

import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 虚拟的DAO，没有表
 * 为了统计仓库物料消耗
 * <p>
 * Created by sunbo@terminus.io on 2018/1/15.
 */
@Repository
public class DoctorWarehouseReportDao {

    @Autowired
    private SqlSessionTemplate sqlSession;


    private final String namespace = "DoctorWarehouseReportDao";


    public Map<String, Object> count(List<Long> farmIds, Date start, Date end) {

        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmIds);
        params.put("startAt", start);
        params.put("endAt", end);


        Map<String, Object> farrowSow = sqlSession.selectOne(namespace + ".countFarrowSow", params);
        Map<String, Object> farrow = sqlSession.selectOne(namespace + ".countFarrow", params);
        Map<String, Object> other = sqlSession.selectOne(namespace + ".count", params);
        Map<String, Object> result = new HashMap<>();
        result.putAll(farrowSow);
        result.putAll(farrow);
        result.putAll(other);
        return other;
    }


    /**
     * 获取事件段内发生领用的猪场ID
     *
     * @param start
     * @param end
     * @return
     */
    public List<Long> findApplyFarm(Date start, Date end) {
        Map<String, Object> params = new HashMap<>();
        params.put("startAt", start);
        params.put("endAt", end);
        return sqlSession.selectList(namespace + ".findFarmId", params);
    }

    /**
     * 获取不同纬度的饲料消耗
     * @param dimensionCriteria
     * @return
     */
    public Double materialApply(DoctorDimensionCriteria dimensionCriteria) {
        return sqlSession.selectOne(namespace + "materialApply", dimensionCriteria);
    }

}
