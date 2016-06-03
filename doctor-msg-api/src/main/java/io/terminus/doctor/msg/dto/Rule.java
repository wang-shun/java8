package io.terminus.doctor.msg.dto;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

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

    /**
     * 发送渠道, 多个以 逗号 分隔
     * 消息发送渠道, 多个以逗号分隔. 0->站内信, 1->短信, 2->邮箱, 3->app推送
     */
    private String channels;

    /**
     * app回调url地址
     */
    private String url;


    /**
     * 发送渠道枚举值
     */
    public enum Channel {

        SYSTEM(0, "站内信", "sys"),
        MESSAGE(1, "短信", "sms"),
        EMAIL(2, "邮箱", "email"),
        APPPUSH(3, "app推送", "app");

        @Getter
        private Integer value;

        @Getter
        private String describe;

        @Getter
        private String suffix;

        Channel(Integer value, String describe, String suffix) {
            this.value = value;
            this.describe = describe;
            this.suffix = suffix;
        }
        public static Channel from(Integer value){
            for(Channel channel : Channel.values()){
                if(Objects.equals(channel.getValue(), value)){
                    return channel;
                }
            }
            return null;
        }
    }
}
