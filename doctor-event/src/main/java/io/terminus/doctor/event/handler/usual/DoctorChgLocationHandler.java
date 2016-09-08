package io.terminus.doctor.event.handler.usual;

import com.google.common.base.MoreObjects;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.contants.DoctorPigExtraKeys;
import io.terminus.doctor.event.dao.DoctorBarnDao;
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
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
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

    @Autowired
    private DoctorGroupReadService doctorGroupReadService;
    @Autowired
    private DoctorGroupWriteService doctorGroupWriteService;

    @Autowired
    public DoctorChgLocationHandler(DoctorPigDao doctorPigDao,
                                    DoctorPigEventDao doctorPigEventDao,
                                    DoctorPigTrackDao doctorPigTrackDao,
                                    DoctorPigSnapshotDao doctorPigSnapshotDao,
                                    DoctorRevertLogDao doctorRevertLogDao,
                                    DoctorBarnDao doctorBarnDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
        this.doctorBarnDao = doctorBarnDao;
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

        // 来源和前往都是 1 和 7 时, 仔猪也要跟着转群
        if(PigType.FARROW_TYPES.contains(fromBarn.getPigType()) && PigType.FARROW_TYPES.contains(toBarn.getPigType())
                && extraMap.get(DoctorPigExtraKeys.farrowingPigletGroupId) != null){
            Long groupId = pigletTrans(extraMap, basic, extra, toBarn);
            extraMap.put(DoctorPigExtraKeys.farrowingPigletGroupId, groupId);
            doctorPigTrack.setExtraMap(extraMap);
        }

        doctorPigTrack.setCurrentBarnId(toBarnId);
        doctorPigTrack.setCurrentBarnName(toBarn.getName());
        doctorPigTrack.addAllExtraMap(extra);
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
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
    private Long pigletTrans(Map<String, Object> extraMap, DoctorBasicInputInfoDto basic, Map<String, Object> extra, DoctorBarn doctorToBarn) {
        //未断奶仔猪id
        Long farrowingPigletGroupId = Long.valueOf(extraMap.get(DoctorPigExtraKeys.farrowingPigletGroupId).toString());
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

        DoctorGroupDetail fromGroup = RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(farrowingPigletGroupId));
        input.setEventAt(DateUtil.toDateString(basic.generateEventAtFromExtra(extra)));
        input.setIsAuto(IsOrNot.YES.getValue());
        input.setCreatorId(basic.getStaffId());
        input.setCreatorName(basic.getStaffName());
        input.setBreedId(fromGroup.getGroup().getBreedId());
        input.setBreedName(fromGroup.getGroup().getBreedName());
        input.setSource(PigSource.LOCAL.getKey());

        //未断奶的数量 = 总 - 断奶
        input.setQuantity(checkCount(extraMap) - Integer.valueOf(String.valueOf(MoreObjects.firstNonNull(extraMap.get(DoctorPigExtraKeys.partWeanPigletsCount), 0))));
        input.setBoarQty(0);
        input.setSowQty(input.getQuantity() - input.getBoarQty());
        input.setAvgWeight(Double.valueOf(String.valueOf(MoreObjects.firstNonNull(extraMap.get(DoctorPigExtraKeys.birthNestAvg), 0D))));
        input.setWeight(MoreObjects.firstNonNull(input.getAvgWeight(), 0D) * MoreObjects.firstNonNull(input.getQuantity(), 0));
        return RespHelper.orServEx(doctorGroupWriteService.groupEventTransGroup(fromGroup, input));
    }

    //校验数量是否存在
    private static Integer checkCount(Map<String, Object> extraMap) {
        if (!extraMap.containsKey(DoctorPigExtraKeys.farrowingLiveCount) || extraMap.get(DoctorPigExtraKeys.farrowingLiveCount) == null) {
            throw new ServiceException("farrow.count.not.found");
        }
        return Integer.valueOf(String.valueOf(extraMap.get(DoctorPigExtraKeys.farrowingLiveCount)));
    }
}
