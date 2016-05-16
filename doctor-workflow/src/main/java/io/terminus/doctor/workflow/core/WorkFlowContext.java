package io.terminus.doctor.workflow.core;

import com.google.common.collect.Lists;
import io.terminus.doctor.workflow.event.IHandler;
import io.terminus.doctor.workflow.event.Interceptor;
import io.terminus.doctor.workflow.utils.AssertHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Desc: 全局上下文实现类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/27
 */
@Slf4j
@Component
public class WorkFlowContext implements Context{

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 全局上下文对象
     */
    private ConcurrentHashMap context;

    @PostConstruct
    public void initContext() {
        context = new ConcurrentHashMap();
        if(applicationContext == null) {
            log.error("[workflow context] -> get spring applicationContext failed");
            AssertHelper.throwException("get spring applicationContext failed");
        }
        // 1. 初始化拦截器
        initInterceptors(applicationContext.getBeansOfType(Interceptor.class));

        // 2. 初始化事件
        initEventHandlers(applicationContext.getBeansOfType(IHandler.class));

    }

    @Override
    public <T> void put(String name, T t) {
        context.put(name, t);
    }

    @Override
    public <T> T get(String name) {
        return (T) context.get(name);
    }

    @Override
    public <T> T get(Class<T> clazz) {
        List<T> list = getList(clazz);
        if(list == null) {
            return null;
        }
        if(list.size() > 1) {
            log.error("[workflow context] -> get single class failed");
            AssertHelper.throwException("work flow context get single class error, the class is: {}", clazz);
        }
        return list.get(0);
    }

    @Override
    public <T> List<T> getList(Class<T> clazz) {
        List list = Lists.newArrayList();
        // 上下文中获取
        context.forEach((beanName, bean)-> {
            if (clazz != null && clazz.isAssignableFrom(bean.getClass())) {
                list.add(bean);
            }
        });
        return list;
    }

    /**
     * 初始化系统全局拦截
     * @param interceptorsMap
     */
    private void initInterceptors(Map<String, Interceptor> interceptorsMap) {
        if(interceptorsMap != null && interceptorsMap.size() > 0) {
            interceptorsMap.forEach((beanName, i) -> {
                context.put(beanName, i);
            });
        }
    }

    /**
     * 初始化所有的事件处理
     * @param IHandlersMap
     */
    private void initEventHandlers(Map<String, IHandler> IHandlersMap) {
        if(IHandlersMap != null && IHandlersMap.size() > 0) {
            IHandlersMap.forEach((beanName, i) -> {
                context.put(beanName, i);
            });
        }
    }

    @Override
    public String toString() {
        return context.toString();
    }
}
