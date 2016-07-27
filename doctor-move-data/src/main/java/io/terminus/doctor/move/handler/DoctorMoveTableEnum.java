package io.terminus.doctor.move.handler;

import io.terminus.doctor.move.model.B_ChangeReason;
import io.terminus.doctor.move.model.B_Customer;
import io.terminus.doctor.move.model.TB_FieldValue;
import io.terminus.doctor.move.model.View_BoarCardList;
import io.terminus.doctor.move.model.View_EventListBoar;
import io.terminus.doctor.move.model.View_EventListGain;
import io.terminus.doctor.move.model.View_EventListSow;
import io.terminus.doctor.move.model.View_FarmInfo;
import io.terminus.doctor.move.model.View_FarmMember;
import io.terminus.doctor.move.model.View_GainCardList;
import io.terminus.doctor.move.model.View_PigLocationList;
import io.terminus.doctor.move.model.View_SowCardList;
import lombok.Getter;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/27
 */

public enum DoctorMoveTableEnum {

    //基础数据
    TB_FieldValue(TB_FieldValue.class),

    //变动原因
    B_ChangeReason(B_ChangeReason.class),

    //客户
    B_Customer(B_Customer.class),

    //公司猪场
    view_FarmInfo(View_FarmInfo.class),

    //职员
    view_FarmMember(View_FarmMember.class),

    //猪舍
    view_PigLocationList(View_PigLocationList.class),

    //猪群
    view_GainCardList(View_GainCardList.class),
    view_EventListGain(View_EventListGain.class),

    //公猪
    view_BoarCardList(View_BoarCardList.class),
    view_EventListBoar(View_EventListBoar.class),

    //母猪
    view_SowCardList(View_SowCardList.class),
    view_EventListSow(View_EventListSow.class);

    @Getter
    private final Class clazz;

    DoctorMoveTableEnum(Class clazz) {
        this.clazz = clazz;
    }
}
