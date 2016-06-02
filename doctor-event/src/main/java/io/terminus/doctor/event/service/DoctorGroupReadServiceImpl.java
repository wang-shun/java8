package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
            return Response.ok(new DoctorGroupDetail(checkGroupExist(groupId), checkGroupTrackExist(groupId)));
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("find group detail by groupId failed, groupId:{}, cause:{}", groupId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorGroupDetail>> pagingGroup(DoctorGroupSearchDto groupSearchDto, Integer pageNo, Integer size) {
        try {
            PageInfo pageInfo = PageInfo.of(pageNo, size);
            Paging<DoctorGroup> groupPaging = doctorGroupDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), groupSearchDto);
            List<DoctorGroupDetail> groupDetails = groupPaging.getData().stream()
                    .map(group -> new DoctorGroupDetail(group, doctorGroupTrackDao.findByGroupId(group.getId())))
                    .collect(Collectors.toList());
            return Response.ok(new Paging<>(groupPaging.getTotal(), groupDetails));
        } catch (Exception e) {
            log.error("paging group by groupSearchDto failed, groupSearchDto:{}, pageNo:{}, size:{}, cause:{}",
                    groupSearchDto, pageNo, size, Throwables.getStackTraceAsString(e));
            return Response.fail("group.find.fail");
        }
    }

    @Override
    public Response<List<DoctorGroupDetail>> findGroupDetail(DoctorGroupSearchDto groupSearchDto) {
        try {
            return Response.ok(doctorGroupDao.findBySearchDto(groupSearchDto).stream()
                    .map(group -> new DoctorGroupDetail(group, doctorGroupTrackDao.findByGroupId(group.getId())))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("find group detial by groupSearchDto failed, groupSearchDto:{}, cause:{}", groupSearchDto, Throwables.getStackTraceAsString(e));
            return Response.fail("group.find.fail");
        }
    }

    @Override
    public Response<List<Integer>> findEventTypesByGroupIds(List<Long> groupIds) {
        try {
            return Response.ok(Lists.newArrayList(
                    GroupEventType.MOVE_IN.getValue(),
                    GroupEventType.CHANGE.getValue(),
                    GroupEventType.TRANS_GROUP.getValue(),
//                    GroupEventType.TURN_SEED.getValue(),  // TODO: 16/6/2  商品猪转为种猪的规则待定 
                    GroupEventType.LIVE_STOCK.getValue(),
                    GroupEventType.DISEASE.getValue(),
                    GroupEventType.ANTIEPIDEMIC.getValue(),
                    GroupEventType.TRANS_FARM.getValue(),
                    GroupEventType.CLOSE.getValue()));
        } catch (Exception e) {
            log.error("find eventType by groupIds failed, groupIds:{}, cause:{}", groupIds, Throwables.getStackTraceAsString(e));
            return Response.fail("eventType.find.fail");
        }
    }

    private DoctorGroup checkGroupExist(Long groupId) {
        DoctorGroup group = doctorGroupDao.findById(groupId);
        if (group == null) {
            throw new ServiceException("group.not.found");
        }
        return group;
    }

    private DoctorGroupTrack checkGroupTrackExist(Long groupId) {
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(groupId);
        if (groupTrack == null) {
            throw new ServiceException("group.track.not.found");
        }
        return groupTrack;
    }
}
