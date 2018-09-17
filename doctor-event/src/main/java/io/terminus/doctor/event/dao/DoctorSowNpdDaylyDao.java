package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorSowNpdDayly;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * @program: doctor
 * @description: ${description}
 * @author: YuSQ
 * @create: 2018-09-04 16:58
 **/
@Repository
public class DoctorSowNpdDaylyDao extends MyBatisDao<DoctorSowNpdDayly> {
    public int deleteNPD(Map<String, Object> params) {
        return sqlSession.delete(sqlId("deleteNPD"),params);
    }
}
