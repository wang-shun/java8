package io.terminus.doctor.schedule.msg.producer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.dto.RuleValue;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 母猪应淘汰提示
 * <p>
 * 1. 母猪胎次
 * <p>
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/6
 */
@Component
@Slf4j
public class SowEliminateProducer extends AbstractJobProducer {

    public SowEliminateProducer() {
        super(Category.SOW_ELIMINATE);
    }

    @Override
    protected void message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {
        Rule rule = ruleRole.getRule();
        // ruleValue map
        Map<Integer, RuleValue> ruleValueMap = Maps.newHashMap();
        for (int i = 0; rule.getValues() != null && i < rule.getValues().size(); i++) {
            RuleValue ruleValue = rule.getValues().get(i);
            ruleValueMap.put(ruleValue.getId(), ruleValue);
        }
        DoctorPig pig = DoctorPig.builder()
                .farmId(ruleRole.getFarmId())
                .pigType(DoctorPig.PigSex.SOW.getKey())
                .build();
        // 批量获取母猪信息
        Long total = RespHelper.orServEx(doctorPigReadService.getPigCount(ruleRole.getFarmId(), DoctorPig.PigSex.SOW));
        // 计算size, 分批处理
        Long page = getPageSize(total, 100L);
        for (int i = 1; i <= page; i++) {
            List<DoctorPigInfoDto> pigs = RespHelper.orServEx(doctorPigReadService.pagingDoctorInfoDtoByPig(pig, i, 100)).getData();
            // 过滤出未离场的猪
            pigs = pigs.stream().filter(pigDto ->
                    !Objects.equals(PigStatus.Removal.getKey(), pigDto.getStatus())
            ).collect(Collectors.toList());
            // 处理每个猪
            for (int j = 0; pigs != null && j < pigs.size(); j++) {
                try {
                    DoctorPigInfoDto pigDto = pigs.get(j);
                    //根据用户拥有的猪舍权限过滤拥有user
                    List<SubUser> sUsers = filterSubUserBarnId(subUsers, pigDto.getBarnId());
                    // 母猪的updatedAt与当前时间差 (天)
                    Double timeDiff = (double) (DateTime.now().minus(pigDto.getUpdatedAt().getTime()).getMillis() / 86400000);
                    for (Integer key : ruleValueMap.keySet()) {
                        //是否需要发送消息
                        Boolean isSend = false;
                        RuleValue ruleValue = ruleValueMap.get(key);
                        if (key == 1) {
                            if (pigDto.getParity() != null) {
                                //当前胎次大于或等于预定值
                                isSend = pigDto.getParity() > ruleValue.getValue().intValue() - 1;
                            }
                        } else if (key == 2) {
                            List<DoctorPigEvent> doctorPigEvents = pigDto.getDoctorPigEvents().stream().filter(doctorPigEvent -> Objects.equals(doctorPigEvent.getType(), PigEvent.FARROWING.getKey())).sorted(Comparator.comparing(DoctorPigEvent::getId).reversed()).collect(Collectors.toList());
                            if (doctorPigEvents.size() > 1) {
                                //最近两胎产仔数小于或等于预定值
                                isSend = doctorPigEvents.get(0).getLiveCount() + doctorPigEvents.get(1).getLiveCount() < ruleValue.getValue().intValue() + 1;
                            }
                        } else if (key == 3) {
                            Long count = pigDto.getDoctorPigEvents().stream().filter(doctorPigEvent -> Objects.equals(doctorPigEvent.getType(), PigEvent.PREG_CHECK.getKey())
                                    && !Objects.equals(doctorPigEvent.getPregCheckResult(), PregCheckResult.YANG.getKey())).count();
                            //累计返情、流产、阴性大于或等于预定值
                            isSend = count > ruleValue.getValue().intValue() - 1;

                        } else if (key == 4) {
                            //连续返情、阴性、流产的次数
                            Integer count = 0;
                            List<DoctorPigEvent> events = pigDto.getDoctorPigEvents().stream().filter(doctorPigEvent -> Objects.equals(doctorPigEvent.getType(), PigEvent.MATING.getKey()) || Objects.equals(doctorPigEvent.getType(), PigEvent.PREG_CHECK.getKey())).collect(Collectors.toList());
                            List<List<DoctorPigEvent>> lists = getPigList(events, PigEvent.MATING.getKey());
                            for (List<DoctorPigEvent> list : lists) {
                                DoctorPigEvent doctorPigEvent;
                                if (list.isEmpty()) {
                                    break;
                                }
                                if (list.size() > 1) {
                                    doctorPigEvent = list.get(list.size() - 1);
                                } else {
                                    doctorPigEvent = list.get(0);
                                }
                                if (!Objects.equals(doctorPigEvent.getPregCheckResult(), PregCheckResult.YANG.getKey())) {
                                    count++;
                                } else {
                                    count = 0;
                                }
                            }
                            //连续返情、流产、阴性数大于或等于预定值
                            isSend = count > ruleValue.getValue().intValue() - 1;
                        }
                        if (isSend) {
                            pigDto.setReason(ruleValue.getDescribe() + ruleValue.getValue().intValue());
                            getMessage(pigDto, ruleRole, sUsers, null, null, rule.getUrl(), PigEvent.REMOVAL.getKey(), ruleValue.getId());
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("[sowEliminateProduce]-handle.message.failed");
                }
            }
        }

    }

    private List<List<DoctorPigEvent>> getPigList(List<DoctorPigEvent> events, Integer type) {
        List<List<DoctorPigEvent>> results = Lists.newArrayList();
        List<DoctorPigEvent> tempList = Lists.newArrayList();
        events = events.stream().sorted((a, b) -> b.getEventAt().compareTo(a.getEventAt())).collect(Collectors.toList());
        for (DoctorPigEvent event : events) {
            if (Objects.equals(event.getType(), type)) {
                results.add(tempList);
                tempList = Lists.newArrayList();
            } else {
                tempList.add(event);
            }
        }
        return results;
    }
}
