package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialApplyReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSettlementService;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.web.core.export.Exporter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 猪舍领用
 */
@Slf4j
@RestController
@RequestMapping("api/doctor/warehouse/piggeryCollar")
public class PiggeryCollarController {

    @RpcConsumer
    private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

    @RpcConsumer
    private DoctorWarehouseSettlementService doctorWarehouseSettlementService;

    @Autowired
    private Exporter exporter;

    // 仓库领用明细报表 （陈娟 2018-10-17）
    @RequestMapping(method = RequestMethod.GET, value = "/collarReport")
    public Paging<Map> collarReport(@RequestParam Integer pageNo,
                            @RequestParam Integer pageSize,
                            @RequestParam Long orgId,
                            @RequestParam Long farmId,
                            @RequestParam String startDate,
                            @RequestParam String endDate,
                            @RequestParam(required = false) Integer materialType,
                            @RequestParam(required = false) String materialName,
                            @RequestParam(required = false) Integer pigType,
                            @RequestParam(required = false) String pigBarnName,
                            @RequestParam(required = false) String pigGroupName){
        // 判断筛选条件是否有猪群
        Integer flag = 0;
        if(null != pigGroupName && !pigGroupName.equals("")){
            flag = 1;
        }
        Paging<Map> mapPaging = RespHelper.or500(doctorWarehouseMaterialApplyReadService.collarReport(flag,pageNo, pageSize, orgId, farmId, startDate, endDate, materialType, materialName, pigType, pigBarnName, pigGroupName));
        return  mapPaging;
    }

