package io.terminus.doctor.move.tools;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.enums.FarrowingType;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.InType;
import io.terminus.doctor.event.enums.MatingType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.util.EventUtil;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportBoar;
import io.terminus.doctor.move.dto.DoctorImportGroup;
import io.terminus.doctor.move.dto.DoctorImportGroupEvent;
import io.terminus.doctor.move.dto.DoctorImportPigEvent;
import io.terminus.doctor.move.dto.DoctorImportSow;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static io.terminus.doctor.event.enums.PigEvent.*;

/**
 * Created by xjn on 17/8/28.
 * excel 一行信息拆分成多个事件的输入
 */
@Slf4j
@Component
public class DoctorImportInputSplitter {
    private Map<String, List<PigEvent>> statusToEventMap = initStatusToEventMap();

    public List<DoctorImportPigEvent> splitForBoar(List<DoctorImportBoar> importBoarList) {
        return importBoarList.stream().map(this::split).collect(Collectors.toList());
    }

    public List<DoctorImportPigEvent> splitForSow(List<DoctorImportSow> importSowList,
                                                  DoctorImportBasicData importBasicData) {
        preTreat(importSowList, importBasicData);

        List<DoctorImportPigEvent> list = Lists.newArrayList();
        importSowList.forEach(importSow -> list.addAll(split(importSow)));
        return list;
    }

    public List<DoctorImportGroupEvent> splitForGroup(List<DoctorImportGroup> importGroupList) {

        List<DoctorImportGroupEvent> list = Lists.newArrayList();
        importGroupList.forEach(importGroup -> list.addAll(split(importGroup)));
        return list;
    }

    private DoctorImportPigEvent split(DoctorImportBoar importBoar) {
        return DoctorImportPigEvent.builder()
                .pigCode(importBoar.getBoarCode())
                .barnName(importBoar.getBarnName())
                .eventAt(importBoar.getInFarmIn())
                .eventName(PigEvent.ENTRY.getName())
                .birthday(importBoar.getBirthday())
                .pigSex(DoctorPig.PigSex.BOAR.getDesc())
                .source(importBoar.getSource())
                .breedName(importBoar.getBreedName())
                .boarType(importBoar.getBoarType())
                .build();
    }

    private List<DoctorImportPigEvent> split(DoctorImportSow importSow) {
        List<PigEvent> executeEvents = getExecuteSowEvents(importSow.getCurrentStatus(), importSow.getParityStage());
        return executeEvents.parallelStream().map(pigEvent -> {
            DoctorImportPigEvent importPigEvent = DoctorImportPigEvent.builder()
                    .pigCode(importSow.getSowCode())
                    .eventName(pigEvent.getName())
                    .pigSex(DoctorPig.PigSex.SOW.getDesc())
                    .source(PigSource.LOCAL.getDesc())
                    .birthday(importSow.getBirthDate())
                    .breedName(importSow.getBreed())
                    .parity(importSow.getParity())
                    .mateType(MatingType.MANUAL.getDesc())
                    .mateBoarCode(importSow.getBoarCode())
                    .mateOperator(importSow.getMateStaffName())
                    .farrowingType(FarrowingType.USUAL.getDesc())
                    .birthNestAvg(importSow.getNestWeight())
                    .healthyCount(EventUtil.minusInt(importSow.getLiveCount(), importSow.getWeakCount()))
                    .weakCount(nullToZero(importSow.getWeakCount()))
                    .jixingCount(nullToZero(importSow.getJixingCount()))
                    .deadCount(nullToZero(importSow.getDeadCount()))
                    .mummyCount(nullToZero(importSow.getMummyCount()))
                    .blackCount(nullToZero(importSow.getBlackCount()))
                    .partWeanPigletsCount(nullToZero(importSow.getLiveCount()))
                    .partWeanAvgWeight(EventUtil.getAvgWeight(importSow.getWeanWeight(), importSow.getWeakCount()))
                    .build();
            fill(importPigEvent, importSow, pigEvent);
            return importPigEvent;
        }).collect(Collectors.toList());
    }

