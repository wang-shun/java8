package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * Desc: 回滚时，需要更新的类型
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/21
 */

public enum RollbackType {

    //日报实时更新
    DAILY_LIVESTOCK(1, "存栏日报"),
    DAILY_DEAD(2, "死淘日报"),
    DAILY_FARROW(3, "分娩日报"),
    DAILY_MATE(4, "配种日报"),
    DAILY_SALE(5, "销售日报"),
    DAILY_WEAN(6, "断奶日报"),

    //记录回滚时间，晚上job更新
    MONTHLY_REPORT(7, "生产月报(包括存栏月报)"),
    MONTHLY_PARITY(8, "胎次产仔分析月报"),

    //直接删除
    GROUP_BATCH(9, "猪群批次总结"),

    //搜索实时更新
    SEARCH_BARN(10, "猪舍es搜索"),
    SEARCH_PIG(11, "猪es搜索"),
    SEARCH_GROUP(12, "猪群es搜索"),
    SEARCH_GROUP_DELETE(13, "猪群es删除索引"),
    SEARCH_PIG_DELETE(14, "猪es删除索引");


    @Getter
    private final int value;
    @Getter
    private final String desc;

    RollbackType(int value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public static RollbackType from(int value){
        return Arrays.stream(RollbackType.values())
                .filter(type -> Objects.equals(type.getValue(), value)).findFirst().orElse(null);
    }
}
