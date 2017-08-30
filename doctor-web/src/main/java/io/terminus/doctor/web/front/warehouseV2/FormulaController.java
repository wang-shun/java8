package io.terminus.doctor.web.front.warehouseV2;

import com.google.common.collect.ImmutableMap;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.dto.DoctorMaterialProductRatioDto;
import io.terminus.doctor.basic.dto.DoctorWarehouseStockHandleDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseFormulaDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.FeedFormula;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandler;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandlerDetail;
import io.terminus.doctor.basic.service.*;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockWriteService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.pampas.common.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

/**
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
        DoctorBasicMaterial feed = RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(dto.getMaterialId()));

        FeedFormula feedFormula = new FeedFormula();
        feedFormula.setFeedId(feed.getId());
        feedFormula.setFeedName(feed.getName());
        feedFormula.setFarmId(dto.getFarmId());
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
        DoctorBasicMaterial feed = RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(dto.getMaterialId()));

        FeedFormula feedFormula = new FeedFormula();
        feedFormula.setId(id);
        feedFormula.setFeedId(feed.getId());
        feedFormula.setFeedName(feed.getName());
        feedFormula.setFarmId(dto.getFarmId());
        feedFormula.setFarmName(RespHelper.or500(doctorFarmReadService.findFarmById(dto.getFarmId())).getName());
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


    @RequestMapping(method = RequestMethod.POST, value = "produce")
    public boolean produce(@RequestParam("farmId") Long farmId,
                           @RequestParam("warehouseId") Long warehouseId,
                           @RequestParam("feedFormulaId") Long feedFormulaId,
                           @RequestParam("materialProduceJson") String materialProduceJson) {

        FeedFormula feedFormula = RespHelper.or500(feedFormulaReadService.findFeedFormulaById(feedFormulaId));

        if (null == feedFormula)
            throw new JsonResponseException("formula.not.found");

        FeedFormula.FeedProduce feedProduce = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(materialProduceJson, FeedFormula.FeedProduce.class);
        // 校验用户修改数量信息
        validateCountRange(feedProduce);

        // 查询对应的饲料
        DoctorBasicMaterial feed = RespHelper.orServEx(doctorBasicMaterialReadService.findBasicMaterialById(feedFormula.getFeedId()));

        // 校验生产后的入仓仓库类型
        DoctorWareHouse wareHouse = RespHelper.orServEx(doctorWareHouseReadService.findById(warehouseId));
        checkState(Objects.equals(wareHouse.getType(), feed.getType()), "produce.targetWarehouseType.fail");



        List<FeedFormula.MaterialProduceEntry> totalOut = new ArrayList<>(feedProduce.getMaterialProduceEntries());
        if (null != feedProduce.getMedicalProduceEntries() && !feedProduce.getMedicalProduceEntries().isEmpty())
            totalOut.addAll(feedProduce.getMedicalProduceEntries());

        WarehouseFormulaDto formulaDto = new WarehouseFormulaDto();
        formulaDto.setFarmId(farmId);
        formulaDto.setWarehouseId(warehouseId);
        formulaDto.setHandleDate(new Date());
        formulaDto.setFeedMaterial(feed);
        formulaDto.setFeedMaterialId(feed.getId());
        formulaDto.setFeedMaterialQuantity(new BigDecimal(feedProduce.getTotal()));
        List<WarehouseFormulaDto.WarehouseFormulaDetail> details = new ArrayList<>();
        for (FeedFormula.MaterialProduceEntry entry : totalOut) {
            WarehouseFormulaDto.WarehouseFormulaDetail detail = new WarehouseFormulaDto.WarehouseFormulaDetail();
            detail.setMaterialId(entry.getMaterialId());
            detail.setMaterialName(entry.getMaterialName());
            detail.setQuantity(new BigDecimal(entry.getMaterialCount()));
            details.add(detail);
        }
        formulaDto.setDetails(details);
        Response<Boolean> response = doctorWarehouseStockWriteService.formula(formulaDto);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());
        return true;
//        //入库
//        List<DoctorWarehouseStockHandleDto> inStockHandle = new ArrayList<>();
//        //出库
//        List<DoctorWarehouseStockHandleDto> outStockHandle = new ArrayList<>();
//
//
//        final BigDecimal tottalMoney = new BigDecimal(0);
//
//        Date handleDate = new Date();
//
//        //物料消耗
//        List<FeedFormula.MaterialProduceEntry> totalConsumMaterial = new ArrayList<>(feedProduce.getMaterialProduceEntries());
//        totalConsumMaterial.addAll(feedProduce.getMedicalProduceEntries());
//        totalConsumMaterial.forEach(material -> {
//            //获取库存
//            Response<List<DoctorWarehouseStock>> stockResponse = doctorWarehouseStockReadService.list(farmId, material.getMaterialId());
//            if (!stockResponse.isSuccess() || stockResponse.getResult().isEmpty())
//                throw new JsonResponseException("material.stock.not.found");
//
//            List<DoctorWarehouseStock> stocks = stockResponse.getResult();
//            BigDecimal materialTotalStock = stocks.stream().map(DoctorWarehouseStock::getQuantity).reduce((a, b) -> a.add(b)).orElse(new BigDecimal(0));
//            if (materialTotalStock.doubleValue() < material.getMaterialCount().doubleValue())
//                throw new JsonResponseException("material.stock.not.enough");
//
//            //对库存排序，从大到小，最大化减少需要操作的库存
//            List<DoctorWarehouseStock> sortedStocks = stocks.stream().sorted((s1, s2) ->
//                    s2.getQuantity().compareTo(s1.getQuantity())
//            ).collect(Collectors.toList());
//
//            double needConsumeMaterialCount = material.getMaterialCount().doubleValue();
//            for (DoctorWarehouseStock stock : sortedStocks) {
//
//                if (needConsumeMaterialCount <= 0)
//                    break;
//
//                double outStockNumber = stock.getQuantity().doubleValue() >= needConsumeMaterialCount ?
//                        needConsumeMaterialCount :
//                        stock.getQuantity().doubleValue();
//
//
//                DoctorWarehouseStockHandleDto outHandle = new DoctorWarehouseStockHandleDto();
//                outHandle.setNumber(new BigDecimal(outStockNumber));
//                outHandle.setHandleDate(handleDate);
//
//                outHandle.setStock(stock);
//                DoctorWarehouseStockHandlerDetail detail = new DoctorWarehouseStockHandlerDetail();
//                detail.setNumber(outHandle.getNumber());
//                outHandle.setHandleDetail(detail);
//                outStockHandle.add(outHandle);
//                needConsumeMaterialCount = needConsumeMaterialCount - outStockNumber;
////                tottalMoney.add(outHandle.getNumber().multiply(new BigDecimal(stock.getUnitPrice())));
//            }
//
//        });
//
//        DoctorWarehouseStockHandleDto inHandle = new DoctorWarehouseStockHandleDto();
//        inHandle.setNumber(new BigDecimal(feedProduce.getTotal()));
//        DoctorWarehouseStock criteria = new DoctorWarehouseStock();
//        Response<DoctorWarehouseStock> inStockResponse = doctorWarehouseStockReadService.findOneByCriteria(criteria);
//        if (!inStockResponse.isSuccess())
//            throw new JsonResponseException("find.stock.failed");
//        if (null == inStockResponse.getResult()) {
//            DoctorWarehouseStock stock = new DoctorWarehouseStock();
//            stock.setFarmId(farmId);
//            stock.setWarehouseId(warehouseId);
//            stock.setWarehouseName(wareHouse.getWareHouseName());
//            stock.setWarehouseType(wareHouse.getType());
//            stock.setMaterialId(feed.getId());
//            stock.setMaterialName(feed.getName());
//            stock.setManagerId(wareHouse.getManagerId());
//            stock.setUnit(feed.getUnitName());
//            //计算价格
//
////            stock.setUnitPrice(tottalMoney.divide(new BigDecimal(feedProduce.getTotal())).longValue());
//            inHandle.setStock(stock);
//        } else {
//            inHandle.setStock(inStockResponse.getResult());
//        }
//        DoctorWarehouseStockHandlerDetail handlerDetail = new DoctorWarehouseStockHandlerDetail();
//        handlerDetail.setNumber(inHandle.getNumber());
//        inHandle.setHandleDetail(handlerDetail);
//        inHandle.setHandleDate(handleDate);
//        inStockHandle.add(inHandle);
//
//
//        DoctorWarehouseStockHandler stockHandle = new DoctorWarehouseStockHandler();
//        stockHandle.setFarmId(farmId);
//        //配方生产后的物料的入仓仓库
//        stockHandle.setWarehouseId(warehouseId);
//        stockHandle.setHandlerType(WarehouseMaterialHandleType.FORMULA.getValue());
//        stockHandle.setHandlerDate(handleDate);
//        doctorWarehouseStockWriteService.outAndIn(inStockHandle, outStockHandle, stockHandle);

    }


    private void validateCountRange(FeedFormula.FeedProduce materialProduce) {
        Double realTotal = materialProduce.getMaterialProduceEntries().stream()
                .map(FeedFormula.MaterialProduceEntry::getMaterialCount)
                .reduce((a, b) -> a + b).orElse(0D);
        checkState(!Objects.equals(0, realTotal.intValue()), "input.materialProduceTotal.error");
    }


    private void buildProduceInfo(FeedFormula.FeedProduce materialProduce) {
        materialProduce.getMaterialProduceEntries().forEach(s -> {
            s.setMaterialName(RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(s.getMaterialId())).getName());
        });

        materialProduce.getMedicalProduceEntries().forEach(s -> {
            s.setMaterialName(RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(s.getMaterialId())).getName());
        });
    }
}
