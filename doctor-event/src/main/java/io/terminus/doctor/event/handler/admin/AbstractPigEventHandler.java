package io.terminus.doctor.event.handler.admin;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;

import static io.terminus.common.utils.JsonMapper.JSON_NON_DEFAULT_MAPPER;

/**
 * Created by sunbo@terminus.io on 2017/10/9.
 */
public class AbstractPigEventHandler<T extends BasePigEventInputDto> {

    protected static JsonMapper jsonMapper = JSON_NON_DEFAULT_MAPPER;
}
