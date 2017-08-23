package io.terminus.doctor.basic.service;

import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;

import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialHandle;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;

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
    public Response<List<DoctorWarehouseMaterialHandle>> list(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorWarehouseMaterialHandleDao.list(criteria));
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


}
