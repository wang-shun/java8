package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.ValueOfInput;

import java.util.List;

/**
 * Desc: 投入品价值
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/14
 */
public interface DoctorValueOfInputReadService {

    Response<List<ValueOfInput>> rankingValueOfInput(Long farmId, Integer type);

}
