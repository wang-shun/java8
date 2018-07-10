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
import java.util.ArrayList;
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
    public List<Map<String,Object>> getMating(Long farmId, Date beginDate, Date endDate,String pigCode,String operatorName,int isdelivery){
        List<Map<String,Object>> matingList = doctorReportDeliverDao.getMating(farmId, beginDate, endDate,pigCode,operatorName);
        List<Map<String,Object>> delivery = new ArrayList<>(); //分娩了
        List<Map<String,Object>> nodeliver = new ArrayList<>();//未分娩
        for(int i = 0; i<matingList.size(); i++){
            Map map = matingList.get(i);
            String a = String.valueOf(map.get("pig_status"));
            if(a.equals(String.valueOf(PigStatus.Entry.getKey()))){
                map.put("pig_status",PigStatus.Entry.getName());
            }
            if(a.equals(String.valueOf(PigStatus.Removal.getKey()))){
                map.put("pig_status",PigStatus.Removal.getName());
            }
            if(a.equals(String.valueOf(PigStatus.Mate.getKey()))){
                map.put("pig_status",PigStatus.Mate.getName());
            }
            if(a.equals(String.valueOf(PigStatus.Pregnancy.getKey()))){
                map.put("pig_status",PigStatus.Pregnancy.getName());
            }
            if(a.equals(String.valueOf(PigStatus.KongHuai.getKey()))){
                map.put("pig_status",PigStatus.KongHuai.getName());
            }
            if(a.equals(String.valueOf(PigStatus.Farrow.getKey()))){
                map.put("pig_status",PigStatus.Farrow.getName());
            }
            if(a.equals(String.valueOf(PigStatus.FEED.getKey()))){
                map.put("pig_status",PigStatus.FEED.getName());
            }
            if(a.equals(String.valueOf(PigStatus.Wean.getKey()))){
                map.put("pig_status",PigStatus.Wean.getName());
            }
            if(a.equals(String.valueOf(PigStatus.CHG_FARM.getKey()))){
                map.put("pig_status",PigStatus.CHG_FARM.getName());
            }
            BigInteger id = (BigInteger)map.get("id");
            BigInteger pig_id = (BigInteger)map.get("pig_id");
            int parity = (int)map.get("parity");
            List<Map<String,Object>> deliveryBarn = doctorReportDeliverDao.deliveryBarn(id,pig_id);//判断是否分娩以及查询分娩猪舍
            if(deliveryBarn != null) {
                if (deliveryBarn.size() != 0) {
                    map.put("deliveryFarm", (String) deliveryBarn.get(0).get("farm_name"));
                    map.put("deliveryBarn", (String) deliveryBarn.get(0).get("barn_name"));
                    map.put("deliveryDate", (Date) deliveryBarn.get(0).get("event_at"));
                    map.put("notdelivery", "");
                    map.put("deadorescape", "");
                    map.put("check_event_at", "");
                    map.put("leave_event_at", "");
                    delivery.add(map);
                } else {
                    map.put("deliveryBarn", "未分娩");
                    map.put("deliveryDate", "");
                    map.put("deliveryFarm", "未分娩");
                    Map<String,Object> idsameparity = doctorReportDeliverDao.idsameparity(id,pig_id, parity);//判断是否存在同一胎次多次配种
                    //存在同一胎次多次配种情况下的最近一次配种
                    BigInteger id1 = null;
                    if(idsameparity != null){
                        id1 = (BigInteger)idsameparity.get("id");
                    }
                    //查询妊娠检查结果
                    Map<String,Object> notdelivery = doctorReportDeliverDao.notdelivery(id,pig_id, parity,id1);
                    if(notdelivery != null) {
                        int b = (int) notdelivery.get("preg_check_result");
                        if (b == 1) {
                            map.put("notdelivery", "阳性");
                        }
                        if (b == 2) {
                            map.put("notdelivery", "阴性");
                        }
                        if (b == 3) {
                            map.put("notdelivery", "流产");
                        }
                        if (b == 4) {
                            map.put("notdelivery", "返情");
                        }
                        map.put("check_event_at",notdelivery.get("event_at"));
                    }else{
                        map.put("notdelivery", "");
                        map.put("check_event_at", "");
                    }
                    //死逃的
                    Map<String,Object> leave = doctorReportDeliverDao.leave(id,pig_id, parity,id1);
                    if(leave != null) {
                        long b = (long) leave.get("change_type_id");
                        if (b == 110) {
                            map.put("deadorescape", "死亡");
                        }else if (b == 111) {
                            map.put("deadorescape", "淘汰");
                        }else{
                            map.put("deadorescape", "");
                        }
                        map.put("leave_event_at",leave.get("event_at"));
                    }else{
                        map.put("deadorescape", "");
                        map.put("leave_event_at", "");
                    }
                    nodeliver.add(map);
                }
            }
        }
        if(isdelivery == 1){
            return delivery;
        }else if(isdelivery == 2){
            return nodeliver;
        }else
            return matingList;
    }
}
