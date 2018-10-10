package io.terminus.doctor.move.manager;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportBoar;
import io.terminus.doctor.move.dto.DoctorImportGroupEvent;
import io.terminus.doctor.move.dto.DoctorImportPigEvent;
import io.terminus.doctor.move.dto.DoctorImportSow;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.handler.DoctorSourceDataHandler;
import io.terminus.doctor.move.model.View_EventListBoar;
import io.terminus.doctor.move.model.View_EventListGain;
import io.terminus.doctor.move.model.View_EventListSow;
import io.terminus.doctor.move.tools.DoctorImportEventExecutor;
import io.terminus.doctor.move.tools.DoctorImportExcelAnalyzer;
import io.terminus.doctor.move.tools.DoctorImportInputSplitter;
import io.terminus.doctor.move.tools.DoctorMoveEventExecutor;
import io.terminus.doctor.user.dao.DoctorFarmMoveErrorDao;
import io.terminus.doctor.user.dao.PrimaryUserDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorFarmMoveError;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.event.handler.DoctorAbstractEventHandler.grateGroupCode;
import static io.terminus.doctor.move.tools.DoctorMessageConverter.assembleErrorAttach;

/**
 * Created by xjn on 17/8/4.
 * 迁移与导入
 */
@Slf4j
@Component
public class DoctorMoveAndImportManager {

    @Autowired
    private DoctorSourceDataHandler doctorSourceDataHandler;
    @Autowired
    private DoctorPigDao doctorPigDao;
    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorGroupDao doctorGroupDao;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;
    @Autowired
    private DoctorBarnDao doctorBarnDao;
    @Autowired
    private DoctorMoveEventExecutor moveEventExecutor;
    @Autowired
    private DoctorImportEventExecutor importEventExecutor;
    @Autowired
    private DoctorImportExcelAnalyzer importExcelAnalyzer;
    @Autowired
    private DoctorImportInputSplitter importInputSplitter;
    @Autowired
    private DoctorFarmMoveErrorDao doctorFarmMoveErrorDao;
    @Autowired
    private PrimaryUserDao primaryUserDao;
    @Autowired
    private SubDao subDao;

    public void movePig(Long moveId, DoctorMoveBasicData moveBasicData) {

        //获取所有猪事件的原始数据
        List<View_EventListSow> sowRawEventList = doctorSourceDataHandler.getAllRawSowEvent(moveId, moveBasicData.getDoctorFarm());
        List<View_EventListBoar> boarRawEventList = doctorSourceDataHandler.getAllRawBoarEvent(moveId, moveBasicData.getDoctorFarm());

        //按猪维度分组
        Map<String, List<View_EventListSow>> sowOutIdToRawEventMap = sowRawEventList.stream()
                .collect(Collectors.groupingBy(View_EventListSow::getPigOutId));
        Map<String, List<View_EventListBoar>> boarOutIdToRawEventMap = boarRawEventList.stream()
                .collect(Collectors.groupingBy(View_EventListBoar::getPigOutId));

        log.info("move sow total:{}, event total:{}", sowOutIdToRawEventMap.keySet().size(), sowRawEventList.size());
        log.info("move boar total:{}, event total:{}", boarOutIdToRawEventMap.keySet().size(), boarRawEventList.size());

        //循环执行事件
        try {
            rollbackPigForMove(moveBasicData.getDoctorFarm().getId());

            boarOutIdToRawEventMap.entrySet().parallelStream().forEach(entry ->
                    moveEventExecutor.executePigEvent(moveBasicData, entry.getValue()));

            Map<String, DoctorPig> boarMap = Maps.newHashMap();
            doctorPigDao.findPigsByFarmIdAndPigType(moveBasicData.getDoctorFarm().getId(), DoctorPig.PigSex.BOAR.getKey())
                    .forEach(boar -> boarMap.put(boar.getPigCode(), boar));
            moveBasicData.setBoarMap(boarMap);
            if (isNull(moveBasicData.getGroupMap())) {
                List<DoctorGroup> farrowGroupList = doctorGroupDao
                        .findByFarmIdAndPigTypeAndStatus(moveBasicData.getDoctorFarm().getId(),
                                PigType.DELIVER_SOW.getValue(), DoctorGroup.Status.CREATED.getValue());
                moveBasicData.setGroupMap(farrowGroupList.stream().collect(Collectors.toMap(DoctorGroup::getOutId, v -> v)));
            }

            sowOutIdToRawEventMap.entrySet().parallelStream().forEach(entry ->
                    moveEventExecutor.executePigEvent(moveBasicData, entry.getValue()));
        } catch (Exception e) {
            // TODO: 17/8/8 测试暂时注释
//            rollbackPig(moveBasicData.getDoctorFarm().getId());
            throw e;
        }
    }

