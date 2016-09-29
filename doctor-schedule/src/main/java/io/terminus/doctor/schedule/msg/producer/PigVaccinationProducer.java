package io.terminus.doctor.schedule.msg.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.client.util.Maps;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.DataRange;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.VaccinationDateType;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorVaccinationPigWarn;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import io.terminus.doctor.event.service.DoctorVaccinationPigWarnReadService;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.dto.RuleValue;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleRoleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.doctor.schedule.msg.producer.factory.GroupDetailFactory;
import io.terminus.doctor.schedule.msg.producer.factory.PigDtoFactory;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 猪只免疫规则
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/6
 */
@Component
@Slf4j
public class PigVaccinationProducer extends AbstractJobProducer {

    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorVaccinationPigWarnReadService doctorVaccinationPigWarnReadService;

    @Autowired
    public PigVaccinationProducer(DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
                                  DoctorMessageRuleReadService doctorMessageRuleReadService,
                                  DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService,
                                  DoctorMessageReadService doctorMessageReadService,
                                  DoctorMessageWriteService doctorMessageWriteService,
                                  DoctorPigReadService doctorPigReadService,
                                  DoctorPigWriteService doctorPigWriteService,
                                  DoctorVaccinationPigWarnReadService doctorVaccinationPigWarnReadService,
                                  DoctorGroupReadService doctorGroupReadService,
                                  DoctorMessageTemplateReadService doctorMessageTemplateReadService,
                                  DoctorUserDataPermissionReadService doctorUserDataPermissionReadService) {
        super(doctorMessageTemplateReadService,
                doctorMessageRuleTemplateReadService,
                doctorMessageRuleReadService,
                doctorMessageRuleRoleReadService,
                doctorMessageReadService,
                doctorMessageWriteService,
                doctorPigReadService,
                doctorPigWriteService,
                doctorUserDataPermissionReadService,
                Category.PIG_VACCINATION);
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorVaccinationPigWarnReadService = doctorVaccinationPigWarnReadService;
    }

    @Override
    protected List<DoctorMessage> message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {
        log.info("猪只免疫消息产生 --- PigVaccinationProducer 开始执行");
        List<DoctorMessage> messages = Lists.newArrayList();

        Rule rule = ruleRole.getRule();
        // ruleValue map
        Map<Integer, RuleValue> ruleValueMap = Maps.newHashMap();
        for (int i = 0; rule.getValues() != null && i < rule.getValues().size(); i++) {
            RuleValue ruleValue = rule.getValues().get(i);
            ruleValueMap.put(ruleValue.getId(), ruleValue);
        }

        if (StringUtils.isNotBlank(rule.getChannels())) {
            // 获取所有的预设免疫规则
            List<DoctorVaccinationPigWarn> vaccinationWarns = RespHelper.orServEx(
                    doctorVaccinationPigWarnReadService.findVaccinationPigWarnsByFarmId(ruleRole.getFarmId()));
            if (vaccinationWarns == null || vaccinationWarns.isEmpty()) {
                return messages;
            }
            for (DoctorVaccinationPigWarn warn : vaccinationWarns) {
                // 判断规则是否在有效期内
                if (warn.getStartDate() != null && DateTime.now().isBefore(new DateTime(warn.getStartDate()))) {
                    continue;
                }
                if (warn.getEndDate() != null && DateTime.now().isAfter(new DateTime(warn.getEndDate()))) {
                    continue;
                }
                PigType pigType = PigType.from(warn.getPigType());
                if (pigType == null) {
                    continue;
                }
                // 一次处理每种类型
                switch (pigType) {
                    // 配种母猪
                    case MATE_SOW:
                        checkMateSow(warn, ruleRole, rule, subUsers);
                        break;
                    // 妊娠母猪
                    case PREG_SOW:
                        checkPregSow(warn, ruleRole, rule, subUsers);
                        break;
                    // 分娩母猪
                    case DELIVER_SOW:
                        checkDeliverSow(warn, ruleRole, rule, subUsers);
                        break;
                    // 后备母猪
                    case RESERVE_SOW:
                        checkReservePig(warn, ruleRole, rule, subUsers, DoctorPig.PIG_TYPE.SOW.getKey());
                        break;
                    // 后备公猪/种公猪
                    case RESERVE_BOAR:case BOAR:
                        checkReservePig(warn, ruleRole, rule, subUsers, DoctorPig.PIG_TYPE.BOAR.getKey());
                        break;
                    // 保育猪
                    case NURSERY_PIGLET:
                        checkPigGroup(warn, ruleRole, rule, subUsers, PigType.NURSERY_PIGLET.getValue());
                        break;
                    // 育肥猪
                    case FATTEN_PIG:
                        checkPigGroup(warn, ruleRole, rule, subUsers, PigType.FATTEN_PIG.getValue());
                        break;
                    // 产房仔猪
                    case FARROW_PIGLET:
                        checkPigGroup(warn, ruleRole, rule, subUsers, PigType.FARROW_PIGLET.getValue());
                        break;
                    default:
                        break;
                }
            }
        }

        log.info("猪只免疫消息产生 --- PigVaccinationProducer 结束执行, 产生 {} 条消息", messages.size());
        return messages;
    }

