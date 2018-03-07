package io.terminus.doctor.event.helper;

import com.google.common.collect.Maps;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * Created by xjn on 18/3/6.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorEventBaseHelper {

    private final DoctorPigEventDao doctorPigEventDao;

    private static  final Map<Integer, Integer> EVENT_TO_STATUS = Maps.newHashMap();

    static {
        EVENT_TO_STATUS.put(PigEvent.MATING.getKey(), PigStatus.Mate.getKey());
        EVENT_TO_STATUS.put(PigEvent.TO_FARROWING.getKey(), PigStatus.Farrow.getKey());
        EVENT_TO_STATUS.put(PigEvent.CHG_FARM_IN.getKey(), PigStatus.Farrow.getKey());
        EVENT_TO_STATUS.put(PigEvent.FARROWING.getKey(), PigStatus.FEED.getKey());
        EVENT_TO_STATUS.put(PigEvent.WEAN.getKey(), PigStatus.Wean.getKey());
    }

    @Autowired
    public DoctorEventBaseHelper(DoctorPigEventDao doctorPigEventDao) {
        this.doctorPigEventDao = doctorPigEventDao;
    }

    /**
     * 获取猪当前状态
     * @param pigId 猪id
     * @return 当前状态
     */
    public Integer getCurrentStatus(Long pigId) {
        return getStatusBeforeEventAt(pigId, new Date());
    }

    /**
     * 获取猪指定时间前的状态
     * @param pigId 猪id
     * @param eventAt 事件时间
     * @return 猪状态
     */
    public Integer getStatusBeforeEventAt(Long pigId, Date eventAt) {
        DoctorPigEvent beforeStatusEvent = doctorPigEventDao.getLastStatusEventBeforeEventAt(pigId, eventAt);
        return getStatus(beforeStatusEvent);
    }

    /**
     * 获取当前胎次
     * @param pigId 猪id
     * @return 当前胎次
     */
    public Integer getCurrentParity(Long pigId) {
        DoctorPigEvent entryEvent = doctorPigEventDao.queryLastEnter(pigId);
        Integer matingCount = doctorPigEventDao.findWeanToMatingCount(pigId);
        return entryEvent.getParity() + matingCount;
    }

    /**
     * 获取哺乳母猪未断奶数量
     * @param pigId 猪id
     * @param currentParity 当前胎次
     * @return 未断奶数
     */
    public Integer getSowUnWeanCount(Long pigId, Integer currentParity) {
        return doctorPigEventDao.findUnWeanCountByParity(pigId, currentParity);
    }

    /**
     * 根据事件获取猪状态
     * @param pigEvent 状态事件
     * @return 猪状态
     */
    public static Integer getStatus(DoctorPigEvent pigEvent) {
        //1.进场
        if (Objects.equals(pigEvent.getType(), PigEvent.ENTRY.getKey())) {
            return Objects.equals(pigEvent.getKind(), DoctorPig.PigSex.SOW.getKey()) ?
                    PigStatus.Entry.getKey() :
                    PigStatus.BOAR_ENTRY.getKey();
        }

        //2.妊娠检查
        if (Objects.equals(pigEvent.getType(), PigEvent.PREG_CHECK.getKey())) {
            return Objects.equals(pigEvent.getPregCheckResult(), PregCheckResult.YANG.getKey())
                    ? PigStatus.Pregnancy.getKey() : PigStatus.KongHuai.getKey();
        }

        //3.其他事件
        return EVENT_TO_STATUS.get(pigEvent.getType());
    }
}
