package io.terminus.doctor.msg.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 规则对象
 *      @see io.terminus.doctor.msg.model.DoctorMessageRuleTemplate#ruleValue
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/1
 */
@Data
public class Rule implements Serializable {
    private static final long serialVersionUID = -1232594098374078645L;

    /**
     * 规则对应的值
     */
    private List<RuleValue> values;

    /**
     * 频率, 小时为单位;
     * 若小于0, 表示消息只通知一次
     */
    private Integer frequence;
}
