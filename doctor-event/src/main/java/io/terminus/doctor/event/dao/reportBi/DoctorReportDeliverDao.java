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
    public List<Map<String,Object>> deliveryBarn(BigInteger id,BigInteger pigId , Date eventAt){
        Map<String,Object> map = new HashMap<>();
        map.put("id",id);
        map.put("pigId",pigId);
        map.put("eventAt",eventAt);
        return getSqlSession().selectList(sqlId("deliveryBarn"), map);
    }
}
