package io.terminus.doctor.basic.service.warehouseV2;

import com.sun.tools.javac.api.ClientCodeWrapper;
import io.terminus.doctor.basic.dao.DoctorWarehouseVendorDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseVendorOrgDao;

import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.enums.WarehouseVendorDeleteFlag;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendor;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendorOrg;
import io.terminus.doctor.common.exception.InvalidException;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Transactional
    @ExceptionHandle("doctor.warehouse.vendor.create.fail")
    public Response<Long> create(DoctorWarehouseVendor doctorWarehouseVendor) {

        if (null != doctorWarehouseVendorDao.findByName(doctorWarehouseVendor.getName()))
            throw new InvalidException("doctor.vendor.name.duplicate", doctorWarehouseVendor.getName());

        doctorWarehouseVendorDao.create(doctorWarehouseVendor);
        return Response.ok(doctorWarehouseVendor.getId());
    }

    @Override
    @Transactional
    @ExceptionHandle("doctor.warehouse.vendor.update.fail")
    public Response<Boolean> update(DoctorWarehouseVendor doctorWarehouseVendor) {

        DoctorWarehouseVendor vendor = doctorWarehouseVendorDao.findByName(doctorWarehouseVendor.getName());
        if (null != vendor && vendor.getId() != doctorWarehouseVendor.getId())
            throw new InvalidException("doctor.vendor.name.duplicate", doctorWarehouseVendor.getName());

        return Response.ok(doctorWarehouseVendorDao.update(doctorWarehouseVendor));
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
    @ExceptionHandle("doctor.warehouse.vendor.delete.fail")
    public Response<Boolean> logicDelete(Long id) {
        DoctorWarehouseVendor vendor = doctorWarehouseVendorDao.findById(id);
        if (null != vendor) {
            vendor.setDeleteFlag(WarehouseVendorDeleteFlag.DELETE.getValue());
            doctorWarehouseVendorDao.update(vendor);
        } else {
            log.warn("warehouse vendor [{}] not found,ignore logic delete");
        }

        return Response.ok(true);
    }

    @Override
    @ExceptionHandle("warehouse.vendor.bound.fail")
    @Transactional
    public Response<Boolean> boundToOrg(String vendorIds, Long orgId) {

        doctorWarehouseVendorOrgDao.deleteByOrg(orgId);

        for (String id : Arrays.stream(vendorIds.split(",")).collect(Collectors.toSet())) { //过滤潜在重复的内容

            if (!NumberUtils.isNumber(id))
                throw new InvalidException("warehouse.vendor.id.not.number", id);

            Long vendorId = Long.parseLong(id);

            if (!doctorWarehouseVendorOrgDao.create(DoctorWarehouseVendorOrg.builder()
                    .vendorId(vendorId)
                    .orgId(orgId)
                    .build()))
                return Response.fail("warehouse.vendor.bound.fail");
        }
        return Response.ok(true);
    }
}