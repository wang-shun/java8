package io.terminus.doctor.event.helper;

import com.google.common.collect.Maps;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.reportBi.DoctorReportBiDataSynchronize;
import io.terminus.doctor.event.reportBi.listener.DoctorReportBiReaTimeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static io.terminus.doctor.event.handler.DoctorAbstractEventHandler.IGNORE_EVENT;

/**
 * Created by xjn on 18/3/6.
 * email:xiaojiannan@terminus.io
 * 事件相关处理方法
 */
@Slf4j
@Component
public class DoctorEventBaseHelper {

    private final DoctorPigEventDao doctorPigEventDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final Date START_DATE = DateUtil.toDate("2018-03-13");
    private final DoctorReportBiDataSynchronize synchronize;
    private final CoreEventDispatcher coreEventDispatcher;


    private static  final Map<Integer, Integer> EVENT_TO_STATUS = Maps.newHashMap();

    static {
        EVENT_TO_STATUS.put(PigEvent.MATING.getKey(), PigStatus.Mate.getKey());
        EVENT_TO_STATUS.put(PigEvent.TO_FARROWING.getKey(), PigStatus.Farrow.getKey());
        EVENT_TO_STATUS.put(PigEvent.CHG_FARM_IN.getKey(), PigStatus.Farrow.getKey());
        EVENT_TO_STATUS.put(PigEvent.FOSTERS_BY.getKey(), PigStatus.FEED.getKey());
        EVENT_TO_STATUS.put(PigEvent.FARROWING.getKey(), PigStatus.FEED.getKey());
        EVENT_TO_STATUS.put(PigEvent.WEAN.getKey(), PigStatus.Wean.getKey());
    }

    @Autowired
    public DoctorEventBaseHelper(DoctorPigEventDao doctorPigEventDao, DoctorGroupEventDao doctorGroupEventDao, DoctorReportBiDataSynchronize synchronize, CoreEventDispatcher coreEventDispatcher) {
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.synchronize = synchronize;
        this.coreEventDispatcher = coreEventDispatcher;
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

        //3.离场事件
        if (Objects.equals(pigEvent.getType(), PigEvent.REMOVAL.getKey())) {
            return Objects.equals(pigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())
                    ? PigStatus.Removal.getKey() : PigStatus.BOAR_LEAVE.getKey();

        }
        //4.其他事件
        return EVENT_TO_STATUS.get(pigEvent.getType());
    }

    /**
     * 获取猪群仔猪数量
     * @param groupId 猪群id
     * @return 仔猪数量
     */
    public Integer getGroupQuantity(Long groupId) {
        return doctorGroupEventDao.getEventCount(groupId);
    }

    /**
     * 校验猪track变化后，track的数据与事件推导出的数据是否一致
     * @param newTrack
     */
    public void validTrackAfterUpdate(DoctorPigTrack newTrack) {

        //指定时间之前的数据不再校验
        if (notNull(newTrack.getCreatedAt()) && newTrack.getCreatedAt().before(START_DATE)) {
            return;
        }

        //公猪不用校验
        if (Objects.equals(newTrack.getPigType(), DoctorPig.PigSex.BOAR.getKey())) {
            return;
        }

        //校验状态
        Integer pigStatus = getCurrentStatus(newTrack.getPigId());
        expectTrue(Objects.equals(newTrack.getStatus(), pigStatus),
                "pig.status.error.after.update",
                PigStatus.from(pigStatus).getName(), PigStatus.from(newTrack.getStatus()).getName());

        //校验胎次
        Integer parity = getCurrentParity(newTrack.getPigId());
        expectTrue(Objects.equals(newTrack.getCurrentParity(), parity),
                "pig.parity.error.after.update", parity, newTrack.getCurrentParity());

        //如果是猪状态为哺乳校验未断奶数
        if (Objects.equals(newTrack.getStatus(), PigStatus.FEED.getKey())) {
            Integer unwean = getSowUnWeanCount(newTrack.getPigId(), newTrack.getCurrentParity());
            expectTrue(Objects.equals(newTrack.getUnweanQty(), unwean),
                    "pig.unwean.count.error.after.update", unwean, newTrack.getUnweanQty());
        }
    }

