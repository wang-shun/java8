package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;

import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportNpd;
import org.springframework.stereotype.Repository;

import java.util.*;

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

    public DoctorReportNpd findByOrgAndSumAt(List<Long> farmIds, Date month) {
        Map<String, Object> params = new HashMap<>();
        params.put("farmIds", farmIds);
        params.put("sumAt", month);

        return this.sqlSession.selectOne(this.sqlId("findByOrgAndSumAt"), params);
    }


    public List<DoctorReportNpd> sumForDimension(DoctorDimensionCriteria dimensionCriteria) {


        return this.sqlSession.selectList(this.sqlId(""));
    }

    public void delete() {
        this.sqlSession.delete(this.sqlId("deleteAll"));
    }


}
