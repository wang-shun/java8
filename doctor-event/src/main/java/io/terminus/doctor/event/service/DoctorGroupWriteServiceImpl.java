package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.model.DoctorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 猪群卡片表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorGroupWriteServiceImpl implements DoctorGroupWriteService {

    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorGroupManager doctorGroupManager;

    @Autowired
    public DoctorGroupWriteServiceImpl(DoctorGroupDao doctorGroupDao,
                                       DoctorGroupTrackDao doctorGroupTrackDao,
                                       DoctorGroupManager doctorGroupManager) {
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorGroupManager = doctorGroupManager;
    }

    @Override
    public Response<Long> createNewGroup(DoctorGroup group, @Valid DoctorNewGroupInput newGroupInput) {
        try {
            Long groupId = doctorGroupManager.createNewGroup(group, newGroupInput);
            return Response.ok(groupId);
        } catch (Exception e) {
            log.error("create group failed, group:{}, newGroupInput:{}, cause:{}", group, newGroupInput, Throwables.getStackTraceAsString(e));
            return Response.fail("group.create.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventAntiepidemic(DoctorGroupDetail groupDetail, @Valid DoctorAntiepidemicGroupInput antiepidemic) {
        try {
            doctorGroupManager.groupEventAntiepidemic(groupDetail.getGroup(), groupDetail.getGroupTrack(), antiepidemic);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventAntiepidemic failed, groupDetail:{}, antiepidemic:{}, cause:{}", groupDetail, antiepidemic, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventAntiepidemic failed, groupDetail:{}, antiepidemic:{}, cause:{}", groupDetail, antiepidemic, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.antiepidemic.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventChange(DoctorGroupDetail groupDetail, @Valid DoctorChangeGroupInput change) {
        try {
            doctorGroupManager.groupEventChange(groupDetail.getGroup(), groupDetail.getGroupTrack(), change);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventChange failed, groupDetail:{}, change:{}, cause:{}", groupDetail, change, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventChange failed, groupDetail:{}, change:{}, cause:{}", groupDetail, change, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.change.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventClose(DoctorGroupDetail groupDetail, @Valid DoctorCloseGroupInput close) {
        try {
            doctorGroupManager.groupEventClose(groupDetail.getGroup(), groupDetail.getGroupTrack(), close);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventClose failed, groupDetail:{}, close:{}, cause:{}", groupDetail, close, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventClose failed, groupDetail:{}, close:{}, cause:{}", groupDetail, close, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.close.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventDisease(DoctorGroupDetail groupDetail, @Valid DoctorDiseaseGroupInput disease) {
        try {
            doctorGroupManager.groupEventDisease(groupDetail.getGroup(), groupDetail.getGroupTrack(), disease);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventDisease failed, groupDetail:{}, disease:{}, cause:{}", groupDetail, disease, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventDisease failed, groupDetail:{}, disease:{}, cause:{}", groupDetail, disease, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.disease.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventLiveStock(DoctorGroupDetail groupDetail, @Valid DoctorLiveStockGroupInput liveStock) {
        try {
            doctorGroupManager.groupEventLiveStock(groupDetail.getGroup(), groupDetail.getGroupTrack(), liveStock);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventLiveStock failed, groupDetail:{}, liveStock:{}, cause:{}", groupDetail, liveStock, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventLiveStock failed, groupDetail:{}, liveStock:{}, cause:{}", groupDetail, liveStock, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.liveStock.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventMoveIn(DoctorGroupDetail groupDetail, @Valid DoctorMoveInGroupInput moveIn) {
        try {
            doctorGroupManager.groupEventMoveIn(groupDetail.getGroup(), groupDetail.getGroupTrack(), moveIn);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventMoveIn failed, groupDetail:{}, moveIn:{}, cause:{}", groupDetail, moveIn, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventMoveIn failed, groupDetail:{}, moveIn:{}, cause:{}", groupDetail, moveIn, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.moveIn.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventTransFarm(DoctorGroupDetail groupDetail, @Valid DoctorTransFarmGroupInput transFarm) {
        try {
            doctorGroupManager.groupEventTransFarm(groupDetail.getGroup(), groupDetail.getGroupTrack(), transFarm);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventTransFarm failed, groupDetail:{}, transFarm:{}, cause:{}", groupDetail, transFarm, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventTransFarm failed, groupDetail:{}, transFarm:{}, cause:{}", groupDetail, transFarm, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.transFarm.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventTransGroup(DoctorGroupDetail groupDetail, @Valid DoctorTransGroupInput transGroup) {
        try {
            doctorGroupManager.groupEventTransGroup(groupDetail.getGroup(), groupDetail.getGroupTrack(), transGroup);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventTransGroup failed, groupDetail:{}, transGroup:{}, cause:{}", groupDetail, transGroup, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventTransGroup failed, groupDetail:{}, transGroup:{}, cause:{}", groupDetail, transGroup, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.transGroup.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventTurnSeed(DoctorGroupDetail groupDetail, @Valid DoctorTurnSeedGroupInput turnSeed) {
        try {
            // TODO: 16/5/31 商品猪转种猪不能手工录入???

            //发布统计事件
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventTurnSeed failed, groupDetail:{}, turnSeed:{}, cause:{}", groupDetail, turnSeed, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventTurnSeed failed, groupDetail:{}, turnSeed:{}, cause:{}", groupDetail, turnSeed, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.turnSeed.fail");
        }
    }

    @Override
    public Response<Boolean> incrDayAge() {
        try {
            List<DoctorGroup> groups = doctorGroupDao.fingByStatus(DoctorGroup.Status.CREATED.getValue());
            if (notEmpty(groups)) {
                doctorGroupTrackDao.incrDayAge(groups.stream().map(DoctorGroup::getId).collect(Collectors.toList()));
            }
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("incr day age failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("incr.group.dayAge.fail");
        }
    }
}