    /**
     * 校验配种母猪是否需要免疫
     * 可能存在的日期类型:
     *          固定日龄, 固定日期, 固定体重, 转舍, 配种
     */
    private void checkMateSow(DoctorVaccinationPigWarn warn, DoctorMessageRuleRole ruleRole, Rule rule, List<SubUser> subUsers) {
        Map<Integer, List<DoctorPigInfoDto>> sowPigInfoDtos = getPigInfoDtos(ruleRole, DoctorPig.PIG_TYPE.SOW.getKey());
        Iterator<List<DoctorPigInfoDto>> iterator = sowPigInfoDtos.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().forEach(sowPig -> {
                DateTime vaccDate = getVaccinationDate(sowPig);
                // 1. 固定日龄
                if (checkFixedDayAge(warn, sowPig.getDateAge(), vaccDate)) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 2. 固定日期
                if (checkFixedDate(warn, vaccDate)) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 3. 固定体重
                if (checkFixedWeight(warn, vaccDate, sowPig.getWeight(), getCheckWeightDate(sowPig))) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 4. 转舍
                if (checkChangeLocation(warn, vaccDate, getChangeLocationDate(sowPig))) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 5. 配种, 母猪处于待配种状态
                if (Objects.equals(VaccinationDateType.BREEDING.getValue(), warn.getVaccinationDateType()) &&
                        Objects.equals(sowPig.getStatus(), PigStatus.Mate.getKey())) {
                    // (当前日期 - 配置的天数) 大于 处于待配种状态的日期
                    if (DateTime.now().minusDays(warn.getInputValue()).isAfter(new DateTime(sowPig.getUpdatedAt()))) {
                        if (vaccDate == null || vaccDate.isBefore(new DateTime(sowPig.getUpdatedAt()))) {
                            doctorMessageWriteService.createMessages(
                                    getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                        }
                    }
                }
            });
        }
    }

    /**
     * 校验妊娠母猪是否需要免疫
     * 可能存在的日期类型:
     *          固定日龄, 固定日期, 固定体重, 转舍, 妊娠检查
     */
    private void checkPregSow(DoctorVaccinationPigWarn warn, DoctorMessageRuleRole ruleRole, Rule rule, List<SubUser> subUsers) {
        Map<Integer, List<DoctorPigInfoDto>> sowPigInfoDtos = getPigInfoDtos(ruleRole, DoctorPig.PIG_TYPE.SOW.getKey());
        Iterator<List<DoctorPigInfoDto>> iterator = sowPigInfoDtos.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().forEach(sowPig -> {
                DateTime vaccDate = getVaccinationDate(sowPig);
                // 1. 固定日龄
                if (checkFixedDayAge(warn, sowPig.getDateAge(), vaccDate)) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 2. 固定日期
                if (checkFixedDate(warn, vaccDate)) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 3. 固定体重
                if (checkFixedWeight(warn, vaccDate, sowPig.getWeight(), getCheckWeightDate(sowPig))) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 4. 转舍
                if (checkChangeLocation(warn, vaccDate, getChangeLocationDate(sowPig))) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 5. 妊娠检测, 母猪处于阳性状态
                if (Objects.equals(VaccinationDateType.PREG_CHECK.getValue(), warn.getVaccinationDateType()) &&
                        Objects.equals(sowPig.getStatus(), PigStatus.Pregnancy.getKey())) {
                    // (当前日期 - 配置的天数) 大于 处于阳性状态的日期
                    if (DateTime.now().minusDays(warn.getInputValue()).isAfter(new DateTime(sowPig.getUpdatedAt()))) {
                        if (vaccDate == null || vaccDate.isBefore(new DateTime(sowPig.getUpdatedAt()))) {
                            doctorMessageWriteService.createMessages(
                                    getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                        }
                    }
                }

            });
        }
    }

