package io.terminus.doctor.event.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DoctorDeliveryReadService {
    Map<String,Object> getMating(Long farmId, Date beginDate, Date endDate,String pigCode,String operatorName,int isdelivery);
    List<Map<String,Object>> sowsReport(Long farmId,Date time,String pigCode,String operatorName,Integer barnType,Integer breed,Integer parity,Integer pigStatus,Date beginInFarmTime, Date endInFarmTime);
}
