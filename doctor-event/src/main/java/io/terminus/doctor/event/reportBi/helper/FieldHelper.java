package io.terminus.doctor.event.reportBi.helper;

import io.terminus.doctor.common.utils.Checks;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dao.reportBi.DoctorFiledUrlDao;
import io.terminus.doctor.event.dto.reportBi.DoctorFiledUrlCriteria;
import io.terminus.doctor.event.model.DoctorFiledUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public String filedUrl(DoctorFiledUrlCriteria criteria, Integer value , String filedName) {
        if (isNull(criteria.getFarmId())) {
            return value.toString();
        }
        criteria.setValue(value);
        criteria.setFiledName(filedName);
        return filedUrl(criteria);
    }

    public String filedUrl(DoctorFiledUrlCriteria criteria) {
        String url = Checks.expectNotNull(filedUrlMap.get(criteria.getFiledName()), "filed.name.is.illegal");
        url = url + "&farmId=" + criteria.getFarmId();

        // TODO: 18/1/14 其他字段

        map.put("value", criteria.getValue());
        map.put("url", url);
        return JSON.toJson(map);
    }
}
