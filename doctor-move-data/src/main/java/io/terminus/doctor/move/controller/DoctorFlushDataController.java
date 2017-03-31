package io.terminus.doctor.move.controller;

import com.google.common.collect.Lists;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 16:57 2017/3/30
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/flush/data")
public class DoctorFlushDataController {

    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorGroupSnapshotDao doctorGroupSnapshotDao;


    @Autowired
    public DoctorFlushDataController(DoctorGroupTrackDao doctorGroupTrackDao,
                                     DoctorGroupSnapshotDao doctorGroupSnapshotDao) {
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
    }


    @RequestMapping(value = "/groupsnapshots")
    public void viewGroupSnapshots(@RequestParam Long groupId){
        Long newGroupId = 999999999L;
        doctorGroupTrackDao.deleteByGroupId(newGroupId);
        List<DoctorGroupSnapshot>  snapshots = doctorGroupSnapshotDao.findByGroupId(groupId);
        List<DoctorGroupTrack> tracks  = Lists.newArrayList();
        snapshots.forEach(doctorGroupSnapshot -> {
            DoctorGroupSnapShotInfo info = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.fromJson(doctorGroupSnapshot.getToInfo(), DoctorGroupSnapShotInfo.class);
            DoctorGroupTrack track = info.getGroupTrack();
            track.setGroupId(newGroupId);
            tracks.add(track);
        });
        doctorGroupTrackDao.creates(tracks);
    }
}
