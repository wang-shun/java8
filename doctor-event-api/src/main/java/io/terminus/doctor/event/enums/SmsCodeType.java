package io.terminus.doctor.event.enums;

import java.util.Objects;

/**
 * 陈增辉 on 16/7/7.
 * 短信验证码类型
 */
public enum SmsCodeType {
    REGISTER(0, "user.register.code", "注册"),
    RESET_PASSWORD(1, "user.sms.code", "重置密码");

    private int value;
    private String template; //短信内容模板名称, 用于查询表 parana_message_templates 的 name 字段
    private String desc;

    SmsCodeType(int value, String template, String desc){
        this.value = value;
        this.template = template;
        this.desc = desc;
    }

    public static SmsCodeType from(Integer value){
        for(SmsCodeType type : SmsCodeType.values()){
            if(Objects.equals(value, type.value)){
                return type;
            }
        }
        return null;
    }

    public int value(){
        return this.value;
    }

    public String template(){
        return this.template;
    }

    public String desc(){
        return this.desc;
    }
}
