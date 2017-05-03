package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupMaterialDao;
import io.terminus.doctor.event.model.DoctorMasterialDatailsGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.objenesis.instantiator.sun.MagicInstantiator;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by terminus on 2017/4/18.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorGroupMaterialReadServerImpl implements DoctorGroupMaterialReadServer{

    @RpcConsumer
    private DoctorGroupMaterialDao doctorGroupMaterialDao;

    @Override
    public Response<Paging<DoctorMasterialDatailsGroup>> findMasterialDatailsGroup(Map<String, Object> map, Integer pageNo, Integer size) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, size);
            return Response.ok(doctorGroupMaterialDao.findMasterialDatails(map, pageInfo.getOffset(), pageInfo.getLimit()));
        }catch (Exception e) {
            log.error("find doctor group material fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find doctor group material fail");
        }
    }
}
