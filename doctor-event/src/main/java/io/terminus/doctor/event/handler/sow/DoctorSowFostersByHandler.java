package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
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
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static io.terminus.doctor.event.constants.DoctorPigExtraConstants.EVENT_PIG_ID;

/**
 * Created by yaoqijun.
 * Date:2016-06-08
 * Email:yaoqj@terminus.io
 * Descirbe:  被拼窝的母猪事件信息的录入
 */
@Component
@Slf4j
public class DoctorSowFostersByHandler extends DoctorAbstractEventFlowHandler {

    private final DoctorGroupWriteService doctorGroupWriteService;

    private final DoctorGroupReadService doctorGroupReadService;

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    @Autowired
    public DoctorSowFostersByHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao,
                                     DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao,
                                     DoctorRevertLogDao doctorRevertLogDao,
                                     DoctorGroupWriteService doctorGroupWriteService,
                                     DoctorGroupReadService doctorGroupReadService,
                                     DoctorBarnDao doctorBarnDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao, doctorBarnDao);
        this.doctorGroupWriteService = doctorGroupWriteService;
        this.doctorGroupReadService = doctorGroupReadService;
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack,
                                                   DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {

        // 校验当前的母猪状态 status 的存在方式
        Integer currentStatus = doctorPigTrack.getStatus();
        checkState(
                Objects.equals(currentStatus, PigStatus.FEED.getKey()) ||
                        Objects.equals(currentStatus, PigStatus.Wean.getKey()), "foster.currentSowStatus.error");

        // 转群操作
        Long groupId = groupSowEventCreate(doctorPigTrack, basic, extra);

        //添加当前母猪的健崽猪的数量信息
        Integer healthCount = MoreObjects.firstNonNull(doctorPigTrack.getFarrowQty(), 0);
        Integer fosterCount = (Integer) extra.get("fostersCount");
        extra.put("farrowingLiveCount", healthCount + fosterCount);
        extra.put("farrowingPigletGroupId", groupId);
        doctorPigTrack.addAllExtraMap(extra);

        doctorPigTrack.setGroupId(groupId);  //groupId = -1 置成 NULL
        doctorPigTrack.setFarrowQty(MoreObjects.firstNonNull(doctorPigTrack.getFarrowQty(), 0) + fosterCount);  //分娩数
        doctorPigTrack.setUnweanQty(MoreObjects.firstNonNull(doctorPigTrack.getUnweanQty(), 0) + fosterCount);  //未断奶数

        // 修改当前的母猪状态信息
        doctorPigTrack.setStatus(PigStatus.FEED.getKey());
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }

    /**
     * 对应的仔猪转群操作
     *
     * @param basicInputInfoDto
     * @param extra
     */
    private Long groupSowEventCreate(DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto, Map<String, Object> extra) {

        //拼窝的数据extra
        Long fromSowId = Long.valueOf(extra.get(EVENT_PIG_ID).toString());
        DoctorPigTrack fromSowTrack = doctorPigTrackDao.findByPigId(fromSowId);
        Long fromGroupId = fromSowTrack.getGroupId();

        //被拼窝的数据extra
        Map<String, Object> toSowTrackMap = JSON_MAPPER.fromJson(doctorPigTrack.getExtra(), JSON_MAPPER.createCollectionType(Map.class, String.class, Object.class));
        Long toGroupId = Long.valueOf(toSowTrackMap.get("farrowingPigletGroupId").toString());
        String toGroupCode = String.valueOf(toSowTrackMap.get("groupCode"));

        // 构建Input 信息
        DoctorTransGroupInput doctorTransGroupInput = new DoctorTransGroupInput();
        doctorTransGroupInput.setToBarnId(doctorPigTrack.getCurrentBarnId());
        doctorTransGroupInput.setToBarnName(doctorPigTrack.getCurrentBarnName());
        doctorTransGroupInput.setToGroupId(toGroupId);
        doctorTransGroupInput.setToGroupCode(toGroupCode);

        if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey())) {
            doctorTransGroupInput.setIsCreateGroup(0);
        } else {
            doctorTransGroupInput.setIsCreateGroup(1);
        }

        doctorTransGroupInput.setQuantity(Integer.valueOf(extra.get("fostersCount").toString()));
        doctorTransGroupInput.setBoarQty(Integer.valueOf(extra.get("boarFostersCount").toString()));
        doctorTransGroupInput.setSowQty(doctorTransGroupInput.getQuantity() - doctorTransGroupInput.getBoarQty());
        doctorTransGroupInput.setWeight(Double.valueOf(extra.get("fosterTotalWeight").toString()));
        doctorTransGroupInput.setAvgWeight(doctorTransGroupInput.getWeight() / doctorTransGroupInput.getQuantity());    //均重
        doctorTransGroupInput.setEventAt(DateTime.now().toString(DTF));
        doctorTransGroupInput.setIsAuto(1);
        doctorTransGroupInput.setCreatorId(basicInputInfoDto.getStaffId());
        doctorTransGroupInput.setCreatorName(basicInputInfoDto.getStaffName());

        return RespHelper.orServEx(doctorGroupWriteService.groupEventTransGroup(
                RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(fromGroupId)),
                doctorTransGroupInput));
    }
}
