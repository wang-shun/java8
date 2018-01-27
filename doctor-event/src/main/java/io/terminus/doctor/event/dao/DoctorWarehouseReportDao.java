package io.terminus.doctor.event.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.google.common.collect.Lists;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.reportBi.helper.DateHelper;
import io.terminus.doctor.event.reportBi.model.WarehouseReportTempResult;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

import javax.management.DescriptorKey;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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


    public List<Date> getChangedDate(Date date) {
        return this.sqlSession.selectList(namespace + ".findChangedDate", Collections.singletonMap("date", date));
    }

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

    public Map<String, Date> getMaxAndMinDate() {
        return this.sqlSession.selectOne(namespace + ".findMaxAndMinDate");
    }


    public Map<Long, List<WarehouseReportTempResult>> count(Integer dateType, Integer orgType) {

        Map<String, Object> params = new HashMap<>();
        params.put("dateType", dateType);
        params.put("orgType", orgType);

        List<WarehouseReportTempResult> other = this.sqlSession.selectList(namespace + ".countOrg", params);
        //产房仔猪,其中的pig_type都为7，需要设置一个另外的值，为了与产房母猪区分，因为枚举中没有区分，就设一个魔法值
        List<WarehouseReportTempResult> farrow = this.sqlSession.selectList(namespace + ".countFarrowOrg", params);
        farrow.forEach(f -> f.setPigType(22));
        //产房母猪,其中的pig_type也都为7
        List<WarehouseReportTempResult> farrowSow = this.sqlSession.selectList(namespace + ".countFarrowSowOrg", params);

        List<WarehouseReportTempResult> all = new ArrayList<>();
        all.addAll(other);
        all.addAll(farrow);
        all.addAll(farrowSow);
        return all.stream().collect(Collectors.groupingBy(WarehouseReportTempResult::getOrgOrFarmId));
    }


