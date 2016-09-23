package io.terminus.doctor.web.front.warehouse.controller;

import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.warehouse.dto.DoctorMaterialProductRatioDto;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseBasicDto;
import io.terminus.doctor.warehouse.enums.IsOrNot;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.doctor.warehouse.service.DoctorMaterialInWareHouseReadService;
import io.terminus.doctor.warehouse.service.DoctorMaterialInfoReadService;
import io.terminus.doctor.warehouse.service.DoctorMaterialInfoWriteService;
import io.terminus.doctor.warehouse.service.DoctorWareHouseReadService;
import io.terminus.doctor.web.front.warehouse.dto.DoctorMaterialInfoCreateDto;
import io.terminus.doctor.web.front.warehouse.dto.DoctorMaterialInfoUpdateDto;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.select.Evaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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

    private final DoctorWareHouseReadService doctorWareHouseReadService;

    private final DoctorFarmReadService doctorFarmReadService;

    private final UserReadService<User> userReadService;

    private final DoctorBasicReadService doctorBasicReadService;

    private final DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    @Autowired
    public DoctorMaterialInfos(DoctorMaterialInfoWriteService doctorMaterialInfoWriteService,
                               DoctorMaterialInfoReadService doctorMaterialInfoReadService,
                               DoctorFarmReadService doctorFarmReadService,
                               UserReadService<User> userReadService,
                               DoctorBasicReadService doctorBasicReadService,
                               DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService,
                               DoctorWareHouseReadService doctorWareHouseReadService,
                               DoctorBasicMaterialReadService doctorBasicMaterialReadService){
        this.doctorMaterialInfoWriteService = doctorMaterialInfoWriteService;
        this.doctorMaterialInfoReadService = doctorMaterialInfoReadService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.userReadService = userReadService;
        this.doctorBasicReadService = doctorBasicReadService;
        this.doctorMaterialInWareHouseReadService = doctorMaterialInWareHouseReadService;
        this.doctorWareHouseReadService = doctorWareHouseReadService;
        this.doctorBasicMaterialReadService = doctorBasicMaterialReadService;
    }

