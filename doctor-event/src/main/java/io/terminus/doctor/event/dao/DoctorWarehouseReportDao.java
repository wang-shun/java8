package io.terminus.doctor.event.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.google.common.collect.Lists;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.enums.DateDimension;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
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


    public List<WarehouseReport> count(Integer dateType) {

        List<Map<String, Object>> orgOtherCounts = this.sqlSession.selectList(namespace + ".countByOrg", Collections.singletonMap("type", dateType));
        List<Map<String, Object>> orgFarrowCounts = this.sqlSession.selectList(namespace + ".countFarrowByOrg", Collections.singletonMap("type", dateType));

        for (Map<String, Object> farrow : orgFarrowCounts) {
            farrow.put("pigType".toUpperCase(), 22);//修改产房仔猪类型，不然会与产房母猪冲突，无法区分
        }

        List<Map<String, Object>> orgFarrowSowCounts = this.sqlSession.selectList(namespace + ".countFarrowSowByOrg", Collections.singletonMap("type", dateType));

        Map<Long/*公司*/, Map<Short/*猪舍类型*/, Map<Short/*物料类型*/, AmountAndQuantityDto>>> middleResult = new HashMap<>();
        Map<Long/*公司*/, Date> orgDate = new HashMap<>();


        convert(Lists.newArrayList(orgOtherCounts, orgFarrowCounts, orgFarrowSowCounts), middleResult, orgDate);

        List<WarehouseReport> result = new ArrayList<>();
        for (Long orgId : middleResult.keySet()) {
            WarehouseReport report = new WarehouseReport();
            report.setOrgId(orgId);
            report.setApplyDate(orgDate.get(orgId));

            AmountAndQuantityDto amountAndQuantityDto;

            Map<Short, Map<Short, AmountAndQuantityDto>> otherApply = middleResult.get(orgId);

            /*产房仔猪--并没有这个猪舍类型，只能自定义一个不重复的魔法值*/
            amountAndQuantityDto = getValue(otherApply, 22, WareHouseType.FEED.getKey());
            report.setFarrowFeedCount(amountAndQuantityDto.getQuantity());
            report.setFarrowFeedAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, 22, WareHouseType.MATERIAL.getKey());
            report.setFarrowMaterialAmount(amountAndQuantityDto.getAmount());
            report.setFarrowMaterialCount(amountAndQuantityDto.getQuantity());
            amountAndQuantityDto = getValue(otherApply, 22, WareHouseType.VACCINATION.getKey());
            report.setFarrowVaccineAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, 22, WareHouseType.MEDICINE.getKey());
            report.setFarrowDrugAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, 22, WareHouseType.CONSUME.getKey());
            report.setFarrowConsumerAmount(amountAndQuantityDto.getAmount());

            /*产房母猪*/
            amountAndQuantityDto = getValue(otherApply, PigType.DELIVER_SOW.getValue(), WareHouseType.FEED.getKey());
            report.setFarrowFeedCount(amountAndQuantityDto.getQuantity());
            report.setFarrowFeedAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, PigType.DELIVER_SOW.getValue(), WareHouseType.MATERIAL.getKey());
            report.setFarrowMaterialAmount(amountAndQuantityDto.getAmount());
            report.setFarrowMaterialCount(amountAndQuantityDto.getQuantity());
            amountAndQuantityDto = getValue(otherApply, PigType.DELIVER_SOW.getValue(), WareHouseType.VACCINATION.getKey());
            report.setFarrowVaccineAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, PigType.DELIVER_SOW.getValue(), WareHouseType.MEDICINE.getKey());
            report.setFarrowDrugAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, PigType.DELIVER_SOW.getValue(), WareHouseType.CONSUME.getKey());
            report.setFarrowConsumerAmount(amountAndQuantityDto.getAmount());

            /*后备*/
            amountAndQuantityDto = getValue(otherApply, PigType.RESERVE.getValue(), WareHouseType.FEED.getKey());
            report.setHoubeiFeedAmount(amountAndQuantityDto.getAmount());
            report.setHoubeiFeedCount(amountAndQuantityDto.getQuantity());
            amountAndQuantityDto = getValue(otherApply, PigType.RESERVE.getValue(), WareHouseType.MATERIAL.getKey());
            report.setHoubeiMaterialAmount(amountAndQuantityDto.getAmount());
            report.setHoubeiMaterialCount(amountAndQuantityDto.getQuantity());
            amountAndQuantityDto = getValue(otherApply, PigType.RESERVE.getValue(), WareHouseType.VACCINATION.getKey());
            report.setHoubeiVaccineAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, PigType.RESERVE.getValue(), WareHouseType.MEDICINE.getKey());
            report.setHoubeiDrugAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, PigType.RESERVE.getValue(), WareHouseType.CONSUME.getKey());
            report.setHoubeiConsumerAmount(amountAndQuantityDto.getAmount());

            /*配怀*/
            amountAndQuantityDto = merge(getValue(otherApply, PigType.MATE_SOW.getValue(), WareHouseType.FEED.getKey()),
                    getValue(otherApply, PigType.PREG_SOW.getValue(), WareHouseType.FEED.getKey()));
            report.setPeiHuaiFeedCount(amountAndQuantityDto.getQuantity());
            report.setPeiHuaiFeedAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = merge(getValue(otherApply, PigType.MATE_SOW.getValue(), WareHouseType.MATERIAL.getKey()),
                    getValue(otherApply, PigType.PREG_SOW.getValue(), WareHouseType.MATERIAL.getKey()));
            report.setPeiHuaiMaterialCount(amountAndQuantityDto.getQuantity());
            report.setPeiHuaiMaterialAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = merge(getValue(otherApply, PigType.MATE_SOW.getValue(), WareHouseType.VACCINATION.getKey()),
                    getValue(otherApply, PigType.PREG_SOW.getValue(), WareHouseType.VACCINATION.getKey()));
            report.setPeiHuaiVaccineAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = merge(getValue(otherApply, PigType.MATE_SOW.getValue(), WareHouseType.MEDICINE.getKey()),
                    getValue(otherApply, PigType.PREG_SOW.getValue(), WareHouseType.MEDICINE.getKey()));
            report.setPeiHuaiDrugAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = merge(getValue(otherApply, PigType.MATE_SOW.getValue(), WareHouseType.CONSUME.getKey()),
                    getValue(otherApply, PigType.PREG_SOW.getValue(), WareHouseType.CONSUME.getKey()));
            report.setPeiHuaiConsumerAmount(amountAndQuantityDto.getAmount());

            /*保育*/
            amountAndQuantityDto = getValue(otherApply, PigType.NURSERY_PIGLET.getValue(), WareHouseType.FEED.getKey());
            report.setNurseryFeedAmount(amountAndQuantityDto.getAmount());
            report.setNurseryFeedCount(amountAndQuantityDto.getQuantity());
            amountAndQuantityDto = getValue(otherApply, PigType.NURSERY_PIGLET.getValue(), WareHouseType.MATERIAL.getKey());
            report.setNurseryMaterialAmount(amountAndQuantityDto.getAmount());
            report.setNurseryMaterialCount(amountAndQuantityDto.getQuantity());
            amountAndQuantityDto = getValue(otherApply, PigType.NURSERY_PIGLET.getValue(), WareHouseType.VACCINATION.getKey());
            report.setNurseryVaccineAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, PigType.NURSERY_PIGLET.getValue(), WareHouseType.MEDICINE.getKey());
            report.setNurseryDrugAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, PigType.NURSERY_PIGLET.getValue(), WareHouseType.CONSUME.getKey());
            report.setNurseryConsumerAmount(amountAndQuantityDto.getAmount());

            /*育肥*/
            amountAndQuantityDto = getValue(otherApply, PigType.FATTEN_PIG.getValue(), WareHouseType.FEED.getKey());
            report.setFattenFeedAmount(amountAndQuantityDto.getAmount());
            report.setFattenFeedCount(amountAndQuantityDto.getQuantity());
            amountAndQuantityDto = getValue(otherApply, PigType.FATTEN_PIG.getValue(), WareHouseType.MATERIAL.getKey());
            report.setFattenMaterialAmount(amountAndQuantityDto.getAmount());
            report.setFattenMaterialCount(amountAndQuantityDto.getQuantity());
            amountAndQuantityDto = getValue(otherApply, PigType.FATTEN_PIG.getValue(), WareHouseType.VACCINATION.getKey());
            report.setFattenVaccineAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, PigType.FATTEN_PIG.getValue(), WareHouseType.MEDICINE.getKey());
            report.setFattenDrugAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, PigType.FATTEN_PIG.getValue(), WareHouseType.CONSUME.getKey());
            report.setFattenConsumerAmount(amountAndQuantityDto.getAmount());

            /*公猪*/
            amountAndQuantityDto = getValue(otherApply, PigType.BOAR.getValue(), WareHouseType.FEED.getKey());
            report.setBoarFeedAmount(amountAndQuantityDto.getAmount());
            report.setBoarFeedCount(amountAndQuantityDto.getQuantity());
            amountAndQuantityDto = getValue(otherApply, PigType.BOAR.getValue(), WareHouseType.MATERIAL.getKey());
            report.setBoarMaterialAmount(amountAndQuantityDto.getAmount());
            report.setBoarMaterialCount(amountAndQuantityDto.getQuantity());
            amountAndQuantityDto = getValue(otherApply, PigType.BOAR.getValue(), WareHouseType.VACCINATION.getKey());
            report.setBoarVaccineAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, PigType.BOAR.getValue(), WareHouseType.MEDICINE.getKey());
            report.setBoarDrugAmount(amountAndQuantityDto.getAmount());
            amountAndQuantityDto = getValue(otherApply, PigType.BOAR.getValue(), WareHouseType.CONSUME.getKey());
            report.setBoarConsumerAmount(amountAndQuantityDto.getAmount());

            result.add(report);
        }

        return result;
    }

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
        private Date applyDate;
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
