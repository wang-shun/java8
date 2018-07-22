package io.terminus.doctor.event.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DoctorDeliveryReadService {
    Map<String,Object> getMating(Long farmId, Date beginDate, Date endDate,String pigCode,String operatorName,int isdelivery);
    List<Map<String,Object>> sowsReport(Long farmId,Date time,String pigCode,String operatorName,Long barnId,int breed,int parity,int pigStatus,Date inFarmTime);
}
