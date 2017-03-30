package io.terminus.doctor.move.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 17:19 2017/3/30
 */

public class JsonFormatUtils {

    private static Logger logger = LoggerFactory.getLogger(JsonFormatUtils.class);
    public static JsonFormatUtils JSON_NON_EMPTY_MAPPER = new JsonFormatUtils(JsonInclude.Include.NON_EMPTY);
    public static JsonFormatUtils JSON_NON_DEFAULT_MAPPER = new JsonFormatUtils(JsonInclude.Include.NON_DEFAULT);
    private final List<DateFormat> formatList;
    private final JsonInclude.Include include;


    private List<DateFormat> initFormatList() {
        List<DateFormat> formatList = Lists.newArrayList();
        formatList.add(new SimpleDateFormat("yyyy-MM-dd"));
        formatList.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        formatList.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        formatList.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        formatList.add(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz"));
        return formatList;
    }

    public JsonFormatUtils(JsonInclude.Include include){
        this.include = include;
        this.formatList = initFormatList();
    }


    public <T> T fromJson(String jsonString, Class<T> clazz) {
        if (Strings.isNullOrEmpty(jsonString)) {
            return null;
        }
        JsonMapperUtil JSON_MAPPER = JsonMapperUtil.JSON_NON_EMPTY_MAPPER;
        if(Objects.equals(include, JsonInclude.Include.NON_DEFAULT)){
            JSON_MAPPER = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER;
        }
        T result = null;
        for(DateFormat dateFormat : formatList) {
            try {
                JSON_MAPPER.getInnerMapper().setDateFormat(dateFormat);
                result = JSON_MAPPER.getInnerMapper().readValue(jsonString, clazz);
                break;
            } catch (InvalidFormatException e){
                logger.warn("parse json string error:" + jsonString, e);
                continue;
            } catch (IOException e) {
                logger.warn("parse json string error:" + jsonString, e);
                return null;
            }
        }
        return result;
    }


}
