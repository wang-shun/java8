package io.terminus.doctor.schedule.msg.producer;

import com.google.api.client.util.Maps;
import io.terminus.doctor.basic.dto.DoctorMaterialConsumeAvgDto;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeAvgReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.dto.RuleValue;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.schedule.msg.dto.DoctorMessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Desc: 仓库库存不足提示
 *          1. 物料剩余天数
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/6
 */
@Component
@Slf4j
public class StorageShortageProducer extends AbstractJobProducer {

    @Autowired
    private DoctorMaterialConsumeAvgReadService doctorMaterialConsumeAvgReadService;

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

        // 获取farmid下所有跟踪的仓库和物料数据
        List<DoctorMaterialConsumeAvgDto> materialConsumeAvgs = RespHelper.orServEx(doctorMaterialConsumeAvgReadService.findMaterialConsumeAvgsByFarmId(ruleRole.getFarmId()));
        for (int i = 0; materialConsumeAvgs != null && i < materialConsumeAvgs.size(); i++) {
            DoctorMaterialConsumeAvgDto materialConsumeAvg = materialConsumeAvgs.get(i);
            Integer lotConsumeDay = materialConsumeAvg.getLotConsumeDay();
            if (ruleValueMap.get(1) != null && lotConsumeDay != null) {
                // 如果剩余使用天数 小于 配置的天数
                RuleValue ruleValue = ruleValueMap.get(1);
                if (lotConsumeDay < ruleValue.getValue()) {
                    DoctorMessageInfo messageInfo = DoctorMessageInfo.builder()
                            .timeDiff(lotConsumeDay.doubleValue())
                            .lotNumber(materialConsumeAvg.getLotNumber())
                            .wareHouseId(materialConsumeAvg.getWareHouseId())
                            .wareHouseName(materialConsumeAvg.getWareHouseName())
                            .businessId(materialConsumeAvg.getMaterialId())
                            .businessType(DoctorMessage.BUSINESS_TYPE.WAREHOUSE.getValue())
                            .code(materialConsumeAvg.getMaterialName())
                            .operatorId(materialConsumeAvg.getManagerId())
                            .operatorName(materialConsumeAvg.getManagerName())
                            .reason(ruleValue.getDescribe())
                            .ruleValueId(ruleValue.getId())
                            .build();
                    createMessage(subUsers, ruleRole, messageInfo);
                }
            }
        }
    }

}
