package io.terminus.doctor.common.enums;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 10:39 2017/3/29
 * 来源,表明是录入,excel导入,旧软件导入
 */

public enum SourceType {

    INPUT(1, "软件录入"),
    IMPORT(2, "导入数据"),
    MOVE(3, "迁移数据"),
    ADD(4, "补录事件"),
    TRANS_FARM(5,"转场复制的事件"),
    IMPORT_OLD(6, "原导入逻辑的事件"),
    MOVE_OLD(7, "原迁移逻辑的事件");

    @Getter
    private Integer value;
    @Getter
    private String desc;

    SourceType(Integer value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public static SourceType from(Integer value) {
        for (SourceType es : SourceType.values()) {
            if (Objects.equal(value, es.getValue())) {
                return es;
            }
        }
        return null;
    }

    public static List<Integer> UN_MODIFY = Lists.newArrayList(IMPORT.getValue()
            , MOVE.getValue(), TRANS_FARM.getValue());
}
