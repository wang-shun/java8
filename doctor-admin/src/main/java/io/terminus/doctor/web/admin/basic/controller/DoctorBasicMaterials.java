package io.terminus.doctor.web.admin.basic.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.basic.service.DoctorBasicMaterialWriteService;
import io.terminus.doctor.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
/**
 * Desc: 基础物料表Controller
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-16
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/admin/basicMaterial")
public class DoctorBasicMaterials {

    @RpcConsumer
    private DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    @RpcConsumer
    private DoctorBasicMaterialWriteService doctorBasicMaterialWriteService;

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
    * 创建或更新DoctorBasicMaterial
    * @return 是否成功
    */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOrUpdateBasicMaterial(@RequestBody DoctorBasicMaterial basicMaterial) {
        checkNotNull(basicMaterial, "basicMaterial.not.null");

        // TODO: 权限中心校验权限

        if (basicMaterial.getId() == null) {

            RespHelper.or500(doctorBasicMaterialWriteService.createBasicMaterial(basicMaterial));
        } else {

            RespHelper.or500(doctorBasicMaterialWriteService.updateBasicMaterial(basicMaterial));
        }
        return Boolean.TRUE;
    }

    /**
     * 根据主键id删除DoctorBasicMaterial
     * @return 是否成功
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public Boolean deleteBasicMaterial(@RequestParam("basicMaterialId") Long basicMaterialId) {
        DoctorBasicMaterial basicMaterial = RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(basicMaterialId));

        return RespHelper.or500(doctorBasicMaterialWriteService.deleteBasicMaterialById(basicMaterialId));
    }

    /**
     * 查询全部基础物料(可以根据输入码过滤)
     * @return 基础物料list
     */
    @RequestMapping(value = "/srm", method = RequestMethod.GET)
    public List<DoctorBasicMaterial> deleteBasicMaterial(@RequestParam(value = "srm", required = false) String srm) {
        return RespHelper.or500(doctorBasicMaterialReadService.finaBasicMaterialFilterBySrm(srm));
    }
}