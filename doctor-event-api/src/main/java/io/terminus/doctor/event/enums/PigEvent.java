package io.terminus.doctor.event.enums;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public enum PigEvent {

    CHG_LOCATION(1, "转舍", "转舍事件", 0),
    CHG_FARM(2, "转场", "转场事件", 0),
    CONDITION(3, "体况", "体况事件", 0),
    DISEASE(4, "疾病", "疾病事件", 0),
    VACCINATION(5, "防疫", "免疫事件", 0),
    REMOVAL(6, "离场", "离场事件", 0),
    ENTRY(7, "进场", "进场事件", 0),       //进场事件
    SEMEN(8, "采精", "公猪采精事件信息", 2),     //只有公猪有此事件
    MATING(9, "配种", "母猪配种事件", 1),
    TO_PREG(10, "转舍", "转入妊娠舍", 1),
    PREG_CHECK(11, "妊娠检查", "妊娠检查", 1),
    TO_MATING(12, "转舍", "转入配种舍", 1),
    TO_FARROWING(14, "转舍", "去分娩信息", 1),
    FARROWING(15, "分娩", "分娩", 1),
    WEAN(16, "断奶", "断奶事件", 1),
    FOSTERS(17, "拼窝", "拼窝事件", 1),
    FOSTERS_BY(19, "被拼窝", "被拼窝母猪", 1),
    PIGLETS_CHG(18, "仔猪变动", "母猪仔猪变动事件信息", 1);


    @Getter
    private Integer key;

    @Getter
    private String name;

    @Getter
    private String desc;

    @Getter
    private Integer type; //0 表示公猪母猪共有事件, 1 母猪事件, 2 公猪事件

    PigEvent(Integer key, String name, String desc, Integer type){
        this.key = key;
        this.name = name;
        this.desc = desc;
        this.type = type;
    }

    public static PigEvent from(Integer key){
        for(PigEvent pigEvent : PigEvent.values()){
            if(Objects.equals(pigEvent.getKey(), key)){
                return pigEvent;
            }
        }
        return null;
    }

    public static PigEvent from(String name){
        for(PigEvent pigEvent : PigEvent.values()){
            if(Objects.equals(pigEvent.name, name)){
                return pigEvent;
            }
        }
        return null;
    }

    public static PigEvent fromDesc(String desc){
        for(PigEvent pigEvent : PigEvent.values()){
            if(Objects.equals(pigEvent.desc, desc)){
                return pigEvent;
            }
        }
        return null;
    }

    public static List<PigEvent> from(List<Integer> types){
        return Arrays.asList(values()).stream().filter(pigEvent -> types.contains(pigEvent.getType())).collect(Collectors.toList());
    }

    public static final List<Integer> NOTICE_MESSAGE_PIG_EVENT = Lists.newArrayList(CONDITION.getKey(), VACCINATION.getKey(), REMOVAL.getKey(), MATING.getKey(), PREG_CHECK.getKey(), TO_FARROWING.getKey(), WEAN.getKey());
}
