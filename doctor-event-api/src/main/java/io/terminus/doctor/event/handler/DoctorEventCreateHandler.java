package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;

import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:  事件模块Handler 数据操作方式
 */
public interface DoctorEventCreateHandler {

    /**
     * 校验Handler 的处理方式
     * @param basic 事件基础信息内容
     * @param extra 不同事件扩展信息
     * @param context 冗余环境变量信息
     */
    Boolean preHandler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String,Object> context) throws RuntimeException;

    /**
     * 事件信息处理handler
     * @param basic
     * @param extra
     * @param context
     */
    void handler(DoctorBasicInputInfoDto basic, Map<String,Object> extra, Map<String,Object> context) throws RuntimeException;

    /**
     * after handler
     * @param basic
     * @param extra
     * @param context
     */
    void afterHandler(DoctorBasicInputInfoDto basic, Map<String,Object> extra, Map<String,Object> context) throws RuntimeException;
}
