package io.terminus.doctor.move.builder.pig;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.enums.FarrowingType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportPigEvent;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static io.terminus.doctor.event.handler.DoctorAbstractEventHandler.grateGroupCode;

/**
 * Created by xjn on 17/8/4.
 * 分娩
 */
@Component
public class DoctorFarrowInputBuilder implements DoctorPigEventInputBuilder {
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BasePigEventInputDto buildFromMove(DoctorMoveBasicData moveBasicData,
                                              View_EventListPig pigRawEvent) {
        View_EventListSow event = (View_EventListSow) pigRawEvent;
        Map<String, DoctorGroup> groupMap = moveBasicData.getGroupMap();

        DoctorFarrowingDto farrow = new DoctorFarrowingDto();
        builderCommonOperation.fillPigEventCommonInput(farrow, moveBasicData, pigRawEvent);

        farrow.setFarrowingDate(event.getEventAt());       // 分娩日期
        farrow.setWeakCount(event.getWeakCount());         // 弱崽数量
        farrow.setMnyCount(event.getMummyCount());         // 木乃伊数量
        farrow.setJxCount(event.getJxCount());             // 畸形数量
        farrow.setDeadCount(event.getDeadCount());         // 死亡数量
        farrow.setBlackCount(event.getBlackCount());       // 黑太数量
        farrow.setHealthCount(event.getHealthyCount());    // 健仔数量
        farrow.setFarrowingLiveCount(event.getHealthyCount() + event.getWeakCount()); //活仔数 = 健 + 弱
        farrow.setFarrowRemark(event.getRemark());
        farrow.setBirthNestAvg(event.getEventWeight());    //出生窝重
        farrow.setFarrowStaff1(event.getStaffName());      //接生员1
        farrow.setFarrowStaff2(event.getStaffName());      //接生员2
        farrow.setFarrowIsSingleManager(event.getIsSingleManage());    //是否个体管理
        Long groupId = notNull(groupMap.get(event.getToGroupOutId()))
                ? groupMap.get(event.getToGroupOutId()).getId() : -2L;
        farrow.setGroupId(groupId);
        farrow.setGroupCode(event.getToGroupCode());       // 仔猪猪群Code
        farrow.setNestCode(event.getNestCode());           // 窝号
        FarrowingType farrowingType = FarrowingType.from(event.getFarrowType());
        farrow.setFarrowingType(farrowingType == null ? null : farrowingType.getKey());

        return farrow;
    }

    @Override
    public BasePigEventInputDto buildFromImport(DoctorImportBasicData importBasicData, DoctorImportPigEvent importPigEvent) {
        DoctorFarrowingDto farrow = new DoctorFarrowingDto();
        builderCommonOperation.fillPigEventCommonInput(farrow, importBasicData, importPigEvent);

        farrow.setFarrowingDate(importPigEvent.getEventAt());
        farrow.setWeakCount(importPigEvent.getWeakCount());
        farrow.setHealthCount(importPigEvent.getHealthyCount());
        farrow.setJxCount(importPigEvent.getJixingCount());
        farrow.setDeadCount(importPigEvent.getDeadCount());
        farrow.setMnyCount(importPigEvent.getMummyCount());
        farrow.setBlackCount(importPigEvent.getBlackCount());
        farrow.setFarrowingLiveCount(MoreObjects.firstNonNull(importPigEvent.getHealthyCount(),0) +
                MoreObjects.firstNonNull(importPigEvent.getWeakCount(), 0));
        farrow.setGroupCode(grateGroupCode(farrow.getBarnName(), farrow.eventAt()));                                          // 仔猪猪群Code
        farrow.setBedCode(importPigEvent.getBedCode());

        FarrowingType farrowingType = FarrowingType.from(importPigEvent.getFarrowingType());
        expectTrue(notNull(farrowingType), "farrowingType.not.fund", importPigEvent.getFarrowingType());
        farrow.setFarrowingType(farrowingType.getKey());

        return farrow;
    }
}
