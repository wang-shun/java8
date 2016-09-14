package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.manager.DoctorGroupReportManager;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪群批次总结表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-09-13
 */
@Slf4j
@Service
@RpcProvider
public class DoctorGroupBatchSummaryWriteServiceImpl implements DoctorGroupBatchSummaryWriteService {

    private final DoctorGroupReportManager doctorGroupReportManager;

    @Autowired
    public DoctorGroupBatchSummaryWriteServiceImpl(DoctorGroupReportManager doctorGroupReportManager) {
        this.doctorGroupReportManager = doctorGroupReportManager;
    }

    @Override
    public Response<Long> createGroupBatchSummary(DoctorGroupBatchSummary groupBatchSummary) {
        try {
            doctorGroupReportManager.createGroupBatchSummary(groupBatchSummary);
            return Response.ok(groupBatchSummary.getId());
        } catch (Exception e) {
            log.error("create groupBatchSummary failed, groupBatchSummary:{}, cause:{}", groupBatchSummary, Throwables.getStackTraceAsString(e));
            return Response.fail("groupBatchSummary.create.fail");
        }
    }
}
