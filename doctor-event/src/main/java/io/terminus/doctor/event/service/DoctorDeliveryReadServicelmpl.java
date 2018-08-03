package io.terminus.doctor.event.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
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
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorBarnDao doctorBarnDao;

    @Autowired
    public DoctorDeliveryReadServicelmpl(DoctorReportDeliverDao doctorReportDeliverDao,DoctorPigEventDao doctorPigEventDao,DoctorGroupEventDao doctorGroupEventDao,DoctorBarnDao doctorBarnDao) {
        this.doctorReportDeliverDao = doctorReportDeliverDao;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorBarnDao = doctorBarnDao;
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
    public List<Map<String,Object>> sowsReport(Long farmId,Date time,String pigCode,String operatorName,Long barnId,Integer breed,Integer parity,Integer pigStatus,Date beginInFarmTime, Date endInFarmTime){
        List<Map<String,Object>> inFarmPigId = null;
        if(pigStatus == 0){//全部
            inFarmPigId = doctorPigEventDao.getInFarmPigId(farmId,time,pigCode,breed, beginInFarmTime,endInFarmTime);//查询某个时间点之前所有进场和转场转入的猪
        }else if(pigStatus == 2){//离场
            inFarmPigId = doctorPigEventDao.getInFarmPigId2(farmId,time,pigCode,breed, beginInFarmTime,endInFarmTime);//查询某个时间点之前所有进场和转场转入的猪
        } else if(pigStatus == 10){//转场
            inFarmPigId = doctorPigEventDao.getInFarmPigId3(farmId,time,pigCode,breed, beginInFarmTime,endInFarmTime);//查询某个时间点之前所有进场和转场转入的猪
        }else {
            inFarmPigId = doctorPigEventDao.getInFarmPigId(farmId, time, pigCode, breed, beginInFarmTime, endInFarmTime);//查询某个时间点之前所有进场和转场转入的猪
        }
        /*boolean f = true;
        boolean g = true;
        boolean h = true;
        List<Map<String,Object>> j = new ArrayList<>();*/
        for(Iterator<Map<String,Object>> it = inFarmPigId.iterator();it.hasNext();){
            Map map = it.next();
            int source = (int)map.get("source");
            if (source == 1) {
                map.put("source","本厂");
            }else if (source == 2) {
                map.put("source","外购");
            }else{
                map.put("source","");
            }
            BigInteger id = (BigInteger)map.get("id");
            BigInteger pigId = (BigInteger)map.get("pig_id");
            Date eventAt = (Date)map.get("event_at");
            //BigInteger isBarn = doctorPigEventDao.isBarn(id,pigId,eventAt,time);//如果后面又转舍事件
            Map<String,Object> afterEvent = doctorPigEventDao.afterEvent(pigId,time);//查当前时间后面的第一个事件，利用这个时间求当前猪舍和猪场，如果没有则按当前猪场和猪舍
            BigInteger afterEventFarmId = null;
            if(afterEvent != null) {
                afterEventFarmId = (BigInteger) afterEvent.get("farm_id");
           // }
            //if(isBarn != null) {
                Map<String,Object> currentBarn = doctorPigEventDao.findBarn((BigInteger)afterEvent.get("id"),id,pigId,eventAt,time,operatorName,barnId);//如果后面又转舍事件,去后面事件的猪舍
                if(currentBarn != null) {
                    map.put("current_barn_name", currentBarn.get("barn_name"));
                    map.put("staff_name", currentBarn.get("staff_name"));//饲养员
                } else{
                    it.remove();
                    continue;
                    //f = false;
                }
            }else{
                Map<String,Object> currentBarns = doctorPigEventDao.findBarns(pigId,operatorName,barnId);//否则取当前猪舍
                afterEventFarmId = (BigInteger)doctorPigEventDao.findBarns(pigId,null,null).get("farm_id");
                if(currentBarns != null) {
                    map.put("current_barn_name", currentBarns.get("current_barn_name"));
                    map.put("staff_name", currentBarns.get("staff_name"));//饲养员
                } else{
                    it.remove();
                    continue;
                    //g = false;
                }
            }

                Map<String,Object> frontEvent = doctorPigEventDao.frontEvent(parity,pigId,time,pigStatus);
                if(frontEvent != null) {
                    map.put("parity", frontEvent.get("parity"));//母猪胎次
                    String Status = null;

                    if(afterEventFarmId == null || afterEventFarmId.equals(new BigInteger(farmId.toString()))) {
                        int status = (int) frontEvent.get("pig_status_after");

                        if (status == PigStatus.Entry.getKey()) {
                            Status = PigStatus.Entry.getName();
                        }
                        if (status == PigStatus.Removal.getKey()) {
                            Status = PigStatus.Removal.getName();
                        }
                        if (status == PigStatus.Mate.getKey()) {
                            Status = PigStatus.Mate.getName();
                        }
                        if (status == PigStatus.Pregnancy.getKey()) {
                            Status = PigStatus.Pregnancy.getName();
                        }
                        if (status == PigStatus.KongHuai.getKey()) {
                            Status = PigStatus.KongHuai.getName();
                        }
                        if (status == PigStatus.Farrow.getKey()) {
                            Status = PigStatus.Farrow.getName();
                        }
                        if (status == PigStatus.FEED.getKey()) {
                            Status = PigStatus.FEED.getName();
                        }
                        if (status == PigStatus.Wean.getKey()) {
                            Status = PigStatus.Wean.getName();
                        }
                        if (status == PigStatus.CHG_FARM.getKey()) {
                            Status = PigStatus.CHG_FARM.getName();
                        }
                    } else {
                        Status = "转场";
                    }
                    map.put("status", Status);//母猪状态
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
                /*if(f && g && h){
                    j.add(map);
                }
                if(j.size() == 20){
                    break;
                }*/
            }
       // }
        //}
        return inFarmPigId;
    }

    @Override
    public List<Map<String, Object>> boarReport(Long farmId, Date queryDate, String pigCode, String staffName, Integer barnId, Integer breedId, Date beginDate, Date endDate) {
        List<Map<String, Object>> inFarmBoarId = null;
        inFarmBoarId = doctorPigEventDao.getInFarmBoarId1(farmId,queryDate,barnId,pigCode,breedId,staffName,beginDate,endDate);
//        if(pigStatus == 11){
//            inFarmBoarId = doctorPigEventDao.getInFarmBoarId1(farmId,queryDate,barnId,pigCode,breedId,staffName,beginDate,endDate);
//        } else if(pigStatus == 12){
//            inFarmBoarId = doctorPigEventDao.getInFarmBoarId2(farmId,queryDate,barnId,pigCode,breedId,staffName,beginDate,endDate);
//        } else if (pigStatus == 13){
//            inFarmBoarId = doctorPigEventDao.getInFarmBoarId3(farmId,queryDate,barnId,pigCode,breedId,staffName,beginDate,endDate);
//        }
        for (Iterator<Map<String,Object>> it = inFarmBoarId.iterator();it.hasNext();) {
            Map map = it.next();
            if (map.get("source") != null){
                int source = (int)map.get("source");
                if (source == 1) {
                    map.put("source","本厂");
                }
                if (source == 2) {
                    map.put("source","外购");
                }
            }
            BigInteger id = (BigInteger)map.get("id");
            BigInteger pigId = (BigInteger)map.get("pig_id");
            Date eventAt = (Date)map.get("event_at");
            BigInteger isBoarBarn = doctorPigEventDao.isBoarBarn(id,pigId,eventAt,queryDate); //该时间点后是否有转舍事件发生
            if(isBoarBarn != null) {
                Map<String,Object> currentBoarBarn = doctorPigEventDao.findBoarBarn(isBoarBarn,id,pigId,eventAt,queryDate,staffName,barnId);//如果后面又转舍事件,去后面事件的猪舍
                if(currentBoarBarn != null) {
                    map.put("current_barn_name", currentBoarBarn.get("barn_name"));
                    map.put("staff_name", currentBoarBarn.get("staff_name"));//饲养员
                } else{
                    it.remove();
                }
            }else{
                Map<String,Object> currentBoarBarn = doctorPigEventDao.findBoarBarns(pigId,staffName,barnId);//否则取当前猪舍
                if(currentBoarBarn != null) {
                    map.put("current_barn_name", currentBoarBarn.get("current_barn_name"));
                    map.put("staff_name", currentBoarBarn.get("staff_name"));//饲养员
                } else{
                    it.remove();
                }
            }
//            if(pigStatus == 13){
//                Map<String,Object> currentBoarBarn = doctorPigEventDao.findBoarBarn1(pigId,staffName,barnId,farmId,queryDate);//取转场前猪舍
//                if(currentBoarBarn != null) {
//                    map.put("current_barn_name", currentBoarBarn.get("barn_name"));
//                    map.put("staff_name", currentBoarBarn.get("staff_name"));//饲养员
//                } else{
//                    it.remove();
//                }
//            }
//            if(pigStatus == 11){
//                map.put("status","已进场");
//            }
//            if(pigStatus == 12){
//                map.put("status","已离场");
//            }
//            if(pigStatus == 13){
//                map.put("status","已转场");
//            }
            map.put("status","已进场");

        }
        return inFarmBoarId;
    }

    @Override
    public Map<String,Object> groupReport(Long farmId,Date time,String groupCode,String operatorName,Long barn,Integer groupType,Integer groupStatus,Date buildBeginGroupTime,Date buildEndGroupTime,Date closeBeginGroupTime,Date closeEndGroupTime){
        List<Map<String,Object>> groupList = doctorGroupEventDao.groupList(farmId, time, barn, groupCode, operatorName, groupType, buildBeginGroupTime, buildEndGroupTime);;
        if(groupStatus == 1) {
            List<Map<String,Object>> groupList1 = doctorGroupEventDao.groupList1(farmId, time, barn, groupCode, operatorName, groupType, buildBeginGroupTime, buildEndGroupTime);
            groupList.addAll(groupList1);
        }
        int zongcunlan = 0;
        for(int i=0;i<groupList.size();i++){
            Map map = groupList.get(i);
            int status = (int) map.get("pig_type");
            if(status == 7){
                map.put("pig_type","产房仔猪");
            }
            if(status == 2){
                map.put("pig_type","保育猪");
            }
            if(status == 3){
                map.put("pig_type","育肥猪");
            }
            if(status == 4){
                map.put("pig_type","后备猪");
            }
            Long groupId = (Long)map.get("group_id");
            Integer getCunlan = doctorGroupEventDao.getCunlan(groupId,time);
            if(getCunlan != null) {
                map.put("cunlanshu", getCunlan);
                zongcunlan = zongcunlan + getCunlan;
            }else{
                map.put("cunlanshu", 0);
            }
            Double getInAvgweight = doctorGroupEventDao.getInAvgweight(groupId,time);
            if(getInAvgweight != null) {
                map.put("inAvgweight", (double)Math.round(getInAvgweight*1000)/1000);
            }else{
                map.put("inAvgweight", 0);
            }
            Double getOutAvgweight = doctorGroupEventDao.getOutAvgweight(groupId,time);
            if(getOutAvgweight != null) {
                map.put("outAvgweight", (double)Math.round(getOutAvgweight*1000)/1000);
            }else{
                map.put("outAvgweight", 0);
            }
            Double getAvgDayAge = doctorGroupEventDao.getAvgDayAge(groupId,time);
            if(getAvgDayAge != null && getCunlan != null && getCunlan != 0) {
                map.put("getAvgDayAge", (double)Math.round((getAvgDayAge / getCunlan)*1000)/1000);
            }else{
                map.put("getAvgDayAge", 0);
            }
        }
        Map<String,Object> data = new HashMap<>();
        data.put("data",groupList);
        data.put("zongcunlan",zongcunlan);
        return data;
    }
    public List<Map<String,Object>> barnsReport(Long farmId,String operatorName,Long isbarnId,Date beginTime,Date endTime){
        List<Long> barnIds = new ArrayList<>();
        if(isbarnId == 0) {
            barnIds = doctorBarnDao.findBarnIdsByfarmId(farmId, operatorName);
        }else{
             barnIds.add(isbarnId);
        }
        if(barnIds != null) {
            List<Map<String,Object>> list = new ArrayList<>();
            //Map map = new HashMap();
            for(Long barnId: barnIds) {

                Map<String, Object> map = doctorBarnDao.findBarnTypeById(barnId);
                int barnType = (int) (map.get("pig_type"));
                if (barnType == 5 || barnType == 6 || barnType == 7 || barnType == 9) {
                    Integer qichucunlan = doctorBarnDao.qichucunlan(farmId, barnId, beginTime);
                    Integer qimucunlan = doctorBarnDao.qimucunlan(farmId, barnId, endTime);
                    if (qichucunlan != null) {
                        map.put("qichucunlan", qichucunlan);
                    } else {
                        map.put("qichucunlan", 0);
                    }
                    if (qimucunlan != null) {
                        map.put("qimucunlan", qimucunlan);
                    } else {
                        map.put("qimucunlan", 0);
                    }
                    List<Map<Integer, Long>> jianshao = doctorBarnDao.jianshao(barnId, beginTime, endTime);
                    Long qitajianshao = 0L;
                    if (jianshao != null) {
                        for (int i = 0; i < jianshao.size(); i++) {
                            Long a = jianshao.get(i).get("change_type_id");
                            if (a == 109) {
                                map.put("xiaoshou", jianshao.get(i).get("count"));
                            }
                            if (a == 110) {
                                map.put("siwang", jianshao.get(i).get("count"));
                            }
                            if (a == 111) {
                                map.put("taotai", jianshao.get(i).get("count"));
                            }
                            if (a == 112 || a == 113 || a == 114 || a == 115) {
                                qitajianshao = qitajianshao + jianshao.get(i).get("count");
                            }
                        }
                        map.put("qitajianshao", qitajianshao);
                    } else {
                        map.put("xiaoshou", 0);
                        map.put("siwang", 0);
                        map.put("taotai", 0);
                        map.put("qitajianshao", 0);
                    }
                    Integer zhuanchu = doctorBarnDao.zhuanchu(barnId, beginTime, endTime);
                    if (zhuanchu != null) {
                        map.put("zhuanchu", zhuanchu);
                    } else {
                        map.put("zhuanchu", 0);
                    }
                    Long zhuanru = qimucunlan - qichucunlan + (zhuanchu == null ? 0 : zhuanchu) + (map.get("xiaoshou") == null ? 0 : (Long) map.get("xiaoshou")) + (map.get("siwang") == null ? 0 : (Long) map.get("siwang")) + (map.get("taotai") == null ? 0 : (Long) map.get("taotai")) + (map.get("qitajianshao") == null ? 0 : (Long) map.get("qitajianshao"));
                    map.put("zhuanru", zhuanru);
                }
                if (barnType == 2 || barnType == 3 || barnType == 4) {
                    Integer qichucunlan = doctorBarnDao.groupqichucunlan(farmId, barnId, beginTime);
                    Integer qimucunlan = doctorBarnDao.groupqimucunlan(farmId, barnId, endTime);
                    if (qichucunlan != null) {
                        map.put("qichucunlan", qichucunlan);
                    } else {
                        map.put("qichucunlan", 0);
                    }
                    if (qimucunlan != null) {
                        map.put("qimucunlan", qimucunlan);
                    } else {
                        map.put("qimucunlan", 0);
                    }
                    Integer zhuanru = doctorBarnDao.groupzhuanru(barnId,beginTime,endTime);
                    map.put("zhuanru", zhuanru);
                    Integer zhuanchu = doctorBarnDao.groupzhuanchu(barnId,beginTime,endTime);
                    if (zhuanchu != null) {
                        map.put("zhuanchu", zhuanchu);
                    } else {
                        map.put("zhuanchu", 0);
                    }
                    List<Map<Integer, Long>> jianshao = doctorBarnDao.groupjianshao(barnId, beginTime, endTime);
                    int qitajianshao = 0;
                    if (jianshao != null) {
                        for (int i = 0; i < jianshao.size(); i++) {
                            Long a = jianshao.get(i).get("change_type_id");
                            if (a == 109) {
                                map.put("xiaoshou", jianshao.get(i).get("count"));
                            }
                            if (a == 110) {
                                map.put("siwang", jianshao.get(i).get("count"));
                            }
                            if (a == 111) {
                                map.put("taotai", jianshao.get(i).get("count"));
                            }
                            if (a == 112 || a == 113 || a == 114 || a == 115) {
                                Object  ob = jianshao.get(i).get("count");
                                int b =Integer.parseInt(ob.toString());
                               qitajianshao = qitajianshao+b;
                            }
                        }
                        map.put("qitajianshao", qitajianshao);
                    } else {
                        map.put("xiaoshou", 0);
                        map.put("siwang", 0);
                        map.put("taotai", 0);
                        map.put("qitajianshao", 0);
                    }
                }
                list.add(map);
            }
            return list;
        } else{
            return null;
        }
    }

}
