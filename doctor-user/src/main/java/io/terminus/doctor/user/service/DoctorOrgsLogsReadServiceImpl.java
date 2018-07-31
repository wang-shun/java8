package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorOrgsLogsDao;
import io.terminus.doctor.user.model.DoctorOrgsLogs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-06-13 19:41:03
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorOrgsLogsReadServiceImpl implements DoctorOrgsLogsReadService {

    @Autowired
    private DoctorOrgsLogsDao doctorOrgsLogsDao;

    @Override
    public Response<DoctorOrgsLogs> findById(Long id) {
        try{
            return Response.ok(doctorOrgsLogsDao.findById(id));
        }catch (Exception e){
            log.error("failed to find doctor orgs logs by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.orgs.logs.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorOrgsLogs>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorOrgsLogsDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging doctor orgs logs by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.orgs.logs.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorOrgsLogs>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorOrgsLogsDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor orgs logs, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.orgs.logs.list.fail");
        }
    }

}
