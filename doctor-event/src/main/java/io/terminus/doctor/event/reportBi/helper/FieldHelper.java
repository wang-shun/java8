package io.terminus.doctor.event.reportBi.helper;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.utils.Checks;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dao.reportBi.DoctorFiledUrlDao;
import io.terminus.doctor.event.dto.reportBi.DoctorFiledUrlCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorGroupDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorFiledUrl;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * Created by xjn on 18/1/14.
 * email:xiaojiannan@terminus.io
 */
@Slf4j
@Component
public class FieldHelper {
    private final DoctorFiledUrlDao doctorFiledUrlDao;
    private Map<String, String> filedUrlMap;
    private Map<String, Object> map = new HashMap<>();
    private final ToJsonMapper JSON = ToJsonMapper.JSON_NON_EMPTY_MAPPER;

    @Autowired
    public FieldHelper(DoctorFiledUrlDao doctorFiledUrlDao) {
        this.doctorFiledUrlDao = doctorFiledUrlDao;
        List<DoctorFiledUrl> filedUrls = doctorFiledUrlDao.listAll();
        filedUrlMap = filedUrls.stream().collect(Collectors.toMap(DoctorFiledUrl::getName, DoctorFiledUrl::getUrl));
    }

    public void fillGroupFiledUrl(DoctorFiledUrlCriteria filedUrlCriteria, DoctorGroupDaily groupDaily, Integer orzType, Integer dateType) {
        if (Objects.equals(orzType, OrzDimension.FARM.getValue())) {
            filedUrlCriteria.setFarmId(groupDaily.getFarmId());
            filedUrlCriteria.setPigType(groupDaily.getPigType());
            DateDimension dateDimension = DateDimension.from(dateType);
            filedUrlCriteria.setStart(DateUtil.toDateString(DateHelper.withDateStartDay(groupDaily.getSumAt(), dateDimension)));
            filedUrlCriteria.setEnd(DateUtil.toDateString(DateHelper.withDateEndDay(groupDaily.getSumAt(), dateDimension)));
        }
    }

    public void fillPigFiledUrl(DoctorFiledUrlCriteria filedUrlCriteria, DoctorPigDaily pigDaily, Integer orzType, Integer dateType) {
        if (Objects.equals(orzType, OrzDimension.FARM.getValue())) {
            filedUrlCriteria.setFarmId(pigDaily.getFarmId());
            DateDimension dateDimension = DateDimension.from(dateType);
            filedUrlCriteria.setStart(DateUtil.toDateString(DateHelper.withDateStartDay(pigDaily.getSumAt(), dateDimension)));
            filedUrlCriteria.setEnd(DateUtil.toDateString((DateHelper.withDateEndDay(pigDaily.getSumAt(), dateDimension))));
        }
    }

    public String filedUrl(DoctorFiledUrlCriteria criteria, Integer value , String filedName) {
        if (isNull(criteria.getFarmId())) {
            return value.toString();
        }
        criteria.setValue(value);
        criteria.setFiledName(filedName);
        return filedUrl(criteria);
    }

    public Double deadWeedOutRate(DoctorGroupDaily groupDaily, Integer orzType){
        Integer deadWeedOut = groupDaily.getDead() + groupDaily.getWeedOut();
        try {
            if (Objects.equals(orzType, OrzDimension.FARM.getValue())) {
                return get(deadWeedOut, groupDaily.getStart() + groupDaily.getTurnInto());
            }
            return get(deadWeedOut, groupDaily.getStart() + groupDaily.getTurnInto()
                    - MoreObjects.firstNonNull(groupDaily.getChgFarmIn(), 0));
        } catch (Exception e) {
            log.error("dead weed out rate failed, groupDaily:{}, orzType:{}", groupDaily, orzType);
        }
        return 0.0;
    }

    public Integer groupTurnInto(DoctorGroupDailyExtend dailyExtend, Integer orzType) {
        if (Objects.equals(orzType, OrzDimension.FARM.getValue())) {
            return dailyExtend.getTurnInto();
        }
        return EventUtil.minusInt(dailyExtend.getTurnInto(), dailyExtend.getChgFarmIn());
    }

    private String filedUrl(DoctorFiledUrlCriteria criteria) {
        String url = Checks.expectNotNull(filedUrlMap.get(criteria.getFiledName()), "filed.name.is.illegal");
        url = url + "&farmId=" + criteria.getFarmId() + "&beginDate=" + criteria.getStart() + "&endDate=" + criteria.getEnd();
        if (!isNull(criteria.getPigType())) {
            url = url + "&pigType=" + criteria.getPigType().toString();
        }

        map.put("value", criteria.getValue());
        map.put("url", url);
        return JSON.toJson(map);
    }

    public static Double get(Integer denominator, Integer molecular) {
        if (isNull(denominator) || isNull(molecular) || molecular == 0) {
            return 0D;
        }
        return new BigDecimal(denominator).divide(new BigDecimal(molecular), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static Integer getInteger(Integer denominator, Integer molecular) {
        if (isNull(denominator) || isNull(molecular) || molecular == 0) {
            return 0;
        }
        return new BigDecimal(denominator).divide(new BigDecimal(molecular), BigDecimal.ROUND_HALF_UP).intValue();
    }

    public Integer turnIntoAge(DoctorGroupDailyExtend groupDaily, Integer orzType) {
        if (Objects.equals(orzType, OrzDimension.ORG.getValue())) {
            return EventUtil.minusInt(groupDaily.getTurnIntoAge(), groupDaily.getChgFarmInAge());
        }
        return groupDaily.getTurnIntoAge();
    }

    public Double turnIntoWeight(DoctorGroupDailyExtend groupDaily, Integer orzType) {
        if (Objects.equals(orzType, OrzDimension.ORG.getValue())) {
            return EventUtil.minusDouble(groupDaily.getTurnIntoWeight(), groupDaily.getChgFarmInWeight());
        }
        return groupDaily.getTurnIntoWeight();
    }

}
