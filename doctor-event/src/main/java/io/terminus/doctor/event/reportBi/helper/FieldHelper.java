package io.terminus.doctor.event.reportBi.helper;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.utils.Checks;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dao.reportBi.DoctorFiledUrlDao;
import io.terminus.doctor.event.dto.reportBi.DoctorFiledUrlCriteria;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorFiledUrl;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorPigDaily;
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
            filedUrlCriteria.setStart(DateHelper.withDateStartDay(groupDaily.getSumAt(), dateDimension));
            filedUrlCriteria.setEnd(DateHelper.withDateEndDay(groupDaily.getSumAt(), dateDimension));
        }
    }

    public void fillPigFiledUrl(DoctorFiledUrlCriteria filedUrlCriteria, DoctorPigDaily pigDaily, Integer orzType, Integer dateType) {
        if (Objects.equals(orzType, OrzDimension.FARM.getValue())) {
            filedUrlCriteria.setFarmId(pigDaily.getFarmId());
            DateDimension dateDimension = DateDimension.from(dateType);
            filedUrlCriteria.setStart(DateHelper.withDateStartDay(pigDaily.getSumAt(), dateDimension));
            filedUrlCriteria.setEnd(DateHelper.withDateEndDay(pigDaily.getSumAt(), dateDimension));
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
            if (isNull(groupDaily.getChgFarmIn())) {
                System.out.println("");;
            }
            return get(deadWeedOut, groupDaily.getStart() + groupDaily.getTurnInto()
                    - MoreObjects.firstNonNull(groupDaily.getChgFarmIn(), 0));
        } catch (Exception e) {
            System.out.println("");
        }
        return 0.0;
    }

    public String filedUrl(DoctorFiledUrlCriteria criteria) {
        String url = Checks.expectNotNull(filedUrlMap.get(criteria.getFiledName()), "filed.name.is.illegal");
        url = url + "&farmId=" + criteria.getFarmId();

        // TODO: 18/1/1

        map.put("value", criteria.getValue());
        map.put("url", url);
        return JSON.toJson(map);
    }

    public Double get(Integer denominator, Integer molecular) {
        if (isNull(denominator) || isNull(molecular) || molecular == 0) {
            return 0D;
        }
        return new BigDecimal(denominator).divide(new BigDecimal(molecular), BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
