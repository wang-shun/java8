/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.common.constants;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-14
 */
public final class JacksonType {

    public static final TypeReference<List<String>> LIST_OF_STRING = new TypeReference<List<String>>() {};

    public static final TypeReference<Map<String, String>> MAP_OF_STRING = new TypeReference<Map<String,String>>(){};

    public static final TypeReference<Map<String, Integer>> MAP_OF_INTEGER = new TypeReference<Map<String, Integer>>(){};
}
