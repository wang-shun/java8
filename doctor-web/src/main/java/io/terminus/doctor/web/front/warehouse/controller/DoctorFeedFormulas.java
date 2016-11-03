package io.terminus.doctor.web.front.warehouse.controller;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.warehouse.dto.DoctorMaterialProductRatioDto;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseBasicDto;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.doctor.warehouse.model.FeedFormula;
import io.terminus.doctor.warehouse.service.DoctorMaterialInWareHouseReadService;
import io.terminus.doctor.warehouse.service.DoctorWareHouseReadService;
import io.terminus.doctor.warehouse.service.FeedFormulaReadService;
import io.terminus.doctor.warehouse.service.FeedFormulaWriteService;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * 陈增辉
 * 通过配方来生产饲料的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/warehouse/materialInfo")
public class DoctorFeedFormulas {

    private final DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService;
    private final DoctorGroupWebService doctorGroupWebService;
    private final DoctorWareHouseReadService doctorWareHouseReadService;

    private final DoctorFarmReadService doctorFarmReadService;

    private final DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    private final FeedFormulaReadService feedFormulaReadService;

    private final FeedFormulaWriteService feedFormulaWriteService;

    @Autowired
    public DoctorFeedFormulas(DoctorGroupWebService doctorGroupWebService,
                              DoctorFarmReadService doctorFarmReadService,
                              DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService,
                              DoctorWareHouseReadService doctorWareHouseReadService,
                              DoctorBasicMaterialReadService doctorBasicMaterialReadService,
                              FeedFormulaReadService feedFormulaReadService,
                              FeedFormulaWriteService feedFormulaWriteService){
        this.doctorGroupWebService = doctorGroupWebService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorMaterialInWareHouseReadService = doctorMaterialInWareHouseReadService;
        this.doctorWareHouseReadService = doctorWareHouseReadService;
        this.doctorBasicMaterialReadService = doctorBasicMaterialReadService;
        this.feedFormulaReadService = feedFormulaReadService;
        this.feedFormulaWriteService = feedFormulaWriteService;
    }

    /**
     * 分页查询配方
     * @param farmId
     * @param materialName
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/pagingDoctorFeedInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Paging<FeedFormula> pagingDoctorFeedInfo(@RequestParam("farmId") Long farmId,
                                                           @RequestParam("materialName") String materialName,
                                                           @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                           @RequestParam(value = "pageSize", required = false) Integer pageSize){
        return RespHelper.or500(feedFormulaReadService.paging(null, farmId, materialName, pageNo, pageSize));
    }

    @RequestMapping(value = "/queryMaterialInWareHouseInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorMaterialInWareHouse queryDoctorMaterialInWareHouseById(@RequestParam("materialInWareHouseId") Long materialInWareHouseId){
        return RespHelper.or500(doctorMaterialInWareHouseReadService.queryDoctorMaterialInWareHouse(materialInWareHouseId));
    }

    @RequestMapping(value = "/queryMaterialInWareHouse", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorMaterialInWareHouse queryMaterialInWareHouse(@RequestParam("wareHouseId") Long wareHouseId, @RequestParam("materialId") Long materialId){
        DoctorWareHouse wareHouse = RespHelper.or500(doctorWareHouseReadService.findById(wareHouseId));
        return RespHelper.or500(doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(wareHouse.getFarmId(), materialId, wareHouseId));
    }

    /**
     * 录入对应的物料生产规则信息
     * @param dto
     * @return
     */
    @RequestMapping(value = "/rules", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean createMaterialRules(@RequestBody DoctorMaterialProductRatioDto dto){
        dto.getProduce().calculateTotalPercent();
        buildProduceInfo(dto.getProduce());
        // 此配方要生产的饲料
        DoctorBasicMaterial feed = RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(dto.getMaterialId()));

        FeedFormula feedFormula = new FeedFormula();
        feedFormula.setFeedId(feed.getId());
        feedFormula.setFeedName(feed.getName());
        feedFormula.setFarmId(dto.getFarmId());
        feedFormula.setFarmName(RespHelper.or500(doctorFarmReadService.findFarmById(dto.getFarmId())).getName());
        feedFormula.setFormulaMap(ImmutableMap.of("materialProduce", JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(dto.getProduce())));

        FeedFormula exist = RespHelper.or500(feedFormulaReadService.findFeedFormulaById(feed.getId(), dto.getFarmId()));
        if(exist == null){
            RespHelper.or500(feedFormulaWriteService.createFeedFormula(feedFormula));
        }else{
            feedFormula.setId(exist.getId());
            RespHelper.or500(feedFormulaWriteService.updateFeedFormula(feedFormula));
        }
        return true;
    }

