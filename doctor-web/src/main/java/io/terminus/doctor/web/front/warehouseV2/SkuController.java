package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.doctor.basic.enums.WarehouseSkuStatus;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendor;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSkuReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSkuWriteService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseVendorReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.web.front.warehouseV2.dto.WarehouseSkuDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2017/10/13.
 */
@RestController
@RequestMapping("api/doctor/warehouse/sku")
public class SkuController {

    @RpcConsumer
    private DoctorWarehouseSkuReadService doctorWarehouseSkuReadService;
    @RpcConsumer
    private DoctorWarehouseSkuWriteService doctorWarehouseSkuWriteService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private DoctorWarehouseVendorReadService doctorWarehouseVendorReadService;
    @RpcConsumer
    private DoctorBasicReadService doctorBasicReadService;

    @RpcConsumer
    private DoctorWarehouseStockReadService doctorWarehouseStockReadService;

    @RequestMapping(method = RequestMethod.GET, value = "paging")
    public Paging<WarehouseSkuDto> query(@RequestParam(required = false) Long orgId,
                                         @RequestParam(required = false) Long farmId,
                                         @RequestParam(required = false) Integer type,
                                         @RequestParam(required = false) String srm,
                                         @RequestParam(required = false) String srmOrName,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(required = false) Integer pageNo,
                                         @RequestParam(required = false) Integer pageSize) {

        if (null == orgId && null == farmId)
            throw new JsonResponseException("warehouse.sku.org.id.or.farm.id.not.null");

        Map<String, Object> params = new HashMap<>();
        if (null != orgId)
            params.put("orgId", orgId);
        else {
            DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
            if (null == farm)
                throw new JsonResponseException("farm.not.found");
            params.put("orgId", farm.getOrgId());
        }

        if (StringUtils.isNotBlank(srm))
            params.put("srm", srm);
        if (null != type)
            params.put("type", type);
        if (StringUtils.isNotBlank(srmOrName))
            params.put("nameOrSrmLike", srmOrName);
        if (null != status)
            params.put("status", status);

        Paging<DoctorWarehouseSku> skuPaging = RespHelper.or500(doctorWarehouseSkuReadService.paging(pageNo, pageSize, params));

        return new Paging<WarehouseSkuDto>(skuPaging.getTotal(),
                skuPaging.getData().stream().map(sku -> {
                    WarehouseSkuDto skuDto = new WarehouseSkuDto();
                    skuDto.copyFrom(sku);
                    skuDto.setUnitId(Long.parseLong(sku.getUnit()));
                    DoctorBasic unit = RespHelper.or500(doctorBasicReadService.findBasicById(skuDto.getUnitId()));
                    if (null != unit)
                        skuDto.setUnit(unit.getName());
                    skuDto.setVendorName(RespHelper.or500(doctorWarehouseVendorReadService.findNameById(sku.getVendorId())));
                    return skuDto;
                }).collect(Collectors.toList()));
    }

