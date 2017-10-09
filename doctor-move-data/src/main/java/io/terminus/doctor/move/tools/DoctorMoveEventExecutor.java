package io.terminus.doctor.move.tools;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.Checks;
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
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListGain;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.user.dao.DoctorFarmMoveErrorDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorFarmMoveError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.terminus.common.utils.Arguments.notNull;

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
    @Autowired
    private DoctorFarmMoveErrorDao doctorFarmMoveErrorDao;
    @Autowired
    private DoctorMessageConverter converter;

    public void executePigEvent(DoctorMoveBasicData moveBasicData, List<? extends View_EventListPig> rawEventList) {
        DoctorBasicInputInfoDto basicInputInfoDto = buildBasicInputInfo(moveBasicData);
        try {
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
                    String error;
                    if (e instanceof InvalidException) {
                        error = converter.convert((InvalidException) e).getMessage();
                    } else {
                        error = Throwables.getStackTraceAsString(e);
                    }
                    log.error("pigOutId:{}, eventOutId:{}", rawPigEvent.getPigOutId(), rawPigEvent.getEventOutId());
                    DoctorFarmMoveError doctorFarmMoveError = DoctorFarmMoveError.builder()
                            .farmName(moveBasicData.getDoctorFarm().getName())
                            .code(rawPigEvent.getPigCode())
                            .type(DoctorFarmMoveError.TYPE.PIG.getValue())
                            .outId(rawPigEvent.getPigOutId())
                            .eventName(rawPigEvent.getEventName())
                            .eventAt(rawPigEvent.getEventAt())
                            .eventOutId(rawPigEvent.getEventOutId())
                            .error(error)
                            .build();
                    doctorFarmMoveErrorDao.create(doctorFarmMoveError);
                    throw e;
                }
            });
        } catch (Exception e) {
        }
    }

    public void executeNewGroupEvent(DoctorMoveBasicData moveBasicData, List<View_EventListGain> rawEventList) {
        DoctorGroupEventInputBuilder builder = doctorBuilderFactory.getGroupBuilder(GroupEventType.NEW.getDesc());
        rawEventList.forEach(rawNewGroupEvent -> {
            try {
                BaseGroupInput newInput = builder.buildFromMove(moveBasicData, rawNewGroupEvent);
                groupManager.createNewGroup(Lists.newArrayList(), buildGroup(moveBasicData, newInput), (DoctorNewGroupInput) newInput);
            } catch (Exception e) {
                String error;
                if (e instanceof InvalidException) {
                    error = converter.convert((InvalidException) e).getMessage();
                } else {
                    error = Throwables.getStackTraceAsString(e);
                }
                DoctorFarmMoveError doctorFarmMoveError = DoctorFarmMoveError.builder()
                        .farmName(moveBasicData.getDoctorFarm().getName())
                        .code(rawNewGroupEvent.getGroupCode())
                        .type(DoctorFarmMoveError.TYPE.GROUP.getValue())
                        .outId(rawNewGroupEvent.getGroupOutId())
                        .eventName(rawNewGroupEvent.getEventTypeName())
                        .eventAt(rawNewGroupEvent.getEventAt())
                        .eventOutId(rawNewGroupEvent.getGroupEventOutId())
                        .error(error)
                        .build();
                doctorFarmMoveErrorDao.create(doctorFarmMoveError);
            }
        });
    }

    public void executeGroupEvent(DoctorMoveBasicData moveBasicData, List<View_EventListGain> rawEventList) {
        try {
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
                    String error;
                    if (e instanceof InvalidException) {
                        error = converter.convert((InvalidException) e).getMessage();
                    } else {
                        error = Throwables.getStackTraceAsString(e);
                    }
                    DoctorFarmMoveError doctorFarmMoveError = DoctorFarmMoveError.builder()
                            .farmName(moveBasicData.getDoctorFarm().getName())
                            .code(rawGroupEvent.getGroupCode())
                            .type(DoctorFarmMoveError.TYPE.GROUP.getValue())
                            .outId(rawGroupEvent.getGroupOutId())
                            .eventName(rawGroupEvent.getEventTypeName())
                            .eventAt(rawGroupEvent.getEventAt())
                            .eventOutId(rawGroupEvent.getGroupEventOutId())
                            .error(error)
                            .build();
                    doctorFarmMoveErrorDao.create(doctorFarmMoveError);
                    throw e;
                }
            });
        }catch (Exception e){}
    }

    private DoctorBasicInputInfoDto buildBasicInputInfo(DoctorMoveBasicData moveBasicData) {
        DoctorFarm farm = moveBasicData.getDoctorFarm();
        return DoctorBasicInputInfoDto.builder().farmId(farm.getId())
                .farmName(farm.getName())
                .orgId(farm.getOrgId())
                .orgName(farm.getOrgName())
                .staffId(moveBasicData.getDefaultUser().getUserId())
                .staffName(moveBasicData.getDefaultUser().getRealName())
                .build();
    }

    private DoctorGroupDetail buildGroupDetail(DoctorMoveBasicData moveBasicData,
                                               View_EventListGain rawGroupEvent) {
        DoctorGroup group = Checks.checkNotNull(moveBasicData.getGroupMap().get(rawGroupEvent.getGroupOutId()),
                "move.group.not.fund");
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(group.getId());
        return new DoctorGroupDetail(group, groupTrack);
    }


    private DoctorGroup buildGroup(DoctorMoveBasicData moveBasicData, BaseGroupInput newGroupInput) {

        DoctorGroup group = BeanMapper.map(newGroupInput, DoctorGroup.class);
        group.setRemark(null);
        //设置猪场公司信息
        DoctorFarm farm = moveBasicData.getDoctorFarm();
        group.setFarmName(farm.getName());
        group.setOrgId(farm.getOrgId());
        group.setOrgName(farm.getOrgName());
        return group;
    }

}
