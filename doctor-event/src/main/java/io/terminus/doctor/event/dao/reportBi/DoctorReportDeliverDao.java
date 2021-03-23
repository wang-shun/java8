package io.terminus.doctor.event.dao.reportBi;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportDeliver;
import org.springframework.stereotype.Repository;


import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 14:33:07
 * Created by [ your name ]
 */
@Repository
public class DoctorReportDeliverDao extends MyBatisDao<DoctorReportDeliver> {
    public void deleteAll(){
        getSqlSession().delete(sqlId("deleteAll"));
    }
    public DoctorReportDeliver findByDimension(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("findByDimension"), dimensionCriteria);
    }

    public List<DoctorReportDeliver> findBy(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectList(sqlId("findBy"), dimensionCriteria);
    }

    public DoctorReportDeliver sumBy(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("sumBy"), dimensionCriteria);
    }

    public List<Map<String,Object>> getMating(Long farmId , Date beginDate, Date endDate,String pigCode,String operatorName){
        Map<String,Object> map = new HashMap<>();
        map.put("farmId",farmId);
        map.put("beginDate",beginDate);
        map.put("endDate",endDate);
        map.put("pigCode",pigCode);
        map.put("operatorName",operatorName);
        return getSqlSession().selectList(sqlId("deliveryReport"), map);
    }
    public List<Map<String,Object>> deliveryBarn(BigInteger id,BigInteger pigId){
        Map<String,Object> map = new HashMap<>();
        map.put("id",id);
        map.put("pigId",pigId);
        return getSqlSession().selectList(sqlId("deliveryBarn"), map);
    }
    public Map<String,Object> notdelivery(BigInteger id,BigInteger pigId , int parity ,BigInteger id1 ){
        Map<String,Object> map = new HashMap<>();
        map.put("parity",parity);
        map.put("pigId",pigId);
        map.put("id1",id1);
        map.put("id",id);
        return getSqlSession().selectOne(sqlId("notdelivery"), map);
    }
    public Map<String,Object> leave(BigInteger id,BigInteger pigId , int parity , BigInteger id1){
        Map<String,Object> map = new HashMap<>();
        map.put("parity",parity);
        map.put("pigId",pigId);
        map.put("id1",id1);
        map.put("id",id);
        return getSqlSession().selectOne(sqlId("leave"), map);
    }
    public Map<String,Object> idsameparity(BigInteger id ,BigInteger pigId , int parity){
        Map<String,Object> map = new HashMap<>();
        map.put("id",id);
        map.put("parity",parity);
        map.put("pigId",pigId);
        return getSqlSession().selectOne(sqlId("idsameparity"), map);
    }
    public Map<String,Object> getMatingCount(BigInteger pigId , Date event_at){
        Map<String,Object> map = new HashMap<>();
        map.put("event_at",event_at);
        map.put("pigId",pigId);
        return getSqlSession().selectOne(sqlId("getMatingCount"), map);
    }

    public Map<String,Object> getFarmId(BigInteger pigId){
        Map<String,Object> map = new HashMap<>();
        map.put("pigId",pigId);
        return getSqlSession().selectOne(sqlId("getFarmId"),map);
    }
}
