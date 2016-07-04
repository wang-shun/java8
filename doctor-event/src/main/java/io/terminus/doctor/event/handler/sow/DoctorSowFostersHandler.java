package io.terminus.doctor.event.handler.sow;

import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.workflow.core.Execution;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe: 注意拼窝事件关联事件信息的处理方式
 */
@Component
public class DoctorSowFostersHandler extends DoctorAbstractEventFlowHandler{

    private final DoctorGroupWriteService doctorGroupWriteService;

    private final DoctorGroupReadService doctorGroupReadService;

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Autowired
    public DoctorSowFostersHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao,
                                   DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao,
                                   DoctorRevertLogDao doctorRevertLogDao,
                                   DoctorGroupWriteService doctorGroupWriteService, DoctorGroupReadService doctorGroupReadService) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
        this.doctorGroupWriteService = doctorGroupWriteService;
        this.doctorGroupReadService = doctorGroupReadService;
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String,Object> context) {
        // 校验当前的母猪状态 status 的存在方式
        Integer currentStatus = doctorPigTrack.getStatus();
        checkState(
                Objects.equals(currentStatus, PigStatus.FEED.getKey()) ||
                        Objects.equals(currentStatus, PigStatus.Wean.getKey()), "foster.currentSowStatus.error");

        // 转群操作
        Long groupId = groupSowEventCreate(doctorPigTrack, basic, extra);

        //添加当前母猪的健崽猪的数量信息
        Map<String,Object> extraMap = doctorPigTrack.getExtraMap();
        Integer healthCount = (Integer) extraMap.get("farrowingLiveCount");
        Integer fosterCount= (Integer) extra.get("fostersCount");
        extra.put("farrowingLiveCount", healthCount + fosterCount);
        extra.put("farrowingPigletGroupId", groupId);
        doctorPigTrack.addAllExtraMap(extra);

        // 修改当前的母猪状态信息
        doctorPigTrack.setStatus(PigStatus.FEED.getKey());
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }

    /**
     * 对应的仔猪转群操作
     * @param basicInputInfoDto
     * @param extra
     */
    private Long groupSowEventCreate(DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto, Map<String,Object> extra){

        Long fosterById = Long.valueOf(extra.get("fosterSowId").toString());

        DoctorPigTrack doctorFosterByPigTrack = doctorPigTrackDao.findByPigId(fosterById);
        Map<String,Object> fosterByPigExtra = doctorFosterByPigTrack.getExtraMap();
        Long fosterByGroupId = Long.valueOf(fosterByPigExtra.get("farrowingPigletGroupId").toString());

        Map<String,Object> trackExtra = doctorFosterByPigTrack.getExtraMap();

        // 构建Input 信息
        DoctorTransGroupInput doctorTransGroupInput = new DoctorTransGroupInput();
        doctorTransGroupInput.setToBarnId(Long.valueOf(trackExtra.get("toBarnId").toString()));
        doctorTransGroupInput.setToBarnName(trackExtra.get("toBarnName").toString());
        doctorTransGroupInput.setToGroupId(Long.valueOf(trackExtra.get("farrowingPigletGroupId").toString()));
        doctorTransGroupInput.setToGroupCode(trackExtra.get("groupCode").toString());

        if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey())){
            doctorTransGroupInput.setIsCreateGroup(0);
        }else {
            doctorTransGroupInput.setIsCreateGroup(1);
        }

        doctorTransGroupInput.setQuantity(Integer.valueOf(extra.get("fostersCount").toString()));
        doctorTransGroupInput.setBoarQty(Integer.valueOf(extra.get("boarFostersCount").toString()));
        doctorTransGroupInput.setSowQty(Integer.valueOf(extra.get("sowFostersCount").toString()));

        doctorTransGroupInput.setWeight(Double.valueOf(extra.get("fosterTotalWeight").toString()));
        doctorTransGroupInput.setEventAt(DateTime.now().toString(DTF));
        doctorTransGroupInput.setIsAuto(1);
        doctorTransGroupInput.setCreatorId(basicInputInfoDto.getStaffId());
        doctorTransGroupInput.setCreatorName(basicInputInfoDto.getStaffName());

        return RespHelper.orServEx(doctorGroupWriteService.groupEventTransGroup(
                RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(fosterByGroupId)),
                doctorTransGroupInput));
    }
}
