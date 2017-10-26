package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dao.DoctorWarehouseVendorDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseVendorOrgDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendor;

import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendorOrg;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-26 15:57:14
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseVendorWriteServiceImpl implements DoctorWarehouseVendorWriteService {

    @Autowired
    private DoctorWarehouseVendorDao doctorWarehouseVendorDao;
    @Autowired
    private DoctorWarehouseVendorOrgDao doctorWarehouseVendorOrgDao;

    @Override
    public Response<Long> create(DoctorWarehouseVendor doctorWarehouseVendor) {
        try {
            doctorWarehouseVendorDao.create(doctorWarehouseVendor);
            return Response.ok(doctorWarehouseVendor.getId());
        } catch (Exception e) {
            log.error("failed to create doctor warehouse vendor, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.vendor.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorWarehouseVendor doctorWarehouseVendor) {
        try {
            return Response.ok(doctorWarehouseVendorDao.update(doctorWarehouseVendor));
        } catch (Exception e) {
            log.error("failed to update doctor warehouse vendor, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.vendor.update.fail");
        }
    }

    @Override
    public Response<Boolean> delete(Long id) {
        try {
            return Response.ok(doctorWarehouseVendorDao.delete(id));
        } catch (Exception e) {
            log.error("failed to delete doctor warehouse vendor by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.vendor.delete.fail");
        }
    }

    @Override
    @ExceptionHandle("warehouse.vendor.bound.fail")
    public Response<Boolean> boundToOrg(Long vendorId, Long orgId) {

        Optional<DoctorWarehouseVendorOrg> vendorOrg = doctorWarehouseVendorOrgDao.findByOrgAndVendor(vendorId, orgId);
        if (!vendorOrg.isPresent()) {
            if (!doctorWarehouseVendorOrgDao.create(DoctorWarehouseVendorOrg.builder()
                    .vendorId(vendorId)
                    .orgId(orgId)
                    .build()))
                return Response.ok(false);
        }
        return Response.ok(true);
    }
}