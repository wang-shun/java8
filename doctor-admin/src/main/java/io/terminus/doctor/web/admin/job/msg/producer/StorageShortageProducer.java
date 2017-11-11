package io.terminus.doctor.web.admin.job.msg.producer;

import com.google.api.client.util.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeAvgReadService;
import io.terminus.doctor.basic.service.DoctorWareHouseReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialApplyReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSkuReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockReadService;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.msg.Rule;
import io.terminus.doctor.event.dto.msg.RuleValue;
import io.terminus.doctor.event.dto.msg.SubUser;
import io.terminus.doctor.event.enums.Category;
import io.terminus.doctor.event.model.DoctorMessage;
import io.terminus.doctor.event.model.DoctorMessageRuleRole;
import io.terminus.doctor.web.admin.job.msg.dto.DoctorMessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Desc: 仓库库存不足提示
 * 1. 物料剩余天数
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/6
 */
@Component
@Slf4j
public class StorageShortageProducer extends AbstractJobProducer {

    @Autowired
    private DoctorMaterialConsumeAvgReadService doctorMaterialConsumeAvgReadService;

    @RpcConsumer
    private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

    @RpcConsumer
    private DoctorWarehouseStockReadService doctorWarehouseStockReadService;

    @RpcConsumer
    private DoctorWarehouseSkuReadService doctorWarehouseSkuReadService;

    @RpcConsumer
    private DoctorWareHouseReadService doctorWareHouseReadService;

    public StorageShortageProducer() {
        super(Category.STORAGE_SHORTAGE);
    }