    /**
     * 校验分娩母猪是否需要免疫
     * 可能存在的日期类型:
     *          固定日龄, 固定日期, 固定体重, 转舍, 分娩
     */
    private void checkDeliverSow(DoctorVaccinationPigWarn warn, DoctorMessageRuleRole ruleRole, Rule rule, List<SubUser> subUsers) {
        Map<Integer, List<DoctorPigInfoDto>> sowPigInfoDtos = getPigInfoDtos(ruleRole, DoctorPig.PIG_TYPE.SOW.getKey());
        Iterator<List<DoctorPigInfoDto>> iterator = sowPigInfoDtos.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().forEach(sowPig -> {
                DateTime vaccDate = getVaccinationDate(sowPig);
                // 1. 固定日龄
                if (checkFixedDayAge(warn, sowPig.getDateAge(), vaccDate)) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 2. 固定日期
                if (checkFixedDate(warn, vaccDate)) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 3. 固定体重
                if (checkFixedWeight(warn, vaccDate, sowPig.getWeight(), getCheckWeightDate(sowPig))) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 4. 转舍
                if (checkChangeLocation(warn, vaccDate, getChangeLocationDate(sowPig))) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 5. 分娩, 母猪处于哺乳状态
                if (Objects.equals(VaccinationDateType.DELIVER.getValue(), warn.getVaccinationDateType()) &&
                        Objects.equals(sowPig.getStatus(), PigStatus.FEED.getKey())) {
                    // (当前日期 - 配置的天数) 大于 处于哺乳状态的日期
                    if (DateTime.now().minusDays(warn.getInputValue()).isAfter(new DateTime(sowPig.getUpdatedAt()))) {
                        if (vaccDate == null || vaccDate.isBefore(new DateTime(sowPig.getUpdatedAt()))) {
                            doctorMessageWriteService.createMessages(
                                    getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                        }
                    }
                }
            });
        }
    }

