package io.terminus.doctor.event.handler.usual;

import com.google.common.base.MoreObjects;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTransGroupEventHandler;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.doctor.common.enums.PigType.MATING_TYPES;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorChgLocationHandler extends DoctorAbstractEventHandler{

    private final DoctorBarnDao doctorBarnDao;

    private final DoctorGroupDao doctorGroupDao;

    @Autowired
    private DoctorGroupReadService doctorGroupReadService;
    @Autowired
    private DoctorGroupEventManager doctorGroupEventManager;

    @Autowired
    public DoctorChgLocationHandler(DoctorPigDao doctorPigDao,
                                    DoctorPigEventDao doctorPigEventDao,
                                    DoctorPigTrackDao doctorPigTrackDao,
                                    DoctorPigSnapshotDao doctorPigSnapshotDao,
                                    DoctorRevertLogDao doctorRevertLogDao,
                                    DoctorBarnDao doctorBarnDao,
                                    DoctorGroupDao doctorGroupDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
        this.doctorBarnDao = doctorBarnDao;
        this.doctorGroupDao = doctorGroupDao;
    }

    @Override
    public Boolean preHandler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {
        return Objects.equals(basic.getEventType(), PigEvent.CHG_LOCATION.getKey());
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {
        Long toBarnId = Params.getWithConvert(extra,"chgLocationToBarnId",a->Long.valueOf(a.toString()));

        Map<String, Object> extraMap = JsonMapper.nonEmptyMapper().fromJson(doctorPigTrack.getExtra(),
                JsonMapper.nonEmptyMapper().createCollectionType(Map.class, String.class, Object.class));

        //校验猪舍类型是否相同, 只有同类型才可以普通转舍
        DoctorBarn fromBarn = doctorBarnDao.findById(doctorPigTrack.getCurrentBarnId());
        DoctorBarn toBarn = doctorBarnDao.findById(toBarnId);
        checkBarnTypeEqual(fromBarn, toBarn);

        Long pigEventId = (Long) context.get("doctorPigEventId");

        // 来源和前往都是 1 和 7 时, 仔猪也要跟着转群
        if(PigType.FARROW_TYPES.contains(fromBarn.getPigType()) && PigType.FARROW_TYPES.contains(toBarn.getPigType())
                && doctorPigTrack.getGroupId() != null){
            Long groupId = pigletTrans(doctorPigTrack, basic, extra, toBarn, pigEventId);
            extraMap.put("farrowingPigletGroupId", groupId);
            doctorPigTrack.setExtraMap(extraMap);
            doctorPigTrack.setGroupId(groupId);  //更新猪群id
        }

        doctorPigTrack.setCurrentBarnId(toBarnId);
        doctorPigTrack.setCurrentBarnName(toBarn.getName());
        doctorPigTrack.addAllExtraMap(extra);
        doctorPigTrack.addPigEvent(basic.getPigType(), pigEventId);
        return doctorPigTrack;
    }

    private void checkBarnTypeEqual(DoctorBarn fromBarn, DoctorBarn toBarn) {
        if (fromBarn == null || toBarn == null) {
            throw new ServiceException("barn.type.not.equal");
        }

        //配种舍 <=> 妊娠舍
        if (MATING_TYPES.contains(fromBarn.getPigType()) && MATING_TYPES.contains(toBarn.getPigType())) {
            return;
        }
        if (!Objects.equals(fromBarn.getPigType(), toBarn.getPigType())) {
            throw new ServiceException("barn.type.not.equal");
        }
    }

    //未断奶仔猪转群
    private Long pigletTrans(DoctorPigTrack pigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, DoctorBarn doctorToBarn, Long pigEventId) {
        //未断奶仔猪id
        DoctorTransGroupInput input = new DoctorTransGroupInput();
        input.setToBarnId(doctorToBarn.getId());
        input.setToBarnName(doctorToBarn.getName());
        List<DoctorGroup> groupList = RespHelper.orServEx(doctorGroupReadService.findGroupByCurrentBarnId(doctorToBarn.getId()));
        if (notEmpty(groupList)) {
            input.setIsCreateGroup(IsOrNot.NO.getValue());
            DoctorGroup toGroup = groupList.get(0);
            input.setToGroupId(toGroup.getId());
            input.setToGroupCode(toGroup.getGroupCode());
        } else {
            input.setIsCreateGroup(IsOrNot.YES.getValue());
        }

        DoctorGroupDetail fromGroup = RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(pigTrack.getGroupId()));
        input.setEventAt(DateUtil.toDateString(basic.generateEventAtFromExtra(extra)));
        input.setIsAuto(IsOrNot.YES.getValue());
        input.setCreatorId(basic.getStaffId());
        input.setCreatorName(basic.getStaffName());
        input.setBreedId(fromGroup.getGroup().getBreedId());
        input.setBreedName(fromGroup.getGroup().getBreedName());
        input.setSource(PigSource.LOCAL.getKey());
        input.setSowEvent(true);    //由母猪触发的猪群事件

        //未断奶的数量 = 总 - 断奶
        input.setQuantity(pigTrack.getUnweanQty());
        input.setBoarQty(0);
        input.setSowQty(input.getQuantity() - input.getBoarQty());
        input.setAvgWeight((MoreObjects.firstNonNull(pigTrack.getFarrowAvgWeight(), 0D)));
        input.setWeight(input.getAvgWeight() * input.getQuantity());
        input.setRelPigEventId(pigEventId);
        doctorGroupEventManager.handleEvent(fromGroup, input, DoctorTransGroupEventHandler.class);
        if (Objects.equals(input.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            DoctorGroup toGroup = doctorGroupDao.findByFarmIdAndGroupCode(fromGroup.getGroup().getFarmId(), input.getToGroupCode());
            return toGroup.getId();
        }
        return input.getToGroupId();
    }
}
