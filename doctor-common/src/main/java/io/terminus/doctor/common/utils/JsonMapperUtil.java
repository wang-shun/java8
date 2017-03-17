package io.terminus.doctor.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;


/**
 * Created by xjn on 17/1/23.
 * json 转换工具
 */
public class JsonMapperUtil {
    private static Logger logger = LoggerFactory.getLogger(JsonMapperUtil.class);
    public static final JsonMapperUtil JSON_NON_EMPTY_MAPPER = new JsonMapperUtil(JsonInclude.Include.NON_EMPTY);
    public static final JsonMapperUtil JSON_NON_DEFAULT_MAPPER = new JsonMapperUtil(JsonInclude.Include.NON_DEFAULT);
    private ObjectMapper mapper;

    private JsonMapperUtil(JsonInclude.Include include) {
        mapper = new ObjectMapper();
        //设置输出时包含属性的风格
        mapper.setSerializationInclusion(include);
        //设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        mapper.registerModule(new GuavaModule());
    }


    /**
     * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
     */
    public static JsonMapperUtil nonEmptyMapper() {
        return nonEmptyMapperWithFormat(null);
    }

    /**
     * 获取带有格式的jsonMapper
     * @param format 时间格式
     * @return 新建的mapper
     */
    public static JsonMapperUtil nonEmptyMapperWithFormat(DateFormat format) {
        JsonMapperUtil jsonMapperUtil =  new JsonMapperUtil(JsonInclude.Include.NON_EMPTY);
        return format != null? jsonMapperUtil.setDateFormat(format) : jsonMapperUtil;
    }

    /**
     * 创建只输出初始值被改变的属性到Json字符串的Mapper, 最节约的存储方式，建议在内部接口中使用。
     */
    public static JsonMapperUtil nonDefaultMapper() {
        return nonDefaultMapperWithFormat(null);
    }

    /**
     * 获取带有时间格式defaultMapper
     * @param format 时间格式
     * @return 新建的mapper
     */
    public static JsonMapperUtil nonDefaultMapperWithFormat(DateFormat format) {
        JsonMapperUtil jsonMapperUtil =  new JsonMapperUtil(JsonInclude.Include.NON_DEFAULT);
        return format != null? jsonMapperUtil.setDateFormat(format) : jsonMapperUtil;
    }

    /**
     * Object可以是POJO，也可以是Collection或数组。
     * 如果对象为Null, 返回"null".
     * 如果集合为空集合, 返回"[]".
     */
    public String toJson(Object object) {

        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            logger.warn("write to json string error:" + object, e);
            return null;
        }
    }

    /**
     * 反序列化POJO或简单Collection如List<String>.
     * <p/>
     * 如果JSON字符串为Null或"null"字符串, 返回Null.
     * 如果JSON字符串为"[]", 返回空集合.
     * <p/>
     * 如需反序列化复杂Collection如List<MyBean>, 请使用fromJson(String,JavaType)
     *
     * @see #fromJson(String, com.fasterxml.jackson.databind.JavaType)
     */
    public <T> T fromJson(String jsonString, Class<T> clazz) {
        if (Strings.isNullOrEmpty(jsonString)) {
            return null;
        }

        try {
            return mapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            logger.warn("parse json string error:" + jsonString, e);
            return null;
        }
    }

    /**
     * 反序列化复杂Collection如List<Bean>, 先使用函數createCollectionType构造类型,然后调用本函数.
     *
     * @see #createCollectionType(Class, Class...)
     */
    @SuppressWarnings("unchecked")
    public <T> T fromJson(String jsonString, JavaType javaType) {
        if (Strings.isNullOrEmpty(jsonString)) {
            return null;
        }

        try {
            return (T) mapper.readValue(jsonString, javaType);
        } catch (Exception e) {
            logger.warn("parse json string error:" + jsonString, e);
            return null;
        }
    }


    public JsonNode treeFromJson(String jsonString) throws IOException {
        return mapper.readTree(jsonString);
    }

    public <T> T treeToValue(JsonNode node, Class<T> clazz) throws JsonProcessingException {
        return mapper.treeToValue(node, clazz);
    }

    /**
     * 構造泛型的Collection Type如:
     * ArrayList<MyBean>, 则调用constructCollectionType(ArrayList.class,MyBean.class)
     * HashMap<String,MyBean>, 则调用(HashMap.class,String.class, MyBean.class)
     */
    public JavaType createCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    /**
     * 當JSON裡只含有Bean的部分屬性時，更新一個已存在Bean，只覆蓋該部分的屬性.
     */
    @SuppressWarnings("unchecked")
    public <T> T update(String jsonString, T object) {
        try {
            return (T) mapper.readerForUpdating(object).readValue(jsonString);
        } catch (JsonProcessingException e) {
            logger.warn("update json string:" + jsonString + " to object:" + object + " error.", e);
        } catch (IOException e) {
            logger.warn("update json string:" + jsonString + " to object:" + object + " error.", e);
        }
        return null;
    }

    /**
     * 輸出JSONP格式數據.
     */
    public String toJsonP(String functionName, Object object) {
        return toJson(new JSONPObject(functionName, object));
    }

    /**
     * 設定是否使用Enum的toString函數來讀寫Enum,
     * 為False時時使用Enum的name()函數來讀寫Enum, 默認為False.
     * 注意本函數一定要在Mapper創建後, 所有的讀寫動作之前調用.
     */
    public void enableEnumUseToString() {
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    }

    private JsonMapperUtil setDateFormat(DateFormat format) {
        mapper.setDateFormat(format);
        return this;
    }

    /**
     * 取出Mapper做进一步的设置或使用其他序列化API.
     */
    public ObjectMapper getMapper() {
        return mapper;
    }
}
