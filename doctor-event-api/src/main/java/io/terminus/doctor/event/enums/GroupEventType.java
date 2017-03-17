package io.terminus.doctor.event.enums;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

/**
 * Desc: 猪群事件类型枚举
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */

public enum GroupEventType {

    NEW(1, "新建猪群"),
    MOVE_IN(2, "转入猪群"),
    CHANGE(3, "猪群变动"),
    TRANS_GROUP(4, "猪群转群"),
    TURN_SEED(5, "商品猪转为种猪"),
    LIVE_STOCK(6, "猪只存栏"),
    DISEASE(7, "疾病"),
    ANTIEPIDEMIC(8, "防疫"),
    TRANS_FARM(9, "转场"),
    CLOSE(10, "关闭猪群"),
    WEAN(11, "断奶");

    @Getter
    private final int value;
    @Getter
    private final String desc;

    GroupEventType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static GroupEventType from(int number) {
        for (GroupEventType type : GroupEventType.values()) {
            if (Objects.equal(type.value, number)) {
                return type;
            }
        }
        return null;
    }

    public static GroupEventType from(String desc) {
        for (GroupEventType type : GroupEventType.values()) {
            if (Objects.equal(type.desc, desc)) {
                return type;
            }
        }
        return null;
    }

    //空猪群不能操作事件的类型
    public static final List<Integer> EMPTY_GROUPS = Lists.newArrayList(
            GroupEventType.CHANGE.getValue(),
            GroupEventType.TRANS_GROUP.getValue(),
            GroupEventType.TURN_SEED.getValue(),
            GroupEventType.LIVE_STOCK.getValue(),
            GroupEventType.DISEASE.getValue(),
            GroupEventType.ANTIEPIDEMIC.getValue(),
            GroupEventType.TRANS_FARM.getValue()
    );

    public static final List<Integer> NOTICE_MESSAGE_GROUP_EVENT = Lists.newArrayList(CLOSE.getValue());
}
