package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorDataFactorDao;
import io.terminus.doctor.event.model.DoctorDataFactor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc: 信用模型计算因子写服务实现
 * Mail: hehaiyang@terminus.io
 * Date: 2017/3/17
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDataFactorReadServiceImpl implements DoctorDataFactorReadService {

    private final DoctorDataFactorDao doctorDataFactorDao;

    @Autowired
    public DoctorDataFactorReadServiceImpl(DoctorDataFactorDao doctorDataFactorDao) {
        this.doctorDataFactorDao = doctorDataFactorDao;
    }

    @Override
    public Response<DoctorDataFactor> findById(Long id) {
        try{
            return Response.ok(doctorDataFactorDao.findById(id));
        }catch (Exception e){
            log.error("failed to find doctor data factor by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.data.factor.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorDataFactor>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorDataFactorDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging doctor data factor by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.data.factor.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorDataFactor>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorDataFactorDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor data factor , cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.data.factor .list.fail");
        }
    }

}
