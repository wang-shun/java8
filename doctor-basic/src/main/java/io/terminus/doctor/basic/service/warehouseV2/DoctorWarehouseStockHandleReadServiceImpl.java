package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dao.DoctorWarehouseStockHandleDao;

import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-31 14:49:19
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseStockHandleReadServiceImpl implements DoctorWarehouseStockHandleReadService {

    @Autowired
    private DoctorWarehouseStockHandleDao doctorWarehouseStockHandleDao;

    @Override
    public Response<DoctorWarehouseStockHandle> findById(Long id) {
        try{
            return Response.ok(doctorWarehouseStockHandleDao.findById(id));
        }catch (Exception e){
            log.error("failed to find doctor warehouse stock handle by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.handle.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseStockHandle>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseStockHandleDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging doctor warehouse stock handle by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.handle.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseStockHandle>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorWarehouseStockHandleDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor warehouse stock handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.handle.list.fail");
        }
    }

}
