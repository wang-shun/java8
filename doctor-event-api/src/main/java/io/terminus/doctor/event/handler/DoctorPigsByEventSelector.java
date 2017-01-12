package io.terminus.doctor.event.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dto.DoctorSuggestPigSearch;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;

import java.util.Map;

import static io.terminus.doctor.event.enums.PigEvent.*;

/**
 * Created by xjn on 17/1/12.
 * 根据猪事件构建能够执行此事件的猪查询条件
 */
public class DoctorPigsByEventSelector {
    private static final Map<Integer, DoctorSuggestPigSearch> eventMap = configuration();

    public static DoctorSuggestPigSearch select(Integer eventType) {
        if (eventType == null) {
            throw new ServiceException("eventType.not.null");
        }
        return eventMap.get(eventType);
    }
    private static Map<Integer, DoctorSuggestPigSearch> configuration() {
        Map<Integer, DoctorSuggestPigSearch> criteriaMap = Maps.newHashMap();
        //在配种或妊娠舍状态不是阳性的猪-》配种
        criteriaMap.put(MATING.getKey(), DoctorSuggestPigSearch.builder().notStatus(PigStatus.Pregnancy.getKey()).isRemoval(IsOrNot.NO.getValue())
                .barnTypes(Lists.newArrayList(PigType.MATE_SOW.getValue(), PigType.PREG_SOW.getValue())).notMatingCount(3).build());

        //配种、空怀、阳性的猪 -》妊娠检查
        criteriaMap.put(PREG_CHECK.getKey(), DoctorSuggestPigSearch.builder()
                .statuses(Lists.newArrayList(PigStatus.Mate.getKey(), PigStatus.KongHuai.getKey(), PigStatus.Pregnancy.getKey())).isRemoval(IsOrNot.NO.getValue()).build());

        //待分娩的猪 -》分娩
        criteriaMap.put(FARROWING.getKey(), DoctorSuggestPigSearch.builder().status(PigStatus.Farrow.getKey()).isRemoval(IsOrNot.NO.getValue()).build());

        //哺乳的猪 -》断奶
        criteriaMap.put(WEAN.getKey(), DoctorSuggestPigSearch.builder().status(PigStatus.FEED.getKey()).isRemoval(IsOrNot.NO.getValue()).build());

        //哺乳的猪 -》拼窝
        criteriaMap.put(FOSTERS.getKey(), DoctorSuggestPigSearch.builder().status(PigStatus.FEED.getKey()).isRemoval(IsOrNot.NO.getValue()).build());

        //哺乳的猪 -》仔猪变动
        criteriaMap.put(PIGLETS_CHG.getKey(), DoctorSuggestPigSearch.builder().status(PigStatus.FEED.getKey()).isRemoval(IsOrNot.NO.getValue()).build());

        //不是哺乳的猪 -》转场
        criteriaMap.put(CHG_FARM.getKey(), DoctorSuggestPigSearch.builder().notStatus(PigStatus.FEED.getKey()).isRemoval(IsOrNot.NO.getValue()).build());

        //不是哺乳的猪 -》离场
        criteriaMap.put(REMOVAL.getKey(), DoctorSuggestPigSearch.builder().notStatus(PigStatus.FEED.getKey()).isRemoval(IsOrNot.NO.getValue()).build());

        //公猪 -》采精
        criteriaMap.put(SEMEN.getKey(), DoctorSuggestPigSearch.builder().sex(DoctorPig.PigSex.BOAR.getKey()).isRemoval(IsOrNot.NO.getValue()).build());

        //所有未离场的猪
        criteriaMap.put(CHG_LOCATION.getKey(), DoctorSuggestPigSearch.builder().isRemoval(IsOrNot.NO.getValue()).build());
        criteriaMap.put(CONDITION.getKey(), DoctorSuggestPigSearch.builder().isRemoval(IsOrNot.NO.getValue()).build());
        criteriaMap.put(DISEASE.getKey(), DoctorSuggestPigSearch.builder().isRemoval(IsOrNot.NO.getValue()).build());
        criteriaMap.put(VACCINATION.getKey(), DoctorSuggestPigSearch.builder().isRemoval(IsOrNot.NO.getValue()).build());
        return criteriaMap;
    }
}
