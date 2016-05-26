package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪群卡片明细表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorGroupTrackReadServiceImpl implements DoctorGroupTrackReadService {

    private final DoctorGroupTrackDao doctorGroupTrackDao;

    @Autowired
    public DoctorGroupTrackReadServiceImpl(DoctorGroupTrackDao doctorGroupTrackDao) {
        this.doctorGroupTrackDao = doctorGroupTrackDao;
    }

    @Override
    public Response<DoctorGroupTrack> findGroupTrackById(Long groupTrackId) {
        try {
            return Response.ok(doctorGroupTrackDao.findById(groupTrackId));
        } catch (Exception e) {
            log.error("find groupTrack by id failed, groupTrackId:{}, cause:{}", groupTrackId, Throwables.getStackTraceAsString(e));
            return Response.fail("groupTrack.find.fail");
        }
    }

}
