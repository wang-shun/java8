package io.terminus.doctor.event.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.event.dao.DoctorDataFactorDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportDeliverDao;
import io.terminus.doctor.event.manager.FactorManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RpcProvider
public class DoctorDeliveryReadServicelmpl implements DoctorDeliveryReadService{

    private final DoctorReportDeliverDao doctorReportDeliverDao;

    @Autowired
    public DoctorDeliveryReadServicelmpl(DoctorReportDeliverDao doctorReportDeliverDao) {
        this.doctorReportDeliverDao = doctorReportDeliverDao;
    }

    @Override
    public List<Map<String,Object>> getMating(Long farmId, Date beginDate, Date endDate){
        List<Map<String,Object>> matingList = doctorReportDeliverDao.getMating(farmId, beginDate, endDate);
        for(Map map : matingList){
            List<Map<String,Object>> deliveryBarn = doctorReportDeliverDao.deliveryBarn((BigInteger)map.get("pig_id"), (Date)map.get("event_at"),(Integer)map.get("parity"));
            if(deliveryBarn.size() != 0){
                map.put("deliveryBarn",(String)deliveryBarn.get(0).get("barn_name"));
            } else{
                map.put("deliveryBarn","");
            }
        }
        return matingList;
    }
}