    /**
     * 预生产数量信息 （通过默认规则设定，原料配比的数量）
     * @param produceCount 生产的数量
     * @return
     */
    @RequestMapping(value = "/preProduceMaterial", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public FeedFormula.FeedProduce preProduceMaterial(@RequestParam("materialId") Long materialId, @RequestParam("produceCount") Long produceCount){
        return RespHelper.or500(feedFormulaWriteService.produceMaterial(materialId, produceCount.doubleValue()));
    }

    /**
     * 生产对应的物料信息
     * @param farmId 对应的猪场Id
     * @param wareHouseId 对应的仓库Id
     * @param feedFormulaId 配方id
     * @return
     */
    @RequestMapping(value = "/realProduceMaterial", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean realProduceMaterial(@RequestParam("farmId") Long farmId,
                                       @RequestParam("wareHouseId") Long wareHouseId,
                                       @RequestParam("materialId") Long feedFormulaId,
                                       @RequestParam("materialProduce") String materialProduceJson){
        try{
            FeedFormula feedFormula = RespHelper.or500(feedFormulaReadService.findFeedFormulaById(feedFormulaId));

            FeedFormula.FeedProduce feedProduce = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(materialProduceJson, FeedFormula.FeedProduce.class);
            // 校验用户修改数量信息
            validateCountRange(feedProduce);

            // 查询对应的饲料
            DoctorBasicMaterial feed = RespHelper.orServEx(doctorBasicMaterialReadService.findBasicMaterialById(feedFormula.getFeedId()));

            // 校验仓库
            DoctorWareHouse wareHouse = RespHelper.orServEx(doctorWareHouseReadService.findById(wareHouseId));
            checkState(Objects.equals(wareHouse.getType(), feed.getType()), "produce.targetWarehouseType.fail");

            Long userId = UserUtil.getUserId();

            DoctorWareHouseBasicDto doctorWareHouseBasicDto = DoctorWareHouseBasicDto.builder()
                    .farmId(farmId).farmName(RespHelper.orServEx(doctorFarmReadService.findFarmById(farmId)).getName())
                    .wareHouseId(wareHouseId).wareHouseName(wareHouse.getWareHouseName())
                    .materialId(feed.getId()).materialName(feed.getName())
                    .staffId(userId).staffName(RespHelper.orServEx(doctorGroupWebService.findRealName(userId)))
                    .build();
            return RespHelper.or500(feedFormulaWriteService.produceFeedByFormula(doctorWareHouseBasicDto, wareHouse, feedFormula,
                    feed.getUnitId(), feed.getUnitName(), feedProduce));
        }catch (Exception e){
            log.error("build ware house basic dto fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
    }
    private void validateCountRange(FeedFormula.FeedProduce materialProduce){
        Double realTotal = materialProduce.getMaterialProduceEntries().stream()
                .map(FeedFormula.MaterialProduceEntry::getMaterialCount)
                .reduce((a,b)->a+b).orElse(0D);
        checkState(!Objects.equals(0, realTotal.intValue()), "input.materialProduceTotal.error");
        double dis = (realTotal-materialProduce.getTotal()) * 100d / materialProduce.getTotal();
        checkState(Math.abs(dis)<=5, "produce.materialCountChange.error");
    }

    private void buildProduceInfo(FeedFormula.FeedProduce materialProduce){
        materialProduce.getMaterialProduceEntries().forEach(s->{
            s.setMaterialName(RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(s.getMaterialId())).getName());
        });

        materialProduce.getMedicalProduceEntries().forEach(s->{
            s.setMaterialName(RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(s.getMaterialId())).getName());
        });
    }
}
