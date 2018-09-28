package io.terminus.doctor.event.service;

import com.google.common.base.Stopwatch;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.*;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigNpd;
import io.terminus.doctor.event.model.DoctorReportNpd;
import io.terminus.doctor.event.model.DoctorSowNpdDayly;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2017/12/21.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorReportWriteServiceImpl implements DoctorReportWriteService {

    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorPigReportReadService doctorPigReportReadService;
    @Autowired
    private DoctorPigDailyDao doctorPigDailyDao;
    @Autowired
    private DoctorReportNpdDao doctorReportNpdDao;
    @Autowired
    private DoctorPigNpdDao doctorPigNpdDao;
    @Autowired
    private DoctorSowNpdDaylyDao doctorSowNpdDaylyDao;

    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    @Override
    public void flushNPD(List<Long> farmIds, Date countDate, ReportTime reportTime) {

        if (reportTime == ReportTime.DAY)
            throw new ServiceException("report.time.day.not.support");

        DoctorPigReportReadService.DateDuration dateDuration = doctorPigReportReadService.getDuration(countDate, reportTime);

//        flushNPD(farmIds, dateDuration.getStart(), dateDuration.getEnd());
        NPD(farmIds, dateDuration.getStart(), dateDuration.getEnd());
    }

    @Override
    public void flushNPD(List<Long> farmIds, Date start) {
        Date startAtMonth = DateUtil.monthStart(start);//日期所在月的第一天
        Date end = DateUtil.monthEnd(new Date());

//        flushNPD(farmIds, startAtMonth, end);
        NPD(farmIds, startAtMonth, end);
    }

    @Override
    public void flushNPD(Date start) {

        List<DoctorFarm> farms = RespHelper.orServEx(doctorFarmReadService.findAllFarms());

        flushNPD(farms.stream().map(DoctorFarm::getId).collect(Collectors.toList()), start);
    }

    public void deleteNPD(List<Long> farmIds, int year, int month){
        Map<String, Object> params = new HashMap<>();
        for (int i =0; i < farmIds.size(); i++){
            params.put("farmId",String.valueOf(farmIds.get(i)));
            params.put("year",year);
            params.put("month",month);
        }
        doctorSowNpdDaylyDao.deleteNPD(params);
    }

    public void NPD (List<Long> farmIds, Date startDate, Date endDate){
        Map<String, Object> params = new HashMap<>();
        for (int i =0; i < farmIds.size(); i++){
            params.put("farmId",String.valueOf(farmIds.get(i)));
            params.put("startDate",startDate);
            params.put("endDate",endDate);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        deleteNPD(farmIds, year, month);

        List<Map<String, Object>> listPIG = doctorPigEventDao.selectPIG(params);
        for (int i = 0; i < listPIG.size(); i++) {
            // 加载猪id
            String pigId = String.valueOf(listPIG.get(i).get("id"));
//            if(pigId.equals("997036") || pigId.equals("364859") || pigId.equals("1004429")){
//                params.put("pigId",pigId);
//                flushSowNPD(params);
//             }
            params.put("pigId", listPIG == null ? "0" : String.valueOf(listPIG.get(i).get("id")));
            flushSowNPD(params);
            flushReportNpd(params, startDate, endDate);
        }
    }

    private boolean existsAryValue(String[] arys, String exy) {
        for (String string : arys) {
            if (exy.equals(string)) {
                return true;
            }
        }
        return false;
    }

    private int differentDaysByMillisecond(Date date1, Date date2) {
        int days = (int) ((date1.getTime() - date2.getTime()) / (1000 * 3600 * 24));
        return days;
    }

    public void flushSowNPD(Map<String, Object> params){
        try {
            List<Map<String, Object>> listNPD = doctorPigEventDao.selectNPD(params);

            if (listNPD.size() > 0) {
                String[] pz = new String[] { "1", "4", "5", "6", "9", "11" };
                String[] rj = new String[] { "2", "3" };
                String[] zclc = new String[] { "1", "2", "3", "4", "5", "6", "9", "11" };
                String[] fm = new String[] { "3" };
                String[] dn = new String[] { "7" };

                boolean firstFlag = false; // 最后一个事件是否为阳性的标志变量，false表示否，true表示是

                for (int i = listNPD.size() - 1; i >= 0; i--) // 不考虑最后第一个事件，第一个事件为历史记录
                {
                    String eventType = listNPD.get(i).get("type").toString().trim();
                    params.put("orgId",listNPD.get(i).get("orgId"));
                    if (eventType.equals("11")) {
                        String pregCheckResult = null == listNPD.get(i).get("pregCheckResult")
                                || "".equals(listNPD.get(i).get("pregCheckResult").toString().trim())
                                || "null".equals(listNPD.get(i).get("pregCheckResult").toString().trim().toLowerCase())
                                ? ""
                                : listNPD.get(i).get("pregCheckResult").toString().trim();
                        if (pregCheckResult.equals("1")) {
                            firstFlag = true;
                        }
                        break;
                    }
                }

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                for (int i = 0; i < listNPD.size(); i++) {
                    Map<String, Object> thisObj = listNPD.get(i);
                    String eventType = thisObj.get("eventType").toString().trim();
                    String eventTime = thisObj.get("eventAt").toString().trim();
                    String xh = thisObj.get("xh").toString().trim();
                    //params.put("pigId", listPIG == null ? "0" : String.valueOf(listPIG.get(i).get("id")));
                    String pregCheckResult = (thisObj.get("pregCheckResult") != null ? thisObj.get("pregCheckResult").toString().trim() : "" );
                    String changeTypeId = (thisObj.get("changeTypeId") != null ? thisObj.get("changeTypeId").toString().trim() : "" );

                    DoctorSowNpdDayly entity = new DoctorSowNpdDayly();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(simpleDateFormat.parse(eventTime));
                    entity.setFiscalYear(cal.get(Calendar.YEAR));
                    int month = cal.get(Calendar.MONTH) + 1;
                    entity.setFiscalPeriod(month);
                    int quat = 0;
                    if (month == 1 || month == 2 || month == 3) {
                        quat = 1;
                    } else if (month == 4 || month == 5 || month == 6) {
                        quat = 2;
                    } else if (month == 7 || month == 8 || month == 9) {
                        quat = 3;
                    } else {
                        quat = 4;
                    }
                    entity.setFiscalQuarter(quat);
                    //查找转场猪的,前一个事件
                    params.put("eTime", eventTime);
                    List<Map<String, Object>> listEvent = doctorPigEventDao.selectEvent(params);
                    String eventAt = null;
                    if(listEvent != null){
                        for (int j = 0; j < listEvent.size(); j++){
                            Map<String, Object> mapEvent = listEvent.get(j);
                            eventAt = mapEvent.get("event_at").toString().trim();
                            params.put("eventAt", eventAt);
                        }
                    }

                    //转场事件
                    List<Map<String, Object>> listType = doctorPigEventDao.selectType(params);
                    if(listType != null){
                        for (int k = 0; k < listType.size(); k++){
                            int diffDay1 = 0;
                            Map<String, Object> mapType = listType.get(k);
                            String eventDate = mapType.get("event_at").toString();
                            long farmId = Long.parseLong(mapType.get("farm_id").toString());
                            long orgId = Long.parseLong(mapType.get("org_id").toString());
                            long pigId = Long.parseLong(mapType.get("pig_id").toString());
                            long type = Long.parseLong(mapType.get("type").toString());
                            Date time = simpleDateFormat.parse(eventDate);
                            String listDate = doctorPigEventDao.selectDate(time,farmId,pigId);
                            if (listDate != null){
                                eventAt = listDate;
                            }
                            // 非生产天数
                            if(type == 2){
                                diffDay1 = differentDaysByMillisecond(simpleDateFormat.parse(eventDate),
                                        simpleDateFormat.parse(eventAt));
                                entity.setLastEventName("转场");
                                entity.setLastEventType(10);
                            } else {
                                diffDay1 = differentDaysByMillisecond(simpleDateFormat.parse(eventTime),
                                        simpleDateFormat.parse(eventAt));
                                entity.setLastEventName("转场转入");
                                entity.setLastEventType(11);
                            }
                            if(eventType.equals("1")){
                                entity.setLactationDate(0);
                                entity.setGestationDate(0);
                                entity.setJcNpd(diffDay1);
                                entity.setNpdDate(diffDay1);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setCurrentEventName(thisObj.get("name").toString());
                            } else if(eventType.equals("2")){
                                entity.setLactationDate(0);
                                entity.setGestationDate(0);
                                entity.setJcNpd(diffDay1);
                                entity.setNpdDate(diffDay1);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setCurrentEventName(thisObj.get("name").toString());
                                entity.setCurrentEventDate(simpleDateFormat.parse(eventTime));
                            } else if(eventType.equals("3")){
                                entity.setLactationDate(0);
                                entity.setGestationDate(0);
                                entity.setJcNpd(diffDay1);
                                entity.setNpdDate(diffDay1);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setCurrentEventName(thisObj.get("name").toString());
                                entity.setCurrentEventDate(simpleDateFormat.parse(eventTime));
                            }else if(eventType.equals("4")){
                                entity.setLactationDate(0);
                                entity.setGestationDate(0);
                                entity.setJcNpd(diffDay1);
                                entity.setNpdDate(diffDay1);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setCurrentEventName(thisObj.get("name").toString());
                                entity.setCurrentEventDate(simpleDateFormat.parse(eventTime));
                            }else if(eventType.equals("5")){
                                entity.setLactationDate(0);
                                entity.setGestationDate(0);
                                entity.setJcNpd(diffDay1);
                                entity.setNpdDate(diffDay1);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setCurrentEventName(thisObj.get("name").toString());
                                entity.setCurrentEventDate(simpleDateFormat.parse(eventTime));
                            }else if(eventType.equals("6")){
                                entity.setLactationDate(0);
                                entity.setGestationDate(0);
                                entity.setJcNpd(diffDay1);
                                entity.setNpdDate(diffDay1);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setCurrentEventName(thisObj.get("name").toString());
                                entity.setCurrentEventDate(simpleDateFormat.parse(eventTime));
                            }else if(eventType.equals("7")){
                                entity.setLactationDate(0);
                                entity.setGestationDate(diffDay1);
                                entity.setJcNpd(0);
                                entity.setNpdDate(0);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setCurrentEventName(thisObj.get("name").toString());
                                entity.setCurrentEventDate(simpleDateFormat.parse(eventTime));
                            }else if(eventType.equals("8")){
                                entity.setLactationDate(0);
                                entity.setGestationDate(0);
                                entity.setJcNpd(diffDay1);
                                entity.setNpdDate(diffDay1);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setCurrentEventName(thisObj.get("name").toString());
                                entity.setCurrentEventDate(simpleDateFormat.parse(eventTime));
                            } else if(eventType.equals("9")){
                                entity.setLactationDate(diffDay1);
                                entity.setGestationDate(0);
                                entity.setJcNpd(0);
                                entity.setNpdDate(0);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setCurrentEventName(thisObj.get("name").toString());
                                entity.setCurrentEventDate(simpleDateFormat.parse(eventTime));
                            }

                            entity.setFarmId(farmId);
                            entity.setOrgId(orgId);
                            entity.setPigId(pigId);
                            entity.setBarnId(Long.parseLong(mapType.get("barn_id").toString()));
                            entity.setLastEventDate(simpleDateFormat.parse(eventAt));
                            entity.setFqNpd(0);
                            entity.setLcNpd(0);
                            entity.setTtNpd(0);
                            entity.setSwNpd(0);
                            entity.setDnpzNpd(0);
                            entity.setEventId(Long.parseLong(mapType.get("id").toString().trim()));
                            entity.setBarnId(Long.parseLong(mapType.get("barn_id").toString().trim()));
                            entity.setParity(Integer.parseInt(mapType.get("parity").toString()));
                            doctorSowNpdDaylyDao.create(entity);
                        }
                    }

                    if (xh.equals("0"))
                        continue; // 0表示历史数据

                    if ((firstFlag && (eventType.equals("4") // 阴性
                            || eventType.equals("5") // 流产
                            || eventType.equals("6") // 返情
                    )) || eventType.equals("3") // 所有阳性都不处理
                            ) {
                        continue; // 跳过不处理
                    }

                    if (i > 0) {
                        Map<String, Object> prevObj = null;
                        Map<String, Object> thenObj = listNPD.get(i - 1); // 只找前一条事件记录，不要跳着找
                        String thenEventType = thenObj.get("eventType").toString().trim();
                        if (eventType.equals("2")) // 配种
                        {
                            if (existsAryValue(pz, thenEventType)) // 匹配上了
                            {
                                if (eventType.equals(thenEventType)) { // 复配的情况
                                    Integer currentMatingCount = null == thisObj.get("currentMatingCount")
                                            || "".equals(thisObj.get("currentMatingCount").toString().trim())
                                            || "null".equals(
                                            thisObj.get("currentMatingCount").toString().trim().toLowerCase())
                                            ? 0
                                            : Integer.parseInt(
                                            thisObj.get("currentMatingCount").toString());
                                    if (currentMatingCount <= 1) { // 该字段大于1表示当前事件为复配
                                        prevObj = thenObj;
                                    }
                                } else {
                                    prevObj = thenObj;
                                }
                            }
                        } else if (eventType.equals("4") || eventType.equals("5") || eventType.equals("6")) // 阴性、流产或返情
                        {
                            if (existsAryValue(rj, thenEventType)) // 匹配上了，配种或阳性
                            {
                                // 从日记账表中寻找最后一个事件
                                params.put("edate", eventTime);
                                List<Map<String, Object>> sel = doctorPigEventDao.selectLastEndNPD(params);
                                if (sel.size() > 0) {
                                    prevObj = new LinkedHashMap<String, Object>();
                                    // 真正比较的时间
                                    prevObj.put("compareEventAt", sel.get(0).get("currentEventDate"));
                                    prevObj.put("eventType", thenEventType);
                                    prevObj.put("name", thenObj.get("name"));
                                    prevObj.put("eventAt", thenObj.get("eventAt"));
                                }
                            }
                        } else if (eventType.equals("8")) { // 离场或转场
                            if (existsAryValue(zclc, thenEventType)) // 匹配上了
                            {
                                if (thenEventType.equals("3")) // 阳性
                                {
                                    // 从日记账表中寻找最后一个事件
                                    params.put("edate", eventTime);
                                    List<Map<String, Object>> sel = doctorPigEventDao.selectLastEndNPD(params);
                                    if (sel.size() > 0) {
                                        prevObj = new LinkedHashMap<String, Object>();
                                        // 真正比较的时间
                                        prevObj.put("compareEventAt", sel.get(0).get("currentEventDate"));
                                        prevObj.put("eventType", thenEventType);
                                        prevObj.put("name", thenObj.get("name"));
                                        prevObj.put("eventAt", thenObj.get("eventAt"));
                                    }
                                } else { // 否则就找匹配的前一事件
                                    prevObj = thenObj;
                                }
                            }
                        } else if (eventType.equals("7")) { // 分娩
                            if (existsAryValue(fm, thenEventType)) // 匹配上了
                            {
                                // 查询最后一次配种事件
                                params.put("inderDate", eventTime);
                                List<Map<String, Object>> sel = doctorPigEventDao.selectStartNPD(params);
                                if (sel.size() > 0) {
                                    prevObj = new LinkedHashMap<String, Object>();
                                    // 真正比较的时间
                                    prevObj.put("compareEventAt", sel.get(0).get("currentEventDate"));
                                    prevObj.put("eventType", thenEventType);
                                    prevObj.put("name", thenObj.get("name"));
                                    prevObj.put("eventAt", thenObj.get("eventAt"));
                                }
                            }
                        } else if (eventType.equals("9")) { // 断奶
                            if (existsAryValue(dn, thenEventType)) // 匹配上了
                            {
                                prevObj = thenObj;
                            }
                        }

                        // 开始计算非生产天数与生产天数
                        int diffDay = 0;
                        if (eventType.equals("2") && prevObj != null) // 配种
                        {
                            // 算非生产天数
                            String prevDate = prevObj.get("eventAt").toString();
                            diffDay = differentDaysByMillisecond(simpleDateFormat.parse(eventTime),
                                    simpleDateFormat.parse(prevDate));
                        } else if ((eventType.equals("4") || eventType.equals("5") || eventType.equals("6"))
                                && prevObj != null) // 阴性、流产或返情
                        {
                            String prevDate =  prevObj.get("compareEventAt").toString();
                            diffDay = differentDaysByMillisecond(simpleDateFormat.parse(eventTime),
                                    simpleDateFormat.parse(prevDate));
                        } else if (eventType.equals("8") && prevObj != null) // 离场或转场
                        {
                            String prevDate = null;
                            if (thenEventType.equals("3")) {
                                prevDate = prevObj.get("compareEventAt").toString();
                            } else {
                                prevDate = prevObj.get("eventAt").toString();
                            }
                            diffDay = differentDaysByMillisecond(simpleDateFormat.parse(eventTime),
                                    simpleDateFormat.parse(prevDate));
                        } else if (eventType.equals("7") && prevObj != null) // 分娩
                        {
                            String prevDate = prevObj.get("compareEventAt").toString();
                            diffDay = differentDaysByMillisecond(simpleDateFormat.parse(eventTime),
                                    simpleDateFormat.parse(prevDate));
                        } else if (eventType.equals("9") && prevObj != null) // 断奶
                        {
                            String prevDate = prevObj.get("eventAt").toString();
                            diffDay = differentDaysByMillisecond(simpleDateFormat.parse(eventTime),
                                    simpleDateFormat.parse(prevDate));
                        }

                        // 删除并添加最新记录
                        if (prevObj != null) {
                            int liveCount = 0;
//                            DoctorSowNpdDayly entity = new DoctorSowNpdDayly();
                            entity.setOrgId(Long.parseLong(params.get("orgId").toString()));
                            entity.setFarmId(Long.parseLong(params.get("farmId").toString()));

                            if (eventType.equals("7")) { // 分娩
                                liveCount = null == thisObj.get("liveCount") // 分娩事件
                                        || "".equals(thisObj.get("liveCount").toString().trim())
                                        || "null".equals(thisObj.get("liveCount").toString().trim().toLowerCase()) ? 0
                                        : Integer.parseInt(thisObj.get("liveCount").toString().trim());
                            } else if (eventType.equals("9")) { // 断奶
                                liveCount = null == prevObj.get("liveCount") // 分娩事件
                                        || "".equals(prevObj.get("liveCount").toString().trim())
                                        || "null".equals(prevObj.get("liveCount").toString().trim().toLowerCase()) ? 0
                                        : Integer.parseInt(prevObj.get("liveCount").toString().trim());
                            }

                            entity.setPigId(Long.parseLong(thisObj.get("pigId").toString()));
                            if (thenEventType.equals("3")) {
                                entity.setLastEventName("妊娠阳性");
                            } else if (thenEventType.equals("4")) {
                                entity.setLastEventName("妊娠阴性");
                            } else if (thenEventType.equals("5")) {
                                entity.setLastEventName("妊娠流产");
                            } else if (thenEventType.equals("6")) {
                                entity.setLastEventName("妊娠返情");
                            } else {
                                if (eventType.equals("9") && liveCount == 0) {
                                    entity.setLastEventName("分娩(全死胎)");
                                } else {
                                    entity.setLastEventName(prevObj.get("name").toString());
                                }
                            }
                            entity.setLastEventDate(simpleDateFormat.parse(prevObj.get("eventAt").toString()));
                            entity.setLastEventType(Integer.parseInt(prevObj.get("eventType").toString()));
                            if (eventType.equals("3")) {
                                entity.setCurrentEventName("妊娠阳性");
                            } else if (eventType.equals("4")) {
                                entity.setCurrentEventName("妊娠阴性");
                            } else if (eventType.equals("5")) {
                                entity.setCurrentEventName("妊娠流产");
                            } else if (eventType.equals("6")) {
                                entity.setCurrentEventName("妊娠返情");
                            } else {
                                entity.setCurrentEventName(thisObj.get("name").toString());
                            }
                            entity.setCurrentEventDate(simpleDateFormat.parse(eventTime));

                            if (eventType.equals("7")) {
                                if (liveCount == 0) { // 分娩死胎或0胎的情况下
                                    entity.setCurrentEventName("分娩(全死胎)");
                                }
                                entity.setGestationDate(diffDay); // 孕期天数
                                entity.setLactationDate(0);
                                entity.setNpdDate(0);
                                entity.setJcNpd(0);
                                entity.setFqNpd(0);
                                entity.setLcNpd(0);
                                entity.setTtNpd(0);
                                entity.setSwNpd(0);
                                entity.setDnpzNpd(0);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setLastEventDate(simpleDateFormat.parse(prevObj.get("eventAt").toString()));
                            } else if (eventType.equals("9")) {
                                entity.setCurrentEventName("断奶");
                                entity.setGestationDate(0);
                                entity.setLactationDate(diffDay); // 哺乳期天数
                                entity.setNpdDate(0);
                                entity.setJcNpd(0);
                                entity.setFqNpd(0);
                                entity.setLcNpd(0);
                                entity.setTtNpd(0);
                                entity.setSwNpd(0);
                                entity.setDnpzNpd(0);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setLastEventDate(simpleDateFormat.parse(prevObj.get("eventAt").toString()));
                            } else if(eventType.equals("2") && (thenEventType.equals("1") || thenEventType.equals("11"))) {
                                entity.setCurrentEventName("配种");
                                entity.setGestationDate(0);
                                entity.setLactationDate(0);
                                entity.setJcNpd(diffDay); // jc非生产天数(pz)
                                entity.setNpdDate(diffDay);
                                entity.setFqNpd(0);
                                entity.setLcNpd(0);
                                entity.setTtNpd(0);
                                entity.setSwNpd(0);
                                entity.setDnpzNpd(0);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setLastEventDate(simpleDateFormat.parse(prevObj.get("eventAt").toString()));
                            } else if(eventType.equals("2") && thenEventType.equals("9")) {
                                entity.setCurrentEventName("配种");
                                entity.setGestationDate(0);
                                entity.setLactationDate(0);
                                entity.setDnpzNpd(diffDay); // dnpz非生产天数（配种）
                                entity.setNpdDate(diffDay);
                                entity.setJcNpd(0);
                                entity.setFqNpd(0);
                                entity.setLcNpd(0);
                                entity.setTtNpd(0);
                                entity.setSwNpd(0);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setLastEventDate(simpleDateFormat.parse(prevObj.get("eventAt").toString()));
                            } else if(listType.size() == 0 && eventType.equals("6")) {
                                entity.setCurrentEventName("妊检返情");
                                entity.setGestationDate(0);
                                entity.setLactationDate(0);
                                entity.setFqNpd(diffDay); // fq非生产天数
                                entity.setNpdDate(diffDay);
                                entity.setJcNpd(0);
                                entity.setLcNpd(0);
                                entity.setTtNpd(0);
                                entity.setSwNpd(0);
                                entity.setDnpzNpd(0);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setLastEventDate(simpleDateFormat.parse(prevObj.get("eventAt").toString()));
                            } else if(listType.size() == 0 && eventType.equals("4")) {
                                entity.setCurrentEventName("妊检阴性");
                                entity.setGestationDate(0);
                                entity.setLactationDate(0);
                                entity.setFqNpd(diffDay); // fq非生产天数
                                entity.setNpdDate(diffDay);
                                entity.setJcNpd(0);
                                entity.setLcNpd(0);
                                entity.setTtNpd(0);
                                entity.setSwNpd(0);
                                entity.setDnpzNpd(0);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setLastEventDate(simpleDateFormat.parse(prevObj.get("eventAt").toString()));
                            } else if((eventType.equals("2") && thenEventType.equals("6"))
                                    || (eventType.equals("2") && thenEventType.equals("4"))){
                                entity.setCurrentEventName("配种");
                                entity.setGestationDate(0);
                                entity.setLactationDate(0);
                                entity.setFqNpd(diffDay); // fq非生产天数（配种）
                                entity.setNpdDate(diffDay);
                                entity.setJcNpd(0);
                                entity.setLcNpd(0);
                                entity.setTtNpd(0);
                                entity.setSwNpd(0);
                                entity.setDnpzNpd(0);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setLastEventDate(simpleDateFormat.parse(prevObj.get("eventAt").toString()));
                            } else if((eventType.equals("2") && thenEventType.equals("5"))){
                                entity.setCurrentEventName("配种");
                                entity.setGestationDate(0);
                                entity.setLactationDate(0);
                                entity.setLcNpd(diffDay); // lc非生产天数(配种)
                                entity.setNpdDate(diffDay);
                                entity.setJcNpd(0);
                                entity.setFqNpd(0);
                                entity.setTtNpd(0);
                                entity.setSwNpd(0);
                                entity.setDnpzNpd(0);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setLastEventDate(simpleDateFormat.parse(prevObj.get("eventAt").toString()));
                            }else if(listType.size() == 0 && eventType.equals("5")) {
                                entity.setCurrentEventName("妊检流产");
                                entity.setGestationDate(0);
                                entity.setLactationDate(0);
                                entity.setLcNpd(diffDay); // lc非生产天数
                                entity.setNpdDate(diffDay);
                                entity.setJcNpd(0);
                                entity.setFqNpd(0);
                                entity.setTtNpd(0);
                                entity.setSwNpd(0);
                                entity.setDnpzNpd(0);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setLastEventDate(simpleDateFormat.parse(prevObj.get("eventAt").toString()));
                            } else if(changeTypeId.equals("110")) {
                                entity.setCurrentEventName("死亡");
                                entity.setGestationDate(0);
                                entity.setLactationDate(0);
                                entity.setSwNpd(diffDay); // sw非生产天数
                                entity.setNpdDate(diffDay);
                                entity.setJcNpd(0);
                                entity.setFqNpd(0);
                                entity.setLcNpd(0);
                                entity.setTtNpd(0);
                                entity.setDnpzNpd(0);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setLastEventDate(simpleDateFormat.parse(prevObj.get("eventAt").toString()));
                            } else if(!changeTypeId.equals("110")) {
                                entity.setCurrentEventName("淘汰");
                                entity.setGestationDate(0);
                                entity.setLactationDate(0);
                                entity.setTtNpd(diffDay); // tt非生产天数
                                entity.setNpdDate(diffDay);
                                entity.setJcNpd(0);
                                entity.setFqNpd(0);
                                entity.setLcNpd(0);
                                entity.setSwNpd(0);
                                entity.setDnpzNpd(0);
                                entity.setCurrentEventType(Integer.parseInt(eventType.toString()));
                                entity.setLastEventDate(simpleDateFormat.parse(prevObj.get("eventAt").toString()));
                            }
                            entity.setEventId(Long.parseLong(thisObj.get("eventId").toString().trim()));
                            entity.setBarnId(Long.parseLong(thisObj.get("barnId").toString().trim()));
                            entity.setParity(Integer.parseInt(thisObj.get("parity").toString()));
                            doctorSowNpdDaylyDao.create(entity);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("NpdJournelError===>   " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void flushReportNpd(Map<String, Object> params, Date startDate, Date endDate){
        Map<Long/*farmID*/, Map<String/*year-month*/, Integer/*怀孕天数*/>> farmPregnancy = new HashMap<>();
        Map<Long/*farmID*/, Map<String/*year-month*/, Integer/*哺乳天数*/>> farmLactation = new HashMap<>();
        Map<Long/*farmID*/, Map<String/*year-month*/, Integer/*非生产天数*/>> farmNPD = new HashMap<>();
        Long farmId = Long.parseLong(params.get("farmId").toString());
        DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(farmId));

        for (Date i = startDate; i.before(endDate); i = DateUtils.addMonths(i, 1)) {

            Date monthEndDate = DateUtil.monthEnd(i);

            int dayCount = DateUtil.getDeltaDays(i, monthEndDate) + 1;

            DoctorReportNpd npd = doctorReportNpdDao.findByFarmAndSumAt(farmId, i).orElseGet(() -> new DoctorReportNpd());
            npd.setFarmId(farmId);
            npd.setDays(dayCount);

            if (log.isDebugEnabled())
                log.debug("刷新猪场{},从{}到{}", farm.getId(), DateUtil.toDateString(i), DateUtil.toDateString(monthEndDate));
            Integer sowCount = doctorPigDailyDao.countSow(farmId, i, monthEndDate);
            npd.setSowCount(sowCount);

            npd.setSumAt(i);

            int year = new DateTime(i).getYear();
            int month = new DateTime(i).getMonthOfYear();
            String monthAndYearKey = year + "-" + month;

            npd.setNpd(getCount(farmId, monthAndYearKey, farmNPD));
            npd.setPregnancy(getCount(farmId, monthAndYearKey, farmPregnancy));
            npd.setLactation(getCount(farmId, monthAndYearKey, farmLactation));

            npd.setOrgId(null == farm ? null : farm.getOrgId());
            if (null == npd.getId())
                doctorReportNpdDao.create(npd);
            else
                doctorReportNpdDao.update(npd);
        }
    }

    public void flushNPD(List<Long> farmIds, Date startDate, Date endDate) {

        Stopwatch stopwatch = Stopwatch.createStarted();

        Map<Long/*farmID*/, Map<String/*year-month*/, Integer/*怀孕天数*/>> farmPregnancy = new HashMap<>();
        Map<Long/*farmID*/, Map<String/*year-month*/, Integer/*哺乳天数*/>> farmLactation = new HashMap<>();
        Map<Long/*farmID*/, Map<String/*year-month*/, Integer/*非生产天数*/>> farmNPD = new HashMap<>();
        List<DoctorPigNpd> doctorPigNpds = new ArrayList<>();

        Map<Long, Integer> pigCount = new HashMap<>();
        log.info("start flush npd from {} to {}", startDate, endDate);

        //查询在指定日期内指定猪场发生事件的猪
        //List<Long> pigs = doctorPigEventDao.findPigAtEvent(startDate, endDate, farmIds);

        farmIds.forEach(f -> {

            //查该猪场下所有事件
            Map<Long, List<DoctorPigEvent>> pigEvents = doctorPigEventDao.findForNPD(f, startDate, endDate)
                    .parallelStream()
                    .collect(Collectors.groupingBy(DoctorPigEvent::getPigId));

            log.info("farm {},total {} pig event", f, pigEvents.size());
            //查出所有事件
            pigEvents.forEach((pigId, events) -> {

                DoctorPigNpd pigNpd = new DoctorPigNpd();
                pigNpd.setPigId(pigId);
                pigNpd.setFarmId(f);
                pigNpd.setSumAt(startDate);

                //去除多余的事件
                List<DoctorPigEvent> filterMultiPreCheckEvents = filterMultiPregnancyCheckEvent(events);
                for (int i = 0; i < filterMultiPreCheckEvents.size(); i++) {
//                    if (i == filterMultiPreCheckEvents.size() - 1)
//                        break;

                    DoctorPigEvent currentEvent = filterMultiPreCheckEvents.get(i);
                    DoctorPigEvent beforeEvent = doctorPigEventDao.queryBeforeEvent(currentEvent);
                    if(beforeEvent==null||beforeEvent.getId()==null){
                        continue;
                    }

                    //得到天数
                    int days = DateUtil.getDeltaDays(beforeEvent.getEventAt(), currentEvent.getEventAt());//天数
                    int month = new DateTime(currentEvent.getEventAt()).getMonthOfYear();
                    int year = new DateTime(currentEvent.getEventAt()).getYear();

                    String yearAndMonthKey = year + "-" + month;

                    pigNpd.setOrgId(pigNpd.getOrgId()==null?currentEvent.getOrgId():pigNpd.getOrgId());

                    if (currentEvent.getType().equals(PigEvent.FARROWING.getKey())) {//分娩

                        count(days, currentEvent.getFarmId(), yearAndMonthKey, farmPregnancy);
                        if(currentEvent.getFarmId().equals(f))
                        pigNpd.setPregnancy((pigNpd.getPregnancy()==null?0:pigNpd.getPregnancy())+days);

                    } else if (currentEvent.getType().equals(PigEvent.WEAN.getKey())) {//断奶

                        count(days, currentEvent.getFarmId(), yearAndMonthKey, farmLactation);
                        if(currentEvent.getFarmId().equals(f))
                        pigNpd.setLactation((pigNpd.getLactation()==null?0:pigNpd.getLactation())+days);

                    } else if (currentEvent.getType().equals(PigEvent.CHG_FARM.getKey()) //离场
                            || currentEvent.getType().equals(PigEvent.REMOVAL.getKey())) {
                        if (beforeEvent.getType().equals(PigEvent.FARROWING.getKey())) {
                            //离场前分娩，算哺乳时间
                            count(days, currentEvent.getFarmId(), yearAndMonthKey, farmLactation);
                            if(currentEvent.getFarmId().equals(f))
                            pigNpd.setLactation((pigNpd.getLactation()==null?0:pigNpd.getLactation())+days);

                        }else if(beforeEvent.getType().equals(PigEvent.MATING.getKey())
                                &&(currentEvent.getPigStatusBefore()==PigStatus.Pregnancy.getKey()||currentEvent.getPigStatusBefore()==PigStatus.Farrow.getKey())){
                            //离场前配种，算孕期
                            count(days, currentEvent.getFarmId(), yearAndMonthKey, farmPregnancy);
                            if(currentEvent.getFarmId().equals(f))
                            pigNpd.setPregnancy((pigNpd.getPregnancy()==null?0:pigNpd.getPregnancy())+days);

                        } else /*if (currentEvent.getType().equals(PigEvent.ENTRY.getKey())
                                || currentEvent.getType().equals(PigEvent.WEAN.getKey())
                                || currentEvent.getType().equals(PigEvent.PREG_CHECK.getKey())
                                || currentEvent.getType().equals(PigEvent.MATING.getKey()))*/ {

                            pigCount.compute(pigId, (k, v) -> null == v ? 1 : v + 1);
                            count(days, currentEvent.getFarmId(), yearAndMonthKey, farmNPD);
                            if(currentEvent.getFarmId().equals(f))
                            pigNpd.setNpd((pigNpd.getNpd()==null?0:pigNpd.getNpd())+days);

                        }

                    } else {

                        pigCount.compute(pigId, (k, v) -> null == v ? 1 : v + 1);
                        count(days, currentEvent.getFarmId(), yearAndMonthKey, farmNPD);
                        if(currentEvent.getFarmId().equals(f))
                        pigNpd.setNpd((pigNpd.getNpd()==null?0:pigNpd.getNpd())+days);
                    }
                }
                doctorPigNpds.add(pigNpd);

            });
        });

        farmIds.forEach(f -> {

            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(f));

            for (Date i = startDate; i.before(endDate); i = DateUtils.addMonths(i, 1)) {

                Date monthEndDate = DateUtil.monthEnd(i);

                int dayCount = DateUtil.getDeltaDays(i, monthEndDate) + 1;

                DoctorReportNpd npd = doctorReportNpdDao.findByFarmAndSumAt(f, i).orElseGet(() -> new DoctorReportNpd());
                npd.setFarmId(f);
                npd.setDays(dayCount);

                if (log.isDebugEnabled())
                    log.debug("刷新猪场{},从{}到{}", farm.getId(), DateUtil.toDateString(i), DateUtil.toDateString(monthEndDate));
                Integer sowCount = doctorPigDailyDao.countSow(f, i, monthEndDate);
                npd.setSowCount(sowCount);

                npd.setSumAt(i);

                int year = new DateTime(i).getYear();
                int month = new DateTime(i).getMonthOfYear();
                String monthAndYearKey = year + "-" + month;

                npd.setNpd(getCount(f, monthAndYearKey, farmNPD));
                npd.setPregnancy(getCount(f, monthAndYearKey, farmPregnancy));
                npd.setLactation(getCount(f, monthAndYearKey, farmLactation));

                npd.setOrgId(null == farm ? null : farm.getOrgId());
                if (null == npd.getId())
                    doctorReportNpdDao.create(npd);
                else
                    doctorReportNpdDao.update(npd);
            }
        });

        try {
            doctorPigNpdDao.creates(doctorPigNpds);
        }catch (Exception e){
            log.error("DoctorReportWriteServiceImpl.createsPigNpd:"+e.getMessage());
        }
        log.info("total {} pig", pigCount.size());
        log.debug("use {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }


    /**
     * 过滤多余的事件
     * 先按照事件发生日期排序，正序
     * 如果是妊娠检查，结果为阳性事件一律去除
     * 如果是妊娠检查，结果为反情，流产，阴性，一个月内保留最后一个妊娠检查事件，其余去除
     * 如果是配种事件，并且是复配，过滤
     * 其他事件类型不影响
     *
     * @param pigEvents 已经过滤掉了防疫、疾病、转舍、体况
     * @return
     */
    public List<DoctorPigEvent> filterMultiPregnancyCheckEvent(List<DoctorPigEvent> pigEvents) {

        List<DoctorPigEvent> sortedByEventDate = pigEvents.stream()
                .sorted((e1, e2) -> e1.getEventAt().compareTo(e2.getEventAt()))
                .collect(Collectors.toList());

        List<DoctorPigEvent> filterMultiPreCheckEvents = new ArrayList<>();

        for (int i = 0; i < sortedByEventDate.size(); i++) {

            DoctorPigEvent currentEvent = sortedByEventDate.get(i);

            if (currentEvent.getType().equals(PigEvent.MATING.getKey())) {
                if (null == currentEvent.getCurrentMatingCount())
                    log.warn("current mating count missing,unknown mating several times.pig event[{}]", currentEvent.getId());
                else if (currentEvent.getCurrentMatingCount() > 1)
                    //复配不参与计算NPD
                    continue;
            }

            if (currentEvent.getType().equals(PigEvent.PREG_CHECK.getKey())) {

                if (currentEvent.getPregCheckResult() == null) {
                    log.warn("event[{}] is pregnancy check and has no check result", currentEvent.getId());
                    continue;
                }

                //只留一次妊娠检查
                if (i != sortedByEventDate.size() - 1) {
                    boolean remove = false;
                    for (int j = i + 1; j < sortedByEventDate.size(); j++) {
                        if (sortedByEventDate.get(j).getType().equals(PigEvent.PREG_CHECK.getKey()))//下一笔还是妊娠检查事件
                            //如果下一笔还是同一个月的
                            if (DateUtils.isSameDay(sortedByEventDate.get(j).getEventAt(), currentEvent.getEventAt())) {
                                remove = true;
                                break;//放弃这一笔的妊娠检查事件
                            }
                    }
                    if (remove)
                        continue;
                }


                //如果是阳性，过滤
                if (currentEvent.getPregCheckResult().equals(PregCheckResult.YANG.getKey())) {
                    continue;
                }

            }

            filterMultiPreCheckEvents.add(currentEvent);
        }
        return Collections.unmodifiableList(filterMultiPreCheckEvents);
    }

    private void count(int days,
                       Long farmId,
                       String yearAndMonth,
                       Map<Long/*farmID*/, Map<String/*year-month*/, Integer/*天数*/>> counter) {

        if (counter.containsKey(farmId)) {
            Map<String, Integer> monthCount = counter.get(farmId);
            if (monthCount.containsKey(yearAndMonth)) {
                int oldValue = monthCount.get(yearAndMonth);
                int newValue = days;
                int nowValue = oldValue + newValue;
//                log.info("{}-{}，历史{},新{},计算后{}", farmId, yearAndMonth, oldValue, newValue, nowValue);
                monthCount.put(yearAndMonth, nowValue);
            } else
                monthCount.put(yearAndMonth, days);
        } else {
            Map<String, Integer> monthCount = new HashMap<>();
            monthCount.put(yearAndMonth, days);
            counter.put(farmId, monthCount);
        }
    }

    private int getCount(Long farmId, String yearAndMonth,
                         Map<Long/*farmID*/, Map<String/*year-month*/, Integer/*天数*/>> counter) {
        if (!counter.containsKey(farmId))
            return 0;
        else {
            Map<String, Integer> monthCount = counter.get(farmId);
            if (!monthCount.containsKey(yearAndMonth))
                return 0;
            else
                return monthCount.get(yearAndMonth);
        }
    }
}