    // 仓库领用明细报表导出 （陈娟 2018-10-19）
    @RequestMapping(method = RequestMethod.GET, value = "/collarReport/export")
    public void collarReportExport(@RequestParam Long orgId,
                                    @RequestParam Long farmId,
                                    @RequestParam String startDate,
                                    @RequestParam String endDate,
                                    @RequestParam(required = false) Integer materialType,
                                    @RequestParam(required = false) String materialName,
                                    @RequestParam(required = false) Integer pigType,
                                    @RequestParam(required = false) String pigBarnName,
                                    @RequestParam(required = false) String pigGroupName,
                                    HttpServletRequest request, HttpServletResponse response){
        // 判断筛选条件是否有猪群
        Integer flag = 0;
        if(null != pigGroupName && !pigGroupName.equals("")){
            flag = 1;
        }
        List<Map> maps = doctorWarehouseMaterialApplyReadService.collarReportExport(flag,orgId, farmId, startDate, endDate, materialType, materialName, pigType, pigBarnName, pigGroupName);
        //开始导出
        try {
            //导出名称
            exporter.setHttpServletResponse(request, response, "仓库领用明细报表");

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                //表
                Sheet sheet = workbook.createSheet();
                sheet.createRow(0).createCell(0).setCellValue("仓库领用明细统计");

                Row title = sheet.createRow(1);
                int pos = 2;

                title.createCell(0).setCellValue("领用日期");
                title.createCell(1).setCellValue("物料类型");
                title.createCell(2).setCellValue("物料名称");
                title.createCell(3).setCellValue("仓库名称");
                title.createCell(4).setCellValue("猪舍类型");
                title.createCell(5).setCellValue("猪舍名称");
                title.createCell(6).setCellValue("猪群名称");
                title.createCell(7).setCellValue("数量");
                title.createCell(8).setCellValue("单价");
                title.createCell(9).setCellValue("金额");
                title.createCell(10).setCellValue("单位");
                title.createCell(11).setCellValue("规格");
                title.createCell(12).setCellValue("厂家");

                for(Map m: maps){
                    Row row = sheet.createRow(pos++);

                    row.createCell(0).setCellValue(String.valueOf(m.get("apply_date")));
                    String b=String.valueOf(m.get("type"));
                    if(b.equals(String.valueOf(WareHouseType.FEED.getKey()))){
                        row.createCell(1).setCellValue(WareHouseType.FEED.getDesc());
                    }else if(b.equals(String.valueOf(WareHouseType.MATERIAL.getKey()))){
                        row.createCell(1).setCellValue(WareHouseType.MATERIAL.getDesc());
                    }else if(b.equals(String.valueOf(WareHouseType.VACCINATION.getKey()))){
                        row.createCell(1).setCellValue(WareHouseType.VACCINATION.getDesc());
                    }else if(b.equals(String.valueOf(WareHouseType.MEDICINE.getKey()))){
                        row.createCell(1).setCellValue(WareHouseType.MEDICINE.getDesc());
                    }else if(b.equals(String.valueOf(WareHouseType.CONSUME.getKey()))){
                        row.createCell(1).setCellValue(WareHouseType.CONSUME.getDesc());
                    }

                    row.createCell(2).setCellValue(String.valueOf(m.get("material_name")));
                    row.createCell(3).setCellValue(String.valueOf(m.get("warehouse_name")));
                    String a=String.valueOf(m.get("pig_type"));
                    if(a.equals(String.valueOf(PigType.NURSERY_PIGLET.getValue()))){
                        row.createCell(4).setCellValue(PigType.NURSERY_PIGLET.getDesc());
                    }else if(a.equals(String.valueOf(PigType.FATTEN_PIG.getValue()))){
                        row.createCell(4).setCellValue(PigType.FATTEN_PIG.getDesc());
                    }else if(a.equals(String.valueOf(PigType.RESERVE.getValue()))){
                        row.createCell(4).setCellValue(PigType.RESERVE.getDesc());
                    }else if(a.equals(String.valueOf(PigType.MATE_SOW.getValue()))){
                        row.createCell(4).setCellValue(PigType.MATE_SOW.getDesc());
                    }else if(a.equals(String.valueOf(PigType.PREG_SOW.getValue()))){
                        row.createCell(4).setCellValue(PigType.PREG_SOW.getDesc());
                    }else if(a.equals(String.valueOf(PigType.DELIVER_SOW.getValue()))){
                        row.createCell(4).setCellValue(PigType.DELIVER_SOW.getDesc());
                    }else if(a.equals(String.valueOf(PigType.BOAR.getValue()))){
                        row.createCell(4).setCellValue(PigType.BOAR.getDesc());
                    }
                    row.createCell(5).setCellValue(String.valueOf(m.get("pig_barn_name")));
                    row.createCell(6).setCellValue(String.valueOf(m.get("pig_group_name")));
                    row.createCell(7).setCellValue(String.valueOf(m.get("quantity")));
                    row.createCell(8).setCellValue(String.valueOf(m.get("unit_price")));
                    row.createCell(9).setCellValue(String.valueOf(m.get("amount")));
                    row.createCell(10).setCellValue(String.valueOf(m.get("unit")));
                    row.createCell(11).setCellValue(String.valueOf(m.get("specification")));
                    row.createCell(12).setCellValue(String.valueOf(m.get("vendor_name")));
                }

                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 猪舍领用报表
     * @param farmId
     * @param date
     * @param pigBarnId
     * @param pigType
     * @param type
     * @param materialName
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/piggeryReport")
    public List<Map> piggeryReport(@RequestParam Long farmId,
                                         @RequestParam Long orgId,
                                         @RequestParam String date,
                                         @RequestParam(required = false) Long pigBarnId,
                                         @RequestParam(required = false) Integer pigType,
                                         @RequestParam(required = false) Integer type,
                                         @RequestParam(required = false) String materialName){
//        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
//        response.setHeader("Access-Control-Allow-Origin", "*");
        DoctorWarehouseMaterialApply materialApply=new DoctorWarehouseMaterialApply();
        materialApply.setFarmId(farmId);
        if (null != date){
            String[] split = date.split("-");
            materialApply.setApplyYear( Integer.valueOf(split[0]));
            materialApply.setApplyMonth(Integer.valueOf(split[1]));
        }else{
            Date dd=new Date(System.currentTimeMillis());
            materialApply.setApplyYear(dd.getYear());
            materialApply.setApplyMonth(dd.getMonth());
        }
        if (null != pigBarnId){
            materialApply.setPigBarnId(pigBarnId);
        }
        if (null != pigType){
            materialApply.setPigType(pigType);
        }
        if (null != type){
            materialApply.setType(type);
        }
        if (null != materialName && !materialName.equals("")){
            materialApply.setMaterialName(materialName);
        }

        List<Map> maps = RespHelper.or500(doctorWarehouseMaterialApplyReadService.piggeryReport(orgId, date, materialApply));
        return maps;
    }


    /**
     * 猪舍领用详情
     * @param date
     * @param pigBarnId
     * @param materialName
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/piggeryDetails")
    public List<Map> piggeryDetails(@RequestParam Long orgId,
                                    @RequestParam String date,
                                    @RequestParam Long pigBarnId,
                                    @RequestParam(required = false) String materialName){
        DoctorWarehouseMaterialApply materialApply=new DoctorWarehouseMaterialApply();
        if (null != date){
            String[] split = date.split("-");
            materialApply.setApplyYear( Integer.valueOf(split[0]));
            materialApply.setApplyMonth(Integer.valueOf(split[1]));
        }else{
            Date dd=new Date(System.currentTimeMillis());
            materialApply.setApplyYear( dd.getYear());
            materialApply.setApplyMonth(dd.getMonth());
        }
        if (null != pigBarnId){
            materialApply.setPigBarnId(pigBarnId);
        }
        if (null != materialName && !materialName.equals("")){
            materialApply.setMaterialName(materialName);
        }

        List<Map> maps = RespHelper.or500(doctorWarehouseMaterialApplyReadService.piggeryDetails(orgId,date,materialApply));
        return maps;
    }

    //猪舍领用报表导出
    @RequestMapping(method = RequestMethod.GET, value = "/piggeryReport/export")
    public void piggeryReportExport(@RequestParam Long farmId,
                                    @RequestParam Long orgId,
                                    @RequestParam String date,
                                    @RequestParam(required = false) Long pigBarnId,
                                    @RequestParam(required = false) Integer pigType,
                                    @RequestParam(required = false) Integer type,
                                    @RequestParam(required = false) String materialName,
                                    HttpServletRequest request, HttpServletResponse response){
        //取到值
        DoctorWarehouseMaterialApply materialApply=new DoctorWarehouseMaterialApply();
        materialApply.setFarmId(farmId);
        if (null != date){
            String[] split = date.split("-");
            materialApply.setApplyYear( Integer.valueOf(split[0]));
            materialApply.setApplyMonth(Integer.valueOf(split[1]));
        }else{
            Date dd=new Date(System.currentTimeMillis());
            materialApply.setApplyYear( dd.getYear());
            materialApply.setApplyMonth(dd.getMonth());
        }
        if (null != pigBarnId){
            materialApply.setPigBarnId(pigBarnId);
        }
        if (null != pigType){
            materialApply.setPigType(pigType);
        }
        if (null != type){
            materialApply.setType(type);
        }
        if (null != materialName && !materialName.equals("")){
            materialApply.setMaterialName(materialName);
        }

        List<Map> maps = RespHelper.or500(doctorWarehouseMaterialApplyReadService.piggeryReport(orgId,date,materialApply));

        //开始导出
            try {
            //导出名称
            exporter.setHttpServletResponse(request, response, "猪舍统计报表");

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                //表
                Sheet sheet = workbook.createSheet();
                sheet.createRow(0).createCell(0).setCellValue("猪舍物料统计");

                Row title = sheet.createRow(1);
                int pos = 2;

                title.createCell(0).setCellValue("猪舍");
                title.createCell(1).setCellValue("猪舍类型");
                title.createCell(2).setCellValue("饲养员");
                title.createCell(3).setCellValue("会计年月");
                title.createCell(4).setCellValue("物料编码");
                title.createCell(5).setCellValue("物料名称");
                title.createCell(6).setCellValue("单位）");
                title.createCell(7).setCellValue("数量");
                title.createCell(8).setCellValue("单价");
                title.createCell(9).setCellValue("金额）");
                title.createCell(10).setCellValue("物料类别");
                title.createCell(11).setCellValue("厂家");
                title.createCell(12).setCellValue("规格");
                title.createCell(13).setCellValue("猪场名称");


                for(Map m: maps){
                    Row row = sheet.createRow(pos++);
                    row.createCell(0).setCellValue(String.valueOf(m.get("pig_barn_name")));
                    String a=String.valueOf(m.get("pig_type"));
                    if(a.equals(String.valueOf(PigType.NURSERY_PIGLET.getValue()))){
                        row.createCell(1).setCellValue(PigType.NURSERY_PIGLET.getDesc());
                    }else if(a.equals(String.valueOf(PigType.FATTEN_PIG.getValue()))){
                        row.createCell(1).setCellValue(PigType.FATTEN_PIG.getDesc());
                    }else if(a.equals(String.valueOf(PigType.RESERVE.getValue()))){
                        row.createCell(1).setCellValue(PigType.RESERVE.getDesc());
                    }else if(a.equals(String.valueOf(PigType.MATE_SOW.getValue()))){
                        row.createCell(1).setCellValue(PigType.MATE_SOW.getDesc());
                    }else if(a.equals(String.valueOf(PigType.PREG_SOW.getValue()))){
                        row.createCell(1).setCellValue(PigType.PREG_SOW.getDesc());
                    }else if(a.equals(String.valueOf(PigType.DELIVER_SOW.getValue()))){
                        row.createCell(1).setCellValue(PigType.DELIVER_SOW.getDesc());
                    }else if(a.equals(String.valueOf(PigType.BOAR.getValue()))){
                        row.createCell(1).setCellValue(PigType.BOAR.getDesc());
                    }

                    row.createCell(2).setCellValue(String.valueOf(m.get("staff_name")));
                    if(String.valueOf(m.get("settlement_year"))!=null&&String.valueOf(m.get("settlement_month"))!=null){
                        if(!String.valueOf(m.get("settlement_year")).equals("")&&!String.valueOf(m.get("settlement_month")).equals("")) {
                            row.createCell(3).setCellValue(String.valueOf(m.get("settlement_year")) + "-" + String.valueOf(m.get("settlement_month")) );
                        }
                    }
                    row.createCell(4).setCellValue(String.valueOf(m.get("material_code")));
                    row.createCell(5).setCellValue(String.valueOf(m.get("material_name")));
                    row.createCell(6).setCellValue(String.valueOf(m.get("unit")));
                    row.createCell(7).setCellValue(String.valueOf(m.get("sum_quantity")));
                    row.createCell(8).setCellValue(String.valueOf(m.get("sum_unit_price")));
                    row.createCell(9).setCellValue(String.valueOf(m.get("sum_amount")));
                    String b=String.valueOf(m.get("material_type"));
                    String materialType=new String();
                    if(b.equals(String.valueOf(WareHouseType.FEED.getKey()))){
                        materialType=WareHouseType.FEED.getDesc();
                    }else if(b.equals(String.valueOf(WareHouseType.MATERIAL.getKey()))){
                        materialType=WareHouseType.MATERIAL.getDesc();
                    }else if(b.equals(String.valueOf(WareHouseType.VACCINATION.getKey()))){
                        materialType=WareHouseType.VACCINATION.getDesc();
                    }else if(b.equals(String.valueOf(WareHouseType.MEDICINE.getKey()))){
                        materialType=WareHouseType.MEDICINE.getDesc();
                    }else if(b.equals(String.valueOf(WareHouseType.CONSUME.getKey()))){
                        materialType=WareHouseType.CONSUME.getDesc();
                    }
                    row.createCell(10).setCellValue(materialType);
                    row.createCell(11).setCellValue(String.valueOf(m.get("vendor_name")));
                    row.createCell(12).setCellValue(String.valueOf(m.get("specification")));
                    row.createCell(13).setCellValue(String.valueOf(m.get("farm_name")));
                }

                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //猪舍领用详情导出
    @RequestMapping(method = RequestMethod.GET, value = "/piggeryDetails/export")
    public void piggeryDetailsExport(@RequestParam Long orgId,
                                     @RequestParam String date,
                                     @RequestParam Long pigBarnId,
                                     @RequestParam(required = false) String materialName,
                                    HttpServletRequest request, HttpServletResponse response){
        //取到值
        DoctorWarehouseMaterialApply materialApply=new DoctorWarehouseMaterialApply();
        if (null != date){
            String[] split = date.split("-");
            materialApply.setApplyYear( Integer.valueOf(split[0]));
            materialApply.setApplyMonth(Integer.valueOf(split[1]));
        }else{
            Date dd=new Date(System.currentTimeMillis());
            materialApply.setApplyYear( dd.getYear());
            materialApply.setApplyMonth(dd.getMonth());
        }
        if (null != pigBarnId){
            materialApply.setPigBarnId(pigBarnId);
        }
        if (null != materialName && !materialName.equals("")){
            materialApply.setMaterialName(materialName);
        }

        List<Map> maps = RespHelper.or500(doctorWarehouseMaterialApplyReadService.piggeryDetails(orgId,date,materialApply));

        //开始导出
        try {
            //导出名称
            exporter.setHttpServletResponse(request, response, "猪舍领用详情");

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                //表
                Sheet sheet = workbook.createSheet();
                sheet.createRow(0).createCell(0).setCellValue("猪舍领用详情");

                Row title = sheet.createRow(1);
                int pos = 2;

                title.createCell(0).setCellValue("物料名称");
                title.createCell(1).setCellValue("物料类型");
                title.createCell(2).setCellValue("仓库名称");
                title.createCell(3).setCellValue("事件日期");
                title.createCell(4).setCellValue("会计年月");
                title.createCell(5).setCellValue("事件类型");
                title.createCell(6).setCellValue("数量");
                title.createCell(7).setCellValue("单价");
                title.createCell(8).setCellValue("金额");
                title.createCell(9).setCellValue("猪舍名称");
                title.createCell(10).setCellValue("猪舍类型");
                title.createCell(11).setCellValue("猪群名称");
                title.createCell(12).setCellValue("饲养员");
                title.createCell(13).setCellValue("猪场名称");
                title.createCell(14).setCellValue("单位");
                title.createCell(15).setCellValue("厂家");
                title.createCell(16).setCellValue("规格");


                for(Map m: maps){
                    Row row = sheet.createRow(pos++);
                    row.createCell(0).setCellValue(String.valueOf(m.get("material_name")));
                    String a=String.valueOf(m.get("material_type"));
                    String materialType=new String();
                    if(a.equals(String.valueOf(WareHouseType.FEED.getKey()))){
                        materialType=WareHouseType.FEED.getDesc();
                    }else if(a.equals(String.valueOf(WareHouseType.MATERIAL.getKey()))){
                        materialType=WareHouseType.MATERIAL.getDesc();
                    }else if(a.equals(String.valueOf(WareHouseType.VACCINATION.getKey()))){
                        materialType=WareHouseType.VACCINATION.getDesc();
                    }else if(a.equals(String.valueOf(WareHouseType.MEDICINE.getKey()))){
                        materialType=WareHouseType.MEDICINE.getDesc();
                    }else if(a.equals(String.valueOf(WareHouseType.CONSUME.getKey()))){
                        materialType=WareHouseType.CONSUME.getDesc();
                    }
                    row.createCell(1).setCellValue(materialType);
                    row.createCell(2).setCellValue(String.valueOf(m.get("warehouse_name")));
                    row.createCell(3).setCellValue(String.valueOf(m.get("handle_date")));
                    row.createCell(4).setCellValue(String.valueOf(m.get("settlement_year"))+"-"+String.valueOf(m.get("settlement_month")));
                    String b=String.valueOf(m.get("material_handle_type"));
                    String materialHandleType=new String();
                    if(b.equals(String.valueOf(WarehouseMaterialHandleType.IN.getValue()))){
                        materialHandleType="采购入库";
                    }else if(b.equals(String.valueOf(WarehouseMaterialHandleType.OUT.getValue()))){
                        materialHandleType="领料出库";
                    }else if(b.equals(String.valueOf(WarehouseMaterialHandleType.TRANSFER.getValue()))){
                        materialHandleType="调拨";
                    }else if(b.equals(String.valueOf(WarehouseMaterialHandleType.INVENTORY.getValue()))){
                        materialHandleType="盘点";
                    }else if(b.equals(String.valueOf(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue()))){
                        materialHandleType="盘盈";
                    }else if(b.equals(String.valueOf(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue()))){
                        materialHandleType="盘亏";
                    }else if(b.equals(String.valueOf(WarehouseMaterialHandleType.TRANSFER_IN.getValue()))){
                        materialHandleType="调入";
                    }else if(b.equals(String.valueOf(WarehouseMaterialHandleType.TRANSFER_OUT.getValue()))){
                        materialHandleType="调出";
                    }else if(b.equals(String.valueOf(WarehouseMaterialHandleType.FORMULA_IN.getValue()))){
                        materialHandleType="配方生产入库";
                    }else if(b.equals(String.valueOf(WarehouseMaterialHandleType.FORMULA_OUT.getValue()))){
                        materialHandleType="配方生产出库";
                    }else if(b.equals(String.valueOf(WarehouseMaterialHandleType.RETURN.getValue()))){
                        materialHandleType="退料入库";
                    }
                    row.createCell(5).setCellValue(materialHandleType);
                    row.createCell(6).setCellValue(String.valueOf(m.get("quantity")));
                    row.createCell(7).setCellValue(String.valueOf(m.get("unit_price")));
                    row.createCell(8).setCellValue(String.valueOf(m.get("amount")));
                    row.createCell(9).setCellValue(String.valueOf(m.get("pig_barn_name")));
                    String c=String.valueOf(m.get("pig_type"));
                    String pigType=new String();
                    if(c.equals(String.valueOf(PigType.NURSERY_PIGLET.getValue()))){
                        pigType=PigType.NURSERY_PIGLET.getDesc();
                    }else if(c.equals(String.valueOf(PigType.FATTEN_PIG.getValue()))){
                        pigType=PigType.FATTEN_PIG.getDesc();
                    }else if(c.equals(String.valueOf(PigType.RESERVE.getValue()))){
                        pigType=PigType.RESERVE.getDesc();
                    }else if(c.equals(String.valueOf(PigType.MATE_SOW.getValue()))){
                        pigType=PigType.MATE_SOW.getDesc();
                    }else if(c.equals(String.valueOf(PigType.PREG_SOW.getValue()))){
                        pigType=PigType.PREG_SOW.getDesc();
                    }else if(c.equals(String.valueOf(PigType.DELIVER_SOW.getValue()))){
                        pigType=PigType.DELIVER_SOW.getDesc();
                    }else if(c.equals(String.valueOf(PigType.BOAR.getValue()))){
                        pigType=PigType.BOAR.getDesc();
                    }
                    row.createCell(10).setCellValue(pigType);
                    row.createCell(11).setCellValue(String.valueOf(m.get("pig_group_name")));
                    row.createCell(12).setCellValue(String.valueOf(m.get("staff_name")));
                    row.createCell(13).setCellValue(String.valueOf(m.get("farm_name")));
                    row.createCell(14).setCellValue(String.valueOf(m.get("unit")));
                    row.createCell(15).setCellValue(String.valueOf(m.get("vendor_name")));
                    row.createCell(16).setCellValue(String.valueOf(m.get("specification")));
                }

                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
