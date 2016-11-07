package io.terminus.doctor.move.controller;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.event.search.barn.BarnSearchDumpService;
import io.terminus.doctor.event.search.group.GroupDumpService;
import io.terminus.doctor.event.search.pig.PigDumpService;
import io.terminus.doctor.event.service.DoctorDailyReportWriteService;
import io.terminus.doctor.move.dto.DoctorImportSheet;
import io.terminus.doctor.move.service.DoctorImportDataService;
import io.terminus.doctor.move.service.DoctorMoveReportService;
import io.terminus.zookeeper.pubsub.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/10/19
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/import/data")
public class DoctorImportDataController {

    @Autowired
    private DoctorImportDataService doctorImportDataService;
    @Autowired
    private BarnSearchDumpService barnSearchDumpService;
    @Autowired
    private GroupDumpService groupDumpService;
    @Autowired
    private PigDumpService pigDumpService;
    @Autowired
    private Subscriber subscriber;
    @Autowired
    private DoctorDailyReportWriteService doctorDailyReportWriteService;
    @Autowired
    private DoctorMoveReportService doctorMoveReportService;

    @PostConstruct
    public void init () throws Exception{
        subscriber.subscribe(data -> {
            DataEvent dataEvent = DataEvent.fromBytes(data);
            if(dataEvent != null && dataEvent.getEventType().equals(DataEventType.ImportExcel.getKey())){
                log.warn("成功监听到导数事件, content={}", dataEvent.getContent());
                String fileURL = DataEvent.analyseContent(dataEvent, String.class);
                importByHttpUrl(fileURL);
            }
        });
    }
    /**
     * 导入所有的猪场数据
     * @param path excel文件路径
     * @return 是否成功
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public String importAll(@RequestParam("path") String path) {
        InputStream inputStream = null;
        try {
            File file = new File(path);
            String fileType;
            if(file.getName().endsWith(".xlsx")){
                fileType = "xlsx";
            }else if(file.getName().endsWith(".xls")){
                fileType = "xls";
            }else{
                throw new ServiceException("file.type.error");
            }
            inputStream = new FileInputStream(file);
            this.importByInputStream(inputStream, fileType);

            return "true";
        } catch (ServiceException | JsonResponseException e) {
            log.error("import all excel failed, path:{}, cause:{}", path, Throwables.getStackTraceAsString(e));
            return e.getMessage();
        } catch (Exception e) {
            log.error("import all excel failed, path:{}, cause:{}", path, Throwables.getStackTraceAsString(e));
            return "false";
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private Sheet getSheet(Workbook wk, String name) {
        Sheet sheet = wk.getSheet(name);
        if (sheet == null) {
            throw new JsonResponseException("sheet.not.found");
        }
        return sheet;
    }

    private void importByInputStream(InputStream inputStream, String fileType) throws IOException {
        Workbook workbook;
        switch (fileType) {
            case "xlsx":
                workbook = new XSSFWorkbook(inputStream);  //2007
                break;
            case "xls":
                workbook = new HSSFWorkbook(inputStream);  //2003
                break;
            default:
                throw new ServiceException("file.type.error");
        }

        DoctorImportSheet sheet = new DoctorImportSheet();
        sheet.setFarm(getSheet(workbook, "猪场"));
        sheet.setStaff(getSheet(workbook, "员工"));
        sheet.setBarn(getSheet(workbook, "1.猪舍"));
        sheet.setBreed(getSheet(workbook, "2.品种"));
        sheet.setSow(getSheet(workbook, "3.母猪信息"));
        sheet.setBoar(getSheet(workbook, "4.公猪信息"));
        sheet.setGroup(getSheet(workbook, "5.商品猪（猪群）信息"));
        sheet.setWarehouse(getSheet(workbook, "6.仓库"));
        sheet.setMedicine(getSheet(workbook, "7.药品"));
        sheet.setVacc(getSheet(workbook, "8.疫苗"));
        sheet.setMaterial(getSheet(workbook, "9.原料"));
        sheet.setFeed(getSheet(workbook, "10.饲料"));
        sheet.setConsume(getSheet(workbook, "11.易耗品"));
        Stopwatch watch = Stopwatch.createStarted();
        this.generateReport(doctorImportDataService.importAll(sheet).getId());
        watch.stop();
        int minute = Long.valueOf(watch.elapsed(TimeUnit.MINUTES) + 1).intValue();
        log.warn("database data inserted successfully, elapsed {} minutes, now dumping ElasticSearch", minute);

        barnSearchDumpService.deltaDump(minute);
        groupDumpService.deltaDump(minute);
        pigDumpService.deltaDump(minute);
        log.warn("all data moved successfully, CONGRATULATIONS!!!");
    }

    //生成一年的报表
    private void generateReport(Long farmId){
        DateTime end = DateTime.now().withTimeAtStartOfDay().minusDays(1); //昨天开始时间
        DateTime begin = end.minusYears(1);
        new Thread(() -> {
            doctorDailyReportWriteService.createDailyReports(begin.toDate(), end.toDate(), farmId);
            doctorMoveReportService.moveMonthlyReport(farmId, 12);
            doctorMoveReportService.moveParityMonthlyReport(farmId, 12);
            doctorMoveReportService.moveBoarMonthlyReport(farmId, 12);
        }).start();
    }

    @RequestMapping(value = "/importByHttpUrl", method = RequestMethod.GET)
    public String importByHttpUrl(@RequestParam String fileURL){
        String fileType;
        if(fileURL.endsWith(".xlsx")){
            fileType = "xlsx";
        }else if(fileURL.endsWith(".xls")){
            fileType = "xls";
        }else{
            throw new ServiceException("file.type.error");
        }
        InputStream inputStream = null;
        try {
            inputStream = new URL(fileURL.replace("https", "http")).openConnection().getInputStream();
            importByInputStream(inputStream, fileType);
            return "true";
        } catch (Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
            return "false";
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @RequestMapping(value = "/fixJudgePregDate", method = RequestMethod.GET)
    public String fixJudgePregDate(@RequestParam("farmId") Long farmId, @RequestParam("path") String path) {
        InputStream inputStream = null;
        try {
            File file = new File(path);
            String fileType;
            if(file.getName().endsWith(".xlsx")){
                fileType = "xlsx";
            }else if(file.getName().endsWith(".xls")){
                fileType = "xls";
            }else{
                throw new ServiceException("file.type.error");
            }

            inputStream = new FileInputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);  //2003
            doctorImportDataService.fixJudgePregDate(farmId, getSheet(workbook, "3.母猪信息"));
            return "true";
        } catch (ServiceException | JsonResponseException e) {
            log.error("import all excel failed, path:{}, cause:{}", path, Throwables.getStackTraceAsString(e));
            return e.getMessage();
        } catch (Exception e) {
            log.error("import all excel failed, path:{}, cause:{}", path, Throwables.getStackTraceAsString(e));
            return "false";
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

}
