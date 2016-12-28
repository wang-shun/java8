package io.terminus.doctor.move.service;

import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorGroupBatchSummaryReadService;
import io.terminus.doctor.event.util.EventUtil;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.warehouse.service.DoctorMaterialConsumeProviderReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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
    private DoctorGroupEventDao doctorGroupEventDao;
    @Autowired
    private DoctorKpiDao doctorKpiDao;
    @Autowired
    private DoctorGroupBatchSummaryReadService doctorGroupBatchSummaryReadService;
    @Autowired
    private DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;

    /**
     * 刷猪群批次总结的历史数据，groupTrack
     * @param farmId 猪场id
     */
    @Transactional
    public void flushGroupBatch(Long farmId) {
        Map<Long, List<DoctorGroupEvent>> eventMap = doctorGroupEventDao.findByFarmId(farmId).stream()
                .collect(Collectors.groupingBy(DoctorGroupEvent::getGroupId));

        doctorGroupDao.findByFarmId(farmId).forEach(group -> {
            List<DoctorGroupEvent> events = eventMap.getOrDefault(group.getId(), Collections.emptyList());
            if (Objects.equals(group.getStatus(), DoctorGroup.Status.CLOSED.getValue())) {
                handleClose(group, events);
            } else {
                handleNotClose(group, events);
            }
        });
    }

    //已关闭的猪群刷新批次总结
    private void handleClose(DoctorGroup group, List<DoctorGroupEvent> events) {
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(group.getId());
        Double material = RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null,
                null, null, null, group.getId(), null, null), 0D);
        DoctorGroupBatchSummary summary = RespHelper.orServEx(doctorGroupBatchSummaryReadService.getGroupBatchSummary(group, groupTrack, material));


        summary.setNest(groupTrack.getNest());                                       //窝数
        summary.setLiveCount(groupTrack.getLiveQty());                               //活仔数
        summary.setHealthCount(groupTrack.getHealthyQty());                          //健仔数
        summary.setWeakCount(groupTrack.getWeakQty());                               //弱仔数
        summary.setBirthAvgWeight(EventUtil.get2(EventUtil.getAvgWeight(groupTrack.getBirthWeight(), groupTrack.getLiveQty())));//出生均重(kg)
        summary.setWeanCount(groupTrack.getWeanQty());                               //断奶数
        summary.setUnqCount(groupTrack.getQuaQty());                                 //注意：合格数(需求变更，只需要合格数了，这里翻一下)
        summary.setWeanAvgWeight(EventUtil.get2(EventUtil.getAvgWeight(groupTrack.getWeanWeight(), groupTrack.getWeanQty())));  //断奶均重(kg)
    }

    //未关闭的刷新猪群跟踪的些许数据
    private void handleNotClose(DoctorGroup group, List<DoctorGroupEvent> groupEvents) {
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(group.getId());
        DoctorGroupTrack updateTrack = new DoctorGroupTrack();
        updateTrack.setId(groupTrack.getId());

        doctorGroupTrackDao.update(updateTrack);
    }

    /**
     * 刷新所有猪场的批次总结历史数据
     */
    public void flushGroupBatches() {
        doctorFarmDao.findAll().forEach(farm -> flushGroupBatch(farm.getId()));
    }
}
