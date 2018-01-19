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


    public List<WarehouseReport> count(Integer dateType) {

        List<WarehouseReportTempResult> otherCount = this.sqlSession.selectList(namespace + ".countByOrg", Collections.singletonMap("type", dateType));
        List<WarehouseReportTempResult> farrowCount = this.sqlSession.selectList(namespace + ".countFarrowByOrg", Collections.singletonMap("type", dateType));
        List<WarehouseReportTempResult> farrowSowCount = this.sqlSession.selectList(namespace + ".countFarrowSowByOrg", Collections.singletonMap("type", dateType));
        farrowCount.forEach(f -> f.setPigType(22));

        List<WarehouseReportTempResult> allCount = new ArrayList<>();
        allCount.addAll(otherCount);
        allCount.addAll(farrowCount);
        allCount.addAll(farrowCount);

        Map<Long, List<WarehouseReportTempResult>> tempResultMap = allCount.stream().collect(Collectors.groupingBy(WarehouseReportTempResult::getOrgId));

//        List<Map<String, Object>> orgFarrowCounts = this.sqlSession.selectList(namespace + ".countFarrowByOrg", Collections.singletonMap("type", dateType));

//        for (Map<String, Object> farrow : orgFarrowCounts) {
//            farrow.put("pigType".toUpperCase(), 22);//修改产房仔猪类型，不然会与产房母猪冲突，无法区分
//        }

//        List<Map<String, Object>> orgFarrowSowCounts = this.sqlSession.selectList(namespace + ".countFarrowSowByOrg", Collections.singletonMap("type", dateType));

//        Map<Long/*公司*/, Map<Short/*猪舍类型*/, Map<Short/*物料类型*/, AmountAndQuantityDto>>> middleResult = new HashMap<>();
//        Map<Long/*公司*/, Date> orgDate = new HashMap<>();


//        convert(Lists.newArrayList(orgOtherCounts, orgFarrowCounts, orgFarrowSowCounts), middleResult, orgDate);

        List<WarehouseReport> result = new ArrayList<>();
        for (Long orgId : tempResultMap.keySet()) {

            if (tempResultMap.get(orgId).get(0).getDate() == null) {
                WarehouseReport report = new WarehouseReport();
                report.setOrgId(orgId);
                report.setOrgName(tempResultMap.get(orgId).get(0).getOrgName());
                result.add(report);
            } else {

                Map<Date, List<WarehouseReportTempResult>> dateResult = new HashMap<>();
                for (WarehouseReportTempResult tempResult : tempResultMap.get(orgId)) {
                    if (dateResult.containsKey(tempResult.getDate()))
                        dateResult.get(tempResult.getDate()).add(tempResult);
                    else {
                        List<WarehouseReportTempResult> d = new ArrayList<>();
                        d.add(tempResult);
                        dateResult.put(tempResult.getDate(), d);
                    }
                }

                dateResult.forEach((d, r) -> {
                    WarehouseReport report = new WarehouseReport();
                    report.setOrgId(orgId);
                    report.setOrgName(r.get(0).getOrgName());
                    report.setSumAt(DateHelper.withDateStartDay(r.get(0).getDate(), DateDimension.from(dateType)));

                    r.forEach(rr -> {
                        /*产房仔猪--并没有这个猪舍类型，只能自定义一个不重复的魔法值*/
                        if (rr.getPigType().equals(22)) {
                            switch (rr.getType()) {
                                case 1:
                                    report.setFarrowFeedCount(rr.getQuantity().longValue());
                                    report.setFarrowFeedAmount(rr.getAmount().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));
                                    break;
                                case 2:
                                    report.setFarrowMaterialCount(rr.getQuantity().longValue());
                                    report.setFarrowMaterialAmount(rr.getAmount().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));
                                    break;
                                case 3:
                                    report.setFarrowVaccineAmount(rr.getAmount().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));
                                    break;
                                case 4:
                                    report.setFarrowDrugAmount(rr.getAmount().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));
                                    break;
                                case 5:
                                    report.setFarrowConsumerAmount(rr.getAmount().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));
                            }

                        }
                    });

                    result.add(report);
                });
            }
