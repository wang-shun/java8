package io.terminus.doctor.event.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DoctorDeliveryReadService {
    List<Map<String,Object>> getMating(Long farmId, Date beginDate, Date endDate,String pigCode,String operatorName);
}
