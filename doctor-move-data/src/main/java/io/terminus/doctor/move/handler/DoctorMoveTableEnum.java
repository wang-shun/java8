package io.terminus.doctor.move.handler;

import io.terminus.doctor.move.model.TB_FieldValue;
import lombok.Getter;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/27
 */

public enum DoctorMoveTableEnum {

    //基础数据
    TB_FieldValue(TB_FieldValue.class);

//    //变动原因
//    B_ChangeReason,
//
//    //客户
//    B_Customer,
//
//    //公司猪场
//    view_FarmInfo,
//
//    //职员
//    view_FarmMember,
//
//    //猪舍
//    view_PigLocationList,
//
//    //猪群
//    view_GainCardList,
//    view_EventListGain,
//
//    //公猪
//    view_BoarCardList,
//    view_EventListBoar,
//
//    //母猪
//    view_SowCardList,
//    view_EventListSow;

    @Getter
    private final Class clazz;

    DoctorMoveTableEnum(Class clazz) {
        this.clazz = clazz;
    }
}
