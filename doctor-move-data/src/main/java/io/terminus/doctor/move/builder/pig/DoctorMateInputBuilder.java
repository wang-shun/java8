package io.terminus.doctor.move.builder.pig;

import com.google.common.base.Strings;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.enums.MatingType;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportPigEvent;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 17/8/4.
 * 配种
 */
@Component
public class DoctorMateInputBuilder implements DoctorPigEventInputBuilder {
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;
    @Autowired
    private DoctorPigDao doctorPigDao;

    @Override
    public BasePigEventInputDto buildFromMove(DoctorMoveBasicData moveBasicData,
                                              View_EventListPig pigRawEvent) {
        View_EventListSow event = (View_EventListSow) pigRawEvent;
        Map<String, Long> subMap = moveBasicData.getSubMap();
        Map<String, DoctorPig> boarMap = moveBasicData.getBoarMap();

        DoctorMatingDto mating = new DoctorMatingDto();
        builderCommonOperation.fillPigEventCommonInput(mating, moveBasicData, pigRawEvent);

        mating.setMatingDate(event.getEventAt());                 // 配种日期
        mating.setOperatorName(event.getStaffName());             // 配种人员
        mating.setOperatorId(subMap.get(event.getStaffName()));
        mating.setMattingMark(event.getRemark());                  // 配种mark
        mating.setJudgePregDate(event.getFarrowDate());            //预产日期

        // 配种类型
        MatingType type = MatingType.from(event.getServiceType());
        mating.setMatingType(type == null ? null : type.getKey());

        //配种公猪
        DoctorPig matingPig = boarMap.get(event.getBoarCode());
        mating.setMatingBoarPigId(matingPig == null ? null : matingPig.getId());
        mating.setMatingBoarPigCode(event.getBoarCode());
        return mating;
    }

    @Override
    public BasePigEventInputDto buildFromImport(DoctorImportBasicData importBasicData, DoctorImportPigEvent importPigEvent) {
        DoctorMatingDto mating = new DoctorMatingDto();
        Map<String, Long> userMap = importBasicData.getUserMap();
        builderCommonOperation.fillPigEventCommonInput(mating, importBasicData, importPigEvent);
        mating.setMatingDate(importPigEvent.getEventAt());
        mating.setJudgePregDate(new DateTime(mating.getMatingDate()).plusDays(114).toDate());

        MatingType matingType = MatingType.from(importPigEvent.getMateType());
        expectTrue(notNull(matingType), "mateType.not.fund", importPigEvent.getMateType());
        mating.setMatingType(matingType.getKey());

        if (!Strings.isNullOrEmpty(importPigEvent.getMateOperator())) {
            Long operatorId = userMap.get(importPigEvent.getMateOperator());
            if (isNull(operatorId)) {
                operatorId = -1L;
            }
            mating.setOperatorId(operatorId);
            mating.setOperatorName(importPigEvent.getMateOperator());
        }

        Long mateBoarId;
        String mateBoarCode;
        if (Strings.isNullOrEmpty(importPigEvent.getMateBoarCode())) {
            DoctorPig mateBoar = expectNotNull(importBasicData.getDefaultMateBoar(), "not.has.mate.boar");
            mateBoarId = mateBoar.getId();
            mateBoarCode = mateBoar.getPigCode();
        } else {
            mateBoarCode = importPigEvent.getMateBoarCode();
            DoctorPig mateBoar = doctorPigDao.findPigByFarmIdAndPigCodeAndSex(importBasicData.getDoctorFarm().getId(),
                    importPigEvent.getMateBoarCode(), DoctorPig.PigSex.BOAR.getKey());
            if (isNull(mateBoar)) {
                mateBoarId = -1L;
            } else {
                mateBoarId = mateBoar.getId();
            }
        }
        mating.setMatingBoarPigId(mateBoarId);
        mating.setMatingBoarPigCode(mateBoarCode);
        return mating;
    }
}
