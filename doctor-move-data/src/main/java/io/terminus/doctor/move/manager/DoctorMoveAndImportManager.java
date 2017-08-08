package io.terminus.doctor.move.manager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.utils.Joiners;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.move.builder.group.DoctorGroupEventInputBuilder;
import io.terminus.doctor.move.builder.pig.DoctorPigEventInputBuilder;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.model.View_EventListBoar;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import io.terminus.doctor.move.model.View_SowCardList;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.doctor.common.utils.Checks.checkNotNull;

/**
 * Created by xjn on 17/8/4.
 * 迁移与导入
 */
@Slf4j
@Component
public class DoctorMoveAndImportManager {
    @Autowired
    public DoctorPigEventManager pigEventManager;
    @Autowired
    public DoctorGroupEventManager groupEventManager;
    @Autowired
    public DoctorGroupManager groupManager;
    @Autowired
    public DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    @Autowired
    private Map<String, DoctorPigEventInputBuilder> pigEventBuilderMap;
    @Autowired
    private DoctorPigDao doctorPigDao;
    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;

    public void movePig(Long moveId, DoctorMoveBasicData moveBasicData) {

        //获取所有猪事件的原始数据
        List<View_EventListSow> sowRawEventList = getAllRawSowEvent(moveId, moveBasicData.getDoctorFarm());
        List<View_EventListBoar> boarRawEventList = getAllRawBoarEvent(moveId, moveBasicData.getDoctorFarm());

        //按猪维度分组
        Map<String, List<View_EventListSow>> sowOutIdToRawEventMap = sowRawEventList.stream()
                .collect(Collectors.groupingBy(View_EventListSow::getPigCode));
        Map<String, List<View_EventListBoar>> boarOutIdToRawEventMap = boarRawEventList.stream()
                .collect(Collectors.groupingBy(View_EventListBoar::getPigCode));

        //循环执行事件
        try {
            rollbackPig(moveBasicData.getDoctorFarm().getId());

            boarOutIdToRawEventMap.entrySet().parallelStream().forEach(entry ->
                    executePigEventFromMove(moveBasicData, entry.getValue()));

            Map<String, DoctorPig> boarMap = Maps.newHashMap();
            doctorPigDao.findPigsByFarmIdAndPigType(moveBasicData.getDoctorFarm().getId(), DoctorPig.PigSex.BOAR.getKey())
                    .forEach(boar -> boarMap.put(boar.getPigCode(), boar));
            moveBasicData.setBoarMap(boarMap);

            sowOutIdToRawEventMap.entrySet().parallelStream().forEach(entry ->
                    executePigEventFromMove(moveBasicData, entry.getValue()));
        } catch (Exception e) {
            rollbackPig(moveBasicData.getDoctorFarm().getId());
            throw e;
        }
    }

    public void moveGroup() {

    }

    private void executePigEventFromMove(DoctorMoveBasicData moveBasicData, List<? extends View_EventListPig> rawEventList) {
        log.info("execute pig event from move starting, rawEventList:{}", rawEventList);
        DoctorBasicInputInfoDto basicInputInfoDto = buildBasicInputInfo(moveBasicData);
        rawEventList.forEach(rawPigEvent -> {

            //1.构建事件所需数据
            DoctorPigEventInputBuilder pigEventInputBuilder = getPigBuilder(rawPigEvent.getEventName());

            BasePigEventInputDto pigEventInputDto = pigEventInputBuilder.buildPigEventInputFromMove(moveBasicData, rawPigEvent);

            //2.执行事件
            pigEventManager.eventHandle(pigEventInputDto, basicInputInfoDto);
        });
    }

    public void executeGroupEventFromMove() {
        //1.构建事件所需数据

        //2.执行事件
    }

    public List<View_EventListBoar> getAllRawBoarEvent(Long moveId, DoctorFarm farm) {
        return RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, View_EventListBoar.class, "DoctorPigEvent-EventListBoar")).stream()
                .filter(loc -> isFarm(loc.getFarmOutId(), farm.getOutId())).collect(Collectors.toList());
    }

    public List<View_EventListSow> getAllRawSowEvent(Long moveId, DoctorFarm farm) {
        List<View_SowCardList> sowCards = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, View_SowCardList.class, "DoctorPig-SowCardList")).stream()
                .filter(loc -> isFarm(loc.getFarmOutId(), farm.getOutId()))
                .collect(Collectors.toList());

        List<View_EventListSow> sowEventViews = Lists.newArrayList();
        List<List<View_SowCardList>> sowLists = Lists.partition(sowCards, sowCards.size() / 5 + 1);
        sowLists.forEach(ss -> {
            String sowOutIds = Joiners.COMMA.join(ss.stream().map(s -> brace(s.getPigOutId())).collect(Collectors.toList()));
            sowEventViews.addAll(RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, View_EventListSow.class, "DoctorPigEvent-EventListSow", ImmutableMap.of("sowOutIds", sowOutIds))).stream()
                    .filter(loc -> isFarm(loc.getFarmOutId(), farm.getOutId()))
                    .collect(Collectors.toList())
            );
        });

        return sowEventViews;
    }

    private DoctorBasicInputInfoDto buildBasicInputInfo(DoctorMoveBasicData moveBasicData) {
        DoctorFarm farm = moveBasicData.getDoctorFarm();
        // TODO: 17/8/4 操作人暂时随便设置一个
        return DoctorBasicInputInfoDto.builder().farmId(farm.getId())
                .farmName(farm.getName())
                .orgId(farm.getOrgId())
                .orgName(farm.getOrgName())
                .staffId(-1L)
                .staffName("")
                .build();
    }


    private void rollbackPig(Long farmId) {
        //1、删除pig
        doctorPigDao.deleteByFarmId(farmId);

        //2、删除pigTrack
        doctorPigTrackDao.deleteByFarmId(farmId);

        //3、删除pigEvent
        doctorPigEventDao.deleteByFarmId(farmId);
    }

    private void rollbackGroup(Long farmId) {
        //1、删除group

        //2、删除groupTrack

        //3、删除groupEvent
    }

    private DoctorPigEventInputBuilder getPigBuilder(String eventName) {
        return checkNotNull(pigEventBuilderMap.get(eventName),
                "eventName:" + eventName);
    }

    private DoctorGroupEventInputBuilder getGroupBuilder() {
        return null;
    }

    private static boolean isFarm(String farmOID, String outId) {
        return Objects.equals(farmOID, outId);
    }

    private static String brace(String name) {
        return "'" + name + "'";
    }
}
