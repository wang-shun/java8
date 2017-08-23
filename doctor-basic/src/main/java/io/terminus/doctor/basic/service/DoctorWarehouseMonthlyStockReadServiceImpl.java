package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehouseMonthlyStockDao;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMonthlyStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-17 15:02:59
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseMonthlyStockReadServiceImpl implements DoctorWarehouseMonthlyStockReadService {

    @Autowired
    private DoctorWarehouseMonthlyStockDao doctorWarehouseMonthlyStockDao;

    @Override
    public Response<DoctorWarehouseMonthlyStock> findById(Long id) {
        try{
            return Response.ok(doctorWarehouseMonthlyStockDao.findById(id));
        }catch (Exception e){
            log.error("failed to find doctor warehouse monthly stock by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.monthly.stock.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseMonthlyStock>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseMonthlyStockDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging doctor warehouse monthly stock by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.monthly.stock.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseMonthlyStock>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorWarehouseMonthlyStockDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor warehouse monthly stock, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.monthly.stock.list.fail");
        }
    }

}
