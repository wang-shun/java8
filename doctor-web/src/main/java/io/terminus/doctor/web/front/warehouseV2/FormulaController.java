package io.terminus.doctor.web.front.warehouseV2;

import com.google.common.collect.ImmutableMap;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.dto.DoctorMaterialProductRatioDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseFormulaDto;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.FeedFormula;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.service.*;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSkuReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockWriteService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

import static com.google.common.base.Preconditions.checkState;

/**
 * 配方生产
 * Created by sunbo@terminus.io on 2017/8/18.
 */
@RestController
@RequestMapping("api/doctor/formula")
public class FormulaController {


    @Autowired
    private FeedFormulaReadService feedFormulaReadService;
    @Autowired
    private DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    @Autowired
    private DoctorWareHouseReadService doctorWareHouseReadService;

    @RpcConsumer
    private DoctorWarehouseStockReadService doctorWarehouseStockReadService;

    @RpcConsumer
    private DoctorWarehouseStockWriteService doctorWarehouseStockWriteService;

    @RpcConsumer
    private FeedFormulaWriteService feedFormulaWriteService;

    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    @RpcConsumer
    private DoctorWarehouseSkuReadService doctorWarehouseSkuReadService;

    /********************   2018/04/20  start   ***************************/
    @RequestMapping(method = RequestMethod.GET, value = "/formulaList")
    public Paging<FeedFormula> pagingFormulaList(
            Long farmId,
            String formulaName,
            String feedName,
            Integer pageNo,
            Integer pageSize) {
        return RespHelper.or500(feedFormulaReadService.pagingFormulaList(
                farmId, formulaName, feedName, pageNo, pageSize));
    }

    /********************   2018/04/20  end     ***************************/

    @RequestMapping(method = RequestMethod.GET)
    public Paging<FeedFormula> paging(@RequestParam("farmId") Long farmId,
                                      @RequestParam(value = "materialName", required = false) String materialName,
                                      @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return RespHelper.or500(feedFormulaReadService.paging(null, farmId, materialName, pageNo, pageSize));
    }


    @RequestMapping(method = RequestMethod.POST)
    public boolean create(@RequestBody DoctorMaterialProductRatioDto dto) {
        dto.getProduce().calculateTotalPercent();
        buildProduceInfo(dto.getProduce());
        // 此配方要生产的饲料
//        DoctorBasicMaterial feed = RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(dto.getMaterialId()));
        DoctorWarehouseSku feed = RespHelper.or500(doctorWarehouseSkuReadService.findById(dto.getMaterialId()));

        FeedFormula feedFormula = new FeedFormula();
        feedFormula.setFeedId(feed.getId());
        feedFormula.setFeedName(feed.getName());
        feedFormula.setFarmId(dto.getFarmId());
        feedFormula.setFormulaName(dto.getFormulalName());
        feedFormula.setFarmName(RespHelper.or500(doctorFarmReadService.findFarmById(dto.getFarmId())).getName());
        feedFormula.setFormulaMap(ImmutableMap.of("materialProduce", ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(dto.getProduce())));

        //由于现在可以一个饲料对应多个配方所以不需要判断是否存在，直接创建即可
        RespHelper.or500(feedFormulaWriteService.createFeedFormula(feedFormula));
        return true;
    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public FeedFormula get(@PathVariable Long id) {
        return RespHelper.or500(feedFormulaReadService.findFeedFormulaById(id));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{id}")
    public boolean update(@PathVariable Long id, @RequestBody DoctorMaterialProductRatioDto dto) {

        FeedFormula exist = RespHelper.or500(feedFormulaReadService.findFeedFormulaById(id));
        if (null == exist)
            throw new JsonResponseException("formula.not.exist");

        dto.getProduce().calculateTotalPercent();
        buildProduceInfo(dto.getProduce());
        // 此配方要生产的饲料
//        DoctorBasicMaterial feed = RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(dto.getMaterialId()));
        DoctorWarehouseSku feed = RespHelper.or500(doctorWarehouseSkuReadService.findById(dto.getMaterialId()));

        FeedFormula feedFormula = new FeedFormula();
        feedFormula.setId(id);
        feedFormula.setFeedId(feed.getId());
        feedFormula.setFeedName(feed.getName());
        feedFormula.setFarmId(dto.getFarmId());
        feedFormula.setFarmName(RespHelper.or500(doctorFarmReadService.findFarmById(dto.getFarmId())).getName());
        feedFormula.setFormulaName(dto.getFormulalName());
        feedFormula.setFormulaMap(ImmutableMap.of("materialProduce", ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(dto.getProduce())));

        //更新
        return RespHelper.or500(feedFormulaWriteService.updateFeedFormula(feedFormula));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "{id}")
    public boolean delete(@PathVariable Long id) {
        FeedFormula exist = RespHelper.or500(feedFormulaReadService.findFeedFormulaById(id));
        if (null == exist)
            throw new JsonResponseException("formula.not.exist");
        return RespHelper.or500(feedFormulaWriteService.deleteFeedFormulaById(id));
    }


    @RequestMapping(method = RequestMethod.GET, value = "preview")
    public FeedFormula.FeedProduce preview(@RequestParam("materialId") Long materialId, @RequestParam("produceCount") Long produceCount) {
        return RespHelper.or500(feedFormulaWriteService.produceMaterial(materialId, produceCount.doubleValue()));
    }



    private void buildProduceInfo(FeedFormula.FeedProduce materialProduce) {
//        materialProduce.getMaterialProduceEntries().forEach(s -> {
//            s.setMaterialName(RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(s.getMaterialId())).getName());
//        });
//
//        materialProduce.getMedicalProduceEntries().forEach(s -> {
//            s.setMaterialName(RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(s.getMaterialId())).getName());
//        });
        materialProduce.getMaterialProduceEntries().forEach(s -> {
            s.setMaterialName(RespHelper.or500(doctorWarehouseSkuReadService.findById(s.getMaterialId())).getName());
        });

        materialProduce.getMedicalProduceEntries().forEach(s -> {
            s.setMaterialName(RespHelper.or500(doctorWarehouseSkuReadService.findById(s.getMaterialId())).getName());
        });

    }
}