    private List<DoctorImportGroupEvent> split(DoctorImportGroup importGroup) {
        List<GroupEventType> typeList = getExecuteGroupEvent();
        return typeList.parallelStream().map(eventType -> {
            DoctorImportGroupEvent importGroupEvent = DoctorImportGroupEvent.builder()
                    .groupCode(importGroup.getGroupCode())
                    .eventAt(importGroup.getNewGroupDate())
                    .eventName(eventType.getDesc())
                    .newBarnName(importGroup.getBarnName())
                    .sexName(importGroup.getSex())
                    .source(PigSource.LOCAL.getDesc())
                    .build();
            if (eventType.equals(GroupEventType.MOVE_IN)) {
                importGroupEvent.setInTypeName(InType.PIGLET.getDesc());
                importGroupEvent.setQuantity(importGroup.getLiveStock());
                importGroupEvent.setAvgDayAge(importGroup.getAvgDayAge());
                importGroupEvent.setAvgWeight(importGroup.getAvgWeight());
            }
            return importGroupEvent;
        }).collect(Collectors.toList());
    }

    private Map<String, List<PigEvent>> initStatusToEventMap() {
        Map<String, List<PigEvent>> map = Maps.newHashMap();
        map.put(PigStatus.Entry.getDesc(), Lists.newArrayList(ENTRY));
        map.put(PigStatus.Mate.getDesc(), Lists.newArrayList(ENTRY, MATING));
        map.put(PigStatus.Pregnancy.getDesc(), Lists.newArrayList(ENTRY, MATING, PREG_CHECK));
        map.put(PigStatus.KongHuai.getDesc(), Lists.newArrayList(ENTRY, MATING, PREG_CHECK));
        map.put(PigStatus.Farrow.getDesc(), Lists.newArrayList(ENTRY, MATING, PREG_CHECK, CHG_LOCATION));
        map.put(PigStatus.FEED.getDesc(), Lists.newArrayList(ENTRY, MATING, PREG_CHECK, CHG_LOCATION, FARROWING));
        map.put(PigStatus.Wean.getDesc(), Lists.newArrayList(ENTRY, MATING, PREG_CHECK, CHG_LOCATION, FARROWING, WEAN));
        return map;
    }

    private List<PigEvent> getExecuteSowEvents(String currentStatus,  Integer parityStage) {
        List<PigEvent> executeEvents = statusToEventMap.get(currentStatus);
        expectTrue(!Arguments.isNullOrEmpty(executeEvents), "not.have.execute.event", currentStatus);
        if (DoctorImportSow.ParityStage.firsts.contains(parityStage)) {
            return executeEvents;
        }
        return executeEvents.subList(1, executeEvents.size());
    }

    private List<GroupEventType> getExecuteGroupEvent() {
        return Lists.newArrayList(GroupEventType.NEW, GroupEventType.MOVE_IN);
    }

    private void fill(DoctorImportPigEvent importPigEvent, DoctorImportSow importSow, PigEvent pigEvent) {
        Date eventAt = null;
        String barnName = null;
        switch (pigEvent) {
            case ENTRY:
                eventAt = importSow.getInFarmDate();
                barnName = firstNonEmpty(importSow.getPregBarn(), importSow.getBarnName());
                break;
            case MATING:
                eventAt = importSow.getMateDate();
                barnName = firstNonEmpty(importSow.getPregBarn(), importSow.getBarnName());
                break;
            case PREG_CHECK:
                eventAt = importSow.getPregCheckDate();
                barnName = firstNonEmpty(importSow.getPregBarn(), importSow.getBarnName());
                importPigEvent.setPregCheckResult(importSow.getPregCheckResult());
                break;
            case CHG_LOCATION:
                eventAt = importSow.getPregCheckDate();
                barnName = firstNonEmpty(importSow.getPregBarn(), importSow.getBarnName());
                importPigEvent.setToBarnName(importSow.getFarrowBarnName());
                break;
            case FARROWING:
                eventAt = importSow.getPregDate();
                barnName = importSow.getFarrowBarnName();
                break;
            case WEAN:
                eventAt = importSow.getWeanDate();
                barnName = importSow.getFarrowBarnName();
                importPigEvent.setWeanToBarn(importSow.getWeanToBarn());
                break;
            default: new Date();
        }
        importPigEvent.setEventAt(eventAt);
        importPigEvent.setBarnName(barnName);
    }

