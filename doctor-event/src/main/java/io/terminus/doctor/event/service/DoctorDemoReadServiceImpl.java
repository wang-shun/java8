package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorDemoDao;
import io.terminus.doctor.event.model.DoctorDemo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-03-29 10:49:19
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDemoReadServiceImpl implements DoctorDemoReadService {

    @Autowired
    private DoctorDemoDao doctorDemoDao;

    @Override
    public Response<DoctorDemo> findById(Long id) {
        try{
            return Response.ok(doctorDemoDao.findById(id));
        }catch (Exception e){
            log.error("failed to find doctor demo by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.demo.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorDemo>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorDemoDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging doctor demo by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.demo.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorDemo>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorDemoDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor demo, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.demo.list.fail");
        }
    }

    @Override
    public Response<DoctorDemo> findByName(String name) {
        try {
            return Response.ok(doctorDemoDao.findByName(name));
        } catch (Exception e) {
            log.error("find by name failed, name:{}, cause:{}", name, Throwables.getStackTraceAsString(e));
            return Response.fail("find.by.name.failed");
        }
    }

    @Override
    public Response<Boolean> createDemo(DoctorDemo doctorDemo) {
        try {
            return Response.ok(doctorDemoDao.create(doctorDemo));
        } catch (Exception e) {
            log.error(",cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.demo.failed");
        }
    }
}