//    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
    @Deprecated
    public Long createMaterialInfo(@RequestBody DoctorMaterialInfoCreateDto doctorMaterialInfoCreateDto){
        DoctorMaterialInfo doctorMaterialInfo = null;
        try{
            DoctorFarm doctorFarm = RespHelper.orServEx(doctorFarmReadService.findFarmById(doctorMaterialInfoCreateDto.getFarmId()));
            checkState(!isNull(doctorFarm), "find.doctorFarm.fail");

            Long userId = UserUtil.getUserId();
            Response<User> userResponse = userReadService.findById(userId);
            String username = RespHelper.orServEx(userResponse).getName();

            String unitName = RespHelper.orServEx(doctorBasicReadService.findBasicById(doctorMaterialInfoCreateDto.getUnitId())).getName();
            String unitGroupName = RespHelper.orServEx(doctorBasicReadService.findBasicById(doctorMaterialInfoCreateDto.getUnitGroupId())).getName();

            doctorMaterialInfo = DoctorMaterialInfo.builder()
                    .farmId(doctorMaterialInfoCreateDto.getFarmId()).farmName(doctorFarm.getName())
                    .type(doctorMaterialInfoCreateDto.getType()).canProduce(IsOrNot.NO.getKey())
                    .materialName(doctorMaterialInfoCreateDto.getMaterialName()).inputCode(doctorMaterialInfoCreateDto.getInputCode())
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
//    @RequestMapping(value = "/queryMaterialById", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
    @Deprecated
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
//    @RequestMapping(value = "/pagingMaterialInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
    @Deprecated
    public Paging<DoctorMaterialInfo> pagingDoctorMaterialInfo(@RequestParam("farmId") Long farmId,
                                                               @RequestParam(value = "type", required = false) Integer type,
                                                               @RequestParam(value = "canProduce", required = false) Integer canProduce,
                                                               @RequestParam(value = "materialName", required = false) String materialName,
                                                               @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                               @RequestParam(value = "pageSize",required = false)Integer pageSize){
        return RespHelper.or500(doctorMaterialInfoReadService.pagingMaterialInfos(farmId, type, canProduce, materialName, pageNo, pageSize));
    }

    @RequestMapping(value = "/pagingDoctorFeedInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorMaterialInfo> pagingDoctorFeedInfo(@RequestParam("farmId") Long farmId,
                                                           @RequestParam("materialName") String materialName,
                                                           @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                           @RequestParam(value = "pageSize", required = false) Integer pageSize){

        return RespHelper.or500(doctorMaterialInfoReadService.pagingMaterialInfos(farmId, WareHouseType.FEED.getKey(), IsOrNot.YES.getKey(), materialName, pageNo, pageSize));
//        return pagingDoctorMaterialInfo(farmId, WareHouseType.FEED.getKey(), IsOrNot.YES.getKey(), materialName, pageNo, pageSize);
    }

//    @RequestMapping(value = "/queryAllMaterialInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
    public List<DoctorMaterialInfo> queryAllMaterialInfo(@RequestParam("farmId") Long farmId,
                                                         @RequestParam(value = "type", required = false) Integer type){
        Paging<DoctorMaterialInfo> paging = RespHelper.or500(doctorMaterialInfoReadService.pagingMaterialInfos(farmId, type, null, null, 1, Integer.MAX_VALUE));
        return paging.getData();
    }

    @RequestMapping(value = "/queryMaterialInWareHouseInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorMaterialInWareHouse queryDoctorMaterialInWareHouseById(@RequestParam("materialInWareHouseId") Long materialInWareHouseId){
        return RespHelper.or500(doctorMaterialInWareHouseReadService.queryDoctorMaterialInWareHouse(materialInWareHouseId));
    }

    @RequestMapping(value = "/queryMaterialInWareHouse", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorMaterialInWareHouse queryMaterialInWareHouse(@RequestParam("wareHouseId") Long wareHouseId, @RequestParam("materialId") Long materialId){
        DoctorWareHouse wareHouse = RespHelper.or500(doctorWareHouseReadService.findById(wareHouseId));
        return RespHelper.or500(doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(wareHouse.getFarmId(), materialId, wareHouseId));
    }

    /**
     * 录入对应的物料生产规则信息
     * @param doctorMaterialProductRatioDto
     * @return
     */
    @RequestMapping(value = "/rules", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean createMaterialRules(@RequestBody DoctorMaterialProductRatioDto doctorMaterialProductRatioDto){
        buildProduceInfo(doctorMaterialProductRatioDto.getProduce());
        return RespHelper.or500(doctorMaterialInfoWriteService.createMaterialProductRatioInfo(
                doctorMaterialInfoBuild(doctorMaterialProductRatioDto.getMaterialId(),doctorMaterialProductRatioDto.getFarmId()),
                doctorMaterialProductRatioDto));
    }

    /**
     * 预生产数量信息 （通过默认规则设定，原料配比的数量）
     * @param produceCount 生产的数量
     * @return
     */
    @RequestMapping(value = "/preProduceMaterial", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorMaterialInfo.MaterialProduce preProduceMaterial(@RequestParam("materialId") Long materialId, @RequestParam("produceCount") Long produceCount){
        return RespHelper.or500(doctorMaterialInfoWriteService.produceMaterial(materialId, produceCount.doubleValue()));
    }

    /**
     * 生产对应的物料信息
     * @param farmId 对应的猪场Id
     * @param wareHouseId 对应的仓库Id
     * @param materialId 表 doctor_material_infos 的 id
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

            String farmName = RespHelper.orServEx(doctorFarmReadService.findFarmById(farmId)).getName();
            String wareHouseName = RespHelper.orServEx(doctorWareHouseReadService.queryDoctorWareHouseById(wareHouseId)).getWarehouseName();

            // TODO 获取对应的MaterialName, 名称
            String materialName = RespHelper.orServEx(doctorMaterialInfoReadService.queryById(materialId)).getMaterialName();

            Long userId = UserUtil.getUserId();
            Response<User> response =  userReadService.findById(userId);
            String userName = RespHelper.orServEx(response).getName();

            doctorWareHouseBasicDto = DoctorWareHouseBasicDto.builder()
                    .farmId(farmId).farmName(farmName)
                    .wareHouseId(wareHouseId).wareHouseName(wareHouseName)
                    .materialId(materialId).materialName(materialName)
                    .staffId(userId).staffName(userName)
                    .build();
        }catch (Exception e){
            log.error("build ware house basic dto fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
        return RespHelper.or500(doctorMaterialInfoWriteService.realProduceMaterial(doctorWareHouseBasicDto, materialProduce));
    }

    public void buildProduceInfo(DoctorMaterialInfo.MaterialProduce materialProduce){
        try{
            materialProduce.getMaterialProduceEntries().forEach(s->{
                s.setMaterialName(RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(s.getMaterialId())).getName());
            });

            materialProduce.getMedicalProduceEntries().forEach(s->{
                s.setMaterialName(RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(s.getMaterialId())).getName());
            });
        }catch (Exception e){
            log.error("build produce info fail, materialProduce:{}, cause:{}", materialProduce, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("convert.buildProduce.fail");
        }
    }

    /**
     * 构建对应的DoctorMaterialInfo
     * @param materialInfoId
     * @return
     */
    private DoctorMaterialInfo doctorMaterialInfoBuild(Long materialInfoId, Long farmId){
        DoctorBasicMaterial doctorBasicMaterial = RespHelper.or500(
                doctorBasicMaterialReadService.findBasicMaterialById(materialInfoId));

        String farmName = RespHelper.or500(doctorFarmReadService.findFarmById(farmId)).getName();
        Response<User> userResponse = userReadService.findById(UserUtil.getUserId());
        User user = userResponse.getResult();

        return DoctorMaterialInfo.builder()
                .id(materialInfoId).farmId(farmId).farmName(farmName)
                .type(doctorBasicMaterial.getType()).canProduce(IsOrNot.YES.getKey())
                .materialName(doctorBasicMaterial.getName()).inputCode(doctorBasicMaterial.getName())
                .unitId(doctorBasicMaterial.getUnitId()).unitName(doctorBasicMaterial.getUnitName())
                .unitGroupId(doctorBasicMaterial.getUnitGroupId()).unitGroupName(doctorBasicMaterial.getUnitGroupName())
                .defaultConsumeCount(doctorBasicMaterial.getDefaultConsumeCount()).price(doctorBasicMaterial.getPrice())
                .creatorId(user.getId()).creatorName(user.getName())
                .updatorId(user.getId()).updatorName(user.getName())
                .build();
    }
}
