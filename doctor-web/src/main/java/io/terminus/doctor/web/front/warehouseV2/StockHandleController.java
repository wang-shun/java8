package io.terminus.doctor.web.front.warehouseV2;

import com.google.api.client.util.Charsets;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendor;
import io.terminus.doctor.basic.service.DoctorWareHouseReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialHandleReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSkuReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockHandleReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseVendorReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.warehouseV2.vo.StockHandleExportVo;
import io.terminus.doctor.web.front.warehouseV2.vo.StockHandleVo;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseEventExportVo;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.boot.autoconfigure.web.ConditionalOnEnabledResourceChain;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存操作单据
 * 出库
 * 入库
 * 调拨
 * 盘点
 * Created by sunbo@terminus.io on 2017/10/31.
 */
@RestController
@RequestMapping("api/doctor/warehouse/receipt")
public class StockHandleController {

    @Autowired
    private Exporter exporter;

    @RpcConsumer
    private DoctorWarehouseStockHandleReadService doctorWarehouseStockHandleReadService;
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

    @InitBinder
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
    }

    @RequestMapping(method = RequestMethod.GET)
    public Paging<DoctorWarehouseStockHandle> paging(@RequestParam Long farmId,
                                                     @RequestParam(required = false) Integer pageNo,
                                                     @RequestParam(required = false) Integer pageSize,
                                                     @RequestParam(required = false) Date startDate,
                                                     @RequestParam(required = false) Date endDate,
                                                     @RequestParam(required = false) Long operatorId,
                                                     @RequestParam(required = false) Long warehouseId,
                                                     @RequestParam(required = false) Integer type,
                                                     @RequestParam(required = false) Integer subType) {

        if (null != startDate && null != endDate && startDate.after(endDate))
            throw new JsonResponseException("start.date.after.end.date");

        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmId);
        params.put("warehouseId", warehouseId);
        params.put("operatorId", operatorId);
        params.put("handleType", type);
        params.put("handleSubType", subType);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return RespHelper.or500(doctorWarehouseStockHandleReadService.paging(pageNo, pageSize, params));
    }


    @RequestMapping(method = RequestMethod.GET, value = "{id:\\d+}")
    public StockHandleVo query(@PathVariable Long id) {

        DoctorWarehouseStockHandle stockHandle = RespHelper.or500(doctorWarehouseStockHandleReadService.findById(id));
        if (null == stockHandle)
            return null;

        StockHandleVo vo = new StockHandleVo();
        BeanUtils.copyProperties(stockHandle, vo);

        vo.setDetails(
                RespHelper.or500(doctorWarehouseMaterialHandleReadService.findByStockHandle(stockHandle.getId()))
                        .stream()
                        .map(mh -> {
                            StockHandleVo.Detail detail = new StockHandleVo.Detail();
                            BeanUtils.copyProperties(mh, detail);

                            DoctorWarehouseSku sku = RespHelper.or500(doctorWarehouseSkuReadService.findById(mh.getMaterialId()));
                            if (null != sku) {
                                DoctorWarehouseVendor vendor = RespHelper.or500(doctorWarehouseVendorReadService.findById(sku.getVendorId()));
                                if (vendor != null)
                                    detail.setVendorName(vendor.getShortName());
                                detail.setMaterialCode(sku.getCode());
                                detail.setMaterialSpecification(sku.getSpecification());
                            }

                            return detail;
                        })
                        .collect(Collectors.toList()));

        DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(vo.getFarmId()));
        if (farm != null) {
            vo.setFarmName(farm.getName());
            vo.setOrgName(farm.getOrgName());
        }

        DoctorWareHouse wareHouse = RespHelper.or500(doctorWareHouseReadService.findById(vo.getWarehouseId()));
        if (wareHouse != null) {
            vo.setWarehouseManagerName(wareHouse.getManagerName());
        }

        if (!vo.getDetails().isEmpty()) {
            vo.setWarehouseType(vo.getDetails().get(0).getWarehouseType());
        }

        BigDecimal totalQuantity = new BigDecimal(0);
        long totalUnitPrice = 0L;
        for (StockHandleVo.Detail detail : vo.getDetails()) {
            totalQuantity = totalQuantity.add(detail.getQuantity());
            totalUnitPrice += detail.getUnitPrice();
        }
        vo.setTotalQuantity(totalQuantity.doubleValue());
        vo.setTotalAmount(totalQuantity.multiply(new BigDecimal(totalUnitPrice)).doubleValue());

        return vo;
    }


    @RequestMapping(method = RequestMethod.GET, value = "{id:\\d+}/export")
    public void export(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {

        List<StockHandleExportVo> exportVos = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findByStockHandle(id))
                .stream()
                .map(mh -> {
                    StockHandleExportVo vo = new StockHandleExportVo();
                    BeanUtils.copyProperties(mh, vo);

                    DoctorWarehouseSku sku = RespHelper.or500(doctorWarehouseSkuReadService.findById(mh.getMaterialId()));
                    if (null != sku) {
                        DoctorWarehouseVendor vendor = RespHelper.or500(doctorWarehouseVendorReadService.findById(sku.getVendorId()));
                        if (vendor != null)
                            vo.setVendorName(vendor.getName());
                        vo.setMaterialCode(sku.getCode());
                        vo.setMaterialSpecification(sku.getSpecification());
                    }
                    vo.setUnitPrice(new BigDecimal(mh.getUnitPrice()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    vo.setAmount(new BigDecimal(mh.getUnitPrice()).multiply(vo.getQuantity()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    return vo;
                })
                .collect(Collectors.toList());


        try {
            exporter.setHttpServletResponse(request, response, "仓库单据");

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet();
                Row title = sheet.createRow(0);
                title.createCell(0).setCellValue("物料名称");
                title.createCell(1).setCellValue("厂家");
                title.createCell(2).setCellValue("物料编码");
                title.createCell(3).setCellValue("规格");
                title.createCell(4).setCellValue("单位");
                title.createCell(5).setCellValue("数量");
                title.createCell(6).setCellValue("单价（元）");
                title.createCell(7).setCellValue("金额（元）");
                title.createCell(8).setCellValue("备注");

                int pos = 1;
                for (StockHandleExportVo vo : exportVos) {
                    Row row = sheet.createRow(pos++);
                    row.createCell(0).setCellValue(vo.getMaterialName());
                    row.createCell(1).setCellValue(vo.getVendorName());
                    row.createCell(2).setCellValue(vo.getMaterialCode());
                    row.createCell(3).setCellValue(vo.getMaterialSpecification());
                    row.createCell(4).setCellValue(vo.getUnit());
                    row.createCell(5).setCellValue(vo.getQuantity().doubleValue());
                    row.createCell(6).setCellValue(vo.getUnitPrice());
                    row.createCell(7).setCellValue(vo.getAmount());
                    row.createCell(8).setCellValue(vo.getRemark());
                }

                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        exporter.export(RespHelper.or500(doctorWarehouseMaterialHandleReadService.findByStockHandle(id))
//                .stream()
//                .map(mh -> {
//                    StockHandleExportVo vo = new StockHandleExportVo();
//                    BeanUtils.copyProperties(mh, vo);
//
//                    DoctorWarehouseSku sku = RespHelper.or500(doctorWarehouseSkuReadService.findById(mh.getMaterialId()));
//                    if (null != sku) {
//                        DoctorWarehouseVendor vendor = RespHelper.or500(doctorWarehouseVendorReadService.findById(sku.getVendorId()));
//                        if (vendor != null)
//                            vo.setVendorName(vendor.getName());
//                        vo.setMaterialCode(sku.getCode());
//                        vo.setMaterialSpecification(sku.getSpecification());
//                    }
//                    vo.setUnitPrice(new BigDecimal(mh.getUnitPrice()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
//                    vo.setAmount(new BigDecimal(mh.getUnitPrice()).multiply(vo.getQuantity()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
//                    return vo;
//                })
//                .collect(Collectors.toList()), "web-wareHouse-stock-handle", request, response);
    }


}
