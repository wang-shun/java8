package io.terminus.doctor.msg.enums;

import java.util.Objects;

/**
 * 陈增辉 on 16/7/7.
 * 短信验证码类型
 */
public enum SmsCodeType {
    REGISTER(0, "注册"),
    RESET_PASSWORD(1, "重置密码");

    private int value;
    private String desc;

    SmsCodeType(int value, String desc){
        this.value = value;
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

    public String toString(){
        return this.desc;
    }
}
