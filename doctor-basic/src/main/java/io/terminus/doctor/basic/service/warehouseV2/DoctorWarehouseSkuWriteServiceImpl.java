package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.dao.DoctorBasicMaterialDao;
import io.terminus.doctor.basic.dao.DoctorFarmBasicDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseSkuDao;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.common.exception.InvalidException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-13 13:14:30
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseSkuWriteServiceImpl implements DoctorWarehouseSkuWriteService {

    @Autowired
    private DoctorWarehouseSkuDao doctorWarehouseSkuDao;
    @Autowired
    private DoctorBasicMaterialDao doctorBasicMaterialDao;
    @Autowired
    private DoctorFarmBasicDao doctorFarmBasicDao;

    @Override
    @ExceptionHandle("doctor.warehouse.sku.create.fail")
    public Response<Long> create(DoctorWarehouseSku doctorWarehouseSku) {

        Map<String, Object> params = new HashMap<>();
        params.put("orgId", doctorWarehouseSku.getOrgId());
        params.put("code", doctorWarehouseSku.getCode());
        if (!doctorWarehouseSkuDao.list(params).isEmpty())
            throw new InvalidException("warehouse.sku.code.existed", doctorWarehouseSku.getOrgId(), doctorWarehouseSku.getCode());

//        params.clear();
//        params.put("orgId", doctorWarehouseSku.getOrgId());
//        params.put("farmId", doctorWarehouseSku.getFarmId());
//        params.put("warehouseId", doctorWarehouseSku.getWarehouseId());
//        params.put("itemId", doctorWarehouseSku.getItemId());
//        params.put("name", doctorWarehouseSku.getName());
//        params.put("vendorName", doctorWarehouseSku.getVendorName());
//        params.put("unit", doctorWarehouseSku.getUnit());
//        params.put("specification", doctorWarehouseSku.getSpecification());
//        if (!doctorWarehouseSkuDao.list(params).isEmpty())
//            throw new InvalidException("warehouse.sku.existed");

        DoctorBasicMaterial item = doctorBasicMaterialDao.findById(doctorWarehouseSku.getItemId());
        if (null == item)
            throw new InvalidException("basic.material.not.found", doctorWarehouseSku.getItemId());

//        DoctorFarmBasic doctorFarmBasic = doctorFarmBasicDao.findByFarmId(doctorWarehouseSku.getFarmId());
//        if (null == doctorFarmBasic)
//            throw new InvalidException("farm.basic.not.found", doctorWarehouseSku.getFarmId());

//        if (!doctorFarmBasic.getMaterialIdList().contains(doctorWarehouseSku.getItemId()))
//            throw new InvalidException("basic.material.not.allow.in.this.warehouse");

        doctorWarehouseSku.setType(item.getType());

        doctorWarehouseSkuDao.create(doctorWarehouseSku);
        return Response.ok(doctorWarehouseSku.getId());
    }

    @Override
    @ExceptionHandle("doctor.warehouse.sku.update.fail")
    public Response<Boolean> update(DoctorWarehouseSku doctorWarehouseSku) {

        DoctorBasicMaterial item = doctorBasicMaterialDao.findById(doctorWarehouseSku.getItemId());
        if (null == item)
            throw new InvalidException("basic.material.not.found", doctorWarehouseSku.getItemId());

//        DoctorFarmBasic doctorFarmBasic = doctorFarmBasicDao.findByFarmId(doctorWarehouseSku.getFarmId());
//        if (null == doctorFarmBasic)
//            throw new InvalidException("farm.basic.not.found", doctorWarehouseSku.getFarmId());

//        if (!doctorFarmBasic.getMaterialIdList().contains(doctorWarehouseSku.getItemId()))
//            throw new InvalidException("basic.material.not.allow.in.this.warehouse");

        return Response.ok(doctorWarehouseSkuDao.update(doctorWarehouseSku));
    }

    @Override
    public Response<Boolean> delete(Long id) {
        try {
            return Response.ok(doctorWarehouseSkuDao.delete(id));
        } catch (Exception e) {
            log.error("failed to delete doctor warehouse sku by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.sku.delete.fail");
        }
    }

}