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
                        +"  阳性数/阳性率:"+String.valueOf(map.get("yangxcount"))+"/"+String.valueOf(map.get("yangxrate"))
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
                title.createCell(2).setCellValue("配种猪舍");
                title.createCell(3).setCellValue("当前状态");
                title.createCell(4).setCellValue("配种日期");
                title.createCell(5).setCellValue("配种次数");
                title.createCell(6).setCellValue("公猪耳号");
                title.createCell(7).setCellValue("配种员");
                title.createCell(8).setCellValue("分娩猪场");
                title.createCell(9).setCellValue("分娩猪舍");
                title.createCell(10).setCellValue("预产日期");
                title.createCell(11).setCellValue("实产日期");
                title.createCell(12).setCellValue("妊娠检查结果");
                title.createCell(13).setCellValue("是否死淘");

                for(int i = 0;i<list.size();i++) {
                    Map a = list.get(i);
                    Row row = sheet.createRow(pos++);
                    row.createCell(0).setCellValue(String.valueOf(i+1));
                    row.createCell(1).setCellValue(String.valueOf(a.get("pig_code")));
                    row.createCell(2).setCellValue(String.valueOf(a.get("barn_name")));
                    row.createCell(3).setCellValue(String.valueOf(a.get("pig_status")));
                    row.createCell(4).setCellValue(String.valueOf(a.get("event_at")));
                    row.createCell(5).setCellValue(String.valueOf(a.get("current_mating_count")));
                    row.createCell(6).setCellValue(String.valueOf(a.get("boar_code")));
                    row.createCell(7).setCellValue(String.valueOf(a.get("operator_name")));
                    row.createCell(8).setCellValue(String.valueOf(a.get("deliveryFarm")));
                    row.createCell(9).setCellValue(String.valueOf(a.get("deliveryBarn")));
                    row.createCell(10).setCellValue(String.valueOf(a.get("judge_preg_date")));
                    row.createCell(11).setCellValue(String.valueOf(a.get("deliveryDate")));
                    String check_event_at = "";
                    String leave_event_at = "";
                    if (!String.valueOf(a.get("check_event_at")).equals("")) {
                        check_event_at = "("+String.valueOf(a.get("check_event_at"))+")";
                    }
                    if (!String.valueOf(a.get("leave_event_at")).equals("")) {
                        leave_event_at = "("+String.valueOf(a.get("leave_event_at"))+")";
                    }
                    row.createCell(12).setCellValue(String.valueOf(a.get("notdelivery"))+check_event_at);
                    row.createCell(13).setCellValue(String.valueOf(a.get("deadorescape"))+leave_event_at);
                }
                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}