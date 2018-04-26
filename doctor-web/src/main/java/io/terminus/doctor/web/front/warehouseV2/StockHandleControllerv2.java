package io.terminus.doctor.web.front.warehouseV2;

import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.basic.service.DoctorWareHouseReadService;
import io.terminus.doctor.basic.service.warehouseV2.*;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.ResponseUtil;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.web.core.export.Exporter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/doctor/warehouse/receipt")
public class StockHandleControllerv2 {

    @Autowired
    private Exporter exporter;
    @RpcConsumer
    private DoctorWarehouseStockHandleReadService doctorWarehouseStockHandleReadService;
    @RpcConsumer
    private DoctorWarehouseStockHandleWriteService doctorWarehouseStockHandleWriteService;
    @RpcConsumer
    private DoctorWarehouseMaterialHandleReadService doctorWarehouseMaterialHandleReadService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private DoctorWareHouseReadService doctorWareHouseReadService;
    @RpcConsumer
    private DoctorWarehouseSkuReadService doctorWarehouseSkuReadService;
    @RpcConsumer
    private DoctorWarehouseVendorReadService doctorWarehouseVendorReadService;
    @RpcConsumer
    private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

    @InitBinder
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
    }

    //公司单据数据展示
    @RequestMapping(value = "/companyReport", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseUtil<List<List<Map>>> companyReport(@RequestParam(required = false,value = "farmId") Long farmId,
                                                       @RequestParam(required = false,value = "orgId") Long orgId,
                                                       @RequestParam(required = false,value = "settlementDateStart") Date settlementDateStart,
                                                       @RequestParam(required = false,value = "settlementDateEnd") Date settlementDateEnd){
        if (null != settlementDateStart && null != settlementDateEnd && settlementDateStart.after(settlementDateEnd))
            throw new JsonResponseException("start.date.after.end.date");

        Map<String, Object> params = new HashMap<>();
        params.put("farmId",farmId);
        params.put("settlementDateStart",settlementDateStart);
        params.put("settlementDateEnd",settlementDateEnd);
        params.put("orgId",orgId);
        return doctorWarehouseMaterialHandleReadService.companyReport(params);
    }

    //仓库单据展示
    @RequestMapping(value = "/warehouseReport", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseUtil<List<List<Map>>> warehouseReport(@RequestParam(required = false,value = "farmId") Long farmId,
                                                   @RequestParam(required = false,value = "settlementDateStart") Date settlementDateStart,
                                                   @RequestParam(required = false,value = "settlementDateEnd") Date settlementDateEnd){

        if (null != settlementDateStart && null != settlementDateEnd && settlementDateStart.after(settlementDateEnd))
            throw new JsonResponseException("start.date.after.end.date");

        Map<String, Object> params = new HashMap<>();
        params.put("farmId",farmId);
        params.put("settlementDateStart",settlementDateStart);
        params.put("settlementDateEnd",settlementDateEnd);

        return doctorWarehouseMaterialHandleReadService.warehouseReport(params);

    }

    //仓库月报
    @RequestMapping(value = "/monthWarehouseDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<List<Map>> warehouseReport(@RequestParam(required = true,value = "warehouseId") Long warehouseId,
                                               @RequestParam(required = true,value = "settlementDate") Date settlementDate
                                                     ){
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId",warehouseId);
        params.put("settlementDate",settlementDate);
        return doctorWarehouseMaterialHandleReadService.monthWarehouseDetail(params);

    }

    //报表导出
    @RequestMapping(value = "{type:\\d+}/export2", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public void export( HttpServletRequest request, HttpServletResponse response,
                       @PathVariable(value = "type")Integer type,
                        @RequestParam(required = false,value = "orgId") Long orgId,
                       @RequestParam(required = false,value = "farmId") Long farmId,
                       @RequestParam(required = false,value = "settlementDateStart") Date settlementDateStart,
                       @RequestParam(required = false,value = "settlementDateEnd") Date settlementDateEnd,
                       @RequestParam(required = false,value = "warehouseId") Long warehouseId,
                       @RequestParam(required = false,value = "settlementDate") Date settlementDate) {
        HashMap<String, Object> params = Maps.newHashMap();

        try {
            switch (type) {
                case 1:
                    params.put("orgId", orgId);
                    params.put("settlementDateStart", settlementDateStart);
                    params.put("settlementDateEnd", settlementDateEnd);
                    ResponseUtil<List<List<Map>>> listResponse = doctorWarehouseMaterialHandleReadService.companyReport(params);
                    List<List<Map>> result = (List<List<Map>>)listResponse.getResult();
                    //开始导出
                    exporter.setHttpServletResponse(request, response, "公司报表");

                    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                        //表
                        Sheet sheet = workbook.createSheet();
                        //样式
                        sheet.addMergedRegion(new CellRangeAddress(0,0,0, 1));

                        int firstCol = 2;
                        for(int x=0;x<=listResponse.getFarms().size();x++,firstCol+=2){
                            sheet.addMergedRegion(new CellRangeAddress(0,0,firstCol, firstCol+1));
                        }
                        int firstRow = 1;
                        for(int x=0;x<result.size();x++,firstRow+=3){
                            sheet.addMergedRegion(new CellRangeAddress(firstRow,firstRow+2,0,0));
                        }

                        //行
                        Row head = sheet.createRow(0);

                        head.createCell(0);

                        firstCol = 2;
                        for(Map map:listResponse.getFarms()){
                            if(map.get("id")!=null) {
                                head.createCell(firstCol).setCellValue(map.get("name").toString());
                                firstCol+=2;
                            }
                        }
                        head.createCell(head.getLastCellNum()+1).setCellValue("合计");
                        //第一行结束

                        for(int x=0;x<result.size();x++) {
                            List<Map> lists = result.get(x);
                            //每一行的样式
                            firstCol = 2;
                            for (int y=0;y<3;y++) {
                                for(int z=0;z<=listResponse.getFarms().size();z++,firstCol+=2){
                                    sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum()+1,sheet.getLastRowNum()+1,firstCol, firstCol+1));
                                }
                                Row row = null;
                                if(y==0){
                                    row = sheet.createRow(sheet.getLastRowNum()+1);
                                    //月份
                                    Cell cell = row.createCell(0);
                                    if (cell.getStringCellValue() == null || cell.getStringCellValue().equals("")) {
                                        cell.setCellValue(lists.get(lists.size() - 1).get("month").toString() + "月");
                                    }
                                }else{
                                    row = sheet.createRow(sheet.getLastRowNum()+1);
                                    row.createCell(0);
                                }
                                if (sheet.getRow(sheet.getLastRowNum()-1) != null&&sheet.getRow(sheet.getLastRowNum()-1).getCell(1)!=null &&"结余".equals(sheet.getRow(sheet.getLastRowNum()-1).getCell(1).getStringCellValue())) {
                                    row.createCell(1).setCellValue("入库");
                                    for (Map map : lists) {
                                        short last = row.getLastCellNum();
                                        if (map.get("inAmount") != null) {
                                            row.createCell(last).setCellValue(map.get("inAmount").toString());
                                            row.createCell(last+1);
                                        } else if (map.get("allInAmount") != null) {
                                            row.createCell(last).setCellValue(map.get("allInAmount").toString());
                                            row.createCell(last+1);
                                        }
                                    }
                                } else if (sheet.getRow(sheet.getLastRowNum()-1) != null&&sheet.getRow(sheet.getLastRowNum()-1).getCell(1)!=null &&"入库".equals(sheet.getRow(sheet.getLastRowNum()-1).getCell(1).getStringCellValue())) {
                                    row.createCell(1).setCellValue("出库");
                                    for (Map map : lists) {
                                        short last = row.getLastCellNum();
                                        if (map.get("outAmount") != null) {
                                            row.createCell(last).setCellValue(map.get("outAmount").toString());
                                            row.createCell(last+1);
                                        } else if (map.get("allOutAmount") != null) {
                                            row.createCell(last).setCellValue(map.get("allOutAmount").toString());
                                            row.createCell(last+1);
                                        }
                                    }
                                } else {
                                    row.createCell(1).setCellValue("结余");
                                    for (Map map : lists) {
                                        short last = row.getLastCellNum();
                                        if (map.get("balanceAmount") != null) {
                                            row.createCell(last).setCellValue(map.get("balanceAmount").toString());
                                            row.createCell(last+1);
                                        } else if (map.get("allBalanceAmount") != null) {
                                            row.createCell(last).setCellValue(map.get("allBalanceAmount").toString());
                                            row.createCell(last+1);
                                        }
                                    }
                                }
                            }
                        }
                        workbook.write(response.getOutputStream());
                    }
                    break;
                case 2:
                    params.put("farmId", farmId);
                    params.put("settlementDateStart", settlementDateStart);
                    params.put("settlementDateEnd", settlementDateEnd);
                    ResponseUtil<List<List<Map>>> listResponse1 = doctorWarehouseMaterialHandleReadService.warehouseReport(params);
                    List<List<Map>> result1 = (List<List<Map>>)listResponse1.getResult();
                    //开始导出
                    exporter.setHttpServletResponse(request, response, "仓库报表");

                    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                        //表
                        Sheet sheet = workbook.createSheet();
                        //样式
                        sheet.addMergedRegion(new CellRangeAddress(0,0,0, 1));

                        int firstCol = 2;
                        for(int x=0;x<=listResponse1.getFarms().size();x++,firstCol+=2){
                            sheet.addMergedRegion(new CellRangeAddress(0,0,firstCol, firstCol+1));
                        }
                        int firstRow = 1;
                        for(int x=0;x<result1.size();x++,firstRow+=3){
                            sheet.addMergedRegion(new CellRangeAddress(firstRow,firstRow+2,0,0));
                        }

                        //行
                        Row head = sheet.createRow(0);

                        head.createCell(0);

                        firstCol = 2;
                        for(Map map:listResponse1.getFarms()){
                            if(map.get("id")!=null) {
                                head.createCell(firstCol).setCellValue(map.get("name").toString());
                                firstCol+=2;
                            }
                        }
                        head.createCell(head.getLastCellNum()+1).setCellValue("合计");
                        //第一行结束

                        for(int x=0;x<result1.size();x++) {
                            List<Map> lists = result1.get(x);
                            //每一行的样式
                            firstCol = 2;
                            for (int y=0;y<3;y++) {
                                for(int z=0;z<=listResponse1.getFarms().size();z++,firstCol+=2){
                                    sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum()+1,sheet.getLastRowNum()+1,firstCol, firstCol+1));
                                }
                                Row row = null;
                                if(y==0){
                                    row = sheet.createRow(sheet.getLastRowNum()+1);
                                    //月份
                                    Cell cell = row.createCell(0);
                                    if (cell.getStringCellValue() == null || cell.getStringCellValue().equals("")) {
                                        cell.setCellValue(lists.get(lists.size() - 1).get("month").toString() + "月");
                                    }
                                }else{
                                    row = sheet.createRow(sheet.getLastRowNum()+1);
                                    row.createCell(0);
                                }
                                if (sheet.getRow(sheet.getLastRowNum()-1) != null&&sheet.getRow(sheet.getLastRowNum()-1).getCell(1)!=null &&"结余".equals(sheet.getRow(sheet.getLastRowNum()-1).getCell(1).getStringCellValue())) {
                                    row.createCell(1).setCellValue("入库");
                                    for (Map map : lists) {
                                        short last = row.getLastCellNum();
                                        if (map.get("inAmount") != null) {
                                            row.createCell(last).setCellValue(map.get("inAmount").toString());
                                            row.createCell(last+1);
                                        } else if (map.get("allInAmount") != null) {
                                            row.createCell(last).setCellValue(map.get("allInAmount").toString());
                                            row.createCell(last+1);
                                        }
                                    }
                                } else if (sheet.getRow(sheet.getLastRowNum()-1) != null&&sheet.getRow(sheet.getLastRowNum()-1).getCell(1)!=null &&"入库".equals(sheet.getRow(sheet.getLastRowNum()-1).getCell(1).getStringCellValue())) {
                                    row.createCell(1).setCellValue("出库");
                                    for (Map map : lists) {
                                        short last = row.getLastCellNum();
                                        if (map.get("outAmount") != null) {
                                            row.createCell(last).setCellValue(map.get("outAmount").toString());
                                            row.createCell(last+1);
                                        } else if (map.get("allOutAmount") != null) {
                                            row.createCell(last).setCellValue(map.get("allOutAmount").toString());
                                            row.createCell(last+1);
                                        }
                                    }
                                } else {
                                    row.createCell(1).setCellValue("结余");
                                    for (Map map : lists) {
                                        short last = row.getLastCellNum();
                                        if (map.get("balanceAmount") != null) {
                                            row.createCell(last).setCellValue(map.get("balanceAmount").toString());
                                            row.createCell(last+1);
                                        } else if (map.get("allBalanceAmount") != null) {
                                            row.createCell(last).setCellValue(map.get("allBalanceAmount").toString());
                                            row.createCell(last+1);
                                        }
                                    }
                                }
                            }
                        }
                        workbook.write(response.getOutputStream());
                    }
                    break;
                case 3:
                    params.put("warehouseId", warehouseId);
                    params.put("settlementDate", settlementDate);
                    Response<List<Map>> listResponse2 = doctorWarehouseMaterialHandleReadService.monthWarehouseDetail(params);
                    List<Map> result2 = listResponse2.getResult();
                    //开始导出
                    exporter.setHttpServletResponse(request, response, "仓库月报");

                    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                        //表
                        Sheet sheet = workbook.createSheet();
                        //样式
                        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 1));
                        sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
                        sheet.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
                        sheet.addMergedRegion(new CellRangeAddress(0, 1, 4, 4));
                        sheet.addMergedRegion(new CellRangeAddress(0, 1, 5, 5));
                        sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 7));
                        sheet.addMergedRegion(new CellRangeAddress(0, 0, 8, 9));
                        sheet.addMergedRegion(new CellRangeAddress(0, 0, 10, 11));
                        sheet.addMergedRegion(new CellRangeAddress(0, 0, 12, 13));
                        //行
                        Row head = sheet.createRow(0);

                        head.createCell(0).setCellValue("名称");
                        head.createCell(2).setCellValue("编码");
                        head.createCell(3).setCellValue("厂家");
                        head.createCell(4).setCellValue("规格");
                        head.createCell(5).setCellValue("单位");
                        head.createCell(6).setCellValue("月初余额");
                        head.createCell(8).setCellValue("本月入库合计");
                        head.createCell(10).setCellValue("本月出库合计");
                        head.createCell(12).setCellValue("月末余额");
                        //第一行结束

                        Row second = sheet.createRow(1);
                        second.createCell(6).setCellValue("月初余额数量");
                        second.createCell(7).setCellValue("月初余额金额（￥）");
                        second.createCell(8).setCellValue("本月入库数量");
                        second.createCell(9).setCellValue("本月入库金额（￥）");
                        second.createCell(10).setCellValue("本月出库数量");
                        second.createCell(11).setCellValue("本月出库金额（￥）");
                        second.createCell(12).setCellValue("月末余额数量");
                        second.createCell(13).setCellValue("月末余额金额（￥）");
                        //第二行结束

                        for(Map map:result2){
                            sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 1));
                            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                            if(map.get("materialName")!=null) {
                                row.createCell(0).setCellValue(map.get("materialName").toString());
                                row.createCell(2).setCellValue(map.get("code").toString());
                                row.createCell(3).setCellValue(map.get("vendorName").toString());
                                row.createCell(4).setCellValue(map.get("specification").toString());
                                row.createCell(5).setCellValue(map.get("unit").toString());
                                row.createCell(6).setCellValue(map.get("lastQuantity").toString());
                                row.createCell(7).setCellValue(map.get("lastAmount").toString());
                                row.createCell(8).setCellValue(map.get("inQuantity").toString());
                                row.createCell(9).setCellValue(map.get("inAmount").toString());
                                row.createCell(10).setCellValue(map.get("outQuantity").toString());
                                row.createCell(11).setCellValue(map.get("outAmount").toString());
                                row.createCell(12).setCellValue(map.get("balanceQuantity").toString());
                                row.createCell(13).setCellValue(map.get("balanceAmount").toString());
                            }
                        }
                        sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum()+1, sheet.getLastRowNum()+1, 0, 5));
                        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                        row.createCell(0).setCellValue("合计");
                        Map map = result2.get(result2.size() - 1);
                        row.createCell(6).setCellValue(map.get("allLastQuantity").toString());
                        row.createCell(7).setCellValue(map.get("allLastAmount").toString());
                        row.createCell(8).setCellValue(map.get("allInQuantity").toString());
                        row.createCell(9).setCellValue(map.get("allInAmount").toString());
                        row.createCell(10).setCellValue(map.get("allOutQuantity").toString());
                        row.createCell(11).setCellValue(map.get("allOutAmount").toString());
                        row.createCell(12).setCellValue(map.get("allBalanceQuantity").toString());
                        row.createCell(13).setCellValue(map.get("allBalanceAmount").toString());
                        //最后一行结束

                        workbook.write(response.getOutputStream());
                    }
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
            throw new JsonResponseException("warehouse.stock.handle.not.found");
        }
    }

    private Sheet setWorkStyle(Sheet sheet,List<Map> result,Integer rowNumber){
        int firstCol = 2;
        int lastCol =  3;
        for(int x=0;x<=result.size();x++,firstCol+=2,lastCol+=2){
            sheet.addMergedRegion(new CellRangeAddress(rowNumber,rowNumber,firstCol, lastCol));
        }
        return sheet;
    }

}
