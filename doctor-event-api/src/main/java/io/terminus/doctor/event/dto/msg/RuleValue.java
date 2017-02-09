package io.terminus.doctor.event.dto.msg;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 消息规则值
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/31
 */
@Data
public class RuleValue implements Serializable {
    private static final long serialVersionUID = -8931008060015900072L;

    /**
     * 规则的id值
     */
    private Integer id;

    /**
     * 类型
     * 1. 到达某个值, value起作用
     * 2. 到达值范围, leftValue和rightValue起作用
     * <p>
     * 3. 到达某个日期, date起作用
     * 4. 到达日期范围, leftDate和rightDate起作用
     */
    private Integer ruleType;

    /**
     * 描述
     */
    private String describe;

    /**
     * 值判断
     */
    private Double value;
    private Double leftValue;
    private Double rightValue;

    /**
     * 日期判断
     */
    private Date date;
    private Date leftDate;
    private Date rightDate;

    /**
     * @see RuleValue#ruleType
     */
    public enum RuleType {

        VALUE(1, "到达某个值, value起作用"),
        VALUE_RANGE(2, "leftValue和rightValue起作用"),

        DATE(3, "到达某个日期, date起作用"),
        DATE_RANGE(4, "到达日期范围, leftDate和rightDate起作用");

        @Getter
        private Integer value;

        @Getter
        private String describe;

        RuleType(Integer value, String describe) {
            this.value = value;
            this.describe = describe;
        }

    }
}