    @Override
    protected void message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {
        Rule rule = ruleRole.getRule();
        // ruleValue map
        Map<Integer, RuleValue> ruleValueMap = Maps.newHashMap();
        for (int i = 0; rule.getValues() != null && i < rule.getValues().size(); i++) {
            RuleValue ruleValue = rule.getValues().get(i);
            ruleValueMap.put(ruleValue.getId(), ruleValue);
        }

        //查询猪场下尚有库存的sku
        Map<String, Object> params = new HashMap<>();
        params.put("farmId", ruleRole.getFarmId());
        params.put("effective", "true");
        List<DoctorWarehouseStock> stocks = RespHelper.or500(doctorWarehouseStockReadService.list(params));

        params.clear();
        params.put("farmId", ruleRole.getFarmId());
        params.put("skuIds", stocks
                .stream()
                .map(DoctorWarehouseStock::getSkuId)
                .collect(Collectors.toList()));

        Map<Long, List<DoctorWarehouseStock>> stockMap = stocks.stream().collect(Collectors.groupingBy(DoctorWarehouseStock::getSkuId));
        List<DoctorWarehouseMaterialApply> farmApplies = RespHelper.or500(doctorWarehouseMaterialApplyReadService.list(params));

        //计算每天领用数量，推导剩余库存是否足够使用
        //剩余使用天数
        Map<Long, List<DoctorWarehouseMaterialApply>> skuApply = farmApplies.stream().collect(Collectors.groupingBy(DoctorWarehouseMaterialApply::getMaterialId));
        for (Long skuId : skuApply.keySet()) {

            Optional<DoctorWarehouseMaterialApply> maxDateApply = skuApply.get(skuId).stream().max(new Comparator<DoctorWarehouseMaterialApply>() {
                @Override
                public int compare(DoctorWarehouseMaterialApply o1, DoctorWarehouseMaterialApply o2) {
                    return o1.getApplyDate().compareTo(o2.getApplyDate());
                }
            });
            Optional<DoctorWarehouseMaterialApply> minDateApply = skuApply.get(skuId).stream().min(new Comparator<DoctorWarehouseMaterialApply>() {
                @Override
                public int compare(DoctorWarehouseMaterialApply o1, DoctorWarehouseMaterialApply o2) {
                    return o1.getApplyDate().compareTo(o2.getApplyDate());
                }
            });

            if (!maxDateApply.isPresent() || !minDateApply.isPresent()) {
                log.info("can not find sku {} max apply date or min apply date", skuId);
                continue;
            }

            int day = DateUtil.getDeltaDaysAbs(maxDateApply.get().getApplyDate(), minDateApply.get().getApplyDate());//天数
            double usedQuantity = skuApply.get(skuId).stream().mapToDouble(a -> a.getQuantity().doubleValue()).sum();//已领用的量
            double rate = usedQuantity * 100 / day;//消耗速率,乘100我也不知道为什么。原有逻辑，应该是单位换算

            if (stockMap.containsKey(skuId)) {
                double leftDay = stockMap.get(skuId).get(0).getQuantity().doubleValue() / rate;
                if (leftDay < ruleValueMap.get(1).getValue()) {

                    DoctorWarehouseSku sku = RespHelper.or500(doctorWarehouseSkuReadService.findById(skuId));
                    DoctorWareHouse wareHouse = RespHelper.or500(doctorWareHouseReadService.findById(stockMap.get(skuId).get(0).getWarehouseId()));

                    DoctorMessageInfo messageInfo = DoctorMessageInfo.builder()
                            .timeDiff(leftDay)
                            .lotNumber(stockMap.get(skuId).get(0).getQuantity().doubleValue())
                            .wareHouseId(stockMap.get(skuId).get(0).getWarehouseId())
                            .wareHouseName(stockMap.get(skuId).get(0).getWarehouseName())
                            .businessId(skuId)
                            .businessType(DoctorMessage.BUSINESS_TYPE.WAREHOUSE.getValue())
                            .code(null == sku ? "" : sku.getName())
                            .operatorId(wareHouse == null ? null : wareHouse.getManagerId())
                            .operatorName(wareHouse == null ? "" : wareHouse.getManagerName())
                            .reason(ruleValueMap.get(1).getDescribe())
                            .ruleValueId(ruleValueMap.get(1).getId())
                            .build();
                    createMessage(subUsers, ruleRole, messageInfo);
                }
            }
        }


        // 获取farmid下所有跟踪的仓库和物料数据
//        List<DoctorMaterialConsumeAvgDto> materialConsumeAvgs = RespHelper.orServEx(doctorMaterialConsumeAvgReadService.findMaterialConsumeAvgsByFarmId(ruleRole.getFarmId()));
//        for (int i = 0; materialConsumeAvgs != null && i < materialConsumeAvgs.size(); i++) {
//            DoctorMaterialConsumeAvgDto materialConsumeAvg = materialConsumeAvgs.get(i);
//            Integer lotConsumeDay = materialConsumeAvg.getLotConsumeDay();
//            if (ruleValueMap.get(1) != null && lotConsumeDay != null) {
//                // 如果剩余使用天数 小于 配置的天数
//                RuleValue ruleValue = ruleValueMap.get(1);
//                if (lotConsumeDay < ruleValue.getValue()) {
//                    DoctorMessageInfo messageInfo = DoctorMessageInfo.builder()
//                            .timeDiff(lotConsumeDay.doubleValue())
//                            .lotNumber(materialConsumeAvg.getLotNumber())
//                            .wareHouseId(materialConsumeAvg.getWareHouseId())
//                            .wareHouseName(materialConsumeAvg.getWareHouseName())
//                            .businessId(materialConsumeAvg.getMaterialId())
//                            .businessType(DoctorMessage.BUSINESS_TYPE.WAREHOUSE.getValue())
//                            .code(materialConsumeAvg.getMaterialName())
//                            .operatorId(materialConsumeAvg.getManagerId())
//                            .operatorName(materialConsumeAvg.getManagerName())
//                            .reason(ruleValue.getDescribe())
//                            .ruleValueId(ruleValue.getId())
//                            .build();
//                    createMessage(subUsers, ruleRole, messageInfo);
//                }
//            }
//        }
    }


}
