package io.terminus.doctor.user.enums;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

public enum TargetSystem {
    //注意: desc的写法, 由3个分号分隔的字符串组成, 依次对应表 parana_configs 中的字段 key, 用于获取在数据库中配置的值
    //在此增加枚举值时, 不要忘了往表 parana_configs 中添加相应数据
    PIGMALL    (1, "system.pigmall.domain", "system.pigmall.password", "system.pigmall.corp.id"),
    NEVEREST   (2, "system.neverest.domain", "system.neverest.password", "system.neverest.corp.id");

    private final int value;
    @Getter
    private final String keyOfDomain;
    @Getter
    private final String keyOfPasword;
    @Getter
    private final String keyOfCorpId;
    @Getter @Setter
    private String valueOfDomain;
    @Getter @Setter
    private String valueOfPasword;
    @Getter @Setter
    private Long valueOfCorpId;

    TargetSystem(int value, String keyOfDomain, String keyOfPasword, String keyOfCorpId) {
        this.value = value;
        this.keyOfDomain = keyOfDomain;
        this.keyOfPasword = keyOfPasword;
        this.keyOfCorpId = keyOfCorpId;
    }

    public static TargetSystem from(int value) {
        for (TargetSystem type : TargetSystem.values()) {
            if (Objects.equal(type.value, value)) {
                return type;
            }
        }
        return null;
    }

    public int value() {
        return value;
    }

    @Override
    public String toString() {
        return "TargetSystem{" +
                "value=" + value +
                ", keyOfDomain='" + keyOfDomain + '\'' +
                ", keyOfPasword='" + keyOfPasword + '\'' +
                ", keyOfCorpId='" + keyOfCorpId + '\'' +
                ", valueOfDomain='" + valueOfDomain + '\'' +
                ", valueOfPasword='" + valueOfPasword + '\'' +
                ", valueOfCorpId='" + valueOfCorpId + '\'' +
                '}';
    }
}