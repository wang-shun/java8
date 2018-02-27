package io.terminus.doctor.move.tools;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.exception.InvalidException;
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
import org.springframework.beans.factory.annotation.Autowired;
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
import static io.terminus.doctor.move.tools.DoctorMessageConverter.assembleErrorAttach;

/**
 * Created by xjn on 17/8/28.
 * excel 一行信息拆分成多个事件的输入
 */
@Slf4j
@Component
public class DoctorImportInputSplitter {
    @Autowired
    private DoctorImportValidator validator;

    private Map<String, List<PigEvent>> statusToEventMap = initStatusToEventMap();

    public List<DoctorImportPigEvent> splitForBoar(List<DoctorImportBoar> importBoarList) {
        return importBoarList.stream().map(this::split).collect(Collectors.toList());
    }

    public List<DoctorImportPigEvent> splitForSow(List<DoctorImportSow> importSowList,
                                                  DoctorImportBasicData importBasicData) {
        preTreat(importSowList, importBasicData);

        List<DoctorImportPigEvent> list = Lists.newArrayList();
        importSowList.forEach(importSow -> list.addAll(split(validator.valid(importSow))));
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
                .origin(EventUtil.getCent(importBoar.getOrigin()))
                .build();
    }

    private List<DoctorImportPigEvent> split(DoctorImportSow importSow) {
        List<PigEvent> executeEvents = getExecuteSowEvents(importSow.getCurrentStatus(), importSow.getParityStage());
        return executeEvents.parallelStream().map(pigEvent -> {
            DoctorImportPigEvent importPigEvent = DoctorImportPigEvent.builder()
                    .lineNumber(importSow.getLineNumber())
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
                    .bedCode(importSow.getBed())
                    .birthNestAvg(nullToZero(importSow.getNestWeight()))
                    .healthyCount(nullToZero(importSow.getHealthyCount()))
                    .weakCount(nullToZero(importSow.getWeakCount()))
                    .jixingCount(nullToZero(importSow.getJixingCount()))
                    .deadCount(nullToZero(importSow.getDeadCount()))
                    .mummyCount(nullToZero(importSow.getMummyCount()))
                    .blackCount(nullToZero(importSow.getBlackCount()))
                    .partWeanPigletsCount(EventUtil.plusInt(importSow.getHealthyCount(), importSow.getWeakCount()))
                    .partWeanAvgWeight(nullToZero(importSow.getWeanWeight()))
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
                importGroupEvent.setOrigin(EventUtil.getCent(importGroup.getOrigin()));
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

    private List<PigEvent> getExecuteSowEvents(String currentStatus, Integer parityStage) {
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
                barnName = importSow.getPregBarn();
                importPigEvent.setOrigin(EventUtil.getCent(importSow.getOrigin()));
                break;
            case MATING:
                eventAt = importSow.getMateDate();
                barnName = importSow.getPregBarn();
                break;
            case PREG_CHECK:
                eventAt = importSow.getPregCheckDate();
                barnName = importSow.getPregBarn();
                importPigEvent.setPregCheckResult(importSow.getPregCheckResult());
                break;
            case CHG_LOCATION:
                eventAt = importSow.getPregCheckDate();
                barnName = importSow.getPregBarn();
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
            default:
                new Date();
        }
        importPigEvent.setEventAt(eventAt);
        importPigEvent.setBarnName(barnName);
    }

    private void preTreat(List<DoctorImportSow> list, DoctorImportBasicData importBasicData) {
        Map<String, List<DoctorImportSow>> codeToImportSows = list.stream()
                .collect(Collectors.groupingBy(DoctorImportSow::getSowCode));
        codeToImportSows
                .entrySet()
                .parallelStream()
                .forEach(entry -> {
                    List<DoctorImportSow> values = entry.getValue();
                    Integer maxParity = values.stream().mapToInt(DoctorImportSow::getParity).max().getAsInt();
                    for (int i = 0; i < values.size(); i++) {
                        DoctorImportSow importSow = values.get(i);
                        Integer parityStage;
                        if (i == 0) {
                            if (Objects.equals(importSow.getParity(), maxParity) && i == values.size() - 1) {
                                parityStage = DoctorImportSow.ParityStage.FIRST_CURRENT_LAST.getValue();
                            } else if (Objects.equals(importSow.getParity(), maxParity)) {
                                parityStage = DoctorImportSow.ParityStage.FIRST_CURRENT.getValue();
                            } else if (Objects.equals(importSow.getParity(), maxParity - 1)) {
                                parityStage = DoctorImportSow.ParityStage.FIRST_PRE.getValue();
                            } else {
                                parityStage = DoctorImportSow.ParityStage.FIRST.getValue();
                            }
                        } else if (Objects.equals(importSow.getParity(), maxParity)) {
                            if (i == values.size() - 1) {
                                parityStage = DoctorImportSow.ParityStage.CURRENT_LAST.getValue();
                            } else {
                                parityStage = DoctorImportSow.ParityStage.CURRENT.getValue();
                            }
                        } else if (Objects.equals(importSow.getParity(), maxParity - 1)) {
                            parityStage = DoctorImportSow.ParityStage.MIDDLE_PRE.getValue();
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
            expectTrue(notNull(importSow.getMateDate()), true,
                    assembleErrorAttach("", importSow.getLineNumber()), "not.exist.when.not.entry");
            importSow.setInFarmDate(new DateTime(importSow.getMateDate()).minusDays(7).toDate());
        }

        //当前导入行的母猪处于当前胎次的最后一行
        if (DoctorImportSow.ParityStage.currentLasts.contains(importSow.getParityStage())) {
            importSow.setPregBarn(importSow.getBarnName());
            importSow.setPregCheckResult(PregCheckResult.YANG.getDesc());
            if (Objects.equals(importSow.getCurrentStatus(), PigStatus.Wean.getDesc())) {
                importSow.setPregBarn(importBasicData.getDefaultPregBarn().getName());
                importSow.setFarrowBarnName(importBasicData.getDefaultFarrowBarn().getName());
                if (!Objects.equals(importSow.getFarrowBarnName(), importSow.getBarnName())) {
                    importSow.setWeanToBarn(importSow.getBarnName());
                }
            }

            if (Objects.equals(importSow.getCurrentStatus(), PigStatus.FEED.getDesc())) {
                importSow.setPregBarn(importBasicData.getDefaultPregBarn().getName());
                importSow.setFarrowBarnName(importSow.getBarnName());
            }
            if (notNull(PregCheckResult.from(importSow.getCurrentStatus()))) {
                importSow.setPregCheckResult(importSow.getCurrentStatus());
                if (!Objects.equals(importSow.getCurrentStatus(), PigStatus.Pregnancy.getDesc())) {
                    importSow.setCurrentStatus(PigStatus.KongHuai.getDesc());
                    importSow.setPregCheckDate(getCheckDateByRemark(importSow));
                } else if (importBasicData != null &&
                        importBasicData.getBarnMap() != null &&
                        importBasicData.getBarnMap().containsKey(importSow.getBarnName()) &&
                        Objects.equals(importBasicData.getBarnMap().get(importSow.getBarnName()).getPigType()
                                , PigType.DELIVER_SOW.getValue())) {
                    importSow.setPregBarn(importBasicData.getDefaultPregBarn().getName());
                    importSow.setCurrentStatus(PigStatus.Farrow.getDesc());
                    importSow.setFarrowBarnName(importSow.getBarnName());
                }
            }
          //处于当前胎次并不是最后一行
        } else if (DoctorImportSow.ParityStage.currentNotLasts.contains(importSow.getParityStage())) {
            importSow.setCurrentStatus(PigStatus.KongHuai.getDesc());
            importSow.setPregCheckResult(getCheckResultByRemark(importSow).getDesc());
            importSow.setPregCheckDate(getCheckDateByRemark(importSow));
            importSow.setPregBarn(importSow.getBarnName());
            if (importBasicData != null &&
                    importBasicData.getBarnMap() != null &&
                    importBasicData.getBarnMap().containsKey(importSow.getBarnName()) &&
                    Objects.equals(importBasicData.getBarnMap().get(importSow.getBarnName()).getPigType()
                            , PigType.DELIVER_SOW.getValue())) {
                importSow.setPregBarn(importBasicData.getDefaultPregBarn().getName());
            }
            //其他情况
        } else {
            importSow.setPregBarn(importBasicData.getDefaultPregBarn().getName());
            if (isNull(importSow.getPregDate())) {
                importSow.setCurrentStatus(PigStatus.KongHuai.getDesc());
                importSow.setPregCheckResult(getCheckResultByRemark(importSow).getDesc());
                importSow.setPregCheckDate(getCheckDateByRemark(importSow));
            } else {
                importSow.setPregCheckResult(PregCheckResult.YANG.getDesc());
                importSow.setWeanToBarn(importBasicData.getDefaultPregBarn().getName());
                importSow.setFarrowBarnName(importBasicData.getDefaultFarrowBarn().getName());
                if (DoctorImportSow.ParityStage.pres.contains(importSow.getParityStage()) &&
                        importBasicData.getBarnMap() != null &&
                        importBasicData.getBarnMap().containsKey(importSow.getBarnName()) &&
                        PigType.MATING_TYPES.contains(importBasicData.getBarnMap().get(importSow.getBarnName()).getPigType())
                        && !Objects.equals(importSow.getCurrentStatus(), PigStatus.Wean.getDesc())) {
                    importSow.setWeanToBarn(importSow.getBarnName());
                }
                importSow.setCurrentStatus(PigStatus.Wean.getDesc());
            }
        }

        if (isNull(importSow.getBirthDate())) {
            importSow.setBirthDate(new DateTime(importSow.getInFarmDate()).minusYears(1).toDate());
        }
    }

    private Integer nullToZero(Integer value) {
        return isNull(value) ? 0 : value;
    }

    private Double nullToZero(Double value) {
        return isNull(value) ? 0.0D : value;
    }

    private String firstNonEmpty(String first, String second) {
        if (!Strings.isNullOrEmpty(first)) {
            return first;
        }
        return second;
    }

    private static PregCheckResult getCheckResultByRemark(DoctorImportSow importSow) {
        String remark = importSow.getRemark();
        try {
            if (remark.contains("返情")) {
                return PregCheckResult.FANQING;
            }
            if (remark.contains("流产")) {
                return PregCheckResult.LIUCHAN;
            }
            return PregCheckResult.YING;
        } catch (Exception e) {
            throw new InvalidException(false, "get.preg.check.date.failed.from.remark",
                    assembleErrorAttach(null, importSow.getLineNumber()),
                    isNull(remark) ? "" : remark);
        }
    }

    private static Date getCheckDateByRemark(DoctorImportSow importSow) {
        String remark = importSow.getRemark();
        try {
            return DateUtil.toDate(remark.substring(remark.length() - 10, remark.length()));
        } catch (Exception e) {
            throw new InvalidException(false, "get.preg.check.date.failed.from.remark",
                    assembleErrorAttach(null, importSow.getLineNumber()),
                    isNull(remark) ? "" : remark);
        }
    }
}
