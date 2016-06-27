package io.terminus.doctor.web.front.warehouse.controller;

import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.warehouse.dto.DoctorMaterialProductRatioDto;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseBasicDto;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import io.terminus.doctor.warehouse.service.DoctorMaterialInWareHouseReadService;
import io.terminus.doctor.warehouse.service.DoctorMaterialInfoReadService;
import io.terminus.doctor.warehouse.service.DoctorMaterialInfoWriteService;
import io.terminus.doctor.web.front.warehouse.dto.DoctorMaterialInfoCreateDto;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

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

    private final DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService;

    private final DoctorFarmReadService doctorFarmReadService;

    private final UserReadService userReadService;

    private final DoctorBasicReadService doctorBasicReadService;

    @Autowired
    public DoctorMaterialInfos(DoctorMaterialInfoWriteService doctorMaterialInfoWriteService,
                               DoctorMaterialInfoReadService doctorMaterialInfoReadService,
                               DoctorFarmReadService doctorFarmReadService,
                               UserReadService userReadService,
                               DoctorBasicReadService doctorBasicReadService,
                               DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService){
        this.doctorMaterialInfoWriteService = doctorMaterialInfoWriteService;
        this.doctorMaterialInfoReadService = doctorMaterialInfoReadService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.userReadService = userReadService;
        this.doctorBasicReadService = doctorBasicReadService;
        this.doctorMaterialInWareHouseReadService = doctorMaterialInWareHouseReadService;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createMaterialInfo(@RequestBody DoctorMaterialInfoCreateDto doctorMaterialInfoCreateDto){
        DoctorMaterialInfo doctorMaterialInfo = null;
        try{
            DoctorFarm doctorFarm = RespHelper.orServEx(doctorFarmReadService.findFarmById(doctorMaterialInfoCreateDto.getFarmId()));
            checkState(!isNull(doctorFarm), "find.doctorFarm.fail");

            Long userId = UserUtil.getUserId();
            Response<User> userResponse = userReadService.findById(userId);
            String username = RespHelper.orServEx(userResponse).getName();

            String unitName = RespHelper.orServEx(doctorBasicReadService.findUnitById(doctorMaterialInfoCreateDto.getUnitId())).getName();
            String unitGroupName = RespHelper.orServEx(doctorBasicReadService.findUnitById(doctorMaterialInfoCreateDto.getUnitGroupId())).getName();

            doctorMaterialInfo = DoctorMaterialInfo.builder()
                    .farmId(doctorMaterialInfoCreateDto.getFarmId()).farmName(doctorFarm.getName())
                    .type(doctorMaterialInfoCreateDto.getType()).materialName(doctorMaterialInfoCreateDto.getMaterialName()).inputCode(doctorMaterialInfoCreateDto.getInputCode())
                    .remark(doctorMaterialInfoCreateDto.getMark())
                    .unitId(doctorMaterialInfoCreateDto.getUnitId()).unitName(unitName)
                    .unitGroupId(doctorMaterialInfoCreateDto.getUnitGroupId()).unitGroupName(unitGroupName)
                    .defaultConsumeCount(doctorMaterialInfoCreateDto.getDefaultConsumeCount()).price(doctorMaterialInfoCreateDto.getPrice())
                    .creatorId(userId).creatorName(username)
                    .build();
        }catch (Exception e){
            log.error("create material info fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
        return RespHelper.or500(doctorMaterialInfoWriteService.createMaterialInfo(doctorMaterialInfo));
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
    @RequestMapping(value = "/preProduceMaterial", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorMaterialInfo.MaterialProduce preProduceMaterial(@RequestParam("materialId") Long materialId, @RequestParam("produceCount") Long produceCount){
        return RespHelper.or500(doctorMaterialInfoWriteService.produceMaterial(materialId, produceCount));
    }

    /**
     * 生产对应的物料信息
     * @param doctorWareHouseBasicDto Basic Info
     * @param materialProduce 生产信息
     * @return
     */
    /**
     * 生产对应的物料信息
     * @param farmId 对应的猪场Id
     * @param wareHouseId 对应的仓库Id
     * @param materialId 对应的原料MaterialId
     * @param materialProduce 对应的用户定义生产规则
     * @return
     */
    @RequestMapping(value = "/realProduceMaterial", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean realProduceMaterial(@RequestParam("farmId") Long farmId,
                                       @RequestParam("wareHouseId") Long wareHouseId,
                                       @RequestParam("materialId") Long materialId,
                                       @RequestParam("materialProduce") String materialProduceJson){
        DoctorWareHouseBasicDto doctorWareHouseBasicDto = null;
        DoctorMaterialInfo.MaterialProduce materialProduce = null;
        try{
            materialProduce = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(materialProduceJson, DoctorMaterialInfo.MaterialProduce.class);
            DoctorMaterialInWareHouse dto= RespHelper.orServEx(doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(farmId, materialId, wareHouseId));
            Long userId = UserUtil.getUserId();
            Response<User> response =  userReadService.findById(userId);
            String userName = RespHelper.orServEx(response).getName();

            doctorWareHouseBasicDto = DoctorWareHouseBasicDto.builder()
                    .farmId(farmId).farmName(dto.getFarmName())
                    .wareHouseId(wareHouseId).wareHouseName(dto.getWareHouseName())
                    .materialId(materialId).materialName(dto.getMaterialName())
                    .staffId(userId).staffName(userName)
                    .build();
        }catch (Exception e){
            log.error("build ware house basic dto fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
        return RespHelper.orFalse(doctorMaterialInfoWriteService.realProduceMaterial(doctorWareHouseBasicDto, materialProduce));
    }
}
