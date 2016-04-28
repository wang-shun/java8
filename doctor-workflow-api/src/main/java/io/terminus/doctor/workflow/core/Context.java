package io.terminus.doctor.workflow.core;

import java.util.List;

/**
 * Desc: 全局上下文接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/27
 */
public interface Context {

    /**
     * 上下文存放值
     * @param name  key值
     * @param t     value值
     */
    public <T> void put(String name, T t);

    /**
     * 根据name属性获取key值
     * @param name  key值
     * @return
     */
    public <T> T get(String name);

    /**
     * 根据值的类型获取对象, 如果存在多个则抛出异常
     * @param clazz     值的字节类型
     * @return
     */
    public <T> T get(Class<T> clazz);

    /**
     * 根据值的类型获取对象列表
     * @param clazz     值的字节码类型
     * @return
     */
    public <T> List<T> getList(Class<T> clazz);
}
