package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.PigScoreApplyDao;
import io.terminus.doctor.user.model.PigScoreApply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪场评分功能申请
 * Mail: hehaiyang@terminus.io
 * Date: 2017/05/02
 */
@Slf4j
@Service
@RpcProvider
public class PigScoreApplyWriteServiceImpl implements PigScoreApplyWriteService {

    @Autowired
    private PigScoreApplyDao pigScoreApplyDao;

    @Override
    public Response<Long> create(PigScoreApply pigScoreApply) {
        try{
            pigScoreApplyDao.create(pigScoreApply);
            return Response.ok(pigScoreApply.getId());
        }catch (Exception e){
            log.error("failed to create pigScoreApply, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("pigScoreApply.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(PigScoreApply pigScoreApply) {
        try{
            return Response.ok(pigScoreApplyDao.update(pigScoreApply));
        }catch (Exception e){
            log.error("failed to update pigScoreApply, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("pigScoreApply.update.fail");
        }
    }

   @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(pigScoreApplyDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete pigScoreApply by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("delete.pigScoreApply.fail");
        }
    }

}