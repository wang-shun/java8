package io.terminus.doctor.msg.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Desc: 消息的种类, 对应category字段
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/31
 */
public enum Category {

    SYSTEM(0, "一般系统消息"),

    SOW_BREEDING(1,"待配种母猪提示");

    @Getter
    private Integer key;

    @Getter
    private String describe;

    Category(Integer key, String describe){
        this.key = key;
        this.describe = describe;
    }

    public static Category from(Integer key){
        for(Category category : Category.values()){
            if(Objects.equals(category.getKey(), key)){
                return category;
            }
        }
        return null;
    }
}