    @RequestMapping(method = RequestMethod.GET, value = "all")
    public List<WarehouseSkuDto> query(@RequestParam(required = false) Long orgId,
                                       @RequestParam(required = false) Long farmId,
                                       @RequestParam(required = false) Integer type,
                                       @RequestParam(required = false) String srm,
                                       @RequestParam(required = false) String srmOrName,
                                       @RequestParam(required = false) Integer status) {

        if (null == orgId && null == farmId)
            throw new JsonResponseException("warehouse.sku.org.id.or.farm.id.not.null");

        Map<String, Object> params = new HashMap<>();
        if (null != orgId)
            params.put("orgId", orgId);
        else {
            DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
            if (null == farm)
                throw new JsonResponseException("farm.not.found");
            params.put("orgId", farm.getOrgId());
        }

        if (StringUtils.isNotBlank(srm))
            params.put("srm", srm);
        if (null != type)
            params.put("type", type);
        if (StringUtils.isNotBlank(srmOrName))
            params.put("nameOrSrmLike", srmOrName);
        if (null != status)
            params.put("status", status);

        List<DoctorWarehouseSku> skuList = RespHelper.or500(doctorWarehouseSkuReadService.list(params));

        return skuList.stream().map(sku -> {
            WarehouseSkuDto skuDto = new WarehouseSkuDto();
            skuDto.copyFrom(sku);
            skuDto.setUnitId(Long.parseLong(sku.getUnit()));
            DoctorBasic unit = RespHelper.or500(doctorBasicReadService.findBasicById(skuDto.getUnitId()));
            if (null != unit)
                skuDto.setUnit(unit.getName());
            skuDto.setVendorName(RespHelper.or500(doctorWarehouseVendorReadService.findNameById(sku.getVendorId())));
            return skuDto;
        }).collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public WarehouseSkuDto query(@PathVariable Long id) {
        DoctorWarehouseSku sku = RespHelper.or500(doctorWarehouseSkuReadService.findById(id));
        if (null == sku)
            return null;

        WarehouseSkuDto dto = new WarehouseSkuDto();
        BeanUtils.copyProperties(sku, dto);

        dto.setUnitId(Long.parseLong(sku.getUnit()));
        DoctorBasic unit = RespHelper.or500(doctorBasicReadService.findBasicById(dto.getUnitId()));
        if (null != unit)
            dto.setUnit(unit.getName());
        dto.setVendorName(RespHelper.or500(doctorWarehouseVendorReadService.findNameById(sku.getVendorId())));
        return dto;
    }


    @RequestMapping(method = RequestMethod.PUT)
    public boolean edit(@RequestBody @Validated(WarehouseSkuDto.UpdateValid.class) WarehouseSkuDto skuDto, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        if (skuDto.getOrgId() == null && skuDto.getFarmId() == null) {
            throw new JsonResponseException("warehouse.sku.org.id.or.farm.id.not.null");
        }
        if (skuDto.getOrgId() == null) {
            DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(skuDto.getFarmId()));
            if (null == farm)
                throw new JsonResponseException("farm.not.found");
            skuDto.setOrgId(farm.getOrgId());
        }

        if (skuDto.getStatus().equals(WarehouseSkuStatus.FORBIDDEN.getValue())) {
            //改成停用需要检查一下该物料是否有在仓库中
            Map<String, Object> params = new HashMap<>();
            params.put("farmIds", RespHelper.or500(doctorFarmReadService.findFarmsByOrgId(skuDto.getOrgId())).stream().map(DoctorFarm::getId).collect(Collectors.toList()));
            params.put("skuId", skuDto.getId());
            params.put("effective", "true");
            Long count = RespHelper.or500(doctorWarehouseStockReadService.advCount(params));
            if (null != count && count > 0) {
                throw new JsonResponseException("warehouse.sku.has.stock");
            }
        }

        DoctorWarehouseSku sku = new DoctorWarehouseSku();
        BeanUtils.copyProperties(skuDto, sku);
        sku.setUnit(skuDto.getUnitId().toString());
        return RespHelper.or500(doctorWarehouseSkuWriteService.update(sku));
    }


    @RequestMapping(method = RequestMethod.POST)
    public boolean save(@RequestBody @Validated(WarehouseSkuDto.CreateValid.class) WarehouseSkuDto skuDto, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        if (skuDto.getOrgId() == null && skuDto.getFarmId() == null) {
            throw new JsonResponseException("warehouse.sku.org.id.or.farm.id.not.null");
        }
        if (skuDto.getOrgId() == null) {
            DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(skuDto.getFarmId()));
            if (null == farm)
                throw new JsonResponseException("farm.not.found");
            skuDto.setOrgId(farm.getOrgId());
        }

        DoctorWarehouseSku sku = new DoctorWarehouseSku();
        BeanUtils.copyProperties(skuDto, sku);
        sku.setUnit(skuDto.getUnitId().toString());
        return null != RespHelper.or500(doctorWarehouseSkuWriteService.create(sku));
    }


    @RequestMapping(method = RequestMethod.GET, value = "code")
    public String getCode(@RequestParam(required = false) Long orgId,
                          @RequestParam(required = false) Long farmId,
                          @RequestParam Integer type) {

        if (null == orgId && null == farmId)
            throw new JsonResponseException("warehouse.sku.org.id.or.farm.id.not.null");

        if (null == orgId) {
            DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
            if (null == farm)
                throw new JsonResponseException("farm.not.found");
            orgId = farm.getOrgId();
        }

        return RespHelper.or500(doctorWarehouseSkuWriteService.generateCode(orgId, type));
    }
}
