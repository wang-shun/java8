package io.terminus.doctor.event.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.event.dao.DoctorDataFactorDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportDeliverDao;
import io.terminus.doctor.event.enums.PigStatus;
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
    public List<Map<String,Object>> getMating(Long farmId, Date beginDate, Date endDate,String pigCode,String operatorName){
        List<Map<String,Object>> matingList = doctorReportDeliverDao.getMating(farmId, beginDate, endDate,pigCode,operatorName);
        for(Map map : matingList){
            if(String.valueOf(map.get("pig_status")).equals(String.valueOf(PigStatus.Entry.getKey()))){
                map.put("pig_status",PigStatus.Entry.getName());
            }
            if(String.valueOf(map.get("pig_status")).equals(String.valueOf(PigStatus.Removal.getKey()))){
                map.put("pig_status",PigStatus.Removal.getName());
            }
            if(String.valueOf(map.get("pig_status")).equals(String.valueOf(PigStatus.Mate.getKey()))){
                map.put("pig_status",PigStatus.Mate.getName());
            }
            if(String.valueOf(map.get("pig_status")).equals(String.valueOf(PigStatus.Pregnancy.getKey()))){
                map.put("pig_status",PigStatus.Pregnancy.getName());
            }
            if(String.valueOf(map.get("pig_status")).equals(String.valueOf(PigStatus.KongHuai.getKey()))){
                map.put("pig_status",PigStatus.KongHuai.getName());
            }
            if(String.valueOf(map.get("pig_status")).equals(String.valueOf(PigStatus.Farrow.getKey()))){
                map.put("pig_status",PigStatus.Farrow.getName());
            }
            if(String.valueOf(map.get("pig_status")).equals(String.valueOf(PigStatus.FEED.getKey()))){
                map.put("pig_status",PigStatus.FEED.getName());
            }
            if(String.valueOf(map.get("pig_status")).equals(String.valueOf(PigStatus.Wean.getKey()))){
                map.put("pig_status",PigStatus.Wean.getName());
            }
            if(String.valueOf(map.get("pig_status")).equals(String.valueOf(PigStatus.CHG_FARM.getKey()))){
                map.put("pig_status",PigStatus.CHG_FARM.getName());
            }
            List<Map<String,Object>> deliveryBarn = doctorReportDeliverDao.deliveryBarn((BigInteger)map.get("id"),(BigInteger)map.get("pig_id"), (Date)map.get("event_at"));//判断是否分娩以及查询分娩猪舍
            if(deliveryBarn != null) {
                if (deliveryBarn.size() != 0) {
                    map.put("deliveryFarm", (String) deliveryBarn.get(0).get("farm_name"));
                    map.put("deliveryBarn", (String) deliveryBarn.get(0).get("barn_name"));
                    map.put("deliveryDate", (Date) deliveryBarn.get(0).get("event_at"));
                    map.put("notdelivery", "");
                    map.put("deadorescape", "");
                } else {
                    map.put("deliveryBarn", "未分娩");
                    map.put("deliveryDate", "");
                    map.put("deliveryFarm", "未分娩");
                    Map<String,Object> idsameparity = doctorReportDeliverDao.idsameparity((BigInteger)map.get("id"),(BigInteger)map.get("pig_id"), (int)map.get("parity"),(Date)map.get("event_at"));//判断是否存在同一胎次多次配种
                    Date event_at1 = null;//存在同一胎次多次配种情况下的最近一次配种
                    BigInteger id1 = null;
                    if(idsameparity != null){
                        event_at1 = (Date)idsameparity.get("event_at");
                        id1 = (BigInteger)idsameparity.get("id");
                    }
                    //查询妊娠检查结果
                    Map<String,Object> notdelivery = doctorReportDeliverDao.notdelivery((BigInteger)map.get("id"),(BigInteger)map.get("pig_id"), (int)map.get("parity"),(Date)map.get("event_at"), event_at1,id1);
                    if(notdelivery != null) {
                        int a = (int) notdelivery.get("preg_check_result");
                        if (a == 1) {
                            map.put("notdelivery", "阳性");
                        }
                        if (a == 2) {
                            map.put("notdelivery", "阴性");
                        }
                        if (a == 3) {
                            map.put("notdelivery", "流产");
                        }
                        if (a == 4) {
                            map.put("notdelivery", "返情");
                        }
                    }else{
                        map.put("notdelivery", "");
                    }
                    //死逃的
                    Map<String,Object> leave = doctorReportDeliverDao.leave((BigInteger)map.get("id"),(BigInteger)map.get("pig_id"), (int)map.get("parity"),(Date)map.get("event_at"), event_at1,id1);
                    if(leave != null) {
                        long b = (long) leave.get("change_type_id");
                        if (b == 110) {
                            map.put("deadorescape", "死亡");
                        }else if (b == 111) {
                            map.put("deadorescape", "淘汰");
                        }else{
                            map.put("deadorescape", "");
                        }
                    }else{
                        map.put("deadorescape", "");
                    }
                }
            }

        }
        return matingList;
    }
}
