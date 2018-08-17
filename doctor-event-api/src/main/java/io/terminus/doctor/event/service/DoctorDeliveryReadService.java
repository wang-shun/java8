package io.terminus.doctor.event.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DoctorDeliveryReadService {
    Map<String,Object> getMating(Long farmId, Date beginDate, Date endDate,String pigCode,String operatorName,int isdelivery);
    List<Map<String,Object>> sowsReport(Long farmId,Date time,String pigCode,String operatorName,Long barnId,Integer breed,Integer parity,Integer pigStatus,Date beginInFarmTime, Date endInFarmTime, Integer sowsStatus);
    List<Map<String,Object>> boarReport(Long farmId,Integer pigType,Integer boarsStatus,Date queryDate,String pigCode,String staffName,Integer barnId,Integer breedId,Date beginDate,Date endDate);
    Map<String,Object> groupReport(Long farmId,Date time,String groupCode,String operatorName,Long barn,Integer groupType,Integer groupStatus,Date buildBeginGroupTime,Date buildEndGroupTime,Date closeBeginGroupTime,Date closeEndGroupTime);
    List<Map<String,Object>> barnsReport(Long farmId,String operatorName,String barnName,Date beginTime,Date endTime,int pigType);
}
