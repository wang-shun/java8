package io.terminus.doctor.move.controller;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.redis.utils.JedisTemplate;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.service.DoctorDailyGroupWriteService;
import io.terminus.doctor.event.service.DoctorDailyReportWriteService;
import io.terminus.doctor.move.dto.DoctorImportSheet;
import io.terminus.doctor.move.service.DoctorGroupBatchFlushService;
import io.terminus.doctor.move.service.DoctorImportDataService;
import io.terminus.doctor.move.service.DoctorMoveAndImportService;
import io.terminus.doctor.move.service.DoctorMoveDataService;
import io.terminus.doctor.move.service.DoctorMoveReportService;
import io.terminus.doctor.move.util.ImportExcelUtils;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorFarmExport;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.zookeeper.pubsub.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.util.Objects;
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
    private static final String ImportExcelRedisKey = "import-excel-result:";

    @Autowired
    private DoctorImportDataService doctorImportDataService;
    @Autowired
    private Subscriber subscriber;
    @Autowired
    private DoctorDailyReportWriteService doctorDailyReportWriteService;
    @Autowired
    private DoctorMoveReportService doctorMoveReportService;
    @Autowired
    private JedisTemplate jedisTemplate;
    @Autowired
    private DoctorFarmReadService doctorFarmReadService;
    @Autowired
    private DoctorGroupBatchFlushService doctorGroupBatchFlushService;
    @Autowired
    private DoctorMoveDataService doctorMoveDataService;
    @Autowired
    private DoctorDailyGroupWriteService doctorDailyGroupWriteService;
    @Autowired
    private DoctorMoveAndImportService doctorMoveAndImportService;

    @PostConstruct
    public void init () throws Exception{
        subscriber.subscribe(data -> {
            DataEvent dataEvent = DataEvent.fromBytes(data);
            if(dataEvent != null && dataEvent.getEventType().equals(DataEventType.ImportExcel.getKey())){
                log.warn("成功监听到导数事件, content={}", dataEvent.getContent());
                String fileURL = DataEvent.analyseContent(dataEvent, String.class);
                new Thread(() -> {
                    importByHttpUrl(fileURL);
                }).start();
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
            this.importByInputStream(inputStream, fileType, path);

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
                    //ignore this exception
                }
            }
        }
    }

    private Sheet getSheet(Workbook wk, String name) {
        Sheet sheet = wk.getSheet(name);
        if (sheet == null) {
            throw new JsonResponseException("sheet.not.found：" + name);
        }
        return sheet;
    }

    private void importByInputStream(InputStream inputStream, String fileType, String path) throws IOException {
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
//        sheet.setOperator(getSheet(workbook, "记录操作人"));
        sheet.setBarn(getSheet(workbook, "1.猪舍"));
        sheet.setBreed(getSheet(workbook, "2.品种"));
        sheet.setSow(getSheet(workbook, "3.母猪信息"));
        sheet.setBoar(getSheet(workbook, "4.公猪信息"));
        sheet.setGroup(getSheet(workbook, "5.商品猪（猪群）信息"));
//        sheet.setWarehouse(getSheet(workbook, "6.仓库"));
//        sheet.setMedicine(getSheet(workbook, "7.药品"));
//        sheet.setVacc(getSheet(workbook, "8.疫苗"));
//        sheet.setMaterial(getSheet(workbook, "9.原料"));
//        sheet.setFeed(getSheet(workbook, "10.饲料"));
//        sheet.setConsume(getSheet(workbook, "11.易耗品"));
        Stopwatch watch = Stopwatch.createStarted();

        //创建导入记录
        Row farmRow = sheet.getFarm().getRow(1);
        String farmName = ImportExcelUtils.getStringOrThrow(farmRow, 1).replaceAll(" ", "");
        DoctorFarmExport farmExport = DoctorFarmExport.builder().farmName(farmName).url(path).build();
        farmExport.setStatus(DoctorFarmExport.Status.HANDLING.getValue());
        doctorImportDataService.createFarmExport(farmExport);

        //导入数据
        Integer status;
        String errorReason = null;
        Long farmId = null;
        try {
            // 导入数据 公司账号或者猪场账号必须存在一个（陈娟 2018-10-12）
            farmId = doctorMoveAndImportService.importData(sheet);
            status = DoctorFarmExport.Status.SUCCESS.getValue();
        } catch (Exception e) {
            status = DoctorFarmExport.Status.FAILED.getValue();
            if (e instanceof JsonResponseException) {
                errorReason = e.getMessage();
            } else {
                errorReason = Throwables.getStackTraceAsString(e);
            }
        }

        //更新导入状态
        DoctorFarmExport updateFarmExport = new DoctorFarmExport();
        updateFarmExport.setId(farmExport.getId());
        updateFarmExport.setStatus(status);
        updateFarmExport.setErrorReason(errorReason);
        doctorImportDataService.updateFarmExport(updateFarmExport);

        watch.stop();
        int minute = Long.valueOf(watch.elapsed(TimeUnit.MINUTES) + 1).intValue();
        log.warn("database data inserted successfully, elapsed {} minutes", minute);
        log.warn("all data moved succelly, CONGRATULATIONS!!!");

        if (Objects.equals(status, DoctorFarmExport.Status.SUCCESS.getValue())) {
            doctorMoveAndImportService.generateReport(farmId);
        }
    }



    @RequestMapping(value = "/importPig", method = RequestMethod.GET)
    public void importPig(@RequestParam String path, @RequestParam Long farmId,@RequestParam Sub sub) {
        DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
        try {
            Workbook workbook = new HSSFWorkbook(new FileInputStream(new File(path)));
            Sheet sowSheet = workbook.getSheet("3.母猪信息");
            Sheet boarSheet = workbook.getSheet("4.公猪信息");
            doctorMoveAndImportService.importPig(boarSheet, sowSheet,
                    doctorMoveAndImportService.packageImportBasicData(farm,sub));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/importGroup", method = RequestMethod.GET)
    public void importGroup(@RequestParam String path, @RequestParam Long farmId,@RequestParam Sub sub) {
        DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
        try {
            Workbook workbook = new HSSFWorkbook(new FileInputStream(new File(path)));
            Sheet groupSheet = workbook.getSheet("5.商品猪（猪群）信息");
            doctorMoveAndImportService.importGroup(groupSheet,
                    doctorMoveAndImportService.packageImportBasicData(farm,sub));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/importByHttpUrl", method = RequestMethod.GET)
    public void importByHttpUrl(@RequestParam String fileURL){
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
            importByInputStream(inputStream, fileType, fileURL);
        } catch (Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 修复配种率的各种统计
     */
    @RequestMapping(value = "/updateMateEvent", method = RequestMethod.GET)
    public boolean updateMateEvent(@RequestParam(value = "farmId", required = false) Long farmId) {
        try {
            if (farmId != null) {
                doctorImportDataService.updateMateRate(farmId);
            } else {
                RespHelper.or500(doctorFarmReadService.findAllFarms()).forEach(farm -> doctorImportDataService.updateMateRate(farm.getId()));
            }
            return true;
        } catch (Exception e) {
            log.error("update mate event failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 修复分娩后的母猪事件，增加group_id 方便以后统计
     */
    @RequestMapping(value = "/flushFarrowGroupId", method = RequestMethod.GET)
    public boolean flushFarrowGroupId(@RequestParam(value = "farmId", required = false) Long farmId) {
        try {
            log.info("******* flushFarrowGroupId start, farmId:{}", farmId);
            if (farmId != null) {
                doctorImportDataService.flushFarrowGroupId(farmId);
            } else {
                RespHelper.or500(doctorFarmReadService.findAllFarms()).forEach(farm -> doctorImportDataService.flushFarrowGroupId(farm.getId()));
            }
            log.info("******* flushFarrowGroupId end");
            return true;
        } catch (Exception e) {
            log.error("flush farrow groupId failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 刷批次总结
     */
    @RequestMapping(value = "/flushGroupBatch", method = RequestMethod.GET)
    public boolean flushGroupBatch(@RequestParam(value = "farmId", required = false) Long farmId,
                                   @RequestParam(value = "all", defaultValue = "false") boolean all) {
        try {
            log.info("******* flushGroupBatch start, farmId:{}, all:{}", farmId, all);
            if (farmId != null) {
                doctorGroupBatchFlushService.flushGroupBatch(farmId, all);
            } else {
                doctorGroupBatchFlushService.flushGroupBatches(all);
            }
            log.info("******* flushGroupBatch end");
            return true;
        } catch (Exception e) {
            log.error("flush group batch failed, farmId:{}, all:{}, cause:{}", farmId, all, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 刷npd
     */
    @RequestMapping(value = "/flushNpd", method = RequestMethod.GET)
    public boolean flushNpd(@RequestParam(value = "farmId", required = false) Long farmId) {
        try {
            log.info("******* flushNpd start, farmId:{}", farmId);
            if (farmId != null) {
                doctorMoveDataService.flushNpd(farmId);
            } else {
                RespHelper.or500(doctorFarmReadService.findAllFarms()).forEach(farm -> doctorMoveDataService.flushNpd(farm.getId()));
            }
            log.info("******* flushNpd end");
            return true;
        } catch (Exception e) {
            log.error("flushNpd failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 刷新公猪类型
     */
    @RequestMapping(value = "/flushBoarType", method = RequestMethod.GET)
    public boolean flushBoarType(@RequestParam(value = "farmId", required = false) Long farmId) {
        try {
            log.info("******* flushBoarType start, farmId:{}", farmId);
            if (farmId != null) {
                doctorImportDataService.flushBoarType(farmId);
            } else {
                RespHelper.or500(doctorFarmReadService.findAllFarms()).forEach(farm -> doctorImportDataService.flushBoarType(farm.getId()));
            }
            log.info("******* flushBoarType end");
            return true;
        } catch (Exception e) {
            log.error("flushBoarType failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 修改猪场名
     * @param farmId 猪场id
     * @param farmName 新猪场名
     * @return 更新是否成功
     */
    @RequestMapping(value = "/updateFarmName", method = RequestMethod.GET)
    public Boolean updateFarmName(@RequestParam Long farmId,
                                  @RequestParam String farmName) {
        log.info("updateFarmName.starting, farmId:{}, farmName:{}", farmId, farmName);
        doctorImportDataService.updateFarmName(farmId, farmName);
        log.info("updateFarmName.ending");
        return true;
    }

}
