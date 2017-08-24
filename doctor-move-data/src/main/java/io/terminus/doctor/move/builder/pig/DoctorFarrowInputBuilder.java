package io.terminus.doctor.move.builder.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.enums.FarrowingType;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        Map<String, DoctorBarn> barnMap = moveBasicData.getBarnMap();
        Map<String, DoctorGroup> groupMap = moveBasicData.getGroupMap();

        DoctorFarrowingDto farrow = new DoctorFarrowingDto();
        builderCommonOperation.fillPigEventCommonInputFromMove(farrow, moveBasicData, pigRawEvent);

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
        farrow.setGroupId(groupMap.get(event.getToGroupOutId()).getId());
        farrow.setGroupCode(event.getToGroupCode());       // 仔猪猪群Code
        farrow.setNestCode(event.getNestCode());           // 窝号
        FarrowingType farrowingType = FarrowingType.from(event.getFarrowType());
        farrow.setFarrowingType(farrowingType == null ? null : farrowingType.getKey());

        DoctorBarn farrowBarn = barnMap.get(event.getBarnOutId());
        if (farrowBarn != null) {
            farrow.setBarnId(farrowBarn.getId());
            farrow.setBarnName(farrowBarn.getName());
        }
        return farrow;
    }
}
