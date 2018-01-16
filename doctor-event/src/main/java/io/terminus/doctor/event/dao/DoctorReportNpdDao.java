package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;

import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.model.DoctorReportNpd;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

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

    @Deprecated
    public List<DoctorReportNpd> findByFarm(Long farmId, ReportTime reportTime) {
        return this.sqlSession.selectList(this.sqlId("findByFarm"), Collections.singletonMap("farmId", farmId));
    }

    @Deprecated
    public List<DoctorReportNpd> findByOrg(Long orgId, ReportTime reportTime) {
        return this.sqlSession.selectList(this.sqlId("findByOrg"), Collections.singletonMap("orgId", orgId));
    }

    public List<DoctorReportNpd> count(DoctorDimensionCriteria dimensionCriteria) {

        Map<String, Object> params = new HashMap<>();
        params.put("orgType", dimensionCriteria.getOrzType());
        params.put("dateType", dimensionCriteria.getDateType());

        return this.sqlSession.selectList(this.sqlId("report"), params);
    }

    public void delete() {
        this.sqlSession.delete(this.sqlId("deleteAll"));
    }


}