    public void moveGroup(Long moveId, DoctorMoveBasicData moveBasicData) {
        List<View_EventListGain> allRawGroupEvent = doctorSourceDataHandler.getAllRawGroupEvent(moveId);
        List<View_EventListGain> newEventList = doctorSourceDataHandler.getAllRawNewGroupEvent(allRawGroupEvent);
        List<View_EventListGain> excludeNewList = doctorSourceDataHandler.getAllRawGroupEventExcludeNew(allRawGroupEvent);

        Map<String, List<View_EventListGain>> groupOutIdToRawEventMap = excludeNewList.stream()
                .collect(Collectors.groupingBy(View_EventListGain::getGroupOutId));

        log.info("move group total:{}, event total:{}", newEventList.size(), allRawGroupEvent.size());

        try {
            rollbackGroupForMove(moveBasicData.getDoctorFarm().getId());

            moveEventExecutor.executeNewGroupEvent(moveBasicData, newEventList);
            List<DoctorGroup> groupList = doctorGroupDao.findByFarmId(moveBasicData.getDoctorFarm().getId());
            Map<String, DoctorGroup> groupMap = groupList.stream().collect(Collectors.toMap(DoctorGroup::getOutId, v -> v));
            moveBasicData.setGroupMap(groupMap);

            groupOutIdToRawEventMap.entrySet().parallelStream().forEach(entry ->
                    moveEventExecutor.executeGroupEvent(moveBasicData, entry.getValue()));
        } catch (Exception e) {
            // TODO: 17/8/8 测试暂时注释
//            rollbackGroup(moveBasicData.getDoctorFarm().getId());
            throw e;
        }
    }

    public void importPig(Sheet boarSheet, Sheet sowSheet, DoctorImportBasicData importBasicData) {
        try {
            List<DoctorImportBoar> importBoarList = importExcelAnalyzer.getImportBoar(boarSheet);
            List<DoctorImportPigEvent> importBoarEventList = importInputSplitter.splitForBoar(importBoarList);
            log.info("boar pig event total:{}", importBoarEventList.size());
            importBoarEventList.forEach(importPigEvent ->
                    importEventExecutor.executePigEvent(importBasicData, importPigEvent));
            List<DoctorPig> boarList = doctorPigDao.getPigSexList(importBasicData.getDoctorFarm().getId(),
                    DoctorPig.PigSex.BOAR);
            if (!Arguments.isNullOrEmpty(boarList)) {
                importBasicData.setDefaultMateBoar(boarList.get(0));
            }
        } catch (InvalidException e) {
            e.setAttach(assembleErrorAttach(e.getAttach(), boarSheet.getSheetName()));
            throw e;
        }

        try {
            List<DoctorImportSow> importSowList = importExcelAnalyzer.getImportSow(sowSheet);
            beforeImportPigEvent(importBasicData, importSowList);
            List<DoctorImportPigEvent> importSowEventList = importInputSplitter.splitForSow(importSowList, importBasicData);
            log.info("sow pig event total:{}", importSowEventList.size());
            importSowEventList.forEach(importPigEvent ->
                    importEventExecutor.executePigEvent(importBasicData, importPigEvent));
            afterImportPigEvent(importBasicData);
        } catch (InvalidException e) {
            e.setAttach(assembleErrorAttach(e.getAttach(), sowSheet.getSheetName()));
            throw e;
        }
    }

    public void importGroup(Sheet groupSheet, DoctorImportBasicData importBasicData) {
        try {
            List<DoctorImportGroupEvent> importEventList = importInputSplitter
                    .splitForGroup(importExcelAnalyzer.getImportGroup(groupSheet));
            log.info("group event total:{}", importEventList.size());
            importEventList.forEach(importGroupEvent ->
                    importEventExecutor.executeGroupEvent(importBasicData, importGroupEvent));
        } catch (InvalidException e) {
            e.setAttach(assembleErrorAttach(e.getAttach(), groupSheet.getSheetName()));
            throw e;
        }
    }

