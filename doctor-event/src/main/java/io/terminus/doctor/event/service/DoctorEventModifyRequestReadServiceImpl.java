package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.dao.DoctorEventModifyRequestDao;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

/**
 * Created by xjn on 17/3/11.
 * 编辑事件请求读事件
 */
@Slf4j
@Service
@RpcProvider
public class DoctorEventModifyRequestReadServiceImpl implements DoctorEventModifyRequestReadService {
    @Autowired
    private DoctorEventModifyRequestDao modifyRequestDao;
    @Override
    public Response<DoctorEventModifyRequest> findById(@NotNull(message = "requestId.not.null") Long requestId) {
        try {
            return Response.ok(modifyRequestDao.findById(requestId));
        } catch (Exception e) {
            log.info("find by id failed, requestId:{}, cause:{}", requestId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.by.id.failed");
        }
    }

    @Override
    public Response<Paging<DoctorEventModifyRequest>> pagingRequest(DoctorEventModifyRequest modifyRequest, Integer pageNo, Integer pageSize) {
        try {
            PageInfo pageInfo = PageInfo.of(pageNo, pageSize);
            return Response.ok(modifyRequestDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), BeanMapper.convertObjectToMap(modifyRequest)));
        } catch (Exception e) {
            log.info("paging request failed, modifyRequest:{}, cause:{}", modifyRequest, Throwables.getStackTraceAsString(e));
            return Response.fail("paging.request.failed");
        }
    }
}
