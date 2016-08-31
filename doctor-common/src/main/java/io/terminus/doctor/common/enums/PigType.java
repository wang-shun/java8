package io.terminus.doctor.common.enums;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

/**
 * Desc: 猪类枚举
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */

public enum PigType {

    FARROW_PIGLET(1, "产房仔猪", "商品猪", 3),
    NURSERY_PIGLET(2, "保育猪", "商品猪", 4),
    FATTEN_PIG(3, "育肥猪", "商品猪", 5),
    RESERVE_SOW(4, "后备母猪", "种母猪", 6),
    MATE_SOW(5, "配种母猪", "种母猪", 1),
    PREG_SOW(6, "妊娠母猪", "种母猪", 2),
    DELIVER_SOW(7, "分娩母猪", "种母猪", 8),
    RESERVE_BOAR(8, "后备公猪", "种公猪", 6),
    BOAR(9, "种公猪", "种公猪", 7),
    BREEDING(10, "育种猪", "种猪", 9);

    @Getter
    private final int value;
    @Getter
    private final String desc;
    @Getter
    private final String type;
    @Getter
    private final int order;

    PigType(int value, String desc, String type, int order) {
        this.value = value;
        this.desc = desc;
        this.type = type;
        this.order =order;
    }

    public static PigType from(int number) {
        for (PigType type : PigType.values()) {
            if (Objects.equal(type.value, number)) {
                return type;
            }
        }
        return null;
    }

    public static PigType from(String desc) {
        for (PigType type : PigType.values()) {
            if (Objects.equal(type.desc, desc)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isBoar(int value) {
        return RESERVE_BOAR.getValue() == value || BOAR.getValue() == value;
    }

    public static boolean isSow(int value) {
        return MATE_SOW.getValue() == value ||
                PREG_SOW.getValue() == value ||
                DELIVER_SOW.getValue() == value;
    }

    //按照实际情况, 分娩母猪舍也有猪群! 后备母猪也是猪群!!
    public static boolean isGroup(int value) {
        return FARROW_PIGLET.getValue() == value ||
                NURSERY_PIGLET.getValue() == value ||
                FATTEN_PIG.getValue() == value ||
                DELIVER_SOW.getValue() == value ||
                RESERVE_SOW.getValue() == value ||
                RESERVE_BOAR.getValue() == value;
    }

    //产房仔猪的类型
    public static final List<Integer> FARROW_TYPES = Lists.newArrayList(FARROW_PIGLET.getValue(), DELIVER_SOW.getValue());

    //可配种舍的类型
    public static final List<Integer> MATING_TYPES = Lists.newArrayList(MATE_SOW.getValue(), PREG_SOW.getValue());

    //后备舍类型
    public static final List<Integer> HOUBEI_TYPES = Lists.newArrayList(RESERVE_SOW.getValue(), RESERVE_BOAR.getValue());

    //所有类型
    public static final List<Integer> ALL_TYPES = Lists.newArrayList(
            FARROW_PIGLET.getValue(),
            NURSERY_PIGLET.getValue(),
            FATTEN_PIG.getValue(),
            RESERVE_SOW.getValue(),
            MATE_SOW.getValue(),
            PREG_SOW.getValue(),
            DELIVER_SOW.getValue(),
            RESERVE_BOAR.getValue(),
            BOAR.getValue(),
            BREEDING.getValue()
    );
}
