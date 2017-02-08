package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupJoinDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.DoctorEventOperator;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.handler.DoctorRollbackGroupEventHandler;
import io.terminus.doctor.event.handler.rollback.DoctorRollbackHandlerChain;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 猪群卡片表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
@RpcProvider
public class DoctorGroupReadServiceImpl implements DoctorGroupReadService {

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    private final DoctorGroupJoinDao doctorGroupJoinDao;

    @Autowired
    private DoctorRollbackHandlerChain doctorRollbackHandlerChain;

    @Autowired
    public DoctorGroupReadServiceImpl(DoctorGroupDao doctorGroupDao,
                                      DoctorGroupEventDao doctorGroupEventDao,
                                      DoctorGroupTrackDao doctorGroupTrackDao,
                                      DoctorGroupSnapshotDao doctorGroupSnapshotDao, DoctorGroupJoinDao doctorGroupJoinDao) {
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorGroupJoinDao = doctorGroupJoinDao;
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
    public Response<List<DoctorGroup>> findGroupByIds(List<Long> groupIds){
        try {
            return Response.ok(doctorGroupDao.findByIds(groupIds));
        } catch (Exception e) {
            log.error("find group by id failed, groupIds:{}, cause:{}", groupIds, Throwables.getStackTraceAsString(e));
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
    public Response<DoctorGroupSnapShotInfo> findGroupSnapShotInfoByGroupId(Long groupId) {
        try {
            DoctorGroup group = checkGroupExist(groupId);
            DoctorGroupTrack groupTrack = checkGroupTrackExist(groupId);
            DoctorGroupEvent event = doctorGroupEventDao.findById(groupTrack.getRelEventId());
            return Response.ok(new DoctorGroupSnapShotInfo(group, event, groupTrack));
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("find group snapshot by groupId failed, groupId:{}, cause:{}", groupId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.find.fail");
        }
    }

    @Override
    public Response<DoctorGroupSnapshot> findGroupSnapShotByToEventId(Long toEventId) {
        try {
            return Response.ok(doctorGroupSnapshotDao.findGroupSnapshotByToEventId(toEventId));
        } catch (Exception e) {
            log.error("find group snapshot by toEventId failed, toEventId:{}, cause:{}", toEventId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.snapshot.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorGroupDetail>> pagingGroup(DoctorGroupSearchDto groupSearchDto, Integer pageNo, Integer size) {
        try {
            PageInfo pageInfo = PageInfo.of(pageNo, size);
            Map<String, Object> params = JSON_MAPPER.getMapper().convertValue(groupSearchDto, Map.class);
            Paging<DoctorGroup> groupPaging = doctorGroupDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), params);
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
    public Response<Long> getGroupCount(@Valid DoctorGroupSearchDto groupSearchDto) {
        try {
            return Response.ok(doctorGroupJoinDao.getPigCount(groupSearchDto));
        } catch (Exception e) {
            log.error("get group count failed, data:{} cause:{}", groupSearchDto, Throwables.getStackTraceAsString(e));
            return Response.fail("get.group.count");
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
                    GroupEventType.TURN_SEED.getValue(),
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

    @Override
    public Response<Paging<DoctorGroupEvent>> pagingGroupEvent(Long farmId, Long groupId, Integer type, Integer pageNo, Integer size) {
        try {
            PageInfo pageInfo = PageInfo.of(pageNo, size);
            Paging<DoctorGroupEvent> paging = doctorGroupEventDao.paging(pageInfo.getOffset(), pageInfo.getLimit(),
                    MapBuilder.<String, Object>of().put("farmId", farmId).put("groupId", groupId).put("type", type).map());

            paging.setData(setExtraData(paging.getData()));
            return Response.ok(paging);
        } catch (Exception e) {
            log.error("paging group event failed, farmId:{}, groupId:{}, type:{}, pageNo:{}, size:{}, cause:{}",
                    farmId, groupId, type, pageNo, size, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.paging.fail");
        }
    }

    private List<DoctorGroupEvent> setExtraData(List<DoctorGroupEvent> events) {
        return events.stream().map(e -> {
            e.setExtraData(JSON_MAPPER.fromJson(e.getExtra(), JSON_MAPPER.createCollectionType(Map.class, String.class, Object.class)));
            return e;
        }).collect(Collectors.toList());
    }

    @Override
    public Response<DoctorGroupEvent> findGroupEventById(@NotNull(message = "evenId.not.null") Long eventId) {
        try {
            return Response.ok(doctorGroupEventDao.findById(eventId));
        } catch (Exception e) {
            log.error("find group event failed, eventId:{}, cause:{}", eventId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.find.fail");
        }
    }

    @Override
    public Response<DoctorGroupEvent> findLastEventByGroupId(Long groupId) {
        try {
            return Response.ok(doctorGroupEventDao.findLastEventByGroupId(groupId));
        } catch (Exception e) {
            log.error("find last group event failed, groupId:{}, cause:{}", groupId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.find.fail");
        }
    }

    @Override
    public Response<Boolean> checkGroupRepeat(Long farmId, String groupCode) {
        try {
            List<DoctorGroup> groups = RespHelper.orServEx(findGroupsByFarmId(farmId));
            if (groups.stream().map(DoctorGroup::getGroupCode).collect(Collectors.toList()).contains(groupCode)) {
                return Response.ok(Boolean.TRUE);
            }
            return Response.ok(Boolean.FALSE);
        } catch (Exception e) {
            log.error("check group repeat failed, farmId:{}, groupCode:{}, cause:{}",
                    farmId, groupCode, Throwables.getStackTraceAsString(e));
            return Response.fail("groupCode.check.failed");
        }
    }

    @Override
    public Response<List<DoctorGroup>> findGroupByCurrentBarnId(Long barnId) {
        try {
            return Response.ok(doctorGroupDao.findByCurrentBarnId(barnId));
        } catch (Exception e) {
            log.error("find group by current barn id failed, barnId:{}, cause:{}", barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.find.fail");
        }
    }

    @Override
    public Response<List<DoctorGroupEvent>> findGroupEventsByEventTypeAndDate(Long farmId, Integer eventType, Date startAt, Date endAt) {
        try {
            return Response.ok(doctorGroupEventDao.findGroupEventsByEventTypeAndDate(farmId, eventType, startAt, endAt));
        } catch (Exception e) {
            log.error("find group events by event type and date failed, farmId:{}, eventType:{}, startAt:{}, endAt:{}, cause:{}",
                    farmId, eventType, startAt, endAt, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.find.fail");
        }
    }

    /**
     * 根据日期区间和事件类型查询事件列表
     *
     * @param farmId    猪场id
     * @param groupCode 猪群号
     * @return 猪群
     */
    @Override
    public Response<DoctorGroup> findGroupByFarmIdAndGroupCode(Long farmId, String groupCode) {
        try {
            return Response.ok(doctorGroupDao.findByFarmIdAndGroupCode(farmId, groupCode));
        } catch (Exception e) {
            log.error("find group by farmId and groupCode failed, farmId:{}, groupCode:{}, cause:{}", farmId, groupCode, Throwables.getStackTraceAsString(e));
            return Response.fail("group.find.fail");
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

    @Override
    public Response<Long> countByBarnId(Long barnId){
        try{
            return Response.ok(doctorGroupEventDao.countByBarnId(barnId));
        }catch (Exception e) {
            log.error("count group event by barnId fail, barnId={}", barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("count.group.event.by.barn.id.fail");
        }
    }

    @Override
    public Response<Paging<DoctorGroupEvent>> queryGroupEventsByCriteria(Map<String, Object> criteira, Integer pageNo, Integer pageSize) {
        try {
            PageInfo info = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorGroupEventDao.paging(info.getOffset(), info.getLimit(), criteira));
        } catch (Exception e) {
            log.error("query.group.events.by.criteria.failed, cause {}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.group.events.by.criteria.failed");
        }
    }

    @Override
    public Response<Boolean> isLastEvent(Long groupId, Long eventId) {
        try {
            DoctorGroupEvent lastEvent = doctorGroupEventDao.findLastEventByGroupId(groupId);
            if (!Objects.equals(eventId, lastEvent.getId())) {
                return Response.ok(Boolean.FALSE);
            }
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("find group event is last event failed, groupId:{}, eventId:{}, cause:{}", groupId, eventId, Throwables.getStackTraceAsString(e));
            return Response.ok(Boolean.FALSE);
        }
    }

    @Override
    public Response<DoctorGroupEvent> canRollbackEvent(@NotNull(message = "input.groupId.empty") Long groupId) {
        try {
            DoctorGroupEvent groupEvent = doctorGroupEventDao.findLastManualEventByGroupId(groupId);
            if (groupEvent == null) {
                return Response.ok(null);
            }
            for (DoctorRollbackGroupEventHandler handler : doctorRollbackHandlerChain.getRollbackGroupEventHandlers()) {
                if (handler.canRollback(groupEvent)) {
                    return Response.ok(groupEvent);
                }
            }
            return Response.ok(null);
        } catch (Exception e) {
            log.error("can.rollback.event.failed, cause {}", Throwables.getStackTraceAsString(e));
            return Response.fail("can.rollback.event.failed");
        }
    }

    @Override
    public Response<Boolean> eventCanRollback(@NotNull(message = "input.eventId.empty") Long eventId) {
        try {
            DoctorGroupEvent groupEvent = doctorGroupEventDao.findById(eventId);
            for (DoctorRollbackGroupEventHandler handler : doctorRollbackHandlerChain.getRollbackGroupEventHandlers()) {
                if (handler.canRollback(groupEvent)) {
                    return Response.ok(Boolean.TRUE);
                }
            }
            return Response.ok(Boolean.FALSE);
        } catch (Exception e) {
            log.error("event.can.rollback.failed, eventId:{}, cause:{}", eventId, Throwables.getStackTraceAsString(e));
            return Response.fail("event can rollback failed");
        }
    }

    @Override
    public Response<List<DoctorGroupEvent>> queryAllGroupEventByGroupId(@NotNull(message = "input.groupId.empty") Long groupId) {
        try {
            return Response.ok(doctorGroupEventDao.findByGroupId(groupId));
        } catch (Exception e) {
            log.error("query.all.group.event.by.group.id.failed, cause {}", Throwables.getStackTraceAsString(e));
            return Response.fail("query all group event by group id failed");
        }
    }

    @Override
    public Response<Map<Long, Integer>> queryFattenOutBySumAt(String sumAt) {
        try {
            DateTime date = new DateTime(DateUtil.toDate(sumAt));
            if (date.isAfterNow()) {
                return Response.ok(Maps.newHashMap());
            }
            int deltaDay = DateUtil.getDeltaDays(date.withTimeAtStartOfDay().toDate(), DateTime.now().withTimeAtStartOfDay().toDate());
            List<Map<String, Object>> list = doctorGroupTrackDao.queryFattenOutBySumAt(ImmutableMap.of("sumAt", sumAt, "avgDayAge", 180 + deltaDay));
            Map<Long, Integer> fattenOutMap = Maps.newHashMap();
            list.forEach(map -> fattenOutMap.put((long)map.get("farmId"), ((BigDecimal) map.get("fattenOut")).intValue()));
            return Response.ok(fattenOutMap);
        } catch (Exception e) {
            log.error("query.fatten.out.by.sumAt.failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query fatten out by sumAt failed");
        }
    }

    @Override
    public Response<List<DoctorEventOperator>> queryOperators(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorGroupEventDao.findOperators(criteria));
        } catch (Exception e) {
            log.error("query.Operator.failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.Operator.failed");
        }
    }
}
