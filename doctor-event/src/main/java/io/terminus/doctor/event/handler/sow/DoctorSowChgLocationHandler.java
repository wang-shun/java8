package io.terminus.doctor.event.handler.sow;

import com.google.common.collect.Maps;
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
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorPigEvent;
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
        DoctorBarn doctorToBarn = RespHelper.orServEx(doctorBarnReadService.findBarnById(toBarnId));
        DoctorBarn doctorFromBarn = RespHelper.orServEx(doctorBarnReadService.findBarnById(doctorPigTrack.getCurrentBarnId()));
        checkState(!isNull(toBarnId), "input.toBarnId.fail");

        doctorPigTrack.setCurrentBarnId(toBarnId);
        doctorPigTrack.setCurrentBarnName(doctorToBarn.getName());
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
        if(PigType.FARROW_TYPES.contains(doctorFromBarn.getPigType()) && PigType.FARROW_TYPES.contains(doctorToBarn.getPigType())
                && doctorPigTrack.getExtraMap().get(DoctorPigExtraKeys.farrowingPigletGroupId) != null){
            this.pigletTrans(doctorPigTrack, basic, extra, doctorToBarn);
        }

        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }

    private void pigletTrans(DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, DoctorBarn doctorToBarn){
        Long farrowingPigletGroupId = Long.valueOf(doctorPigTrack.getExtraMap().get(DoctorPigExtraKeys.farrowingPigletGroupId).toString());
        DoctorTransGroupInput input = new DoctorTransGroupInput();
        input.setToBarnId(doctorToBarn.getId());
        input.setToBarnName(doctorToBarn.getName());
        List<DoctorGroup> groupList = RespHelper.orServEx(doctorGroupReadService.findGroupByCurrentBarnId(doctorToBarn.getId()));
        if(notEmpty(groupList)){
            input.setIsCreateGroup(1);
            DoctorGroup toGroup = groupList.get(0);
            input.setToGroupId(toGroup.getId());
            input.setToGroupCode(toGroup.getGroupCode());
        }else{
            input.setIsCreateGroup(0);
        }

        DoctorGroupDetail fromGroup = RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(farrowingPigletGroupId));
        DoctorPigEvent doctorPigEvent = buildAllPigDoctorEvent(basic, extra);
        input.setEventAt(DateUtil.toDateString(basic.generateEventAtFromExtra(extra)));
        input.setIsAuto(1);
        input.setCreatorId(doctorPigEvent.getCreatorId());
        input.setCreatorName(doctorPigEvent.getCreatorName());
        input.setBreedId(fromGroup.getGroup().getBreedId());
        input.setBreedName(fromGroup.getGroup().getBreedName());
        input.setQuantity(fromGroup.getGroupTrack().getQuantity());
        input.setSowQty(fromGroup.getGroupTrack().getSowQty());
        input.setBoarQty(fromGroup.getGroupTrack().getBoarQty());
        input.setWeight(fromGroup.getGroupTrack().getWeight());
        input.setAvgWeight(fromGroup.getGroupTrack().getAvgWeight());
        input.setSource(PigSource.LOCAL.getKey());
        RespHelper.orServEx(doctorGroupWriteService.groupEventTransGroup(fromGroup, input));
    }
}
