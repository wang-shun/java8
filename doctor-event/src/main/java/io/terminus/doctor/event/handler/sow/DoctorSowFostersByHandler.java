package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorFosterByDto;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTransGroupEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

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
    protected DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorFosterByDto fosterByDto = (DoctorFosterByDto) inputDto;
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(fosterByDto.getPigId());
        DoctorBarn doctorBarn = doctorBarnDao.findById(doctorPigTrack.getCurrentBarnId());
        checkState(Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()) ||
                (Objects.equals(doctorPigTrack.getStatus(), PigStatus.Wean.getKey()) && Objects.equals(doctorBarn.getPigType(), PigType.DELIVER_SOW.getValue())), "foster.currentSowStatus.error");

        //被拼窝数量
        Integer fosterCount = fosterByDto.getFosterByCount();
        doctorPigTrack.setUnweanQty(MoreObjects.firstNonNull(doctorPigTrack.getUnweanQty(), 0) + fosterCount);  //未断奶数
        doctorPigTrack.setWeanQty(MoreObjects.firstNonNull(doctorPigTrack.getWeanQty(), 0));    //断奶数不变

        // 修改当前的母猪状态信息
        doctorPigTrack.setStatus(PigStatus.FEED.getKey());
        //doctorPigTrack.addPigEvent(basic.getPigType(), pigEventId);
        return doctorPigTrack;
    }

    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        DoctorFosterByDto fosterByDto = (DoctorFosterByDto) inputDto;
        DoctorPigTrack fromPigTrack = doctorPigTrackDao.findByPigId(fosterByDto.getFromSowId());
        //如果不是一个猪舍的拼窝，需要转群操作
        if (!Objects.equals(doctorPigTrack.getCurrentBarnId(), fromPigTrack.getCurrentBarnId())) {
            Long groupId = groupSowEventCreate(doctorEventInfoList, doctorPigTrack, basic, inputDto, doctorPigEvent.getId());
            doctorPigTrack.setGroupId(groupId);
            doctorPigTrackDao.update(doctorPigTrack);
        }

    }

    /**
     * 对应的仔猪转群操作
     *
     * @param basicInputInfoDto
     */
    private Long groupSowEventCreate(List<DoctorEventInfo> eventInfoList, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto, BasePigEventInputDto inputDto, Long pigEventId) {
        DoctorFosterByDto fostersDto = (DoctorFosterByDto) inputDto;
        //拼窝的数据extra
        Long fromSowId = fostersDto.getPigId();
        DoctorPigTrack fromSowTrack = doctorPigTrackDao.findByPigId(fromSowId);
        Long fromGroupId = fromSowTrack.getGroupId();
        DoctorGroup fromGroup = doctorGroupDao.findById(fromGroupId);
        DoctorGroupTrack fromGroupTrack = doctorGroupTrackDao.findByGroupId(fromGroupId);

        // 构建Input 信息
        DoctorTransGroupInput doctorTransGroupInput = new DoctorTransGroupInput();
        doctorTransGroupInput.setToBarnId(doctorPigTrack.getCurrentBarnId());
        doctorTransGroupInput.setToBarnName(doctorPigTrack.getCurrentBarnName());
        if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey())) {
            doctorTransGroupInput.setIsCreateGroup(0);
            DoctorGroup toGroup = doctorGroupDao.findById(doctorPigTrack.getGroupId());
            doctorTransGroupInput.setToGroupId(toGroup.getId());
            doctorTransGroupInput.setToGroupCode(toGroup.getGroupCode());
        } else {
            List<DoctorGroup> groupList = doctorGroupDao.findByCurrentBarnId(doctorPigTrack.getCurrentBarnId());
            if (groupList == null || groupList.size() > 0) {
                throw new ServiceException();
            }
            if (groupList.isEmpty()) {
                doctorTransGroupInput.setIsCreateGroup(1);
                doctorTransGroupInput.setToGroupCode(generateGroupCode(doctorPigTrack.getCurrentBarnName()));
            } else {
                doctorTransGroupInput.setToGroupId(groupList.get(0).getId());
                doctorTransGroupInput.setToGroupCode(groupList.get(0).getGroupCode());
            }
        }

        doctorTransGroupInput.setQuantity(fostersDto.getFosterByCount());
        doctorTransGroupInput.setBoarQty(fostersDto.getBoarFostersByCount());
        doctorTransGroupInput.setSowQty(doctorTransGroupInput.getQuantity() - doctorTransGroupInput.getBoarQty());
        doctorTransGroupInput.setWeight(fostersDto.getFosterByTotalWeight());
        //doctorTransGroupInput.setAvgWeight(fromGroupTrack.getAV);    //均重//// TODO: 17/1/12 不知道从哪取 
        doctorTransGroupInput.setEventAt(DateUtil.toDateString(fostersDto.eventAt()));
        doctorTransGroupInput.setIsAuto(1);
        doctorTransGroupInput.setCreatorId(basicInputInfoDto.getStaffId());
        doctorTransGroupInput.setCreatorName(basicInputInfoDto.getStaffName());
        doctorTransGroupInput.setRelPigEventId(pigEventId);

        doctorTransGroupEventHandler.handle(eventInfoList, fromGroup, fromGroupTrack, doctorTransGroupInput);
        return doctorGroupDao.findByCurrentBarnId(doctorPigTrack.getCurrentBarnId()).get(0).getId();
    }

    private String generateGroupCode(String barnName) {
        return barnName + "(" +DateUtil.toDateString(new Date()) + ")";
    }
}
