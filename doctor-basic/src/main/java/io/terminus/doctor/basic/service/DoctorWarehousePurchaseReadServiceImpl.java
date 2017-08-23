package io.terminus.doctor.basic.service;

import io.terminus.doctor.basic.dao.DoctorWarehousePurchaseDao;

import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehousePurchase;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 00:18:50
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehousePurchaseReadServiceImpl implements DoctorWarehousePurchaseReadService {

    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;

    @Override
    public Response<DoctorWarehousePurchase> findById(Long id) {
        try{
            return Response.ok(doctorWarehousePurchaseDao.findById(id));
        }catch (Exception e){
            log.error("failed to find doctor warehouse purchase by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.purchase.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehousePurchase>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehousePurchaseDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging doctor warehouse purchase by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.purchase.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehousePurchase>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorWarehousePurchaseDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor warehouse purchase, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.purchase.list.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehousePurchase>> list(DoctorWarehousePurchase criteria) {
        try{
            return Response.ok(doctorWarehousePurchaseDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor warehouse purchase, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.purchase.list.fail");
        }
    }
}
