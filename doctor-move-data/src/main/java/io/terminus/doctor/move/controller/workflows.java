package io.terminus.doctor.move.controller;

import io.terminus.common.model.PageInfo;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.move.handler.DoctorMoveWorkflowHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 母猪数据导入到工作流程中
 * <p>
 * 注意: 一定要等母猪的数据(包括 pig, track 以及 event 的数据全部初始化完毕 !!!)
 * <p>
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/7/27
 */
//@RestController
//@RequestMapping("/api/doctor/import")
public class workflows {

    @Autowired
    private DoctorPigDao doctorPigDao;
    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    private DoctorMoveWorkflowHandler doctorMoveWorkflowHandler;

    @RequestMapping(value = "/workflow", method = RequestMethod.GET)
    public Boolean importData() {

        // 批量获取Sow Pigs进行处理
        Integer pageNo = 1;
        Integer pageSize = 300;
        PageInfo pageInfo = new PageInfo(pageNo, pageSize);
        DoctorPig doctorPig = DoctorPig.builder().build();
        doctorPig.setPigType(DoctorPig.PIG_TYPE.SOW.getKey());

        // 处理 母猪
        List<DoctorPig> doctorPigs = doctorPigDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), doctorPig).getData();
        while (!doctorPigs.isEmpty()) {

            List<DoctorPigTrack> doctorPigTracks = doctorPigTrackDao.findByPigIds(doctorPigs.stream().map(DoctorPig::getId).collect(Collectors.toList()));
            Map<Long, DoctorPigTrack> doctorPigTrackMap = doctorPigTracks.stream().collect(Collectors.toMap(DoctorPigTrack::getPigId, v -> v));
            List<DoctorPigInfoDto> pigInfoDtos = doctorPigs.stream().map(s -> DoctorPigInfoDto.buildDoctorPigInfoDto(s, doctorPigTrackMap.get(s.getId()))).collect(Collectors.toList());

            // 迭代处理
            doctorMoveWorkflowHandler.handle(pigInfoDtos.stream().filter(pig -> !Objects.equals(pig.getStatus(), PigStatus.Removal.getKey())).collect(Collectors.toList()));

            // 页数 +1
            pageNo++;
            pageInfo = new PageInfo(pageNo, pageSize);
            doctorPigs = doctorPigDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), doctorPig).getData();
        }

        return Boolean.TRUE;
    }
}