    /**
     * 校验(后备母猪, 后备公猪, 种公猪)是否需要免疫
     * 可能存在的日期类型:
     *          固定日龄, 固定日期, 固定体重, 转舍
     */
    private void checkReservePig(DoctorVaccinationPigWarn warn, DoctorMessageRuleRole ruleRole, Rule rule, List<SubUser> subUsers, Integer pigType) {
        // 获取所有性别的猪
        Map<Integer, List<DoctorPigInfoDto>> sowPigInfoDtos = getPigInfoDtos(ruleRole, DoctorPig.PIG_TYPE.SOW.getKey());
        Iterator<List<DoctorPigInfoDto>> iterator = sowPigInfoDtos.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().forEach(sowPig -> {
                DateTime vaccDate = getVaccinationDate(sowPig);
                // 1. 固定日龄
                if (checkFixedDayAge(warn, sowPig.getDateAge(), vaccDate)) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 2. 固定日期
                if (checkFixedDate(warn, vaccDate)) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 3. 固定体重
                if (checkFixedWeight(warn, vaccDate, sowPig.getWeight(), getCheckWeightDate(sowPig))) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
                // 4. 转舍
                if (checkChangeLocation(warn, vaccDate, getChangeLocationDate(sowPig))) {
                    doctorMessageWriteService.createMessages(
                            getMessage(sowPig, rule.getChannels(), ruleRole, subUsers, null, rule.getUrl(), warn, vaccDate));
                }
            });
        }
    }

    /**
     *
     * 校验(保育猪, 育肥猪)是否需要免疫
     * 可能存在的日期类型:
     *          固定日龄, 固定日期, 固定体重, 转群
     */
    private void checkPigGroup(DoctorVaccinationPigWarn warn, DoctorMessageRuleRole ruleRole, Rule rule, List<SubUser> subUsers, Integer pigType) {
        List<DoctorGroupDetail> groupInfos = getGroupInfos(ruleRole, pigType);
        for (int i = 0; groupInfos != null && i < groupInfos.size(); i++) {
            DoctorGroupDetail groupInfo = groupInfos.get(i);
            DoctorGroupTrack groupTrack = groupInfo.getGroupTrack();
            if (groupTrack != null) {
                DateTime vaccDate = getGroupVaccinationDate(groupTrack);
                // 1. 固定日龄
                if (checkFixedDayAge(warn, groupTrack.getAvgDayAge(), vaccDate)) {
                    doctorMessageWriteService.createMessages(
                            getGroupMessage(groupInfo, rule.getChannels(), ruleRole, subUsers, rule.getUrl(), warn, vaccDate));
                }
                // 2. 固定日期
                if (checkFixedDate(warn, vaccDate)) {
                    doctorMessageWriteService.createMessages(
                            getGroupMessage(groupInfo, rule.getChannels(), ruleRole, subUsers, rule.getUrl(), warn, vaccDate));
                }
                // 3. 固定体重
                if (checkFixedWeight(warn, vaccDate, groupTrack.getAvgWeight(), null)) {
                    doctorMessageWriteService.createMessages(
                            getGroupMessage(groupInfo, rule.getChannels(), ruleRole, subUsers, rule.getUrl(), warn, vaccDate));
                }
                // 4. 转群
                if (checkChangeGroup(warn, vaccDate, getChangeGroupDate(groupTrack))) {
                    doctorMessageWriteService.createMessages(
                            getGroupMessage(groupInfo, rule.getChannels(), ruleRole, subUsers, rule.getUrl(), warn, vaccDate));
                }
            }
        }
    }

    /**
     * 校验是否达到固定日龄的规则
     * @param warn      防疫程序
     * @param dayAge    日龄
     * @param vaccDate  免疫时间
     * @return
     */
    private boolean checkFixedDayAge(DoctorVaccinationPigWarn warn, Integer dayAge, DateTime vaccDate) {
        if (Objects.equals(VaccinationDateType.FIXED_DAY_AGE.getValue(), warn.getVaccinationDateType())) {
            if (dayAge >= warn.getInputValue()) {
                // 如果免疫时间 小于 (达到规则日龄的时间), 则生成消息
                DateTime ruleDayAgeTime = DateTime.now().minusDays(dayAge - warn.getInputValue());
                if (vaccDate == null || vaccDate.isBefore(ruleDayAgeTime)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 校验是否达到固定日期
     * @param warn      防疫程序
     * @param vaccDate  免疫时间
     * @return
     */
    private boolean checkFixedDate(DoctorVaccinationPigWarn warn, DateTime vaccDate) {
        if (Objects.equals(VaccinationDateType.FIXED_DATE.getValue(), warn.getVaccinationDateType())) {
            if (DateTime.now().isAfter(new DateTime(warn.getInputDate()))) {
                // 如果免疫时间 小于 (规则固定日期), 则生成消息
                if (vaccDate == null || vaccDate.isBefore(new DateTime(warn.getInputDate()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检验是否到达固定体重
     * @param warn              免疫程序
     * @param vaccDate          免疫时间
     * @param weight            当前重量
     * @param checkWeightTime   检测重量的时间
     * @return
     */
    private boolean checkFixedWeight(DoctorVaccinationPigWarn warn, DateTime vaccDate, Double weight, DateTime checkWeightTime) {
        if (Objects.equals(VaccinationDateType.FIXED_WEIGHT.getValue(), warn.getVaccinationDateType())) {
            if (weight >= warn.getInputValue()) {
                // 如果免疫时间 小于 (到达规则体重的日期), 则生成消息
                // DateTime weightTime = checkWeightTime;
                if (vaccDate == null /*|| vaccDate.isBefore(weightTime)*/) { //  如果没有免疫, 就免疫
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 校验转舍后是否达到规则要求
     * @param warn          免疫程序
     * @param vaccDate      免疫时间
     * @param chgLocTime    转舍时间
     * @return
     */
    private boolean checkChangeLocation(DoctorVaccinationPigWarn warn, DateTime vaccDate, DateTime chgLocTime) {
        if (Objects.equals(VaccinationDateType.CHANGE_LOC.getValue(), warn.getVaccinationDateType())) {
            // (当前日期 - 配置的天数) 大于 转舍日期
            if (DateTime.now().minusDays(warn.getInputValue()).isAfter(chgLocTime)) {
                // 如果免疫过了, 就不提醒
                if (vaccDate == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 校验转群后是否达到规则要求
     * @param warn          免疫程序
     * @param vaccDate      免疫时间
     * @param chgGroupTime    转舍时间
     * @return
     */
    private boolean checkChangeGroup(DoctorVaccinationPigWarn warn, DateTime vaccDate, DateTime chgGroupTime) {
        if (Objects.equals(VaccinationDateType.CHANGE_GROUP.getValue(), warn.getVaccinationDateType())) {
            // (当前日期 - 配置的天数) 大于 转群日期
            if (DateTime.now().minusDays(warn.getInputValue()).isAfter(chgGroupTime)) {
                // 如果免疫过了, 就不提醒
                if (vaccDate == null) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 获取猪信息
     * @param ruleRole
     * @param pigType   猪性别
     *           @see io.terminus.doctor.event.model.DoctorPig.PIG_TYPE
     */
    private Map<Integer, List<DoctorPigInfoDto>> getPigInfoDtos(DoctorMessageRuleRole ruleRole, Integer pigType) {
        Map<Integer, List<DoctorPigInfoDto>> sowPigInfoDtos = Maps.newHashMap();
        Long total = RespHelper.orServEx(doctorPigReadService.queryPigCount(
                DataRange.FARM.getKey(), ruleRole.getFarmId(), pigType));
        // 计算size, 和page
        Long page = getPageSize(total, 100L);
        DoctorPig pig = DoctorPig.builder()
                .farmId(ruleRole.getFarmId())
                .pigType(pigType)
                .build();
        for (int i = 1; i <= page; i++) {
            List<DoctorPigInfoDto> pigs = RespHelper.orServEx(doctorPigReadService.pagingDoctorInfoDtoByPig(pig, i, 100)).getData();
            // 过滤出未离场的猪
            pigs = pigs.stream().filter(pigDto ->
                    !Objects.equals(PigStatus.Removal.getKey(), pigDto.getStatus())
            ).collect(Collectors.toList());
            sowPigInfoDtos.put(i, pigs);
        }
        return sowPigInfoDtos;
    }

    /**
     * 获取猪群信息
     * @param ruleRole
     * @param pigType   猪种类
     *               @see PigType
     */
    private List<DoctorGroupDetail> getGroupInfos(DoctorMessageRuleRole ruleRole, Integer pigType) {
        DoctorGroupSearchDto searchDto = new DoctorGroupSearchDto();
        searchDto.setFarmId(ruleRole.getFarmId());
        searchDto.setPigType(pigType);
        searchDto.setStatus(DoctorGroupSearchDto.Status.CREATED.getValue());
        return RespHelper.orServEx(doctorGroupReadService.findGroupDetail(searchDto));
    }

    /**
     * 获取猪的最新的免疫时间
     */
    private DateTime getVaccinationDate(DoctorPigInfoDto pigDto) {
        try{
            if(StringUtils.isNotBlank(pigDto.getExtraTrack())) {
                // @see DoctorMatingDto
                Date date = new Date((Long) MAPPER.readValue(pigDto.getExtraTrack(), Map.class).get("vaccinationDate"));
                return new DateTime(date);
            }
        } catch (Exception e) {
            log.error("[PigVaccinationProducer] get vaccination date failed, pigDto is {}", pigDto);
        }
        return null;
    }

    /**
     * 获取猪群的最新免疫时间
     */
    private DateTime getGroupVaccinationDate(DoctorGroupTrack track) {
        try{
            Date antiepidemicAt = track.getExtraEntity().getAntiepidemicAt();
            if (antiepidemicAt != null) {
                return new DateTime(antiepidemicAt);
            }
        } catch (Exception e) {
            log.error("[PigVaccinationProducer] get GroupVaccinationDate failed, DoctorGroupTrack is {}", track);
        }
        return null;
    }

    /**
     * 获取转群时间
     */
    private DateTime getChangeGroupDate(DoctorGroupTrack track) {
        try{
            Date trantsGroupAt = track.getExtraEntity().getTransGroupAt();
            if (trantsGroupAt != null) {
                return new DateTime(trantsGroupAt);
            }
        } catch (Exception e) {
            log.error("[PigVaccinationProducer] get trantsGroup date failed, DoctorGroupTrack is {}", track);
        }
        return null;
    }

    /**
     * 获取最新的体况检查时间
     */
    private DateTime getCheckWeightDate(DoctorPigInfoDto pigDto) {
        try{
            if(StringUtils.isNotBlank(pigDto.getExtraTrack())) {
                // @see DoctorMatingDto
                Date date = new Date((Long) MAPPER.readValue(pigDto.getExtraTrack(), Map.class).get("conditionDate"));
                return new DateTime(date);
            }
        } catch (Exception e) {
            log.error("[PigVaccinationProducer] get check weight date failed, pigDto is {}", pigDto);
        }
        return null;
    }

    /**
     * 获取转舍时间
     */
    private DateTime getChangeLocationDate(DoctorPigInfoDto pigDto) {
        try{
            if(StringUtils.isNotBlank(pigDto.getExtraTrack())) {
                // @see DoctorMatingDto
                Date date = new Date((Long) MAPPER.readValue(pigDto.getExtraTrack(), Map.class).get("changeLocationDate"));
                return new DateTime(date);
            }
        } catch (Exception e) {
            log.error("[PigVaccinationProducer] get change location date failed, pigDto is {}", pigDto);
        }
        return null;
    }


    /**
     * 创建消息
     */
    private List<DoctorMessage> getMessage(DoctorPigInfoDto pigDto, String channels, DoctorMessageRuleRole ruleRole, List<SubUser> subUsers, Double timeDiff, String url, DoctorVaccinationPigWarn warn, DateTime vaccDate) {
        List<DoctorMessage> messages = Lists.newArrayList();
        // 创建消息
        Map<String, Object> jsonData = PigDtoFactory.getInstance().createPigMessage(pigDto, timeDiff, url);
        jsonData.put("pigType", warn.getPigType());
        jsonData.put("materialId", warn.getMaterialId());
        jsonData.put("materialName", warn.getMaterialName());
        jsonData.put("inputValue", warn.getInputValue());
        jsonData.put("inputDate", warn.getInputDate());
        jsonData.put("dose", warn.getDose());
        jsonData.put("vaccinationDateType", warn.getVaccinationDateType());
        jsonData.put("vaccDate", DateTimeFormat.forPattern("yyyy-MM-dd").print(vaccDate));

        Splitters.COMMA.splitToList(channels).forEach(channel -> {
            try {
                messages.addAll(createMessage(subUsers, ruleRole, Integer.parseInt(channel), MAPPER.writeValueAsString(jsonData), null));
            } catch (JsonProcessingException e) {
                log.error("message produce error, cause by {}", Throwables.getStackTraceAsString(e));
            }
        });
        return messages;
    }

    /**
     * 创建消息
     */
    private List<DoctorMessage> getGroupMessage(DoctorGroupDetail detail, String channels, DoctorMessageRuleRole ruleRole, List<SubUser> subUsers, String url, DoctorVaccinationPigWarn warn, DateTime vaccDate) {
        List<DoctorMessage> messages = Lists.newArrayList();
        // 创建消息
        Map<String, Object> jsonData = GroupDetailFactory.getInstance().createGroupMessage(detail, url);
        jsonData.put("pigType", warn.getPigType());
        jsonData.put("materialId", warn.getMaterialId());
        jsonData.put("materialName", warn.getMaterialName());
        jsonData.put("inputValue", warn.getInputValue());
        jsonData.put("inputDate", DateUtil.toDateString(warn.getInputDate()));
        jsonData.put("currDate", DateUtil.toDateString(new Date()));
        jsonData.put("dose", warn.getDose());
        jsonData.put("vaccinationDateType", warn.getVaccinationDateType());
        jsonData.put("vaccDate", DateTimeFormat.forPattern("yyyy-MM-dd").print(vaccDate));

        Splitters.COMMA.splitToList(channels).forEach(channel -> {
            try {
                messages.addAll(createMessage(subUsers, ruleRole, Integer.parseInt(channel), MAPPER.writeValueAsString(jsonData), null));
            } catch (JsonProcessingException e) {
                log.error("message produce error, cause by {}", Throwables.getStackTraceAsString(e));
            }
        });
        return messages;
    }
}
