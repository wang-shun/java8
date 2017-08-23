package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialApplyDao;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialApply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 14:05:59
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseMaterialApplyReadServiceImpl implements DoctorWarehouseMaterialApplyReadService {

    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;

    @Override
    public Response<DoctorWarehouseMaterialApply> findById(Long id) {
        try{
            return Response.ok(doctorWarehouseMaterialApplyDao.findById(id));
        }catch (Exception e){
            log.error("failed to find doctor warehouse material apply by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseMaterialApply>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseMaterialApplyDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging doctor warehouse material apply by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseMaterialApply>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorWarehouseMaterialApplyDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor warehouse material apply, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.list.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseMaterialApply>> list(DoctorWarehouseMaterialApply criteria) {
        try{
            return Response.ok(doctorWarehouseMaterialApplyDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor warehouse material apply, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.list.fail");
        }
    }
}