    private void preTreat(List<DoctorImportSow> list, DoctorImportBasicData importBasicData) {
        Map<String, List<DoctorImportSow>> codeToImportSows = list.stream()
                .collect(Collectors.groupingBy(DoctorImportSow::getSowCode));
        codeToImportSows.entrySet().parallelStream().forEach(entry -> {
            List<DoctorImportSow> values = entry.getValue();
            for (int i = 0; i < values.size(); i++) {
                DoctorImportSow importSow = values.get(i);
                Integer parityStage;
                if (i == 0 && i != values.size() - 1) {
                    if (i != values.size() - 2) {
                        parityStage = DoctorImportSow.ParityStage.FIRST.getValue();
                    } else {
                        parityStage = DoctorImportSow.ParityStage.LAST_FIRST.getValue();
                    }
                } else if (i == 0 && i == values.size() - 1) {
                    parityStage = DoctorImportSow.ParityStage.FIRST_CURRENT.getValue();
                } else if (i != 0 && i == values.size() - 1) {
                    parityStage = DoctorImportSow.ParityStage.CURRENT.getValue();
                } else if (i != 0 && i == values.size() - 2) {
                    parityStage = DoctorImportSow.ParityStage.LAST_MIDDLE.getValue();
                } else {
                    parityStage = DoctorImportSow.ParityStage.MIDDLE.getValue();
                }
                importSow.setParityStage(parityStage);
                setDefaultValue(importSow, importBasicData);
            }
        });
    }

    private void setDefaultValue(DoctorImportSow importSow, DoctorImportBasicData importBasicData) {
        if (notNull(importSow.getMateDate())) {
            Date pregCheckDate = new DateTime(importSow.getMateDate()).plusWeeks(3).toDate();
            importSow.setPregCheckDate(pregCheckDate.after(DateTime.now().withTimeAtStartOfDay().toDate())
                    ? new Date() : pregCheckDate);
        }

        if (isNull(importSow.getInFarmDate())) {
            expectTrue(notNull(importSow.getMateDate()), "not.exist.when.not.entry");
            importSow.setInFarmDate(new DateTime(importSow.getMateDate()).minusDays(7).toDate());
        }

        if (DoctorImportSow.ParityStage.currents.contains(importSow.getParityStage())) {
            if (Objects.equals(importSow.getCurrentStatus(), PigStatus.Wean.getName())) {
                importSow.setPregBarn(importBasicData.getDefaultPregBarn().getName());
                if (Objects.equals(importSow.getFarrowBarnName(), importSow.getBarnName())) {
                    importSow.setWeanToBarn(importSow.getBarnName());
                }
            }

            if (Objects.equals(importSow.getCurrentStatus(), PigStatus.FEED.getName())) {
                importSow.setPregBarn(importBasicData.getDefaultPregBarn().getName());
            }
            importSow.setPregBarn(importSow.getBarnName());
            if (notNull(PregCheckResult.from(importSow.getCurrentStatus()))) {
                importSow.setPregCheckResult(importSow.getCurrentStatus());
                if (!Objects.equals(importSow.getCurrentStatus(), PigStatus.Pregnancy.getDesc())) {
                    importSow.setCurrentStatus(PigStatus.KongHuai.getDesc());
                }
            } else {
                importSow.setPregCheckResult(PregCheckResult.YANG.getDesc());
            }
        } else {
            importSow.setPregBarn(importBasicData.getDefaultPregBarn().getName());
            if (isNull(importSow.getPregDate())) {
                importSow.setCurrentStatus(PigStatus.KongHuai.getDesc());
                importSow.setPregCheckResult(getCheckResultByRemark(importSow.getRemark()).getDesc());
                importSow.setPregCheckDate(getCheckDateByRemark(importSow.getRemark()));
            } else {
                importSow.setCurrentStatus(PigStatus.Wean.getDesc());
                importSow.setPregCheckResult(PregCheckResult.YANG.getDesc());
                importSow.setWeanToBarn(importBasicData.getDefaultPregBarn().getName());
                importSow.setFarrowBarnName(importBasicData.getDefaultFarrowBarn().getName());
            }
        }
    }

    private Integer nullToZero(Integer value) {
        return isNull(value)? 0 : value;
    }

    private String firstNonEmpty(String first, String second) {
        if (!Strings.isNullOrEmpty(first)) {
            return first;
        }
        return second;
    }

    private static PregCheckResult getCheckResultByRemark(String remark) {
        if (remark.contains("返情")) {
            return PregCheckResult.FANQING;
        }
        if (remark.contains("流产")) {
            return PregCheckResult.LIUCHAN;
        }
        return PregCheckResult.YING;
    }

    private static Date getCheckDateByRemark(String remark) {
        try {
            return DateUtil.toDate(remark.substring(remark.length() - 10, remark.length()));
        } catch (Exception e) {
            log.error("get check date by remark failed, remark:{}, cause:{}", remark, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("获取妊娠检查日期失败，请检查：" + remark);
        }
    }
}