    /**
     * 获取猪场主账户个人信息
     * @param farmId 猪场id
     * @return 个人信息
     */
    public PrimaryUser getPrimaryUser(Long farmId) {
        return primaryUserDao.findPrimaryByFarmId(farmId);
    }

    private void beforeImportPigEvent(DoctorImportBasicData importBasicData,
                                      List<DoctorImportSow> importSowList) {
        if (Arguments.isNullOrEmpty(importSowList)) {
            return;
        }
        importBasicData.setDefaultPregBarn(getDefaultPregBarn(importBasicData.getDoctorFarm()));
        importBasicData.setDefaultFarrowBarn(getDefaultFarrowBarn(importBasicData.getDoctorFarm()));
        importBasicData.getBarnMap().put(importBasicData.getDefaultFarrowBarn().getName(),
                importBasicData.getDefaultFarrowBarn());
        importBasicData.getBarnMap().put(importBasicData.getDefaultPregBarn().getName(),
                importBasicData.getDefaultPregBarn());
        initFarrowGroup(importBasicData, importSowList);
    }

    private void afterImportPigEvent(DoctorImportBasicData importBasicData) {
        DoctorBarn defaultFarrowBarn = importBasicData.getDefaultFarrowBarn();
        if (notNull(defaultFarrowBarn)) {
            //删除虚拟猪群(group track event)
            List<DoctorGroup> defaultGroupList = doctorGroupDao.findByCurrentBarnId(defaultFarrowBarn.getId());
            if (!Arguments.isNullOrEmpty(defaultGroupList)) {
                DoctorGroup defaultGroup = defaultGroupList.get(0);
                doctorGroupEventDao.deleteByGroupId(defaultGroup.getId());
                doctorGroupTrackDao.deleteByGroupId(defaultGroup.getId());
                doctorGroupDao.delete(defaultGroup.getId());
            }

            //将初始化的产房置为已删除
            DoctorBarn updateBarn = new DoctorBarn();
            updateBarn.setId(defaultFarrowBarn.getId());
            updateBarn.setStatus(DoctorBarn.Status.CLOSE.getValue());
            doctorBarnDao.update(updateBarn);

            //将初始化的妊娠舍置为已删除
            DoctorBarn updateBarn1 = new DoctorBarn();
            updateBarn1.setId(importBasicData.getDefaultPregBarn().getId());
            updateBarn1.setStatus(DoctorBarn.Status.CLOSE.getValue());
            doctorBarnDao.update(updateBarn1);
        }
    }

    private DoctorBarn getDefaultPregBarn(DoctorFarm farm) {
        DoctorBarn defaultPregBarn = new DoctorBarn();
        defaultPregBarn.setName("初始化妊娠舍");
        defaultPregBarn.setOrgId(farm.getOrgId());
        defaultPregBarn.setOrgName(farm.getOrgName());
        defaultPregBarn.setFarmId(farm.getId());
        defaultPregBarn.setFarmName(farm.getName());
        defaultPregBarn.setPigType(PigType.PREG_SOW.getValue());
        defaultPregBarn.setCanOpenGroup(DoctorBarn.CanOpenGroup.NO.getValue());
        defaultPregBarn.setStatus(DoctorBarn.Status.USING.getValue());
        defaultPregBarn.setCapacity(1000);
        doctorBarnDao.create(defaultPregBarn);
        return defaultPregBarn;
    }

    private DoctorBarn getDefaultFarrowBarn(DoctorFarm farm) {
        DoctorBarn defaultFarrowBarn = new DoctorBarn();
        defaultFarrowBarn.setName("初始化产房");
        defaultFarrowBarn.setOrgId(farm.getOrgId());
        defaultFarrowBarn.setOrgName(farm.getOrgName());
        defaultFarrowBarn.setFarmId(farm.getId());
        defaultFarrowBarn.setFarmName(farm.getName());
        defaultFarrowBarn.setPigType(PigType.DELIVER_SOW.getValue());
        defaultFarrowBarn.setCanOpenGroup(DoctorBarn.CanOpenGroup.YES.getValue());
        defaultFarrowBarn.setStatus(DoctorBarn.Status.USING.getValue());
        defaultFarrowBarn.setCapacity(1000);
        doctorBarnDao.create(defaultFarrowBarn);
        return defaultFarrowBarn;
    }

