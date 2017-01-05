package io.terminus.doctor.move.service;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.CountUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupBatchSummaryDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorGroupBatchSummaryReadService;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.warehouse.service.DoctorMaterialConsumeProviderReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 猪群批次总结刷新历史数据
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/12/27
 */
@Slf4j
@Service
public class DoctorGroupBatchFlushService {

    @Autowired
    private DoctorFarmDao doctorFarmDao;
    @Autowired
    private DoctorGroupDao doctorGroupDao;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorGroupBatchSummaryReadService doctorGroupBatchSummaryReadService;
    @Autowired
    private DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;
    @Autowired
    private DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao;
    @Autowired
    private DoctorKpiDao doctorKpiDao;

    /**
     * 刷猪群批次总结的历史数据，groupTrack
     * @param farmId 猪场id
     */
    @Transactional
    public void flushGroupBatch(Long farmId, boolean all) {
        doctorGroupDao.findByFarmId(farmId).forEach(group -> {
            if (Objects.equals(group.getStatus(), DoctorGroup.Status.CLOSED.getValue())) {
                handleClose(group);
            }
            else if (all) {
                handleNotClose(group);
            }
        });
    }

    //已关闭的猪群刷新批次总结
    private void handleClose(DoctorGroup group) {
        Double material = RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null,
                null, null, null, group.getId(), null, null), 0D);
        updateGroupTrack(group.getId(), true);

        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(group.getId());
        DoctorGroupBatchSummary summary = RespHelper.orServEx(doctorGroupBatchSummaryReadService.getGroupBatchSummary(group, groupTrack, material));

        //如果各种数量是0 处理下
        correctSummary(summary);

        //创建或更新批次总结
        DoctorGroupBatchSummary exist = doctorGroupBatchSummaryDao.findByGroupId(group.getId());
        if (exist == null) {
            doctorGroupBatchSummaryDao.create(summary);
        } else {
            summary.setId(exist.getId());
            doctorGroupBatchSummaryDao.update(summary);
        }
    }

    //未关闭的刷新猪群跟踪的些许数据
    private void handleNotClose(DoctorGroup group) {
        updateGroupTrack(group.getId(), false);
    }

    //纠正总结的错误数据
    private void correctSummary(DoctorGroupBatchSummary summary) {
        if (!PigType.FARROW_TYPES.contains(summary.getPigType())) {
            return;
        }

        //关闭之前的存栏
        Integer quantity = doctorKpiDao.farrowGroupQuantityWhenClose(summary.getGroupId());
        if (summary.getLiveCount() == 0) {
            summary.setLiveCount(quantity);
            summary.setHealthCount(quantity);
            summary.setWeakCount(0);
        }
        if (summary.getWeanCount() == 0) {
            summary.setWeanCount(quantity);
            summary.setUnqCount(quantity);
        }
    }


    private void updateGroupTrack(Long groupId, boolean removal) {
        log.info("====== updateGroupTrack groupId:{}", groupId);
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(groupId);
        DoctorGroupTrack updateTrack = new DoctorGroupTrack();
        updateTrack.setId(groupTrack.getId());

        List<DoctorPigEvent> pigEvents = doctorPigEventDao.findByGroupId(groupId);
        List<DoctorPigEvent> farrowEvents = pigEvents.stream()
                .filter(event -> Objects.equals(event.getType(), PigEvent.FARROWING.getKey()))
                .collect(Collectors.toList());
        List<DoctorPigEvent> weanEvents = pigEvents.stream()
                .filter(event -> Objects.equals(event.getType(), PigEvent.WEAN.getKey()))
                .collect(Collectors.toList());

        updateTrack.setNest(farrowEvents.size());
        updateTrack.setBirthWeight(CountUtil.doubleStream(farrowEvents, e -> MoreObjects.firstNonNull(e.getFarrowWeight(), 0D)).sum());
        updateTrack.setLiveQty(CountUtil.intStream(farrowEvents, e -> MoreObjects.firstNonNull(e.getLiveCount(), 0)).sum());
        updateTrack.setHealthyQty(CountUtil.intStream(farrowEvents, e -> MoreObjects.firstNonNull(e.getHealthCount(), 0)).sum());
        updateTrack.setWeakQty(CountUtil.intStream(farrowEvents, e -> MoreObjects.firstNonNull(e.getWeakCount(), 0)).sum());

        updateTrack.setWeanWeight(CountUtil.doubleStream(weanEvents, e -> MoreObjects.firstNonNull(e.getWeanAvgWeight(), 0D) * MoreObjects.firstNonNull(e.getWeanCount(), 0)).sum());
        updateTrack.setWeanQty(CountUtil.intStream(weanEvents, e -> MoreObjects.firstNonNull(e.getWeanCount(), 0)).sum());
        updateTrack.setUnweanQty(getUnweanQty(removal, groupTrack.getQuantity(), updateTrack.getWeanQty()));

        //合格数
        updateTrack.setQuaQty(CountUtil.intStream(weanEvents, e -> {
            Integer quaQty = MoreObjects.firstNonNull(e.getHealthCount(), MoreObjects.firstNonNull(e.getWeanCount(), 0));
            Map<String, Object> extra = e.getExtraMap();
            if (extra != null && extra.containsKey("qualifiedCount") && extra.get("qualifiedCount") != null) {
                quaQty = Integer.valueOf(String.valueOf(extra.get("qualifiedCount")));
            }
            return MoreObjects.firstNonNull(quaQty, 0);
        }).sum());
        updateTrack.setUnqQty(updateTrack.getWeanQty() - updateTrack.getQuaQty());

        doctorGroupTrackDao.update(updateTrack);
    }

    //获取未断奶数量
    private int getUnweanQty(boolean removal, Integer quantity, Integer weanQty) {
        if (removal) {
            return 0;
        }
        Integer unweanQty = quantity - weanQty;
        return unweanQty < 0 ? 0 : unweanQty;
    }

    /**
     * 刷新所有猪场的批次总结历史数据
     */
    public void flushGroupBatches(boolean all) {
        doctorFarmDao.findAll().forEach(farm -> flushGroupBatch(farm.getId(), all));
    }
}
