package io.terminus.doctor.web.front.report;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.event.service.DoctorDeliveryReadService;
import io.terminus.doctor.web.core.export.Exporter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor/report/")
public class ReportDeliveryController {
    @RpcConsumer
    private DoctorDeliveryReadService doctorDeliveryReadService;

    @Autowired
    private Exporter exporter;

    @RequestMapping(method = RequestMethod.GET, value = "delivery")
    public Map<String,Object> deliveryReport(@RequestParam(required = true) Long farmId,
                                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginDate,
                                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                                   @RequestParam(required = false) String pigCode,
                                                   @RequestParam(required = false) String operatorName,
                                                   @RequestParam(required = false) int isdelivery) {
        return doctorDeliveryReadService.getMating(farmId,beginDate,endDate,pigCode,operatorName,isdelivery);
    }
    //导出excel
    @RequestMapping(method = RequestMethod.GET, value = "delivery/export")
    public void deliveryReports(@RequestParam(required = true) Long farmId,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginDate,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                @RequestParam(required = false) String pigCode,
                                @RequestParam(required = false) String operatorName,
                                @RequestParam(required = false) int isdelivery,
                                HttpServletRequest request, HttpServletResponse response) {
        Map<String,Object> map = doctorDeliveryReadService.getMating(farmId,beginDate,endDate,pigCode,operatorName,isdelivery);
        List<Map<String,Object>> list = (List<Map<String,Object>>) map.get("data");
        //开始导出
        try  {
            //导出名称
            exporter.setHttpServletResponse(request,  response,"分娩率分析报表");
            try  (XSSFWorkbook workbook  =  new  XSSFWorkbook())  {
                //表
                Sheet sheet  =  workbook.createSheet();
                sheet.addMergedRegion(new CellRangeAddress(0,0,0,20));
                Row count = sheet.createRow(0);
                count.createCell(0).setCellValue("总配种数:"+String.valueOf(map.get("matingcount"))
                        +"  分娩数/分娩率:"+String.valueOf(map.get("deliverycount"))+"/"+String.valueOf(map.get("deliveryrate"))
                        +"  返情数/返情率:"+String.valueOf(map.get("fqcount"))+"/"+String.valueOf(map.get("fqrate"))
                        +"  流产数/流产率:"+String.valueOf(map.get("lccount"))+"/"+String.valueOf(map.get("lcrate"))
                        +"  阴性数/阴性率:"+String.valueOf(map.get("yxcount"))+"/"+String.valueOf(map.get("yxrate"))
                        +"  死亡数/死亡率:"+String.valueOf(map.get("swcount"))+"/"+String.valueOf(map.get("swrate"))
                        +"  淘汰数/淘汰率:"+String.valueOf(map.get("ttcount"))+"/"+String.valueOf(map.get("ttrate"))
                );

                Row title  =  sheet.createRow(1);
                int  pos  =  2;

                title.createCell(0).setCellValue("序号");
                title.createCell(1).setCellValue("母猪号");
                title.createCell(2).setCellValue("当前猪舍");
                title.createCell(3).setCellValue("配种猪舍");
                title.createCell(4).setCellValue("当前状态");
                title.createCell(5).setCellValue("配种日期");
                title.createCell(6).setCellValue("配种次数");
                title.createCell(7).setCellValue("公猪耳号");
                title.createCell(8).setCellValue("配种员");
                title.createCell(9).setCellValue("分娩猪场");
                title.createCell(10).setCellValue("分娩猪舍");
                title.createCell(11).setCellValue("预产日期");
                title.createCell(12).setCellValue("实产日期");
                title.createCell(13).setCellValue("妊娠检查结果");
                title.createCell(14).setCellValue("是否死淘");

                for(int i = 0;i<list.size();i++) {
                    Map a = list.get(i);
                    Row row = sheet.createRow(pos++);
                    row.createCell(0).setCellValue(String.valueOf(i+1));
                    row.createCell(1).setCellValue(String.valueOf(a.get("pig_code")));
                    row.createCell(2).setCellValue(String.valueOf(a.get("current_barn_name")));
                    row.createCell(3).setCellValue(String.valueOf(a.get("barn_name")));
                    row.createCell(4).setCellValue(String.valueOf(a.get("pig_status")));
                    row.createCell(5).setCellValue(String.valueOf(a.get("event_at")));
                    row.createCell(6).setCellValue(String.valueOf(a.get("current_mating_count")));
                    row.createCell(7).setCellValue(String.valueOf(a.get("boar_code")));
                    row.createCell(8).setCellValue(String.valueOf(a.get("operator_name")));
                    row.createCell(9).setCellValue(String.valueOf(a.get("deliveryFarm")));
                    row.createCell(10).setCellValue(String.valueOf(a.get("deliveryBarn")));
                    row.createCell(11).setCellValue(String.valueOf(a.get("judge_preg_date")));
                    row.createCell(12).setCellValue(String.valueOf(a.get("deliveryDate")));
                    String check_event_at = "";
                    String leave_event_at = "";
                    if (!String.valueOf(a.get("check_event_at")).equals("") && !String.valueOf(a.get("notdelivery")).equals("阳性")) {
                        check_event_at = "("+String.valueOf(a.get("check_event_at"))+")";
                    }
                    if (!String.valueOf(a.get("leave_event_at")).equals("")) {
                        leave_event_at = "("+String.valueOf(a.get("leave_event_at"))+")";
                    }
                    row.createCell(13).setCellValue(String.valueOf(a.get("notdelivery"))+check_event_at);
                    row.createCell(14).setCellValue(String.valueOf(a.get("deadorescape"))+leave_event_at);
                }
                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 母猪存栏报表
     * @param farmId 猪场id
     * @param time  查询时间
     * @param pigCode   耳号
     * @param operatorName  饲养员
     * @param barnId    猪舍id
     * @param breedId     品种
     * @param parity    胎次
     * @param pigStatus 猪状态
     * @param beginInFarmTime 进场时间
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "sows")
    public List<Map<String,Object>> sowsReport(@RequestParam(required = true) Long farmId,
                                               @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date time,
                                               @RequestParam(required = false) String pigCode,
                                               @RequestParam(required = false) String operatorName,
                                               @RequestParam(required = false) Long barnId,
                                               @RequestParam(required = false) Integer breedId,
                                               @RequestParam(required = false) Integer parity,
                                               @RequestParam(required = true) Integer pigStatus,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginInFarmTime,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endInFarmTime) {
        return doctorDeliveryReadService.sowsReport(farmId,time,pigCode,operatorName,barnId,breedId,parity,pigStatus,beginInFarmTime,endInFarmTime);
    }

    /**
     * 获取公猪存栏报表
     */
    @RequestMapping(value = "boarReport", method = RequestMethod.GET)
    public List<Map<String,Object>> listBoarReport(@RequestParam Long farmId,
                                                   @RequestParam (required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date queryDate,
                                                   @RequestParam (required = false) String staffName,
                                                   @RequestParam (required = false) String pigCode,
                                                   @RequestParam (required = false) Integer barnId,
                                                   @RequestParam (required = false) Integer breedId,
//                                                   @RequestParam (required = false) Integer pigStatus,
                                                   @RequestParam (required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginDate,
                                                   @RequestParam (required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate){
//        if(null != beginDate && null != endDate && beginDate.after(endDate))
//            throw new JsonResponseException("start.date.after.end.date");
        return doctorDeliveryReadService.boarReport(farmId,queryDate,pigCode,staffName,barnId,breedId,beginDate,endDate);

    }

    //母猪存栏报表导出EXCEL
    @RequestMapping(method = RequestMethod.GET, value = "sows/export")
    public void sowsReports(@RequestParam(required = true) Long farmId,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date time,
                            @RequestParam(required = false) String pigCode,
                            @RequestParam(required = false) String operatorName,
                            @RequestParam(required = false) Long barnId,
                            @RequestParam(required = false) Integer breedId,
                            @RequestParam(required = false) Integer parity,
                            @RequestParam(required = false) Integer pigStatus,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginInFarmTime,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endInFarmTime,
                            HttpServletRequest request, HttpServletResponse response) {
        List<Map<String,Object>> ls=doctorDeliveryReadService.sowsReport(farmId,time,pigCode,operatorName,barnId,breedId,parity,pigStatus,beginInFarmTime,endInFarmTime);

        //开始导出
        try  {
            //导出名称
            exporter.setHttpServletResponse(request,  response,"母猪存栏报表");
            try  (XSSFWorkbook workbook  =  new  XSSFWorkbook())  {
                //表
                Sheet sheet  =  workbook.createSheet();
                sheet.addMergedRegion(new CellRangeAddress(0,0,0,20));
                Row count = sheet.createRow(0);


                Row title  =  sheet.createRow(1);
                int  pos  =  2;

                title.createCell(0).setCellValue("序号");

//                title.createCell(1).setCellValue("耳号");
//                title.createCell(2).setCellValue("品种");
//                title.createCell(3).setCellValue("胎次");
//                title.createCell(4).setCellValue("母猪状态");
//                title.createCell(5).setCellValue("猪舍");
//                title.createCell(6).setCellValue("饲养员");
//                title.createCell(7).setCellValue("带仔数");
//                title.createCell(8).setCellValue("来源");
//                title.createCell(9).setCellValue("进场日期");
//                title.createCell(10).setCellValue("出生日期");

                title.createCell(1).setCellValue("耳号");
                title.createCell(2).setCellValue("猪舍");

                title.createCell(3).setCellValue("品种");
                title.createCell(4).setCellValue("胎次");
                title.createCell(5).setCellValue("母猪状态");

                title.createCell(6).setCellValue("饲养员");
                title.createCell(7).setCellValue("带仔数");
                title.createCell(8).setCellValue("来源");
                title.createCell(9).setCellValue("进场日期");
                title.createCell(10).setCellValue("出生日期");

                for(int i = 0;i<ls.size();i++) {
                    Map a = ls.get(i);
                    Row row = sheet.createRow(pos++);
                    row.createCell(0).setCellValue(String.valueOf(i+1));



                    row.createCell(1).setCellValue(String.valueOf(a.get("pig_code")));
                    String rfid=String.valueOf(a.get("current_barn_name"));
                    if(rfid.equals("null")){
                        rfid="";
                    }
                    row.createCell(2).setCellValue(String.valueOf(rfid));

                    row.createCell(3).setCellValue(String.valueOf(a.get("breed_name")));
                    row.createCell(4).setCellValue(String.valueOf(a.get("parity")));
                    row.createCell(5).setCellValue(String.valueOf(a.get("status")));

                    row.createCell(6).setCellValue(String.valueOf(a.get("staff_name")));
                    row.createCell(7).setCellValue(String.valueOf(a.get("daizaishu")));
                    row.createCell(8).setCellValue(String.valueOf(a.get("source")));

                    String str = String.valueOf(a.get("in_farm_date"));
                    if("null".equals(str)){
                        row.createCell(9).setCellValue("");
                    }else {
                        String[] strs = str.split(" ");
                        row.createCell(9).setCellValue(String.valueOf(strs[0]));
                    }

                    String bd=String.valueOf(a.get("birth_date"));
                    if("null".equals(bd)){
                        row.createCell(10).setCellValue(" ");
                    }else {
                        String str1 = String.valueOf(a.get("birth_date"));
                        String[] strs1=str1.split(" ");
                        row.createCell(10).setCellValue(String.valueOf(strs1[0]));
                    }


                }
                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    //公猪存栏报表导出EXCEL
    @RequestMapping(method = RequestMethod.GET, value = "boars/export")
    public void boarsReports(@RequestParam Long farmId,
                             @RequestParam (required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date queryDate,
                             @RequestParam (required = false) String staffName,
                             @RequestParam (required = false) String pigCode,
                             @RequestParam (required = false) Integer barnId,
                             @RequestParam (required = false) Integer breedId,
//                             @RequestParam (required = false) Integer pigStatus,
                             @RequestParam (required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginDate,
                             @RequestParam (required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                            HttpServletRequest request, HttpServletResponse response) {
        List<Map<String,Object>> ls=doctorDeliveryReadService.boarReport(farmId,queryDate,pigCode,staffName,barnId,breedId,beginDate,endDate);

        //开始导出
        try  {
            //导出名称
            exporter.setHttpServletResponse(request,  response,"公猪存栏报表");
            try  (XSSFWorkbook workbook  =  new  XSSFWorkbook())  {
                //表
                Sheet sheet  =  workbook.createSheet();
                sheet.addMergedRegion(new CellRangeAddress(0,0,0,20));
                Row count = sheet.createRow(0);


                Row title  =  sheet.createRow(1);
                int  pos  =  2;

                title.createCell(0).setCellValue("序号");
                title.createCell(1).setCellValue("耳号");
                title.createCell(2).setCellValue("猪舍");
                title.createCell(3).setCellValue("品种");
                title.createCell(4).setCellValue("公猪状态");
                title.createCell(5).setCellValue("饲养员");
                title.createCell(6).setCellValue("来源");
                title.createCell(7).setCellValue("进场日期");
                title.createCell(8).setCellValue("出生日期");

                for(int i = 0;i<ls.size();i++) {
                    Map a = ls.get(i);
                    Row row = sheet.createRow(pos++);
                    row.createCell(0).setCellValue(String.valueOf(i+1));



                    row.createCell(1).setCellValue(String.valueOf(a.get("pig_code")));
                    String rfid=String.valueOf(a.get("current_barn_name"));
                    if(rfid.equals("null")){
                        rfid="";
                    }
                    row.createCell(2).setCellValue(String.valueOf(rfid));

                    row.createCell(3).setCellValue(String.valueOf(a.get("breed_name")));
                    row.createCell(4).setCellValue(String.valueOf(a.get("status")));

                    row.createCell(5).setCellValue(String.valueOf(a.get("staff_name")));
                    row.createCell(6).setCellValue(String.valueOf(a.get("source")));

                    String str = String.valueOf(a.get("in_farm_date"));
                    if("null".equals(str)){
                        row.createCell(7).setCellValue("");
                    }else {
                        String[] strs = str.split(" ");
                        row.createCell(7).setCellValue(String.valueOf(strs[0]));
                    }

                    String bd=String.valueOf(a.get("birth_date"));
                    if("null".equals(bd)){
                        row.createCell(8).setCellValue(" ");
                    }else {
                        String str1 = String.valueOf(a.get("birth_date"));
                        String[] strs1=str1.split(" ");
                        row.createCell(8).setCellValue(String.valueOf(strs1[0]));
                    }
                }
                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     *猪群存栏报表
     * @param farmId
     * @param time
     * @param groupCode
     * @param operatorName
     * @param barn
     * @param groupType
     * @param groupStatus
     * @param buildBeginGroupTime
     * @param buildEndGroupTime
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "group")
    public Map<String,Object> groupReport(@RequestParam(required = true) Long farmId,
                                               @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date time,
                                               @RequestParam(required = false) String groupCode,
                                               @RequestParam(required = false) String operatorName,
                                               @RequestParam(required = false) Long barn,
                                                @RequestParam(required = false) Integer groupType,
                                               @RequestParam(required = false) Integer groupStatus,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date buildBeginGroupTime,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date buildEndGroupTime,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date closeBeginGroupTime,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date closeEndGroupTime) {
        return doctorDeliveryReadService.groupReport(farmId,time,groupCode,operatorName,barn,groupType,groupStatus,buildBeginGroupTime,buildEndGroupTime,closeBeginGroupTime,closeEndGroupTime);
    }

    /**
     * 猪舍存栏报表
     * @param farmId
     * @param operatorName
     * @param barnId
     * @param beginTime
     * @param endTime
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "barns")
    public List<Map<String,Object>> barnsReport(@RequestParam(required = true) Long farmId,
                                               @RequestParam(required = false) String operatorName,
                                               @RequestParam(required = false) Long barnId,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginTime,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
        return doctorDeliveryReadService.barnsReport(farmId,operatorName,barnId,beginTime,endTime);
    }
}