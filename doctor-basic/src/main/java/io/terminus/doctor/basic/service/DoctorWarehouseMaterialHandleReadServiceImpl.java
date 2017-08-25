package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 08:56:13
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseMaterialHandleReadServiceImpl implements DoctorWarehouseMaterialHandleReadService {

    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Override
    public Response<DoctorWarehouseMaterialHandle> findById(Long id) {
        try {
            return Response.ok(doctorWarehouseMaterialHandleDao.findById(id));
        } catch (Exception e) {
            log.error("failed to find doctor warehouse material handle by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseMaterialHandle>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseMaterialHandleDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouse material handle by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.paging.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseMaterialHandle>> paging(Integer pageNo, Integer pageSize, DoctorWarehouseMaterialHandle criteria) {

        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseMaterialHandleDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouse material handle by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.paging.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseMaterialHandle>> advPaging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {

        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseMaterialHandleDao.advPaging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouse material handle by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseMaterialHandle>> list(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorWarehouseMaterialHandleDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.list.fail");
        }
    }


    @Override
    public Response<List<DoctorWarehouseMaterialHandle>> advList(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorWarehouseMaterialHandleDao.advList(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.list.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseMaterialHandle>> list(DoctorWarehouseMaterialHandle criteria) {
        try {
            return Response.ok(doctorWarehouseMaterialHandleDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.list.fail");
        }
    }


    @Override
    public Response<Map<Long, Long>> countWarehouseAmount(List<DoctorWarehouseMaterialHandle> data) {
        Map<Long/*warehouseId*/, Long/*amount*/> amounts = new HashMap<>();
        for (DoctorWarehouseMaterialHandle inHandle : data) {
            if (!amounts.containsKey(inHandle.getWarehouseId()))
                amounts.put(inHandle.getWarehouseId(), inHandle.getQuantity().multiply(new BigDecimal(inHandle.getUnitPrice())).longValue());
            else {
                Long amount = amounts.get(inHandle.getWarehouseId());
                amounts.put(inHandle.getWarehouseId(), inHandle.getQuantity().multiply(new BigDecimal(inHandle.getUnitPrice())).longValue() + amount);
            }
        }
        return Response.ok(amounts);
    }

    @Override
    public Response<Map<WarehouseMaterialHandleType, Map<Long, Long>>> countWarehouseAmount(DoctorWarehouseMaterialHandle criteria, WarehouseMaterialHandleType... types) {


        Map<WarehouseMaterialHandleType, Map<Long, Long>> eachTypeAmounts = new HashMap<>();
        for (WarehouseMaterialHandleType type : types) {
            criteria.setType(type.getValue());
            List<DoctorWarehouseMaterialHandle> handles = doctorWarehouseMaterialHandleDao.list(criteria);
            Map<Long/*warehouseId*/, Long/*amount*/> amounts = countWarehouseAmount(handles).getResult();
            eachTypeAmounts.put(type, amounts);
        }

        return Response.ok(eachTypeAmounts);
    }
}