//
//            WarehouseReport report = new WarehouseReport();
//            report.setOrgId(orgId);
////            report.setApplyDate(orgDate.get(orgId));
//
//            AmountAndQuantityDto amountAndQuantityDto;
//
//            Map<Short, Map<Short, AmountAndQuantityDto>> otherApply = middleResult.get(orgId);
//
//
//            amountAndQuantityDto = getValue(otherApply, 22, WareHouseType.FEED.getKey());
//            report.setFarrowFeedCount(amountAndQuantityDto.getQuantity());
//            report.setFarrowFeedAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, 22, WareHouseType.MATERIAL.getKey());
//            report.setFarrowMaterialAmount(amountAndQuantityDto.getAmount());
//            report.setFarrowMaterialCount(amountAndQuantityDto.getQuantity());
//            amountAndQuantityDto = getValue(otherApply, 22, WareHouseType.VACCINATION.getKey());
//            report.setFarrowVaccineAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, 22, WareHouseType.MEDICINE.getKey());
//            report.setFarrowDrugAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, 22, WareHouseType.CONSUME.getKey());
//            report.setFarrowConsumerAmount(amountAndQuantityDto.getAmount());
//
//            /*产房母猪*/
//            amountAndQuantityDto = getValue(otherApply, PigType.DELIVER_SOW.getValue(), WareHouseType.FEED.getKey());
//            report.setFarrowFeedCount(amountAndQuantityDto.getQuantity());
//            report.setFarrowFeedAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, PigType.DELIVER_SOW.getValue(), WareHouseType.MATERIAL.getKey());
//            report.setFarrowMaterialAmount(amountAndQuantityDto.getAmount());
//            report.setFarrowMaterialCount(amountAndQuantityDto.getQuantity());
//            amountAndQuantityDto = getValue(otherApply, PigType.DELIVER_SOW.getValue(), WareHouseType.VACCINATION.getKey());
//            report.setFarrowVaccineAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, PigType.DELIVER_SOW.getValue(), WareHouseType.MEDICINE.getKey());
//            report.setFarrowDrugAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, PigType.DELIVER_SOW.getValue(), WareHouseType.CONSUME.getKey());
//            report.setFarrowConsumerAmount(amountAndQuantityDto.getAmount());
//
//            /*后备*/
//            amountAndQuantityDto = getValue(otherApply, PigType.RESERVE.getValue(), WareHouseType.FEED.getKey());
//            report.setHoubeiFeedAmount(amountAndQuantityDto.getAmount());
//            report.setHoubeiFeedCount(amountAndQuantityDto.getQuantity());
//            amountAndQuantityDto = getValue(otherApply, PigType.RESERVE.getValue(), WareHouseType.MATERIAL.getKey());
//            report.setHoubeiMaterialAmount(amountAndQuantityDto.getAmount());
//            report.setHoubeiMaterialCount(amountAndQuantityDto.getQuantity());
//            amountAndQuantityDto = getValue(otherApply, PigType.RESERVE.getValue(), WareHouseType.VACCINATION.getKey());
//            report.setHoubeiVaccineAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, PigType.RESERVE.getValue(), WareHouseType.MEDICINE.getKey());
//            report.setHoubeiDrugAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, PigType.RESERVE.getValue(), WareHouseType.CONSUME.getKey());
//            report.setHoubeiConsumerAmount(amountAndQuantityDto.getAmount());
//
//            /*配怀*/
//            amountAndQuantityDto = merge(getValue(otherApply, PigType.MATE_SOW.getValue(), WareHouseType.FEED.getKey()),
//                    getValue(otherApply, PigType.PREG_SOW.getValue(), WareHouseType.FEED.getKey()));
//            report.setPeiHuaiFeedCount(amountAndQuantityDto.getQuantity());
//            report.setPeiHuaiFeedAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = merge(getValue(otherApply, PigType.MATE_SOW.getValue(), WareHouseType.MATERIAL.getKey()),
//                    getValue(otherApply, PigType.PREG_SOW.getValue(), WareHouseType.MATERIAL.getKey()));
//            report.setPeiHuaiMaterialCount(amountAndQuantityDto.getQuantity());
//            report.setPeiHuaiMaterialAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = merge(getValue(otherApply, PigType.MATE_SOW.getValue(), WareHouseType.VACCINATION.getKey()),
//                    getValue(otherApply, PigType.PREG_SOW.getValue(), WareHouseType.VACCINATION.getKey()));
//            report.setPeiHuaiVaccineAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = merge(getValue(otherApply, PigType.MATE_SOW.getValue(), WareHouseType.MEDICINE.getKey()),
//                    getValue(otherApply, PigType.PREG_SOW.getValue(), WareHouseType.MEDICINE.getKey()));
//            report.setPeiHuaiDrugAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = merge(getValue(otherApply, PigType.MATE_SOW.getValue(), WareHouseType.CONSUME.getKey()),
//                    getValue(otherApply, PigType.PREG_SOW.getValue(), WareHouseType.CONSUME.getKey()));
//            report.setPeiHuaiConsumerAmount(amountAndQuantityDto.getAmount());
//
//            /*保育*/
//            amountAndQuantityDto = getValue(otherApply, PigType.NURSERY_PIGLET.getValue(), WareHouseType.FEED.getKey());
//            report.setNurseryFeedAmount(amountAndQuantityDto.getAmount());
//            report.setNurseryFeedCount(amountAndQuantityDto.getQuantity());
//            amountAndQuantityDto = getValue(otherApply, PigType.NURSERY_PIGLET.getValue(), WareHouseType.MATERIAL.getKey());
//            report.setNurseryMaterialAmount(amountAndQuantityDto.getAmount());
//            report.setNurseryMaterialCount(amountAndQuantityDto.getQuantity());
//            amountAndQuantityDto = getValue(otherApply, PigType.NURSERY_PIGLET.getValue(), WareHouseType.VACCINATION.getKey());
//            report.setNurseryVaccineAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, PigType.NURSERY_PIGLET.getValue(), WareHouseType.MEDICINE.getKey());
//            report.setNurseryDrugAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, PigType.NURSERY_PIGLET.getValue(), WareHouseType.CONSUME.getKey());
//            report.setNurseryConsumerAmount(amountAndQuantityDto.getAmount());
//
//            /*育肥*/
//            amountAndQuantityDto = getValue(otherApply, PigType.FATTEN_PIG.getValue(), WareHouseType.FEED.getKey());
//            report.setFattenFeedAmount(amountAndQuantityDto.getAmount());
//            report.setFattenFeedCount(amountAndQuantityDto.getQuantity());
//            amountAndQuantityDto = getValue(otherApply, PigType.FATTEN_PIG.getValue(), WareHouseType.MATERIAL.getKey());
//            report.setFattenMaterialAmount(amountAndQuantityDto.getAmount());
//            report.setFattenMaterialCount(amountAndQuantityDto.getQuantity());
//            amountAndQuantityDto = getValue(otherApply, PigType.FATTEN_PIG.getValue(), WareHouseType.VACCINATION.getKey());
//            report.setFattenVaccineAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, PigType.FATTEN_PIG.getValue(), WareHouseType.MEDICINE.getKey());
//            report.setFattenDrugAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, PigType.FATTEN_PIG.getValue(), WareHouseType.CONSUME.getKey());
//            report.setFattenConsumerAmount(amountAndQuantityDto.getAmount());
//
//            /*公猪*/
//            amountAndQuantityDto = getValue(otherApply, PigType.BOAR.getValue(), WareHouseType.FEED.getKey());
//            report.setBoarFeedAmount(amountAndQuantityDto.getAmount());
//            report.setBoarFeedCount(amountAndQuantityDto.getQuantity());
//            amountAndQuantityDto = getValue(otherApply, PigType.BOAR.getValue(), WareHouseType.MATERIAL.getKey());
//            report.setBoarMaterialAmount(amountAndQuantityDto.getAmount());
//            report.setBoarMaterialCount(amountAndQuantityDto.getQuantity());
//            amountAndQuantityDto = getValue(otherApply, PigType.BOAR.getValue(), WareHouseType.VACCINATION.getKey());
//            report.setBoarVaccineAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, PigType.BOAR.getValue(), WareHouseType.MEDICINE.getKey());
//            report.setBoarDrugAmount(amountAndQuantityDto.getAmount());
//            amountAndQuantityDto = getValue(otherApply, PigType.BOAR.getValue(), WareHouseType.CONSUME.getKey());
//            report.setBoarConsumerAmount(amountAndQuantityDto.getAmount());
//
//            result.add(report);
        }

        return result;
    }

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
    public class AmountAndQuantityDto {

        public AmountAndQuantityDto() {
        }

        public AmountAndQuantityDto(BigDecimal amount, Long quantity) {
            this.amount = amount;
            this.quantity = quantity;
        }

        private BigDecimal amount;

        private Long quantity;
    }
}
