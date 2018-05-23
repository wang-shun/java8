package io.terminus.doctor.web.front.warehouseV2;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApplyPigGroup;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApplyPigGroupDetail;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialApplyReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSettlementService;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseMaterialApplyVo;
import io.terminus.pampas.client.Export;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/doctor/warehouse/materia/apply")
public class WarehouseMateriaApplyController {

   @RpcConsumer
   private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

    @RpcConsumer
    private DoctorWarehouseSettlementService doctorWarehouseSettlementService;


    @Autowired
   private Exporter exporter;

    /**
     * 猪群领用报表
     * @param farmId
     * @param pigType
     * @param pigName
     * @param pigGroupName
     * @param skuType
     * @param skuName
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "piggroup/{farmId}")
    public Map<String,Object> selectPigGroupApply(@PathVariable Integer farmId,
                                                  @RequestParam Long orgId,
                                                  @RequestParam(required = false) String pigType,
                                                  @RequestParam(required = false) String pigName,
                                                  @RequestParam(required = false) String pigGroupName,
                                                  @RequestParam(required = false) Integer skuType,
                                                  @RequestParam(required = false) String skuName,
                                                  @RequestParam(required = false) String openAtStart,
                                                  @RequestParam(required = false) String openAtEnd,
                                                  @RequestParam(required = false) String closeAtStart,
                                                  @RequestParam(required = false) String closeAtEnd) throws ParseException {
        Map<String,Object> a = RespHelper.or500(doctorWarehouseMaterialApplyReadService.selectPigGroupApply(orgId,farmId,pigType,pigName,pigGroupName,skuType,skuName,openAtStart,openAtEnd,closeAtStart,closeAtEnd));
        return a;
    }

    /**
     * 猪群详情
     * @param pigGroupId
     * @param skuId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "piggroup/detail")
    public List<DoctorWarehouseMaterialApplyPigGroupDetail> PigGroupApplyDetail(@RequestParam Long orgId,
                                                                                @RequestParam(required = true) Long pigGroupId,
                                                                                @RequestParam(required = true) Long skuId){
        List<DoctorWarehouseMaterialApplyPigGroupDetail> a = RespHelper.or500(doctorWarehouseMaterialApplyReadService.selectPigGroupApplyDetail(orgId,pigGroupId,skuId));
        return a;
    }

    //猪群详情报表导出
    @RequestMapping(method  =  RequestMethod.GET,  value  =  "/piggroup/detail/export")
    public  void  selectPigGroupApplyExport(@RequestParam Long orgId,
                                            @RequestParam(required = true) Long pigGroupId,
                                            @RequestParam(required = true) Long skuId,
                                      HttpServletRequest  request,  HttpServletResponse  response) {
        //取到值
        List<DoctorWarehouseMaterialApplyPigGroupDetail> a = RespHelper.or500(doctorWarehouseMaterialApplyReadService.selectPigGroupApplyDetail(orgId,pigGroupId,skuId));
        //开始导出
        try  {
            //导出名称
            exporter.setHttpServletResponse(request,  response,"猪群详情报表");

            try  (XSSFWorkbook  workbook  =  new  XSSFWorkbook())  {
                //表
                Sheet  sheet  =  workbook.createSheet();
                sheet.createRow(0).createCell(0).setCellValue("猪群详情物料统计");

                Row  title  =  sheet.createRow(1);
                int  pos  =  2;

                title.createCell(0).setCellValue("物料名称");
                title.createCell(1).setCellValue("物料类型");
                title.createCell(2).setCellValue("仓库名称");
                title.createCell(3).setCellValue("事件日期");
                title.createCell(4).setCellValue("会计年月");
                title.createCell(5).setCellValue("事件类型");
                title.createCell(6).setCellValue("数量");
                title.createCell(7).setCellValue("单价");
                title.createCell(8).setCellValue("金额");
                title.createCell(9).setCellValue("猪舍名");
                title.createCell(10).setCellValue("猪舍类型");
                title.createCell(11).setCellValue("猪群名称");
                title.createCell(12).setCellValue("饲养员");
                title.createCell(13).setCellValue("猪场名称");
                title.createCell(14).setCellValue("单位");
                title.createCell(15).setCellValue("厂家");
                title.createCell(16).setCellValue("规格");


                for(DoctorWarehouseMaterialApplyPigGroupDetail  m:  a){
                    Row  row  =  sheet.createRow(pos++);
                    row.createCell(0).setCellValue(String.valueOf(m.getSkuName()));

                    String b=String.valueOf(m.getSkuType());
                    if(b.equals(String.valueOf(WareHouseType.FEED.getKey()))){
                        row.createCell(1).setCellValue(WareHouseType.FEED.getDesc());
                    }else if(a.equals(String.valueOf(WareHouseType.MATERIAL.getDesc()))){
                        row.createCell(1).setCellValue(WareHouseType.MATERIAL.getDesc());
                    }else if(a.equals(String.valueOf(WareHouseType.VACCINATION.getKey()))){
                        row.createCell(1).setCellValue(WareHouseType.VACCINATION.getDesc());
                    }else if(a.equals(String.valueOf(WareHouseType.MEDICINE.getKey()))){
                        row.createCell(1).setCellValue(WareHouseType.MEDICINE.getDesc());
                    }else if(a.equals(String.valueOf(WareHouseType.CONSUME.getKey()))){
                        row.createCell(1).setCellValue(WareHouseType.CONSUME.getDesc());
                    }

                    row.createCell(2).setCellValue(String.valueOf(m.getWarehouseName()));
                    row.createCell(3).setCellValue(String.valueOf(m.getApplyDate()));
                    row.createCell(4).setCellValue(String.valueOf(m.getSettlementDate()));
                    String f = String.valueOf(m.getType());
                    String materialHandleType=new String();
                    if(f.equals(String.valueOf(WarehouseMaterialHandleType.IN.getValue()))){
                        materialHandleType="采购入库";
                    }else if(f.equals(String.valueOf(WarehouseMaterialHandleType.OUT.getValue()))){
                        materialHandleType="领料出库";
                    }else if(f.equals(String.valueOf(WarehouseMaterialHandleType.TRANSFER.getValue()))){
                        materialHandleType="调拨";
                    }else if(f.equals(String.valueOf(WarehouseMaterialHandleType.INVENTORY.getValue()))){
                        materialHandleType="盘点";
                    }else if(f.equals(String.valueOf(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue()))){
                        materialHandleType="盘盈";
                    }else if(f.equals(String.valueOf(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue()))){
                        materialHandleType="盘亏";
                    }else if(f.equals(String.valueOf(WarehouseMaterialHandleType.TRANSFER_IN.getValue()))){
                        materialHandleType="调入";
                    }else if(f.equals(String.valueOf(WarehouseMaterialHandleType.TRANSFER_OUT.getValue()))){
                        materialHandleType="调出";
                    }else if(f.equals(String.valueOf(WarehouseMaterialHandleType.FORMULA_IN.getValue()))){
                        materialHandleType="配方生产入库";
                    }else if(f.equals(String.valueOf(WarehouseMaterialHandleType.FORMULA_OUT.getValue()))){
                        materialHandleType="配方生产出库";
                    }else if(f.equals(String.valueOf(WarehouseMaterialHandleType.RETURN.getValue()))){
                        materialHandleType="退料入库";
                    }
                    row.createCell(5).setCellValue(materialHandleType);
                    row.createCell(6).setCellValue(String.valueOf(m.getQuantity()));
                    if(m.getUnitPrice().equals(BigDecimal.ZERO)){
                        row.createCell(7).setCellValue("--");
                    }else{
                        row.createCell(7).setCellValue(String.valueOf(m.getUnitPrice()));
                    }
                    if(m.getAmount().equals(BigDecimal.ZERO)){
                        row.createCell(8).setCellValue("--");
                    }else{
                        row.createCell(8).setCellValue(String.valueOf(m.getAmount()));
                    }
                    row.createCell(9).setCellValue(String.valueOf(m.getPigBarnName()));

                    String c=String.valueOf(m.getPigType());
                    if(c.equals(String.valueOf(PigType.NURSERY_PIGLET.getValue()))){
                        row.createCell(10).setCellValue(PigType.NURSERY_PIGLET.getDesc());
                    }else if(c.equals(String.valueOf(PigType.FATTEN_PIG.getValue()))){
                        row.createCell(10).setCellValue(PigType.FATTEN_PIG.getDesc());
                    }else if(c.equals(String.valueOf(PigType.RESERVE.getValue()))){
                        row.createCell(10).setCellValue(PigType.RESERVE.getDesc());
                    }else if(c.equals(String.valueOf(PigType.MATE_SOW.getValue()))){
                        row.createCell(10).setCellValue(PigType.MATE_SOW.getDesc());
                    }else if(c.equals(String.valueOf(PigType.PREG_SOW.getValue()))){
                        row.createCell(10).setCellValue(PigType.PREG_SOW.getDesc());
                    }
                    else if(c.equals(String.valueOf(PigType.DELIVER_SOW.getValue()))){
                        row.createCell(10).setCellValue(PigType.DELIVER_SOW.getDesc());
                    }
                    else if(c.equals(String.valueOf(PigType.BOAR.getValue()))){
                        row.createCell(10).setCellValue(PigType.BOAR.getDesc());
                    }
                    row.createCell(11).setCellValue(String.valueOf(m.getPigGroupName()));
                    row.createCell(12).setCellValue(String.valueOf(m.getStaffName()));
                    row.createCell(13).setCellValue(String.valueOf(m.getFarmName()));
                    row.createCell(14).setCellValue(String.valueOf(m.getUnit()));
                    row.createCell(15).setCellValue(String.valueOf(m.getVendorName()));
                    row.createCell(16).setCellValue(String.valueOf(m.getSpecification()));
                }

                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //猪群领用报表导出
    @RequestMapping(method  =  RequestMethod.GET,  value  =  "/piggroup/{farmId}/export")
    public  void  PigGroupApplyDetailExport(@PathVariable Integer farmId,
                                            @RequestParam Long orgId,
                                            @RequestParam(required = false) String pigType,
                                            @RequestParam(required = false) String pigName,
                                            @RequestParam(required = false) String pigGroupName,
                                            @RequestParam(required = false) Integer skuType,
                                            @RequestParam(required = false) String skuName,
                                            @RequestParam(required = false) String openAtStart,
                                            @RequestParam(required = false) String openAtEnd,
                                            @RequestParam(required = false) String closeAtStart,
                                            @RequestParam(required = false) String closeAtEnd,
                                            HttpServletRequest  request,  HttpServletResponse  response) {
        //取到值
        List<DoctorWarehouseMaterialApplyPigGroup> a =doctorWarehouseMaterialApplyReadService.selectPigGroupApplys(orgId,farmId,pigType,pigName,pigGroupName,skuType,skuName,openAtStart,openAtEnd,closeAtStart,closeAtEnd);
        //开始导出
        try  {
            //导出名称
            exporter.setHttpServletResponse(request,  response,"猪群统计报表");

            try  (XSSFWorkbook  workbook  =  new  XSSFWorkbook())  {
                //表
                Sheet  sheet  =  workbook.createSheet();
                sheet.createRow(0).createCell(0).setCellValue("猪群物料统计");

                Row  title  =  sheet.createRow(1);
                int  pos  =  2;

                title.createCell(0).setCellValue("猪群号");
                title.createCell(1).setCellValue("猪舍名称");
                title.createCell(2).setCellValue("猪群类型");
                title.createCell(3).setCellValue("饲养员");
                title.createCell(4).setCellValue("物料编码");
                title.createCell(5).setCellValue("物料名称");
                title.createCell(6).setCellValue("单位");
                title.createCell(7).setCellValue("数量");
                title.createCell(8).setCellValue("单价");
                title.createCell(9).setCellValue("金额");
                title.createCell(10).setCellValue("物料类型");
                title.createCell(11).setCellValue("厂家");
                title.createCell(12).setCellValue("规格");
                title.createCell(13).setCellValue("建群日期");
                title.createCell(14).setCellValue("关群日期");
                title.createCell(15).setCellValue("猪场名称");

                for(DoctorWarehouseMaterialApplyPigGroup  m:  a){
                    Row  row  =  sheet.createRow(pos++);
                    row.createCell(0).setCellValue(String.valueOf(m.getPigGroupName()));
                    row.createCell(1).setCellValue(String.valueOf(m.getPigName()));
                    String c=String.valueOf(m.getPigType());
                    if(c.equals(String.valueOf(PigType.NURSERY_PIGLET.getValue()))){
                        row.createCell(2).setCellValue(PigType.NURSERY_PIGLET.getDesc());
                    }else if(c.equals(String.valueOf(PigType.FATTEN_PIG.getValue()))){
                        row.createCell(2).setCellValue(PigType.FATTEN_PIG.getDesc());
                    }else if(c.equals(String.valueOf(PigType.RESERVE.getValue()))){
                        row.createCell(2).setCellValue(PigType.RESERVE.getDesc());
                    }else if(c.equals(String.valueOf(PigType.MATE_SOW.getValue()))){
                        row.createCell(2).setCellValue(PigType.MATE_SOW.getDesc());
                    }else if(c.equals(String.valueOf(PigType.PREG_SOW.getValue()))){
                        row.createCell(2).setCellValue(PigType.PREG_SOW.getDesc());
                    }
                    else if(c.equals(String.valueOf(PigType.DELIVER_SOW.getValue()))){
                        row.createCell(2).setCellValue(PigType.DELIVER_SOW.getDesc());
                    }
                    else if(c.equals(String.valueOf(PigType.BOAR.getValue()))){
                        row.createCell(2).setCellValue(PigType.BOAR.getDesc());
                    }
                    row.createCell(3).setCellValue(String.valueOf(m.getStaffName()));
                    row.createCell(4).setCellValue(String.valueOf(m.getCode()));
                    row.createCell(5).setCellValue(String.valueOf(m.getSkuName()));
                    row.createCell(6).setCellValue(String.valueOf(m.getUnit()));
                    row.createCell(7).setCellValue(String.valueOf(m.getQuantity()));

                    if(m.getUnitPrice().equals(BigDecimal.ZERO)){
                        row.createCell(8).setCellValue("--");
                    }else{
                        row.createCell(8).setCellValue(String.valueOf(m.getUnitPrice()));
                    }
                    if(m.getAmount().equals(BigDecimal.ZERO)){
                        row.createCell(9).setCellValue("--");
                    }else{
                        row.createCell(9).setCellValue(String.valueOf(m.getAmount()));
                    }

                    String b=String.valueOf(m.getSkuType());
                    if(b.equals(String.valueOf(WareHouseType.FEED.getKey()))){
                        row.createCell(10).setCellValue(WareHouseType.FEED.getDesc());
                    }else if(a.equals(String.valueOf(WareHouseType.MATERIAL.getDesc()))){
                        row.createCell(10).setCellValue(WareHouseType.MATERIAL.getDesc());
                    }else if(a.equals(String.valueOf(WareHouseType.VACCINATION.getKey()))){
                        row.createCell(10).setCellValue(WareHouseType.VACCINATION.getDesc());
                    }else if(a.equals(String.valueOf(WareHouseType.MEDICINE.getKey()))){
                        row.createCell(10).setCellValue(WareHouseType.MEDICINE.getDesc());
                    }else if(a.equals(String.valueOf(WareHouseType.CONSUME.getKey()))){
                        row.createCell(10).setCellValue(WareHouseType.CONSUME.getDesc());
                    }
                    row.createCell(11).setCellValue(String.valueOf(m.getVendorName()));
                    row.createCell(12).setCellValue(String.valueOf(m.getSpecification()));
                    row.createCell(13).setCellValue(String.valueOf(m.getOpenAt()));
                    String g = m.getCloseAt();
                    if(g != null){
                        row.createCell(14).setCellValue(String.valueOf(g));
                    }else{
                        row.createCell(14).setCellValue("");
                    }
                    row.createCell(15).setCellValue(String.valueOf(m.getFarmName()));
                }

                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