    /**
     * 校验猪群track更新后，猪群track的数据与事件推导出的数据是否一致
     * @param newTrack
     */
    public void validTrackAfterUpdate(DoctorGroupTrack newTrack) {
        //指定时间之前的数据不再校验
        if (notNull(newTrack.getCreatedAt()) && newTrack.getCreatedAt().before(START_DATE)) {
            return;
        }

        Integer quantity = getGroupQuantity(newTrack.getGroupId());
        expectTrue(Objects.equals(newTrack.getQuantity(), quantity),
                "group.quantity.error.after.update", quantity, newTrack.getQuantity());
    }

    /**
     * 事件删改后发送同步数据事件
     * @param farmIds 需要同步猪场id列表
     */
    public void synchronizeReportPublish(Collection<Long> farmIds) {
        log.info("synchronize report data, farmIds:{}", farmIds);
        farmIds.forEach(farmId -> {
            //异步
            String messageId = UUID.randomUUID().toString().replace("-", "");
            coreEventDispatcher.publish(new DoctorReportBiReaTimeEvent(messageId, farmId, OrzDimension.FARM.getValue()));

            //同步
//            try {
//                synchronize.synchronizeRealTimeBiData(farmId, OrzDimension.FARM.getValue());
//            } catch (Exception e) {
//                log.error("synchronize real time bi data error, farmId:{}, date:{}, cause:{}",
//                        farmId, new Date(), Throwables.getStackTraceAsString(e));
//            }
        });
    }

    /**
     * 新建事件同步数据
     * @param infos
     */
    public void synchronizeReportPublishForCreate(List<DoctorEventInfo> infos){
        try {
            if (Arguments.isNullOrEmpty(infos)) {
                return;
            }
            Map<Long, List<DoctorEventInfo>> farmIdToMap = infos.stream().collect(Collectors.groupingBy(DoctorEventInfo::getFarmId));
            synchronizeReportPublish(farmIdToMap.keySet());
        } catch (Exception e) {
            log.info("synchronize report publish for create error");
        }
    }

    /**
     * 是否是最新事件
     *
     * @param pigEvent 猪事件
     */
    public boolean isLastPigManualEvent(DoctorPigEvent pigEvent) {
        if (IGNORE_EVENT.contains(pigEvent.getType())) {
            log.info("体况，疾病，防疫，采精");
            return true;
        }

        if (Objects.equals(pigEvent.getType(), PigEvent.CHG_FARM.getKey())) {
            log.info("转场转出");
            return true;
        }

        if (Objects.equals(pigEvent.getIsAuto(), IsOrNot.YES.getValue())) {
            log.info("自动生成的事件");
            return false;
        }

        DoctorPigEvent lastEvent = doctorPigEventDao.findLastEventExcludeTypes(pigEvent.getPigId(), IGNORE_EVENT);

        if (isNull(lastEvent)) {
            log.info("lastEvent");
            return false;
        }

        //手动事件，比较是否是最新事件
        if (!Objects.equals(lastEvent.getIsAuto(), IsOrNot.YES.getValue())) {
            log.info("手动事件，比较是否是最新事件");
            return Objects.equals(lastEvent.getId(), pigEvent.getId());
        }

        //自动事件
        return lastManual(lastEvent, pigEvent.getId());
    }

    /**
     * 当最新事件是自动事件时，则向上追溯到上一个手动事件，在与原事件id比较
     * @param autoEvent 自动事件
     * @param eventId 原事件id
     * @return
     */
    private Boolean lastManual(DoctorPigEvent autoEvent, Long eventId) {
        if (isNull(autoEvent.getRelPigEventId())) {
            return false;
        }

        DoctorPigEvent preEvent = doctorPigEventDao.findById(autoEvent.getRelPigEventId());
        if (isNull(preEvent)) {
            return false;
        }

        if (!Objects.equals(preEvent.getIsAuto(), IsOrNot.YES.getValue())) {
            return Objects.equals(preEvent.getId(), eventId);
        }
        return lastManual(preEvent, eventId);
    }
}
