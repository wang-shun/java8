package io.terminus.doctor.move.tools;

import com.google.common.collect.Lists;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorGroupInputInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInputInfo;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.move.builder.group.DoctorGroupEventInputBuilder;
import io.terminus.doctor.move.builder.pig.DoctorPigEventInputBuilder;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListGain;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 17/8/25.
 * 迁移事件执行器
 */
@Slf4j
@Component
public class DoctorMoveEventExecutor {

    @Autowired
    private DoctorPigEventManager pigEventManager;
    @Autowired
    private DoctorGroupEventManager groupEventManager;
    @Autowired
    private DoctorGroupManager groupManager;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorBuilderFactory doctorBuilderFactory;

    public void executePigEvent(DoctorMoveBasicData moveBasicData, List<? extends View_EventListPig> rawEventList) {
        DoctorBasicInputInfoDto basicInputInfoDto = buildBasicInputInfo(moveBasicData);
        rawEventList.forEach(rawPigEvent -> {
            try {
                DoctorPigEventInputBuilder builder = doctorBuilderFactory.getPigBuilder(rawPigEvent.getEventName());
                if (notNull(builder)) {
                    //1.构建事件所需数据
                    BasePigEventInputDto pigEventInputDto = builder.buildFromMove(moveBasicData, rawPigEvent);

                    //2.执行事件
                    pigEventManager.eventHandle(pigEventInputDto, basicInputInfoDto);
                }
            } catch (Exception e) {
                log.error("pigOutId:{}, eventOutId:{}", rawPigEvent.getPigOutId(), rawPigEvent.getEventOutId());
                throw e;
            }
        });
    }

    public void executeNewGroupEvent(DoctorMoveBasicData moveBasicData, List<View_EventListGain> rawEventList) {
        DoctorGroupEventInputBuilder builder = doctorBuilderFactory.getGroupBuilder(GroupEventType.NEW.getDesc());
        List<DoctorNewGroupInputInfo> list = rawEventList.stream().map(rawNewGroupEvent -> {
            BaseGroupInput newInput = builder.buildFromMove(moveBasicData, rawNewGroupEvent);
            return new DoctorNewGroupInputInfo(buildGroup(moveBasicData, newInput), (DoctorNewGroupInput) newInput);
        }).collect(Collectors.toList());
        groupManager.batchNewGroupEventHandle(list);
    }

    public void executeGroupEvent(DoctorMoveBasicData moveBasicData, List<View_EventListGain> rawEventList) {
        rawEventList.forEach(rawGroupEvent -> {
            try {
                DoctorGroupEventInputBuilder builder = doctorBuilderFactory.getGroupBuilder(rawGroupEvent.getEventTypeName());
                if (notNull(builder)) {
                    BaseGroupInput groupInput = builder.buildFromMove(moveBasicData, rawGroupEvent);
                    //1.构建事件所需数据
                    DoctorGroupDetail groupDetail = buildGroupDetail(moveBasicData, rawGroupEvent);
                    GroupEventType groupEventType = GroupEventType.from(rawGroupEvent.getEventTypeName());

                    //2.执行事件
                    groupEventManager.batchHandleEvent(Lists.newArrayList(new DoctorGroupInputInfo(groupDetail, groupInput))
                            , groupEventType.getValue());
                }
            } catch (Exception e) {
                log.error("groupOutId:{}, eventOutId:{}", rawGroupEvent.getGroupOutId(), rawGroupEvent.getGroupEventOutId());
                throw e;
            }
        });

    }

    private DoctorBasicInputInfoDto buildBasicInputInfo(DoctorMoveBasicData moveBasicData) {
        DoctorFarm farm = moveBasicData.getDoctorFarm();
        // TODO: 17/8/4 录入人暂时随便设置一个
        return DoctorBasicInputInfoDto.builder().farmId(farm.getId())
                .farmName(farm.getName())
                .orgId(farm.getOrgId())
                .orgName(farm.getOrgName())
                .staffId(-1L)
                .staffName("")
                .build();
    }

    private DoctorGroupDetail buildGroupDetail(DoctorMoveBasicData moveBasicData,
                                               View_EventListGain rawGroupEvent) {
        DoctorGroup group = moveBasicData.getGroupMap().get(rawGroupEvent.getGroupOutId());
        if (isNull(group)) {
//            throw InvalidException();
            log.info("groupOutId:{}", rawGroupEvent.getGroupOutId());
        }
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(group.getId());
        return new DoctorGroupDetail(group, groupTrack);
    }


    private DoctorGroup buildGroup(DoctorMoveBasicData moveBasicData, BaseGroupInput newGroupInput) {

        DoctorGroup group = BeanMapper.map(newGroupInput, DoctorGroup.class);
        group.setRemark(null);
        //设置猪场公司信息
        DoctorFarm farm = moveBasicData.getDoctorFarm();
        expectTrue(notNull(farm), "farm.not.null", group.getFarmId());
        group.setFarmName(farm.getName());
        group.setOrgId(farm.getOrgId());
        group.setOrgName(farm.getOrgName());

        return group;
    }

}
