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
 * Desc: 猪群批次总结表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-09-13
 */
@Slf4j
@Service
@RpcProvider
public class DoctorGroupBatchSummaryWriteServiceImpl implements DoctorGroupBatchSummaryWriteService {

    private final DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao;

    @Autowired
    public DoctorGroupBatchSummaryWriteServiceImpl(DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao) {
        this.doctorGroupBatchSummaryDao = doctorGroupBatchSummaryDao;
    }

    @Override
    public Response<Long> createGroupBatchSummary(DoctorGroupBatchSummary groupBatchSummary) {
        try {
            doctorGroupBatchSummaryDao.create(groupBatchSummary);
            return Response.ok(groupBatchSummary.getId());
        } catch (Exception e) {
            log.error("create groupBatchSummary failed, groupBatchSummary:{}, cause:{}", groupBatchSummary, Throwables.getStackTraceAsString(e));
            return Response.fail("groupBatchSummary.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateGroupBatchSummary(DoctorGroupBatchSummary groupBatchSummary) {
        try {
            return Response.ok(doctorGroupBatchSummaryDao.update(groupBatchSummary));
        } catch (Exception e) {
            log.error("update groupBatchSummary failed, groupBatchSummary:{}, cause:{}", groupBatchSummary, Throwables.getStackTraceAsString(e));
            return Response.fail("groupBatchSummary.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteGroupBatchSummaryById(Long groupBatchSummaryId) {
        try {
            return Response.ok(doctorGroupBatchSummaryDao.delete(groupBatchSummaryId));
        } catch (Exception e) {
            log.error("delete groupBatchSummary failed, groupBatchSummaryId:{}, cause:{}", groupBatchSummaryId, Throwables.getStackTraceAsString(e));
            return Response.fail("groupBatchSummary.delete.fail");
        }
    }
}
