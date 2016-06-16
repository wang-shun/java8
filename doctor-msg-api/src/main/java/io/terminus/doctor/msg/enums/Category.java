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

    SOW_BREEDING(1,"待配种母猪提示"),
    SOW_PREGCHECK(2,"母猪需妊娠检查提示"),
    SOW_PREGHOME(3,"母猪需转入妊娠舍提示"),
    SOW_BIRTHDATE(4,"母猪预产期提示"),
    SOW_NEEDWEAN(5,"母猪需断奶提示"),
    SOW_ELIMINATE(6,"母猪应淘汰提示"),
    SOW_NOTLITTER(10,"母猪未产仔警报"),

    BOAR_ELIMINATE(7,"公猪应淘汰提示"),

    PIG_VACCINATION(8,"猪只免疫提示"),

    STORAGE_SHORTAGE(9,"仓库库存不足提示");

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
