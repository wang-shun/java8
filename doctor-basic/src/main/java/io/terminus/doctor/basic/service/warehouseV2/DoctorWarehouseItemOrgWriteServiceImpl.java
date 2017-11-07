package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dao.DoctorWarehouseItemOrgDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseItemOrg;

import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendorOrg;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseItemOrgWriteService;
import io.terminus.doctor.common.exception.InvalidException;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-11-02 22:15:38
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseItemOrgWriteServiceImpl implements DoctorWarehouseItemOrgWriteService {

    @Autowired
    private DoctorWarehouseItemOrgDao doctorWarehouseItemOrgDao;

    @Override
    public Response<Long> create(DoctorWarehouseItemOrg doctorWarehouseItemOrg) {
        try {
            doctorWarehouseItemOrgDao.create(doctorWarehouseItemOrg);
            return Response.ok(doctorWarehouseItemOrg.getId());
        } catch (Exception e) {
            log.error("failed to create doctor warehouse item org, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.item.org.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorWarehouseItemOrg doctorWarehouseItemOrg) {
        try {
            return Response.ok(doctorWarehouseItemOrgDao.update(doctorWarehouseItemOrg));
        } catch (Exception e) {
            log.error("failed to update doctor warehouse item org, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.item.org.update.fail");
        }
    }

    @Override
    public Response<Boolean> delete(Long id) {
        try {
            return Response.ok(doctorWarehouseItemOrgDao.delete(id));
        } catch (Exception e) {
            log.error("failed to delete doctor warehouse item org by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.item.org.delete.fail");
        }
    }

    @Override
    @Transactional
    @ExceptionHandle("doctor.warehouse.item.bound.to.org.fail")
    public Response<Boolean> boundToOrg(String itemIds, Long orgId) {

        doctorWarehouseItemOrgDao.deleteByOrg(orgId);

        for (String id : Arrays.stream(itemIds.split(",")).collect(Collectors.toSet())) {

            if (StringUtils.isBlank(id))
                continue;

            if (!NumberUtils.isNumber(id))
                throw new InvalidException("warehouse.item.id.not.number", id);

            Long itemId = Long.parseLong(id);

            if (!doctorWarehouseItemOrgDao.create(DoctorWarehouseItemOrg.builder()
                    .itemId(itemId)
                    .orgId(orgId)
                    .build()))
                return Response.fail("warehouse.item.bound.fail");
        }

        return Response.ok(true);
    }
}