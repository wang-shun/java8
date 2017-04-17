package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorDataFactorDao;
import io.terminus.doctor.event.manager.FactorManager;
import io.terminus.doctor.event.model.DoctorDataFactor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc: 信用模型计算因子写服务实现
 * Mail: hehaiyang@terminus.io
 * Date: 2017/3/17
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDataFactorWriteServiceImpl implements DoctorDataFactorWriteService {

    private final DoctorDataFactorDao doctorDataFactorDao;
    private final FactorManager factorManager;

    @Autowired
    public DoctorDataFactorWriteServiceImpl(DoctorDataFactorDao doctorDataFactorDao,
                                            FactorManager factorManager) {
        this.doctorDataFactorDao = doctorDataFactorDao;
        this.factorManager = factorManager;
    }

    @Override
    public Response<Long> create(DoctorDataFactor doctorDataFactor) {
        try{
            doctorDataFactorDao.create(doctorDataFactor);
            return Response.ok(doctorDataFactor.getId());
        }catch (Exception e){
            log.error("failed to create doctor data factor, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.data.factor.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorDataFactor doctorDataFactor) {
        try{
            return Response.ok(doctorDataFactorDao.update(doctorDataFactor));
        }catch (Exception e){
            log.error("failed to update doctor data factor, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.data.factor.update.fail");
        }
    }

    @Override
    public Response<Boolean> batchUpdate(List<DoctorDataFactor> factors) {
        try{
            return Response.ok(factorManager.updateFactors(factors));
        }catch (JsonResponseException je){
            return Response.fail(je.getMessage());
        }catch (Exception e){
            log.error("failed to batch update doctor data factor, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.data.factor.update.fail");
        }
    }

    @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(doctorDataFactorDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete doctor data factor by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("delete.doctor.data.factor.fail");
        }
    }

}