package io.terminus.doctor.event.dao;

import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import lombok.Data;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

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
        return result;
    }


    public List<WarehouseReport> count(Date start, Date end) {


        Map<Long/*公司*/, Map<Integer/*猪舍类型*/, Map<Integer, Map<String, Object>>>> middleResult = new HashMap<>();

        List<Map<String, Object>> orgCounts = this.sqlSession.selectList(namespace + ".countByOrg");
        for (Map<String, Object> orgCount : orgCounts) {
            Long orgId = (Long) orgCount.get("org_id");
            if (middleResult.containsKey(orgId)) {

                Map<Integer, Map<Integer, Map<String, Object>>> m = middleResult.get(orgId);
                if (m.containsKey(orgCount.get("pig_type"))) {

                } else {
                    Map<Integer, Map<String, Object>> mm = new HashMap<>();
                    Map<String, Object> c = new HashMap<>();
                    c.put("quantity", orgCount.get("quantity"));
                    c.put("amount", orgCount.get("amount"));
                    mm.put((Integer) orgCount.get("type"), c);
                    m.put((Integer) orgCount.get("pig_type"), mm);
                }
            } else {
                Map<Integer, Map<Integer, Map<String, Object>>> m = new HashMap<>();
                Map<Integer, Map<String, Object>> mm = new HashMap<>();
                Map<String, Object> c = new HashMap<>();
                c.put("quantity", orgCount.get("quantity"));
                c.put("amount", orgCount.get("amount"));
                mm.put((Integer) orgCount.get("type"), c);
                m.put((Integer) orgCount.get("pig_type"), mm);
                middleResult.put(orgId, m);
            }
        }

        List<WarehouseReport> result = new ArrayList<>();
        for (Long orgId : middleResult.keySet()) {
            WarehouseReport report = new WarehouseReport();
            report.setOrgId(orgId);

//            report.setHoubeiConsumerAmount();

            result.add(report);
        }

        return result;
    }

    private void getValue(List<Map<String, Object>> r, Integer pigType, Integer type) {
        for (Map<String, Object> m : r) {
            if (m.get("pig_type").toString().equals(pigType.toString())) {

            }
        }
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
     *
     * @param dimensionCriteria
     * @return
     */
    public Double materialApply(DoctorDimensionCriteria dimensionCriteria) {
        return sqlSession.selectOne(namespace + ".materialApply", dimensionCriteria);
    }


    @Data
    public class WarehouseReport {
        private Long orgId;

        private Long farmId;

        private Long houbeiFeedCount;
        private BigDecimal houbeiFeedAmount;
        private BigDecimal houbeiMaterialAmount;
        private Long houbeiMaterialCount;
        private BigDecimal houbeiConsumerAmount;
        private BigDecimal houbeiDrugAmount;
        private BigDecimal houbeiVaccineAmount;

        private Long peiHuaiFeedCount;
        private BigDecimal peiHuaiFeedAmount;
        private BigDecimal peiHuaiMaterialAmount;
        private Long peiHuaiMaterialCount;
        private BigDecimal peiHuaiConsumerAmount;
        private BigDecimal peiHuaiDrugAmount;
        private BigDecimal peiHuaiVaccineAmount;

        private Long farrowSowFeedCount;
        private BigDecimal farrowSowFeedAmount;
        private BigDecimal farrowSowMaterialAmount;
        private Long farrowSowMaterialCount;
        private BigDecimal farrowSowConsumerAmount;
        private BigDecimal farrowSowDrugAmount;
        private BigDecimal farrowSowVaccineAmount;

        private Long farrowFeedCount;
        private BigDecimal farrowFeedAmount;
        private BigDecimal farrowMaterialAmount;
        private Long farrowMaterialCount;
        private BigDecimal farrowConsumerAmount;
        private BigDecimal farrowDrugAmount;
        private BigDecimal farrowVaccineAmount;

        private Long nurseryFeedCount;
        private BigDecimal nurseryFeedAmount;
        private BigDecimal nurseryMaterialAmount;
        private Long nurseryMaterialCount;
        private BigDecimal nurseryConsumerAmount;
        private BigDecimal nurseryDrugAmount;
        private BigDecimal nurseryVaccineAmount;

        private Long fattenFeedCount;
        private BigDecimal fattenFeedAmount;
        private BigDecimal fattenMaterialAmount;
        private Long fattenMaterialCount;
        private BigDecimal fattenConsumerAmount;
        private BigDecimal fattenDrugAmount;
        private BigDecimal fattenVaccineAmount;

        private Long boarFeedCount;
        private BigDecimal boarFeedAmount;
        private BigDecimal boarMaterialAmount;
        private Long boarMaterialCount;
        private BigDecimal boarConsumerAmount;
        private BigDecimal boarDrugAmount;
        private BigDecimal boarVaccineAmount;
    }
}
