package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupBatchSummaryDao;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪群批次总结表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-09-13
 */
@Slf4j
@Service
@RpcProvider
public class DoctorGroupBatchSummaryReadServiceImpl implements DoctorGroupBatchSummaryReadService {

    private final DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao;

    @Autowired
    public DoctorGroupBatchSummaryReadServiceImpl(DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao) {
        this.doctorGroupBatchSummaryDao = doctorGroupBatchSummaryDao;
    }

    @Override
    public Response<DoctorGroupBatchSummary> findGroupBatchSummaryById(Long groupBatchSummaryId) {
        try {
            return Response.ok(doctorGroupBatchSummaryDao.findById(groupBatchSummaryId));
        } catch (Exception e) {
            log.error("find groupBatchSummary by id failed, groupBatchSummaryId:{}, cause:{}", groupBatchSummaryId, Throwables.getStackTraceAsString(e));
            return Response.fail("groupBatchSummary.find.fail");
        }
    }

}
