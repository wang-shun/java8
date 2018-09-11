package io.terminus.doctor.event.dao.reportBi;

import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportMating;
import io.terminus.doctor.event.model.DoctorReportSow;
import org.springframework.stereotype.Repository;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 16:18:03
 * Created by [ your name ]
 */
@Repository
public class DoctorReportMatingDao extends MyBatisDao<DoctorReportMating> {
    public void deleteAll(){
        getSqlSession().delete(sqlId("deleteAll"));
    }
    public DoctorReportMating findByDimension(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("findByDimension"), dimensionCriteria);
    }

    public List<DoctorReportMating> findBy(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectList(sqlId("findBy"), dimensionCriteria);
    }

    public DoctorReportMating sumBy(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("sumBy"), dimensionCriteria);
    }

    /*
    得到七日断奶配种数（孔景军）
     */
    public Integer getWeanMateCount(Long farmId , Date startAt, Date endAt, Integer a) {
        Map map = new HashMap();
        map.put("farmId",farmId);
        map.put("startAt",startAt);
        map.put("endAt",endAt);
        map.put("a",a);
        Integer b = getSqlSession().selectOne(sqlId("getWeanMateCount"), map);
        return b;
    }
}
