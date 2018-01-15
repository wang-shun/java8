package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;

import io.terminus.doctor.event.model.DoctorReportNpd;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-15 09:34:12
 * Created by [ your name ]
 */
@Repository
public class DoctorReportNpdDao extends MyBatisDao<DoctorReportNpd> {


    public Optional<DoctorReportNpd> findByFarmAndSumAt(Long farmId, Date month) {

        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmId);
        params.put("sumAt", month);

        return Optional.ofNullable(this.sqlSession.selectOne(this.sqlId("findByFarmAndSumAt"), params));
    }

}
