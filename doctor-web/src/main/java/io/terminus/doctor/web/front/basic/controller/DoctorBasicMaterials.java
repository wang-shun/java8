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

import java.util.List;

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
    @RequestMapping(value = "/paging", method = RequestMethod.GET)
    public Paging<DoctorBasicMaterial> pagingBasicMaterialByTypeFilterBySrm(@RequestBody DoctorBasicMaterialSearchDto basicMaterial) {
        return RespHelper.or500(doctorBasicMaterialReadService.pagingBasicMaterialByTypeFilterBySrm(basicMaterial));
    }

    /**
     * 查询全部基础物料(可以根据输入码过滤)
     * @param type 基础物料类型
     * @see io.terminus.doctor.common.enums.WareHouseType
     * @param srm 输入码
     * @param exIds 排除掉的ids
     * @return 基础物料list
     */
    @RequestMapping(value = "/type", method = RequestMethod.GET)
    public List<DoctorBasicMaterial> finaBasicMaterialByTypeFilterBySrm(@RequestParam(value = "type", required = false) Integer type,
                                                                        @RequestParam(value = "srm", required = false) String srm,
                                                                        @RequestParam(value = "exIds", required = false) String exIds) {
        return RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialByTypeFilterBySrm(type, srm, exIds));
    }

    /**
     * 查询全部物料数据表
     * @return 物料数据信息
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<DoctorBasicMaterial> findAllBasicMaterials() {
        return RespHelper.or500(doctorBasicMaterialReadService.findAllBasicMaterials());
    }
}