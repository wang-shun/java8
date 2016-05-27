package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc: 猪群卡片表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorGroupReadServiceImpl implements DoctorGroupReadService {

    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorGroupSnapshotDao doctorGroupSnapshotDao;

    @Autowired
    public DoctorGroupReadServiceImpl(DoctorGroupDao doctorGroupDao,
                                      DoctorGroupEventDao doctorGroupEventDao,
                                      DoctorGroupTrackDao doctorGroupTrackDao,
                                      DoctorGroupSnapshotDao doctorGroupSnapshotDao) {
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
    }

    @Override
    public Response<DoctorGroup> findGroupById(Long groupId) {
        try {
            return Response.ok(doctorGroupDao.findById(groupId));
        } catch (Exception e) {
            log.error("find group by id failed, groupId:{}, cause:{}", groupId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.find.fail");
        }
    }

    @Override
    public Response<List<DoctorGroup>> findGroupsByFarmId(Long farmId) {
        try {
            return Response.ok(doctorGroupDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find group by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.find.fail");
        }
    }

    @Override
    public Response<DoctorGroupDetail> findGroupDetailByGroupId(Long groupId) {
        try {
            return Response.ok(new DoctorGroupDetail(doctorGroupDao.findById(groupId), doctorGroupTrackDao.findByGroupId(groupId)));
        } catch (Exception e) {
            log.error("find group detail by groupId failed, groupId:{}, cause:{}", groupId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorGroup>> pagingGroup(DoctorGroupSearchDto groupSearchDto, Integer pageNo, Integer size) {
        try {
            PageInfo pageInfo = PageInfo.of(pageNo, size);
            return Response.ok(doctorGroupDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), groupSearchDto));
        } catch (Exception e) {
            log.error("paging group by groupSearchDto failed, groupSearchDto:{}, cause:{}", groupSearchDto, Throwables.getStackTraceAsString(e));
            return Response.fail("group.find.fail");
        }
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

    @Override
    public Response<DoctorGroupSnapshot> findGroupSnapshotById(Long groupSnapshotId) {
        try {
            return Response.ok(doctorGroupSnapshotDao.findById(groupSnapshotId));
        } catch (Exception e) {
            log.error("find groupSnapshot by id failed, groupSnapshotId:{}, cause:{}", groupSnapshotId, Throwables.getStackTraceAsString(e));
            return Response.fail("groupSnapshot.find.fail");
        }
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
