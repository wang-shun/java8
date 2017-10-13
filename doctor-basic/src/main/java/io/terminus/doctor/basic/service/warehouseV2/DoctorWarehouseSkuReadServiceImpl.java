package io.terminus.doctor.basic.service.warehouseV2;

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
 * Date: 2017-10-13 13:14:30
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseSkuReadServiceImpl implements DoctorWarehouseSkuReadService {

    @Autowired
    private DoctorWarehouseSkuDao doctorWarehouseSkuDao;

    @Override
    public Response<DoctorWarehouseSku> findById(Long id) {
        try{
            return Response.ok(doctorWarehouseSkuDao.findById(id));
        }catch (Exception e){
            log.error("failed to find doctor warehouse sku by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.sku.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseSku>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseSkuDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging doctor warehouse sku by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.sku.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseSku>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorWarehouseSkuDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor warehouse sku, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.sku.list.fail");
        }
    }

}