//    @Deprecated
//    public List<WarehouseReport> count(Integer dateType) {
//
//        List<WarehouseReportTempResult> otherCount = this.sqlSession.selectList(namespace + ".countByOrg", Collections.singletonMap("type", dateType));
//        List<WarehouseReportTempResult> farrowCount = this.sqlSession.selectList(namespace + ".countFarrowByOrg", Collections.singletonMap("type", dateType));
//        List<WarehouseReportTempResult> farrowSowCount = this.sqlSession.selectList(namespace + ".countFarrowSowByOrg", Collections.singletonMap("type", dateType));
//        farrowCount.forEach(f -> f.setPigType(22));
//
//        List<WarehouseReportTempResult> allCount = new ArrayList<>();
//        allCount.addAll(otherCount);
//        allCount.addAll(farrowCount);
//        allCount.addAll(farrowCount);
//
//        Map<Long, List<WarehouseReportTempResult>> tempResultMap = allCount.stream().collect(Collectors.groupingBy(WarehouseReportTempResult::getOrgId));
//
//
//        List<WarehouseReport> result = new ArrayList<>();
//        for (Long orgId : tempResultMap.keySet()) {
//
//            if (tempResultMap.get(orgId).get(0).getDate() == null) {
//                WarehouseReport report = new WarehouseReport();
//                report.setOrgId(orgId);
//                report.setOrgName(tempResultMap.get(orgId).get(0).getOrgName());
//                result.add(report);
//            } else {
//
//                Map<Date, List<WarehouseReportTempResult>> dateResult = new HashMap<>();
//                for (WarehouseReportTempResult tempResult : tempResultMap.get(orgId)) {
//                    if (dateResult.containsKey(tempResult.getDate()))
//                        dateResult.get(tempResult.getDate()).add(tempResult);
//                    else {
//                        List<WarehouseReportTempResult> d = new ArrayList<>();
//                        d.add(tempResult);
//                        dateResult.put(tempResult.getDate(), d);
//                    }
//                }
//
//                dateResult.forEach((d, r) -> {
//                    WarehouseReport report = new WarehouseReport();
//                    report.setOrgId(orgId);
//                    report.setOrgName(r.get(0).getOrgName());
//                    report.setSumAt(DateHelper.withDateStartDay(r.get(0).getDate(), DateDimension.from(dateType)));
//
//                    r.forEach(rr -> {
//                        /*产房仔猪--并没有这个猪舍类型，只能自定义一个不重复的魔法值*/
//                        if (rr.getPigType().equals(22)) {
//                            switch (rr.getType()) {
//                                case 1:
//                                    report.setFarrowFeedCount(rr.getQuantity().longValue());
//                                    report.setFarrowFeedAmount(rr.getAmount().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));
//                                    break;
//                                case 2:
//                                    report.setFarrowMaterialCount(rr.getQuantity().longValue());
//                                    report.setFarrowMaterialAmount(rr.getAmount().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));
//                                    break;
//                                case 3:
//                                    report.setFarrowVaccineAmount(rr.getAmount().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));
//                                    break;
//                                case 4:
//                                    report.setFarrowDrugAmount(rr.getAmount().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));
//                                    break;
//                                case 5:
//                                    report.setFarrowConsumerAmount(rr.getAmount().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));
//                            }
//
//                        }
//                    });
//
//                    result.add(report);
//                });
//            }
//        }
//
//        return result;
//    }

    @Deprecated
    private void convert(List<List<Map<String, Object>>> orgCounts,
                         Map<Long/*公司*/, Map<Short/*猪舍类型*/, Map<Short/*物料类型*/, AmountAndQuantityDto>>> middleResult,
                         Map<Long/*公司*/, Date> orgDate) {

//        Map<Long/*公司*/, Map<Short/*猪舍类型*/, Map<Short/*物料类型*/, AmountAndQuantityDto>>> middleResult = new HashMap<>();

        for (List<Map<String, Object>> orgCount : orgCounts) {
            for (Map<String, Object> orgCountResult : orgCount) {
                Long orgId = (Long) orgCountResult.get("orgId".toUpperCase());
                orgDate.put(orgId, (Date) orgCountResult.get("applyDate".toUpperCase()));
                if (middleResult.containsKey(orgId)) {

                    Map<Short, Map<Short, AmountAndQuantityDto>> m = middleResult.get(orgId);
                    if (m.containsKey(orgCountResult.get("pigType".toUpperCase()))) {

                        Map<Short, AmountAndQuantityDto> mm = m.get(orgCountResult.get("pigType".toUpperCase()));

                        mm.put((Short) orgCountResult.get("type".toUpperCase()), new AmountAndQuantityDto(new BigDecimal(orgCountResult.get("amount".toUpperCase()).toString()), ((BigDecimal) orgCountResult.get("quantity".toUpperCase())).longValue()));

                    } else {
                        Map<Short, AmountAndQuantityDto> mm = new HashMap<>();
                        mm.put((Short) orgCountResult.get("type".toUpperCase()), new AmountAndQuantityDto(new BigDecimal(orgCountResult.get("amount".toUpperCase()).toString()), ((BigDecimal) orgCountResult.get("quantity".toUpperCase())).longValue()));
                        m.put((Short) orgCountResult.get("pigType".toUpperCase()), mm);
                    }
                } else {
                    Map<Short, Map<Short, AmountAndQuantityDto>> m = new HashMap<>();
                    Map<Short, AmountAndQuantityDto> mm = new HashMap<>();
                    mm.put((Short) orgCountResult.get("type".toUpperCase()), new AmountAndQuantityDto(new BigDecimal(orgCountResult.get("amount".toUpperCase()).toString()), ((BigDecimal) orgCountResult.get("quantity".toUpperCase())).longValue()));
                    m.put((Short) orgCountResult.get("pigType".toUpperCase()), mm);
                    middleResult.put(orgId, m);
                }
            }
        }
//        return Collections.unmodifiableMap(middleResult);
    }

    @Deprecated
    private AmountAndQuantityDto getValue(Map<Short/*猪舍类型*/, Map<Short, AmountAndQuantityDto>> r, Integer pigType, Integer type) {
        if (null == r)
            return new AmountAndQuantityDto(new BigDecimal(0), 0L);
        if (r.containsKey(pigType.shortValue())) {
            Map<Short, AmountAndQuantityDto> m = r.get(pigType.shortValue());
            if (m.containsKey(type.shortValue())) {
                return m.get(type.shortValue());
            } else {
                return new AmountAndQuantityDto(new BigDecimal(0), 0L);
            }
        } else {
            return new AmountAndQuantityDto(new BigDecimal(0), 0L);
        }
    }

    public void getValue(List<WarehouseReportTempResult> tempResults, String date, Integer pigType, Integer type, WarehouseReport report) {
        Map<String, List<WarehouseReportTempResult>> dateResult = new HashMap<>();
        for (WarehouseReportTempResult tempResult : tempResults) {//获取同一个周期的
            if (tempResult.getDate().equals(date)) {
                if (dateResult.containsKey(date))
                    dateResult.get(date).add(tempResult);
                else {
                    List<WarehouseReportTempResult> t = new ArrayList<>();
                    t.add(tempResult);
                    dateResult.put(date, t);
                }
            }
        }


    }

    private AmountAndQuantityDto merge(AmountAndQuantityDto a1, AmountAndQuantityDto a2) {
        return new AmountAndQuantityDto(a1.getAmount().add(a2.getAmount()), a1.getQuantity() + a2.getQuantity());
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
        private String orgName;
        private Date sumAt;
        private Long farmId;

        private Long houbeiFeedCount = 0L;
        private BigDecimal houbeiFeedAmount = new BigDecimal(0);
        private BigDecimal houbeiMaterialAmount = new BigDecimal(0);
        private Long houbeiMaterialCount = 0L;
        private BigDecimal houbeiConsumerAmount = new BigDecimal(0);
        private BigDecimal houbeiDrugAmount = new BigDecimal(0);
        private BigDecimal houbeiVaccineAmount = new BigDecimal(0);

        private Long peiHuaiFeedCount = 0L;
        private BigDecimal peiHuaiFeedAmount = new BigDecimal(0);
        private BigDecimal peiHuaiMaterialAmount = new BigDecimal(0);
        private Long peiHuaiMaterialCount = 0L;
        private BigDecimal peiHuaiConsumerAmount = new BigDecimal(0);
        private BigDecimal peiHuaiDrugAmount = new BigDecimal(0);
        private BigDecimal peiHuaiVaccineAmount = new BigDecimal(0);

        private Long farrowSowFeedCount = 0L;
        private BigDecimal farrowSowFeedAmount = new BigDecimal(0);
        private BigDecimal farrowSowMaterialAmount = new BigDecimal(0);
        private Long farrowSowMaterialCount = 0L;
        private BigDecimal farrowSowConsumerAmount = new BigDecimal(0);
        private BigDecimal farrowSowDrugAmount = new BigDecimal(0);
        private BigDecimal farrowSowVaccineAmount = new BigDecimal(0);

        private Long farrowFeedCount = 0L;
        private BigDecimal farrowFeedAmount = new BigDecimal(0);
        private BigDecimal farrowMaterialAmount = new BigDecimal(0);
        private Long farrowMaterialCount = 0L;
        private BigDecimal farrowConsumerAmount = new BigDecimal(0);
        private BigDecimal farrowDrugAmount = new BigDecimal(0);
        private BigDecimal farrowVaccineAmount = new BigDecimal(0);

        private Long nurseryFeedCount = 0L;
        private BigDecimal nurseryFeedAmount = new BigDecimal(0);
        private BigDecimal nurseryMaterialAmount = new BigDecimal(0);
        private Long nurseryMaterialCount = 0L;
        private BigDecimal nurseryConsumerAmount = new BigDecimal(0);
        private BigDecimal nurseryDrugAmount = new BigDecimal(0);
        private BigDecimal nurseryVaccineAmount = new BigDecimal(0);

        private Long fattenFeedCount = 0L;
        private BigDecimal fattenFeedAmount = new BigDecimal(0);
        private BigDecimal fattenMaterialAmount = new BigDecimal(0);
        private Long fattenMaterialCount = 0L;
        private BigDecimal fattenConsumerAmount = new BigDecimal(0);
        private BigDecimal fattenDrugAmount = new BigDecimal(0);
        private BigDecimal fattenVaccineAmount = new BigDecimal(0);

        private Long boarFeedCount = 0L;
        private BigDecimal boarFeedAmount = new BigDecimal(0);
        private BigDecimal boarMaterialAmount = new BigDecimal(0);
        private Long boarMaterialCount = 0L;
        private BigDecimal boarConsumerAmount = new BigDecimal(0);
        private BigDecimal boarDrugAmount = new BigDecimal(0);
        private BigDecimal boarVaccineAmount = new BigDecimal(0);
    }

    @Data
    public static class AmountAndQuantityDto {

        public AmountAndQuantityDto() {
            this.amount = new BigDecimal(0);
            this.quantity = 0L;
        }

        public AmountAndQuantityDto(BigDecimal amount, Long quantity) {
            this.amount = amount;
            this.quantity = quantity;
        }

        private BigDecimal amount;

        private Long quantity;
    }
}
