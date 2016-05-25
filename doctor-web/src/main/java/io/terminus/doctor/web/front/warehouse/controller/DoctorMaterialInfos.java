package io.terminus.doctor.web.front.warehouse.controller;

import io.terminus.common.model.Paging;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.warehouse.dto.DoctorMaterialProductRatioDto;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseBasicDto;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import io.terminus.doctor.warehouse.service.DoctorMaterialInfoReadService;
import io.terminus.doctor.warehouse.service.DoctorMaterialInfoWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by yaoqijun.
 * Date:2016-05-25
 * Email:yaoqj@terminus.io
 * Descirbe: 物料信息的管理方式
 */
@Slf4j
@Controller
@RequestMapping("/api/doctor/warehouse/materialInfo")
public class DoctorMaterialInfos {

    private final DoctorMaterialInfoWriteService doctorMaterialInfoWriteService;

    private final DoctorMaterialInfoReadService doctorMaterialInfoReadService;

    public DoctorMaterialInfos(DoctorMaterialInfoWriteService doctorMaterialInfoWriteService,
                               DoctorMaterialInfoReadService doctorMaterialInfoReadService){
        this.doctorMaterialInfoWriteService = doctorMaterialInfoWriteService;
        this.doctorMaterialInfoReadService = doctorMaterialInfoReadService;
    }

    /**
     * 通过Id 筛选Material Info
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryMaterialById", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorMaterialInfo queryMaterialInfoById(@RequestParam("id") Long id){
        return RespHelper.or500(doctorMaterialInfoReadService.queryById(id));
    }

    /**
     * 分页获取原料数据信息
     * @param farmId
     * @param type
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/pagingMaterialInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorMaterialInfo> pagingDoctorMaterialInfo(@RequestParam("farmId") Long farmId,
                                                               @RequestParam(value = "type", required = false) Integer type,
                                                               @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                               @RequestParam(value = "pageSize",required = false)Integer pageSize){
        return RespHelper.or500(doctorMaterialInfoReadService.pagingMaterialInfos(farmId, type, pageNo, pageSize));
    }

    /**
     * 录入对应的物料生产规则信息
     * @param doctorMaterialProductRatioDto
     * @return
     */
    @RequestMapping(value = "/rules", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean createMaterialRules(@RequestBody DoctorMaterialProductRatioDto doctorMaterialProductRatioDto){
        return RespHelper.or500(doctorMaterialInfoWriteService.createMaterialProductRatioInfo(doctorMaterialProductRatioDto));
    }

    /**
     * 预生产数量信息 （通过默认规则设定，原料配比的数量）
     * @param produceId 对应的Material 生产原料的种类
     * @param produceCount 生产的数量
     * @return
     */
    @RequestMapping(value = "/preProduceMaterial", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorMaterialInfo.MaterialProduce preProduceMaterial(@RequestParam("produceId") Long produceId, @RequestParam("produceCount") Long produceCount){
        return RespHelper.or500(doctorMaterialInfoWriteService.produceMaterial(produceId, produceCount));
    }

    /**
     * 生产对应的物料信息
     * @param doctorWareHouseBasicDto Basic Info
     * @param materialProduce 生产信息
     * @return
     */
    @RequestMapping(value = "/realProduceMaterial", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean realProduceMaterial(@RequestParam("doctorWareHouseBasicDto") DoctorWareHouseBasicDto doctorWareHouseBasicDto,
                                       @RequestParam("materialProduce") DoctorMaterialInfo.MaterialProduce materialProduce){
        return RespHelper.orFalse(doctorMaterialInfoWriteService.realProduceMaterial(doctorWareHouseBasicDto, materialProduce));
    }
}
