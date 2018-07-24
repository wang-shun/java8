package io.terminus.doctor.event.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportDeliverDao;
import io.terminus.doctor.event.enums.PigStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Slf4j
@Service
@RpcProvider
public class DoctorDeliveryReadServicelmpl implements DoctorDeliveryReadService{

    private final DoctorReportDeliverDao doctorReportDeliverDao;
    private final DoctorPigEventDao doctorPigEventDao;

    @Autowired
    public DoctorDeliveryReadServicelmpl(DoctorReportDeliverDao doctorReportDeliverDao,DoctorPigEventDao doctorPigEventDao) {
        this.doctorReportDeliverDao = doctorReportDeliverDao;
        this.doctorPigEventDao = doctorPigEventDao;
    }

    @Override
    public Map<String,Object> getMating(Long farmId, Date beginDate, Date endDate,String pigCode,String operatorName,int isdelivery){
        List<Map<String,Object>> matingList = doctorReportDeliverDao.getMating(farmId, beginDate, endDate,pigCode,operatorName);
        List<Map<String,Object>> delivery = new ArrayList<>(); //分娩了
        List<Map<String,Object>> nodeliver = new ArrayList<>();//未分娩
        int deliverycount = 0;//分娩数
        int fqcount = 0;//返情数
        int lccount = 0;//流产数
        int yxcount = 0;//阴性数
        int swcount = 0;//死亡数
        int ttcount = 0;//淘汰数
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

            Map<String,Object> matingCount =  doctorReportDeliverDao.getMatingCount(pig_id,(Date)map.get("event_at"));
            if(matingCount != null){
                map.put("current_mating_count",matingCount.get("current_mating_count"));
            }
            List<Map<String,Object>> deliveryBarn = doctorReportDeliverDao.deliveryBarn(id,pig_id);//判断是否分娩以及查询分娩猪舍
            if(deliveryBarn != null) {
                if (deliveryBarn.size() != 0) {
                    map.put("deliveryFarm", (String) deliveryBarn.get(0).get("farm_name"));
                    map.put("deliveryBarn", (String) deliveryBarn.get(0).get("barn_name"));
                    map.put("deliveryDate", (Date) deliveryBarn.get(0).get("event_at"));
                    map.put("notdelivery", "阳性");
                    map.put("deadorescape", "");
                    map.put("check_event_at", "");
                    map.put("leave_event_at", "");
                    delivery.add(map);
                    deliverycount = deliverycount + 1;
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
                            yxcount = yxcount + 1;
                        }
                        if (b == 3) {
                            map.put("notdelivery", "流产");
                            lccount = lccount + 1;
                        }
                        if (b == 4) {
                            map.put("notdelivery", "返情");
                            fqcount = fqcount + 1;
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
                            swcount = swcount + 1;
                        }else if (b == 111) {
                            map.put("deadorescape", "淘汰");
                            ttcount = ttcount + 1;
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
        int matingcount = 0;
        String deliveryrate = "0";
        String fqrate = "0";
        String lcrate = "0";
        String yxrate = "0";
        String swrate = "0";
        String ttrate = "0";
        if(matingList.size()!=0) {
            matingcount = matingList.size();
            deliveryrate = divide(deliverycount, matingcount);//分娩率
            fqrate = divide(fqcount, matingcount);//返情率
            lcrate = divide(lccount, matingcount);//流产率
            yxrate = divide(yxcount, matingcount);//阴性率
            swrate = divide(swcount, matingcount);//死亡率
            ttrate = divide(ttcount, matingcount);//淘汰率
        }

        Map<String,Object> list = new HashMap<>();
        list.put("matingcount",matingcount);
        list.put("deliverycount",deliverycount);
        list.put("fqcount",fqcount);
        list.put("lccount",lccount);
        list.put("yxcount",yxcount);
        list.put("swcount",swcount);
        list.put("ttcount",ttcount);
        list.put("deliveryrate",deliveryrate);
        list.put("fqrate",fqrate);
        list.put("lcrate",lcrate);
        list.put("yxrate",yxrate);
        list.put("swrate",swrate);
        list.put("ttrate",ttrate);
        if(isdelivery == 1){
            list.put("data",delivery);
        }else if(isdelivery == 2){
            list.put("data",nodeliver);
        }else{
            list.put("data",matingList);
        }
            return list;
    }
    private String divide(int i,int j){
        double k = (double)i/j*100;
        BigDecimal big   =   new  BigDecimal(k);
        String  l = big.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue() +"%";
        return l;
    }

    @Override
    public List<Map<String,Object>> sowsReport(Long farmId,Date time,String pigCode,String operatorName,Integer barnType,Integer breed,Integer parity,Integer pigStatus,Date beginInFarmTime, Date endInFarmTime){
        List<Map<String,Object>> inFarmPigId = doctorPigEventDao.getInFarmPigId(farmId,time,barnType,pigCode,breed,operatorName, beginInFarmTime,endInFarmTime);//查询某个时间点之前所有进场和转场转入的猪
        for(Iterator<Map<String,Object>> it = inFarmPigId.iterator();it.hasNext();){
            Map map = it.next();
            BigInteger id = (BigInteger)map.get("id");
            BigInteger pigId = (BigInteger)map.get("pig_id");
            Date eventAt = (Date)map.get("event_at");
            //Integer a = doctorPigEventDao.isOutFarm(id,pigId,eventAt,farmId,time);//判断后面是否有离场或者转场
            /*if(a != null && a >= 1){
                inFarmPigId.remove(i); //如果后面还有离场或者转场删除
            } else{*/
                /*Map<String,Object> pigInfo = doctorPigEventDao.findPigInfo(pigId);
                if(pigInfo != null) {
                    map.put("current_barn_name", pigInfo.get("current_barn_name"));//猪舍(如果后面没有转舍事件就为当前猪舍)
                    map.put("pig_code", pigInfo.get("pig_code"));//猪耳号
                    map.put("breed_name", pigInfo.get("breed_name"));//品种
                    map.put("source", pigInfo.get("source"));//来源
                    map.put("in_farm_date", pigInfo.get("in_farm_date"));//进场时间
                    map.put("birth_date", pigInfo.get("birth_date"));//出生日期
                    map.put("staff_name", pigInfo.get("staff_name"));//饲养员
                }*/
            BigInteger isBarn = doctorPigEventDao.isBarn(id,pigId,eventAt,farmId,time);//如果后面又转舍事件
            if(isBarn != null) {
                Map<String,Object> currentBarn = doctorPigEventDao.findBarn(isBarn,id,pigId,eventAt,farmId,time,operatorName,barnType);//如果后面又转舍事件,去后面事件的猪舍
                if(currentBarn != null) {
                    map.put("current_barn_name", currentBarn.get("barn_name"));
                    map.put("staff_name", currentBarn.get("staff_name"));//饲养员
                } else{
                    it.remove();
                }
            }else{
                Map<String,Object> currentBarns = doctorPigEventDao.findBarns(pigId,operatorName,barnType);//否则取当前猪舍
                if(currentBarns != null) {
                    map.put("current_barn_name", currentBarns.get("barn_name"));
                    map.put("staff_name", currentBarns.get("staff_name"));//饲养员
                } else{
                    it.remove();
                }
            }
            //Map<String,Object> frontEventId = doctorPigEventDao.frontEventId(pigId,time);//利用前一个事件来求母猪的胎次和状态
            //if(frontEventId != null) {
                Map<String,Object> frontEvent = doctorPigEventDao.frontEvent(parity,pigId,time,pigStatus);
                if(frontEvent != null) {
                    map.put("parity", frontEvent.get("parity"));//母猪胎次
                    map.put("status", frontEvent.get("pig_status_after"));//母猪状态

                    if(frontEvent.get("type") != null){
                        int a = (int)frontEvent.get("type");
                        if(a == 15 || a == 17 || a == 18 ||a == 19 ){
                            if(a == 15){
                                if(frontEvent.get("live_count") != null) {
                                    map.put("daizaishu", frontEvent.get("live_count"));//如果前一個事件是分娩事件，带仔数就是分娩事件的活仔数
                                }else{
                                    map.put("daizaishu",0);
                                }
                            }else{
                                Map<String,Object> nearDeliver = doctorPigEventDao.nearDeliver(pigId,time);
                                BigDecimal daizaishu = new BigDecimal((int)nearDeliver.get("live_count"));
                                List<Map<String,Object>> b = doctorPigEventDao.getdaizaishu(pigId,time,(Date)nearDeliver.get("event_at"));//如果是拼窝或者仔猪变动则需计算
                                for(int i = 0;i < b.size(); i++){
                                    if((int)b.get(i).get("type") == 17){
                                        daizaishu = daizaishu.subtract((BigDecimal)b.get(i).get("quantity"));
                                    }
                                    if((int)b.get(i).get("type") == 18){
                                        daizaishu = daizaishu.subtract((BigDecimal)b.get(i).get("quantity"));
                                    }
                                    if((int)b.get(i).get("type") == 19){
                                        daizaishu = daizaishu.add((BigDecimal)b.get(i).get("quantity"));
                                    }
                                }
                                map.put("daizaishu",daizaishu);
                            }
                        }else{
                            map.put("daizaishu",0);
                        }
                    }else{
                        map.put("daizaishu",0);
                    }
                }else{
                    it.remove();
                }
            }
       // }
        //}
        return inFarmPigId;
    }

    @Override
    public List<Map<String, Object>> boarReport(Long farmId, Date queryDate, String pigCode, String staffName, Integer barnId, Integer breedId, Integer pigStatus, Date beginDate, Date endDate) {
        List<Map<String, Object>> inFarmBoarId = doctorPigEventDao.getInFarmBoarId(farmId,queryDate,barnId,pigCode,breedId,staffName,pigStatus, beginDate,endDate);
        for (Iterator<Map<String,Object>> it = inFarmBoarId.iterator();it.hasNext();) {
            Map map = it.next();
            BigInteger pigId = (BigInteger)map.get("pig_id");
            Date eventAt = (Date)map.get("event_at");
            BigInteger isBoarBarn = doctorPigEventDao.isBoarBarn(pigId,eventAt,farmId);
            if (null != isBoarBarn){
                Map<String,Object> currentBoarBarn = doctorPigEventDao.findBoarBarn(isBoarBarn);//如果后面又转舍事件,去后面事件的猪舍
                if(currentBoarBarn != null) {
                    map.put("barn_name", currentBoarBarn.get("barn_name"));
                    map.put("staff_name", currentBoarBarn.get("staff_name"));//饲养员
                } else{
                    it.remove();
                }
            }
        }
        return inFarmBoarId;
    }
}
