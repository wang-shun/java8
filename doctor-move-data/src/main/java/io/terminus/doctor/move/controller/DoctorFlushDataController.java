package io.terminus.doctor.move.controller;

import com.google.common.collect.Lists;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.move.util.JsonFormatUtils;
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


    public static void main(String[] args){
        String json = "{\"group\":{\"id\":3290,\"orgId\":97,\"orgName\":\"湖北新今农农牧股份有限公司\",\"farmId\":94,\"farmName\":\"湖北新今农农牧股份有限公司\",\"groupCode\":\"后备11舍20170116\",\"openAt\":\"2017-01-16 00:00:00\",\"status\":1,\"initBarnId\":2534,\"initBarnName\":\"后备11舍\",\"currentBarnId\":2534,\"currentBarnName\":\"后备11舍\",\"pigType\":4,\"creatorId\":10403,\"creatorName\":\"cyq@hbxjn\"},\"groupEvent\":{\"id\":74662,\"orgId\":97,\"orgName\":\"湖北新今农农牧股份有限公司\",\"farmId\":94,\"farmName\":\"湖北新今农农牧股份有限公司\",\"groupId\":3290,\"groupCode\":\"后备11舍20170116\",\"eventAt\":\"2017-01-16 00:00:00\",\"type\":1,\"name\":\"新建猪群\",\"desc\":\"【手工录入】猪类型：后备猪#猪群号：后备11舍20170116#猪舍：后备11舍#性别：混合\",\"barnId\":2534,\"barnName\":\"后备11舍\",\"pigType\":4,\"isAuto\":0,\"extra\":\"{\\\"source\\\":1}\",\"extraMap\":{\"source\":1},\"creatorId\":10403,\"creatorName\":\"cyq@hbxjn\"},\"groupTrack\":{\"id\":3290,\"groupId\":3290,\"relEventId\":74662,\"sex\":2,\"quantity\":0,\"boarQty\":0,\"sowQty\":0,\"birthDate\":\"2017-01-16 00:00:00\",\"avgDayAge\":3,\"weanWeight\":0.0,\"birthWeight\":0.0,\"nest\":0,\"liveQty\":0,\"healthyQty\":0,\"weakQty\":0,\"unweanQty\":0,\"weanQty\":0,\"quaQty\":0,\"unqQty\":0,\"extraEntity\":{\"newAt\":\"2017-01-16 00:00:00\"},\"creatorId\":10403,\"creatorName\":\"cyq@hbxjn\"}}";
        DoctorGroupSnapShotInfo info = JsonFormatUtils.JSON_NON_EMPTY_MAPPER.fromJson(json, DoctorGroupSnapShotInfo.class);
        System.out.print("===============");
    }


}
