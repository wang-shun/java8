package io.terminus.doctor.event.handler;

import com.google.common.base.MoreObjects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.enums.PigStatus;

import java.util.Collections;
import java.util.List;

/**
 * Created by xjn on 17/1/17.
 * 猪可转舍猪舍选择
 */

public class DoctorBarnSelector {
    private static final Table<PigStatus, PigType, List<PigType>> barnMap = config();

    public static List<PigType> select(PigStatus pigStatus, PigType pigType) {
        if (pigType == null) {
            throw new ServiceException("pigType.not.null");
        }
        if (pigStatus == null) {
            throw new ServiceException("pigStatus.not.null");
        }
        return MoreObjects.firstNonNull(barnMap.get(pigStatus, pigType), Collections.emptyList());
    }
    private static Table<PigStatus, PigType, List<PigType>> config() {
        Table<PigStatus, PigType, List<PigType>> map = HashBasedTable.create();
        //配种舍母猪可以转配怀舍
        map.put(PigStatus.Entry, PigType.MATE_SOW, Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW));
        map.put(PigStatus.Mate, PigType.MATE_SOW, Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW));
        map.put(PigStatus.KongHuai, PigType.MATE_SOW, Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW));
        map.put(PigStatus.Pregnancy, PigType.MATE_SOW, Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW));
        map.put(PigStatus.Wean, PigType.MATE_SOW, Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW));
        //妊娠舍可以转配怀舍
        map.put(PigStatus.Entry, PigType.PREG_SOW, Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW));
        map.put(PigStatus.Mate, PigType.PREG_SOW, Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW));
        map.put(PigStatus.KongHuai, PigType.PREG_SOW, Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW));
        map.put(PigStatus.Wean, PigType.PREG_SOW, Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW));
        //阳性妊娠母猪可以转配怀产房
        map.put(PigStatus.Pregnancy, PigType.PREG_SOW, Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW, PigType.DELIVER_SOW));
        //待分娩和哺乳只能转产房
        map.put(PigStatus.Farrow, PigType.DELIVER_SOW, Lists.newArrayList(PigType.DELIVER_SOW));
        map.put(PigStatus.FEED, PigType.DELIVER_SOW, Lists.newArrayList(PigType.DELIVER_SOW));
        //空怀和断奶在产房可已转配怀和产房
        map.put(PigStatus.KongHuai, PigType.DELIVER_SOW, Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW, PigType.DELIVER_SOW));
        map.put(PigStatus.Wean, PigType.DELIVER_SOW, Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW, PigType.DELIVER_SOW));
        //公猪可以转公猪舍
        map.put(PigStatus.BOAR_ENTRY, PigType.BOAR, Lists.newArrayList(PigType.BOAR));

        return map;
    }
}
