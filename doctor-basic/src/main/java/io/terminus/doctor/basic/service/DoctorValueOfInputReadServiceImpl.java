package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.ValueOfInputDao;
import io.terminus.doctor.basic.model.ValueOfInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc:
 * Mail: hehaiyang@terminus.io
 * Date: 2017/4/14
 */
@Slf4j
@Service
@RpcProvider
public class DoctorValueOfInputReadServiceImpl implements DoctorValueOfInputReadService {

    private final ValueOfInputDao valueOfInputDao;

    @Autowired
    public DoctorValueOfInputReadServiceImpl(ValueOfInputDao valueOfInputDao) {
        this.valueOfInputDao = valueOfInputDao;
    }

    @Override
    public Response<List<ValueOfInput>> rankingValueOfInput(Long farmId, Integer type) {
        try{
            return Response.ok(valueOfInputDao.rankingValueOfInput(farmId, type));
        }catch (Exception e){
            log.error("failed to ranking value of input by farmId:{} type:{}, cause:{}", farmId, type, Throwables.getStackTraceAsString(e));
            return Response.fail("ranking.value.of.input.fail");
        }
    }
}
