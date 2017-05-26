package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.PigScoreApplyDao;
import io.terminus.doctor.user.model.PigScoreApply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc: 猪场评分功能申请
 * Mail: hehaiyang@terminus.io
 * Date: 2017/05/02
 */
@Slf4j
@Service
@RpcProvider
public class PigScoreApplyReadServiceImpl implements PigScoreApplyReadService {

    @Autowired
    private PigScoreApplyDao pigScoreApplyDao;

    @Override
    public Response<PigScoreApply> findById(Long id) {
        try{
            return Response.ok(pigScoreApplyDao.findById(id));
        }catch (Exception e){
            log.error("failed to find pig score apply by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("apply.find.fail");
        }
    }

    @Override
    public Response<Paging<PigScoreApply>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(pigScoreApplyDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging pig score apply by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("apply.paging.fail");
        }
    }

    @Override
    public Response<List<PigScoreApply>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(pigScoreApplyDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list pig score apply , cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("apply.list.fail");
        }
    }

    @Override
    public Response<PigScoreApply> findByOrgAndFarmId(Long orgId, Long farmId) {
        try{
            return Response.ok(pigScoreApplyDao.findByOrgAndFarmId(orgId, farmId));
        }catch (Exception e){
            log.error("failed to find pig score apply by orgId:{} farmId:{}, cause:{}", orgId, farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.apply.fail");
        }
    }

}
