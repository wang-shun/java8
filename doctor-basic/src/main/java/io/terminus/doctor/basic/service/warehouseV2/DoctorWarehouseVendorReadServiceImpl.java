package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dao.DoctorWarehouseVendorDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendor;

import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-26 15:57:14
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseVendorReadServiceImpl implements DoctorWarehouseVendorReadService {

    @Autowired
    private DoctorWarehouseVendorDao doctorWarehouseVendorDao;

    @Override
    public Response<DoctorWarehouseVendor> findById(Long id) {
        try{
            return Response.ok(doctorWarehouseVendorDao.findById(id));
        }catch (Exception e){
            log.error("failed to find doctor warehouse vendor by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.vendor.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseVendor>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseVendorDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging doctor warehouse vendor by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.vendor.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseVendor>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorWarehouseVendorDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor warehouse vendor, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.vendor.list.fail");
        }
    }

}
