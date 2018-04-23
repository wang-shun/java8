package io.terminus.doctor.web.front.warehouseV2;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialApplyReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseMaterialApplyVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/doctor/warehouse/materia/apply")
public class WarehouseMateriaApplyController {

   @RpcConsumer
    private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

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
}
