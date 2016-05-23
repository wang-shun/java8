package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc: 猪群事件表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorGroupEventReadServiceImpl implements DoctorGroupEventReadService {

    private final DoctorGroupEventDao doctorGroupEventDao;

    @Autowired
    public DoctorGroupEventReadServiceImpl(DoctorGroupEventDao doctorGroupEventDao) {
        this.doctorGroupEventDao = doctorGroupEventDao;
    }

    @Override
    public Response<DoctorGroupEvent> findGroupEventById(Long groupEventId) {
        try {
            return Response.ok(doctorGroupEventDao.findById(groupEventId));
        } catch (Exception e) {
            log.error("find groupEvent by id failed, groupEventId:{}, cause:{}", groupEventId, Throwables.getStackTraceAsString(e));
            return Response.fail("groupEvent.find.fail");
        }
    }

    @Override
    public Response<List<DoctorGroupEvent>> findGroupEventsByFarmId(Long farmId) {
        try {
            return Response.ok(doctorGroupEventDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find groupEvent by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("groupEvent.find.fail");
        }
    }
}