    private void initFarrowGroup(DoctorImportBasicData importBasicData,
                                 List<DoctorImportSow> importSowList) {
        Map<String, List<DoctorImportSow>> map = importSowList.stream().filter(importSow ->
                !Strings.isNullOrEmpty(importSow.getFarrowBarnName())
                        && Objects.equals(importSow.getCurrentStatus(), PigStatus.FEED.getDesc()))
                .collect(Collectors.groupingBy(DoctorImportSow::getFarrowBarnName));
        map.entrySet().forEach(entry -> {
            Date newGroupDate = entry.getValue().stream().
                    min(Comparator.comparing(DoctorImportSow::getPregDate)).get().getPregDate();
            DoctorImportGroupEvent importGroupEvent = DoctorImportGroupEvent.builder()
                    .groupCode(grateGroupCode(entry.getKey(), newGroupDate))
                    .eventAt(newGroupDate)
                    .eventName(GroupEventType.NEW.getDesc())
                    .newBarnName(entry.getKey())
                    .sexName(DoctorGroupTrack.Sex.MIX.getDesc())
                    .source(PigSource.LOCAL.getDesc())
                    .build();
            importEventExecutor.executeGroupEvent(importBasicData, importGroupEvent);
        });
    }

    private void rollbackPigForImport(DoctorImportBasicData importBasicData) {
        Long farmId = importBasicData.getDoctorFarm().getId();
        rollbackPig(farmId);
        rollbackGroup(farmId, Lists.newArrayList(PigType.DELIVER_SOW.getValue()));
        if (notNull(importBasicData.getDefaultFarrowBarn())) {
            doctorBarnDao.delete(importBasicData.getDefaultFarrowBarn().getId());
        }
    }

    private void rollbackPigForMove(Long farmId) {
        rollbackPig(farmId);
        doctorFarmMoveErrorDao.deleteByType(DoctorFarmMoveError.TYPE.PIG.getValue());
    }

    private void rollbackPig(Long farmId) {
        //1、删除pig
        doctorPigDao.deleteByFarmId(farmId);

        //2、删除pigTrack
        doctorPigTrackDao.deleteByFarmId(farmId);

        //3、删除pigEvent
        doctorPigEventDao.deleteByFarmId(farmId);

    }

    private void rollbackGroupForMove(Long farmId) {
        //1、删除group
        doctorGroupDao.deleteByFarmId(farmId);

        //2、删除groupTrack // TODO: 17/8/23 暂不删除因为groupTrack表里没有farmId

        //3、删除groupEvent
        doctorGroupEventDao.deleteByFarmId(farmId);

        doctorFarmMoveErrorDao.deleteByType(DoctorFarmMoveError.TYPE.GROUP.getValue());
    }

    private void rollbackGroupForImport(Long farmId) {
        List<Integer> includePigTypes  = Lists.newArrayList(PigType.NURSERY_PIGLET.getValue(),
                PigType.FATTEN_PIG.getValue(), PigType.RESERVE.getValue());
        rollbackGroup(farmId, includePigTypes);
    }

    private void rollbackGroup(Long farmId, List<Integer> includePigTypes ) {
        //1、删除group
        doctorGroupDao.deleteByFarmId(farmId, includePigTypes);

        //2、删除groupTrack // TODO: 17/8/23 暂不删除因为groupTrack表里没有farmId

        //3、删除groupEvent
        doctorGroupEventDao.deleteByFarmId(farmId, includePigTypes);
    }

    // 得到导入时猪场里面的账号-公司账号（陈娟 2018-10-10）
    public Sub selectDefaultUser(Long farmId) {
        return subDao.selectDefaultUser(farmId);
    }

    // 得到记录操作人（陈娟 2018-10-10）
    public Sub findSubsByFarmIdAndStatusAndUserId(Long farmId, Integer status, Long userId) {
        return subDao.findSubsByFarmIdAndStatusAndUserId(farmId, status, userId);
    }

}
