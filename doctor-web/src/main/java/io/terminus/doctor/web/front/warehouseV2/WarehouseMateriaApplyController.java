package io.terminus.doctor.web.front.warehouseV2;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialApplyReadService;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseMaterialApplyVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/doctor/warehouse/materia/apply")
public class WarehouseMateriaApplyController {

    @Autowired
    private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

    @RequestMapping(method = RequestMethod.GET, value = "piggroup/{farmId}")
    public List<WarehouseMaterialApplyVo> selectPigGroupApply(@PathVariable Integer farmId,
                                                              @RequestParam(required = false) Integer pigType,
                                                              @RequestParam(required = false) String pigName,
                                                              @RequestParam(required = false) String pigGroupName,
                                                              @RequestParam(required = false) Integer skuType,
                                                              @RequestParam(required = false) String skuName,
                                                              @RequestParam(required = false) Date openAt,
                                                              @RequestParam(required = false) Date closeAt){
        doctorWarehouseMaterialApplyReadService.selectPigGroupApply(farmId,pigType,pigName,pigGroupName,skuType,skuName,openAt,closeAt);
        return null;
    }
}
