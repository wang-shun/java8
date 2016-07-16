package io.terminus.doctor.web.front.basic.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Paging;
import io.terminus.doctor.basic.dto.DoctorBasicMaterialSearchDto;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desc: 基础物料表Controller
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-16
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/basicMaterial")
public class DoctorBasicMaterials {

    @RpcConsumer
    private DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    /**
     * 根据id查询基础物料表
     * @param basicMaterialId 主键id
     * @return 基础物料表
     */
    @RequestMapping(value = "/id", method = RequestMethod.GET)
    public DoctorBasicMaterial findBasicMaterialById(@RequestParam("basicMaterialId") Long basicMaterialId) {
        return RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(basicMaterialId));
    }

    /**
     * 分页查询基础物料
     * @param basicMaterial 基础物料
     * @return 基础物料list
     */
    @RequestMapping(value = "/paging", method = RequestMethod.POST)
    public Paging<DoctorBasicMaterial> finaBasicMaterialByTypeFilterBySrm(@RequestBody DoctorBasicMaterialSearchDto basicMaterial) {
        return RespHelper.or500(doctorBasicMaterialReadService.pagingBasicMaterialByTypeFilterBySrm(basicMaterial));
    }
}