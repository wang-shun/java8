package io.terminus.doctor.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import io.terminus.common.utils.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 13:39 2017/3/30
 */

public class ToJsonMapper {
    private static Logger logger = LoggerFactory.getLogger(ToJsonMapper.class);
    public static final ToJsonMapper JSON_NON_EMPTY_MAPPER;
    public static final ToJsonMapper JSON_NON_DEFAULT_MAPPER;
    private ObjectMapper mapper = new ObjectMapper();

    static {
        JSON_NON_EMPTY_MAPPER = new ToJsonMapper(JsonInclude.Include.NON_EMPTY);
        JSON_NON_DEFAULT_MAPPER = new ToJsonMapper(JsonInclude.Include.NON_DEFAULT);
    }

    private ToJsonMapper(JsonInclude.Include include) {
        this.mapper.setSerializationInclusion(include);
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.mapper.registerModule(new GuavaModule());
    }

    public String toJson(Object object) {
        try {
            return this.mapper.writeValueAsString(object);
        } catch (IOException var3) {
            logger.warn("write to json string error:" + object, var3);
            return null;
        }
    }
}
