package io.terminus.doctor.event.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DoctorDeliveryReadService {
    Map<String,Object> getMating(Long farmId, Date beginDate, Date endDate,String pigCode,String operatorName,int isdelivery);
    List<Map<String,Object>> sowsReport(Long farmId,Date time,String pigCode,String operatorName,Integer barnType,Integer breed,Integer parity,Integer pigStatus,Date beginInFarmTime, Date endInFarmTime);
    List<Map<String,Object>> boarReport(Long farmId,Date queryDate,String pigCode,String staffName,Integer barnId,Integer breedId,Integer pigStatus,Date beginDate,Date endDate);
    Map<String,Object> groupReport(Long farmId,Date time,String groupCode,String operatorName,Integer barnType,Integer groupType,Integer groupStatus,Date buildBeginGroupTime,Date buildEndGroupTime,Date closeBeginGroupTime,Date closeEndGroupTime);
}
