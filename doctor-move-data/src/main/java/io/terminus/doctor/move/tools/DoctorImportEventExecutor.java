package io.terminus.doctor.move.tools;

import com.google.common.collect.Lists;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorGroupInputInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.move.builder.group.DoctorGroupEventInputBuilder;
import io.terminus.doctor.move.builder.pig.DoctorPigEventInputBuilder;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportGroupEvent;
import io.terminus.doctor.move.dto.DoctorImportPigEvent;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.common.utils.BeanMapper.map;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 17/8/25.
 * 导入事件执行器
 */
@Slf4j
@Component
public class DoctorImportEventExecutor {

    @Autowired
    private DoctorPigEventManager pigEventManager;
    @Autowired
    private DoctorGroupManager groupManager;
    @Autowired
    private DoctorGroupEventManager groupEventManager;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorGroupDao doctorGroupDao;
    @Autowired
    private DoctorBuilderFactory doctorBuilderFactory;
    @Autowired
    private DoctorEventInputValidator validator;

    public void executePigEvent(DoctorImportBasicData importBasicData, List<DoctorImportPigEvent> importPigEventList,
                                String eventName) {
        DoctorBasicInputInfoDto basicInputInfoDto = buildBasicInputInfo(importBasicData);
        DoctorPigEventInputBuilder builder = doctorBuilderFactory.getPigBuilder(eventName);
        if (isNull(builder)) {
            return;
        }
        List<BasePigEventInputDto> inputDtoList = importPigEventList.stream().map(importPigEvent ->
                builder.buildFromImport(importBasicData, importPigEvent)).collect(Collectors.toList());
        pigEventManager.batchEventsHandle(inputDtoList, basicInputInfoDto);
    }

    public void executePigEvent(DoctorImportBasicData importBasicData, DoctorImportPigEvent importPigEvent) {
        try {
            DoctorBasicInputInfoDto basicInputInfoDto = buildBasicInputInfo(importBasicData);
            DoctorPigEventInputBuilder builder = doctorBuilderFactory.getPigBuilder(importPigEvent.getEventName());
            if (isNull(builder)) {
                return;
            }
            BasePigEventInputDto inputDto = validator.valid(builder.buildFromImport(importBasicData, importPigEvent));
            pigEventManager.eventHandle(inputDto, basicInputInfoDto);
        } catch (InvalidException e) {
            e.setAttach(assembleErrorAttach(e.getAttach(), importPigEvent.getLineNumber()));
            throw e;
        }
    }

    public void executeGroupEvent(DoctorImportBasicData importBasicData, DoctorImportGroupEvent importGroupEvent) {
        try {
            DoctorGroupEventInputBuilder builder = doctorBuilderFactory.getGroupBuilder(importGroupEvent.getEventName());
            if (isNull(builder)) {
                return;
            }
            BaseGroupInput baseGroupInput = validator.valid(builder.buildFromImport(importBasicData, importGroupEvent));

            if (Objects.equals(baseGroupInput.getEventType(), GroupEventType.NEW.getValue())) {
                groupManager.createNewGroup(Lists.newArrayList(), buildGroupBasicData(importBasicData, baseGroupInput),
                        (DoctorNewGroupInput) baseGroupInput);
                return;
            }

            DoctorGroupInputInfo groupInputInfo = new DoctorGroupInputInfo(buildGroupDetail(importBasicData, importGroupEvent),
                    baseGroupInput);
            groupEventManager.batchHandleEvent(Lists.newArrayList(groupInputInfo), baseGroupInput.getEventType());

        } catch (InvalidException e) {
            e.setAttach(assembleErrorAttach(e.getAttach(), importGroupEvent.getLineNumber()));
            throw e;
        }
    }

    private DoctorGroup buildGroupBasicData(DoctorImportBasicData importBasicData, BaseGroupInput baseGroupInput) {

        DoctorNewGroupInput newGroupInput = (DoctorNewGroupInput) baseGroupInput;
        DoctorGroup group = map(newGroupInput, DoctorGroup.class);

        //设置猪场公司信息
        DoctorFarm farm = importBasicData.getDoctorFarm();
        group.setFarmId(farm.getId());
        group.setFarmName(farm.getName());
        group.setOrgId(farm.getOrgId());
        group.setOrgName(farm.getOrgName());

        return group;
    }

    private DoctorGroupDetail buildGroupDetail(DoctorImportBasicData importBasicData,
                                               DoctorImportGroupEvent importGroupEvent) {
        DoctorGroup group = doctorGroupDao.findByFarmIdAndGroupCode(importBasicData.getDoctorFarm().getId(),
                importGroupEvent.getGroupCode());
        expectTrue(notNull(group), "group");
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(group.getId());
        return new DoctorGroupDetail(group, groupTrack);
    }

    private DoctorBasicInputInfoDto buildBasicInputInfo(DoctorImportBasicData importBasicData) {
        DoctorFarm farm = importBasicData.getDoctorFarm();
        // TODO: 17/8/25 录入人暂时随便设置一个
        return DoctorBasicInputInfoDto.builder().farmId(farm.getId())
                .farmName(farm.getName())
                .orgId(farm.getOrgId())
                .orgName(farm.getOrgName())
                .staffId(-1L)
                .staffName("")
                .build();
    }

    private String assembleErrorAttach(String  attach, Integer lineNumber) {
        String line = isNull(lineNumber) ? "" : ",行号:".concat(lineNumber.toString());
        return isNull(attach) ? line : attach.concat(line);
    }
}
