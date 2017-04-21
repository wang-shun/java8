package io.terminus.doctor.web.front.msg.dto;

import io.terminus.doctor.event.dto.msg.RuleValue;
import io.terminus.doctor.event.model.DoctorMessageRule;
import lombok.Data;

import java.util.List;

/**
 * Created by xjn on 17/4/21.
 * 消息规则带有默认规则
 */
@Data
public class DoctorMessageRuleWithDefaultValueDto extends DoctorMessageRule{
    private static final long serialVersionUID = -6746636506005083478L;

    private List<RuleValue> defaultValues;
}
