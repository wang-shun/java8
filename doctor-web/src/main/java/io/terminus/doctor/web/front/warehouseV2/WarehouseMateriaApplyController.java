package io.terminus.doctor.web.front.warehouseV2;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApplyPigGroup;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialApplyReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseMaterialApplyVo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
@RequestMapping("api/doctor/warehouse/materia/apply")
public class WarehouseMateriaApplyController {

   @RpcConsumer
    private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

    /**
     * 猪群领用报表
     * @param farmId
     * @param pigType
     * @param pigName
     * @param pigGroupName
     * @param skuType
     * @param skuName
     * @param openAt
     * @param closeAt
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "piggroup/{farmId}")
    public Map<String,Object> selectPigGroupApply(@PathVariable Integer farmId,
                                                                               @RequestParam(required = false) String pigType,
                                                                               @RequestParam(required = false) String pigName,
                                                                               @RequestParam(required = false) String pigGroupName,
                                                                               @RequestParam(required = false) Integer skuType,
                                                                               @RequestParam(required = false) String skuName,
                                                                               @RequestParam(required = false) Date openAt,
                                                                               @RequestParam(required = false) Date closeAt){
        Map<String,Object> a = RespHelper.or500(doctorWarehouseMaterialApplyReadService.selectPigGroupApply(farmId,pigType,pigName,pigGroupName,skuType,skuName,openAt,closeAt));
        return a;
    }
    @RequestMapping(method = RequestMethod.GET, value = "piggroup/detail/{farmId}")
    public Map<String,Object> PigGroupApplyDetail(@PathVariable Integer farmId,
                                                  @RequestParam(required = false) Long pigGroupId,
                                                  @RequestParam(required = false) Integer skuId){
        //Map<String,Object> a = RespHelper.or500(doctorWarehouseMaterialApplyReadService.selectPigGroupApply(farmId,skuType));
        return null;
    }
}
