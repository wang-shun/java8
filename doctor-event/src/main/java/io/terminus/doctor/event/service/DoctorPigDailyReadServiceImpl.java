package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.model.DoctorPigDaily;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-12 17:33:52
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorPigDailyReadServiceImpl implements DoctorPigDailyReadService {

    @Autowired
    private DoctorPigDailyDao doctorPigDailyDao;

    @Override
    public Response<DoctorPigDaily> findById(Long id) {
        try{
            return Response.ok(doctorPigDailyDao.findById(id));
        }catch (Exception e){
            log.error("failed to find doctor pig daily by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.pig.daily.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorPigDaily>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorPigDailyDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging doctor pig daily by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.pig.daily.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorPigDaily>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorPigDailyDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor pig daily, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.pig.daily.list.fail");
        }
    }

}
