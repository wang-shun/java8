package io.terminus.doctor.event.handler;

import com.google.common.base.MoreObjects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;

import java.util.Collections;
import java.util.List;

/**
 * Desc: 猪事件选择器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/12/26
 */
public class DoctorEventSelector {

    private static final Table<PigStatus, PigType, List<PigEvent>> pigTable = configuration();

    /**
     * 根据猪当前状态和所在猪舍，判断可以执行的事件(状态转换事件)
     * @param pigStatus   状态
     * @param pigType     猪类型
     * @return  猪事件
     */
    public static List<PigEvent> selectPigEvent(PigStatus pigStatus, PigType pigType) {
        if (pigType == null) {
            throw new ServiceException("pigType.not.null");
        }
        if (pigStatus == null) {
            throw new ServiceException("pigStatus.not.null");
        }
        return MoreObjects.firstNonNull(pigTable.get(pigStatus, pigType), Collections.emptyList());
    }

    //配置可以执行的事件
    private static Table<PigStatus, PigType, List<PigEvent>> configuration() {
        Table<PigStatus, PigType, List<PigEvent>> pigTable = HashBasedTable.create();
        // (已进场，配怀舍) => 配种
        pigTable.put(PigStatus.Entry, PigType.MATE_SOW, Lists.newArrayList(PigEvent.MATING));
        pigTable.put(PigStatus.Entry, PigType.PREG_SOW, Lists.newArrayList(PigEvent.MATING));

        // (已配种，配怀舍) => 配种，妊检
        pigTable.put(PigStatus.Mate, PigType.MATE_SOW, Lists.newArrayList(PigEvent.MATING, PigEvent.PREG_CHECK));
        pigTable.put(PigStatus.Mate, PigType.PREG_SOW, Lists.newArrayList(PigEvent.MATING, PigEvent.PREG_CHECK));

        // (阳性，配怀舍) => 妊检(只能到空怀：阴性、流产、返情)
        pigTable.put(PigStatus.Pregnancy, PigType.MATE_SOW, Lists.newArrayList(PigEvent.PREG_CHECK));
        pigTable.put(PigStatus.Pregnancy, PigType.PREG_SOW, Lists.newArrayList(PigEvent.PREG_CHECK));

        // (阳性，产房) => 分娩, 妊娠检查(只能到空怀：阴性、流产、返情)(理论上是不会出现这种情况的，因为阳性在产房会是待分娩)
        pigTable.put(PigStatus.Pregnancy, PigType.DELIVER_SOW, Lists.newArrayList(PigEvent.FARROWING, PigEvent.PREG_CHECK));

        // (空怀，配怀舍) => 配种，妊检(只能到阳性)
        pigTable.put(PigStatus.KongHuai, PigType.MATE_SOW, Lists.newArrayList(PigEvent.MATING, PigEvent.PREG_CHECK));
        pigTable.put(PigStatus.KongHuai, PigType.PREG_SOW, Lists.newArrayList(PigEvent.MATING, PigEvent.PREG_CHECK));

        // (空怀, 产房) => 妊检(只能到阳性，其实是待分娩)
        pigTable.put(PigStatus.KongHuai, PigType.DELIVER_SOW, Lists.newArrayList(PigEvent.PREG_CHECK));

        // (待分娩, 产房) => 分娩, 妊娠检查(只能到空怀：阴性、流产、返情)
        pigTable.put(PigStatus.Farrow, PigType.DELIVER_SOW, Lists.newArrayList(PigEvent.FARROWING, PigEvent.PREG_CHECK));

        // (哺乳，配怀舍) => 拼窝，断奶，仔猪变动
        pigTable.put(PigStatus.FEED, PigType.DELIVER_SOW, Lists.newArrayList(PigEvent.FOSTERS, PigEvent.WEAN, PigEvent.PIGLETS_CHG));

        // (断奶，配怀舍) => 配种
        pigTable.put(PigStatus.Wean, PigType.MATE_SOW, Lists.newArrayList(PigEvent.MATING));
        pigTable.put(PigStatus.Wean, PigType.PREG_SOW, Lists.newArrayList(PigEvent.MATING));

        return pigTable;
    }
}
