package io.terminus.doctor.move.builder.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportPigEvent;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/8/4.
 * 断奶
 */
@Component
public class DoctorPigWeanInputBuilder implements DoctorPigEventInputBuilder {
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BasePigEventInputDto buildFromMove(DoctorMoveBasicData moveBasicData,
                                              View_EventListPig pigRawEvent) {
        View_EventListSow event = (View_EventListSow) pigRawEvent;

        DoctorWeanDto wean = new DoctorWeanDto();
        builderCommonOperation.fillPigEventCommonInput(wean, moveBasicData, pigRawEvent);

        wean.setPartWeanDate(event.getEventAt());           //断奶日期
        wean.setPartWeanRemark(event.getRemark());
        wean.setPartWeanPigletsCount(event.getWeanCount()); //断奶数量
        wean.setPartWeanAvgWeight(event.getWeanWeight());   //断奶平均重量
        return wean;
    }

    @Override
    public BasePigEventInputDto buildFromImport(DoctorImportBasicData importBasicData, DoctorImportPigEvent importPigEvent) {
        DoctorWeanDto wean = new DoctorWeanDto();
        builderCommonOperation.fillPigEventCommonInput(wean, importBasicData, importPigEvent);

        wean.setPartWeanDate(importPigEvent.getEventAt());           //断奶日期
        wean.setPartWeanRemark(importPigEvent.getRemark());
        wean.setFarrowingLiveCount(importPigEvent.getPartWeanPigletsCount());
        wean.setPartWeanPigletsCount(importPigEvent.getPartWeanPigletsCount()); //断奶数量
        wean.setPartWeanAvgWeight(importPigEvent.getPartWeanAvgWeight());   //断奶平均重量
        return wean;
    }
}
