package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.enums.WarehouseSkuStatus;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
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
    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;
    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;
    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;

    @Autowired
    private DoctorWarehouseVendorReadService doctorWarehouseVendorReadService;

    @Override
    @ExceptionHandle("doctor.warehouse.sku.create.fail")
    public Response<Long> create(DoctorWarehouseSku doctorWarehouseSku) {

        Map<String, Object> params = new HashMap<>();
        params.put("orgId", doctorWarehouseSku.getOrgId());
        params.put("code", doctorWarehouseSku.getCode());
        params.put("status", WarehouseSkuStatus.NORMAL.getValue());

        if (!doctorWarehouseSkuDao.list(params).isEmpty())
            throw new InvalidException("warehouse.sku.code.existed", doctorWarehouseSku.getCode());

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

        Map<String, Object> params = new HashMap<>();
        params.put("orgId", doctorWarehouseSku.getOrgId());
        params.put("code", doctorWarehouseSku.getCode());
        params.put("status", WarehouseSkuStatus.NORMAL.getValue());
        List<DoctorWarehouseSku> existedSku = doctorWarehouseSkuDao.list(params);
        if (!existedSku.isEmpty()) {
            if (existedSku.size() > 1)
                throw new InvalidException("warehouse.sku.code.existed", doctorWarehouseSku.getCode());
            if (existedSku.size() == 1 && !existedSku.get(0).getId().equals(doctorWarehouseSku.getId()))
                throw new InvalidException("warehouse.sku.code.existed", doctorWarehouseSku.getCode());
        }

        Boolean update = doctorWarehouseSkuDao.update(doctorWarehouseSku);
        boolean ff=false;
        if(update){
            //type name unit
            //update doctor_warehouse_material_apply set material_name='1022',type=2,unit=101 where material_id=12;
            DoctorWarehouseMaterialApply ma=new DoctorWarehouseMaterialApply();
            ma.setMaterialName(doctorWarehouseSku.getName());
            ma.setUnit(doctorWarehouseSku.getUnit());
            ma.setType(doctorWarehouseSku.getType());
            ma.setMaterialId(doctorWarehouseSku.getId());
            Boolean maBoolean = doctorWarehouseMaterialApplyDao.updateWarehouseMaterialApply(ma);

            //update doctor_warehouse_material_handle set vendor_name= ,material_name= ,unit= where material_id=;
            DoctorWarehouseMaterialHandle mh=new DoctorWarehouseMaterialHandle();
            mh.setVendorName(RespHelper.or500(doctorWarehouseVendorReadService.findNameById(doctorWarehouseSku.getVendorId())));
            mh.setMaterialName(doctorWarehouseSku.getName());
            mh.setUnit(doctorWarehouseSku.getUnit());
            mh.setMaterialId(doctorWarehouseSku.getId());
            Boolean mhBoolean = doctorWarehouseMaterialHandleDao.updateWarehouseMaterialHandle(mh);

            //update doctor_warehouse_stock set sku_name= where sku_id=;
            DoctorWarehouseStock s=new DoctorWarehouseStock();
            s.setSkuName(doctorWarehouseSku.getName());
            s.setSkuId(doctorWarehouseSku.getId());
            Boolean sBoolean = doctorWarehouseStockDao.updateWarehouseStock(s);
            ff=true;
        }

        return Response.ok(ff);
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

    @Override
    @ExceptionHandle("doctor.warehouse.sku.generate.code.fail")
    public Response<String> generateCode(Long orgId, Integer type) {

        String prefix = null;
        switch (type) {
            case 1:         //饲料
                prefix = "SL";
                break;
            case 2:         //原料
                prefix = "YL";
                break;
            case 3:         //疫苗
                prefix = "YM";
                break;
            case 4:         //药品
                prefix = "YP";
                break;
            case 5:         //消耗品
                prefix = "YHP";
                break;
            default:
                throw new InvalidException("warehouse.sku.type.not.support", type);
        }

//        Map<String, Object> params = new HashMap<>();
//        params.put("type", type);
//        params.put("orgId", orgId);
//        params.put("status", WarehouseSkuStatus.NORMAL.getValue());

//        List<DoctorWarehouseSku> skus = doctorWarehouseSkuDao.list(params);
        String lastCode = doctorWarehouseSkuDao.findLastCode(orgId, type);
        if (StringUtils.isBlank(lastCode))
            return Response.ok(prefix + "0001");

        String lasSkuCode = lastCode.substring(prefix.length());
//        if (!NumberUtils.isNumber(lasSkuCode))
//            throw new InvalidException("warehouse.sku.code.format.illegal", lasSkuCode);

        try {
            int lastCodeWithSameOrgAndType = Integer.parseInt(lasSkuCode);
            if (lastCodeWithSameOrgAndType >= 9999)
                throw new InvalidException("warehouse.sku.code.out.of.range", 9999);

            return Response.ok(prefix + StringUtils.leftPad(String.valueOf((lastCodeWithSameOrgAndType + 1)), 4, '0'));
        } catch (NumberFormatException e) {
            throw new InvalidException("warehouse.sku.code.format.illegal", lasSkuCode);
        }

    }

}