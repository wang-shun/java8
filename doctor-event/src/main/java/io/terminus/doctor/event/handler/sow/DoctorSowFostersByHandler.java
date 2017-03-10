package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorFosterByDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTransGroupEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Created by yaoqijun.
 * Date:2016-06-08
 * Email:yaoqj@terminus.io
 * Descirbe:  被拼窝的母猪事件信息的录入
 */
@Component
@Slf4j
public class DoctorSowFostersByHandler extends DoctorAbstractEventHandler {

    @Autowired
    private DoctorTransGroupEventHandler doctorTransGroupEventHandler;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorGroupDao doctorGroupDao;

    @Override
    public DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorFosterByDto fosterByDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorFosterByDto.class);
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(fosterByDto.getPigId());
        DoctorBarn doctorBarn = doctorBarnDao.findById(doctorPigTrack.getCurrentBarnId());
        checkState(Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()) ||
                (Objects.equals(doctorPigTrack.getStatus(), PigStatus.Wean.getKey()) && Objects.equals(doctorBarn.getPigType(), PigType.DELIVER_SOW.getValue())), "被拼窝母猪状态错误,被拼窝猪号:" + fosterByDto.getPigCode());

        //被拼窝数量
        Integer fosterCount = fosterByDto.getFosterByCount();
        doctorPigTrack.setUnweanQty(MoreObjects.firstNonNull(doctorPigTrack.getUnweanQty(), 0) + fosterCount);  //未断奶数
        doctorPigTrack.setWeanQty(MoreObjects.firstNonNull(doctorPigTrack.getWeanQty(), 0));    //断奶数不变

        // 修改当前的母猪状态信息
        doctorPigTrack.setStatus(PigStatus.FEED.getKey());
        return doctorPigTrack;
    }

    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack) {
        DoctorFosterByDto fosterByDto = JSON_MAPPER.fromJson(doctorPigEvent.getExtra(), DoctorFosterByDto.class);
        DoctorPigTrack fromPigTrack = doctorPigTrackDao.findByPigId(fosterByDto.getFromSowId());

        //如果不是一个猪舍的拼窝，需要转群操作
        if (!Objects.equals(doctorPigTrack.getCurrentBarnId(), fromPigTrack.getCurrentBarnId())) {
            Long groupId = groupSowEventCreate(doctorEventInfoList, doctorPigTrack, fosterByDto, doctorPigEvent);
            doctorPigTrack.setGroupId(groupId);
            doctorPigTrackDao.update(doctorPigTrack);
        }
    }

    /**
     * 对应的仔猪转群操作
     */
    private Long groupSowEventCreate(List<DoctorEventInfo> eventInfoList, DoctorPigTrack pigTrack, DoctorFosterByDto fosterByDto, DoctorPigEvent doctorPigEvent) {
        //未断奶仔猪id
        DoctorTransGroupInput input = new DoctorTransGroupInput();
        input.setSowCode(fosterByDto.getFromSowCode());
        input.setToBarnId(pigTrack.getCurrentBarnId());
        input.setToBarnName(pigTrack.getCurrentBarnName());
        List<DoctorGroup> groupList = doctorGroupDao.findByCurrentBarnId(pigTrack.getCurrentBarnId());
        if (notEmpty(groupList)) {
            input.setIsCreateGroup(IsOrNot.NO.getValue());
            DoctorGroup toGroup = groupList.get(0);
            input.setToGroupCode(toGroup.getGroupCode());
            input.setToGroupId(toGroup.getId());
        } else {
            input.setIsCreateGroup(IsOrNot.YES.getValue());
            input.setToGroupCode(grateGroupCode(pigTrack.getCurrentBarnName()));
        }

        //来源猪舍的信息，转群事件应该是来源猪群触发
        DoctorGroup fromGroup = doctorGroupDao.findById(fosterByDto.getFromGroupId());
        DoctorGroupTrack fromGroupTrack = doctorGroupTrackDao.findByGroupId(fromGroup.getId());
        input.setEventAt(DateUtil.toDateString(doctorPigEvent.getEventAt()));
        input.setEventType(GroupEventType.TRANS_GROUP.getValue());

        input.setIsAuto(IsOrNot.YES.getValue());
        input.setCreatorId(doctorPigEvent.getOperatorId());
        input.setCreatorName(doctorPigEvent.getOperatorName());
        input.setBreedId(fromGroup.getBreedId());
        input.setBreedName(fromGroup.getBreedName());
        input.setSowEvent(true);    //由母猪触发的猪群事件
        input.setSource(PigSource.LOCAL.getKey());

        input.setQuantity(fosterByDto.getFosterByCount());
        input.setBoarQty(0);
        input.setSowQty(input.getQuantity() - input.getBoarQty());
        input.setWeight(MoreObjects.firstNonNull(fosterByDto.getFosterByTotalWeight(), 0D));
        input.setAvgWeight(EventUtil.getAvgWeight(input.getWeight(), input.getQuantity()));
        input.setRelPigEventId(doctorPigEvent.getId());

        doctorTransGroupEventHandler.handle(eventInfoList, fromGroup, fromGroupTrack, input);
        if (Objects.equals(input.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            //DoctorGroup toGroup = doctorGroupDao.findByFarmIdAndGroupCode(fromGroup.getFarmId(), input.getToGroupCode());
            return input.getToGroupId();
        }
        return input.getToGroupId();
    }
}
