package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
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
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.workflow.core.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static io.terminus.common.utils.Arguments.notEmpty;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-31
 * Email:yaoqj@terminus.io
 * Descirbe: 对应的母猪状态信息流转(转舍)
 */
@Component
public class DoctorSowChgLocationHandler extends DoctorAbstractEventFlowHandler {

    private final DoctorBarnReadService doctorBarnReadService;
    private final DoctorGroupWriteService doctorGroupWriteService;
    private final DoctorGroupReadService doctorGroupReadService;

    @Autowired
    public DoctorSowChgLocationHandler(DoctorPigDao doctorPigDao,
                                       DoctorPigEventDao doctorPigEventDao,
                                       DoctorPigTrackDao doctorPigTrackDao,
                                       DoctorPigSnapshotDao doctorPigSnapshotDao,
                                       DoctorRevertLogDao doctorRevertLogDao,
                                       DoctorBarnReadService doctorBarnReadService,
                                       DoctorBarnDao doctorBarnDao,
                                       DoctorGroupWriteService doctorGroupWriteService,
                                       DoctorGroupReadService doctorGroupReadService) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao, doctorBarnDao);
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorGroupWriteService = doctorGroupWriteService;
        this.doctorGroupReadService = doctorGroupReadService;
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {

        doctorPigTrack.addAllExtraMap(extra);

        Long toBarnId = Long.valueOf(extra.get("chgLocationToBarnId").toString());
        DoctorBarn toBarn = RespHelper.orServEx(doctorBarnReadService.findBarnById(toBarnId));
        DoctorBarn fromBarn = RespHelper.orServEx(doctorBarnReadService.findBarnById(doctorPigTrack.getCurrentBarnId()));
        checkState(!isNull(toBarnId), "input.toBarnId.fail");

        doctorPigTrack.setCurrentBarnId(toBarnId);
        doctorPigTrack.setCurrentBarnName(toBarn.getName());
        doctorPigTrack.setUpdatorId(basic.getStaffId());
        doctorPigTrack.setUpdatorName(basic.getStaffName());

        // 修改对应的状态信息
        if (Objects.equals(basic.getEventType(), PigEvent.TO_MATING.getKey())) {

            Map<String, Object> newExtraMap = Maps.newHashMap();

            // 断奶后添加对应的胎次信息
            if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.Wean.getKey())) {
                // 断奶进入配种
                newExtraMap.put("hasWeanToMating", true);  // 设置断奶到配置舍标志
            }

            //清空对应的Map 信息内容 （有一次生产过程）
            doctorPigTrack.setExtraMap(newExtraMap);
            doctorPigTrack.setStatus(PigStatus.Entry.getKey());
        } else if (Objects.equals(basic.getEventType(), PigEvent.TO_PREG.getKey())) {
            // 状态妊娠检查相关， 而不是转舍相关
        } else if (Objects.equals(basic.getEventType(), PigEvent.TO_FARROWING.getKey())) {
            doctorPigTrack.setStatus(PigStatus.Farrow.getKey());
        }

        // 来源和前往都是 1 和 7 时, 仔猪也要跟着转群
        if(PigType.FARROW_TYPES.contains(fromBarn.getPigType()) && PigType.FARROW_TYPES.contains(toBarn.getPigType())
                && doctorPigTrack.getExtraMap().get(DoctorPigExtraKeys.farrowingPigletGroupId) != null){
            Long groupId = pigletTrans(doctorPigTrack, basic, extra, toBarn);

        }

        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }

    //未断奶仔猪转群
    private Long pigletTrans(DoctorPigTrack pigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, DoctorBarn doctorToBarn) {
        Map<String, Object> extraMap = JsonMapper.nonEmptyMapper().fromJson(pigTrack.getExtra(), JsonMapper.nonEmptyMapper().createCollectionType(Map.class, String.class, Object.class));

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
        input.setQuantity(checkCount(extraMap) - Integer.valueOf((String) extraMap.get(DoctorPigExtraKeys.partWeanPigletsCount), 0));
        input.setBoarQty(0);
        input.setSowQty(input.getQuantity() - input.getBoarQty());
        input.setAvgWeight(Doubles.tryParse((String) extraMap.get(DoctorPigExtraKeys.birthNestAvg)));
        input.setWeight(MoreObjects.firstNonNull(input.getAvgWeight(), 0D) * MoreObjects.firstNonNull(input.getQuantity(), 0));
        return RespHelper.orServEx(doctorGroupWriteService.groupEventTransGroup(fromGroup, input));
    }

    //校验数量是否存在
    private static Integer checkCount(Map<String, Object> extraMap) {
        if (!extraMap.containsKey(DoctorPigExtraKeys.farrowingLiveCount) || Ints.tryParse((String) extraMap.get(DoctorPigExtraKeys.farrowingLiveCount)) == null) {
            throw new ServiceException("farrow.count.not.found");
        }
        return Ints.tryParse((String) extraMap.get(DoctorPigExtraKeys.farrowingLiveCount));
    }
}
