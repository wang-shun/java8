/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import io.terminus.doctor.web.core.auth.DefaultRoleAuthorizor;
import io.terminus.doctor.web.core.auth.DefaultTypeAuthorizor;
import io.terminus.doctor.web.core.auth.RoleAuthorizor;
import io.terminus.doctor.web.core.auth.TypeAuthorizor;
import io.terminus.lib.file.FileServer;
import io.terminus.lib.file.ImageServer;
import io.terminus.lib.file.aliyun.AliyunFileServer;
import io.terminus.lib.file.aliyun.AliyunImageServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.Filter;
import java.util.concurrent.Executors;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-02-09
 */
@Configuration
@EnableWebMvc
@EnableAutoConfiguration
@ComponentScan
public class DoctorCoreWebConfiguration extends WebMvcConfigurerAdapter {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setCacheSeconds(3600);
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public Filter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Bean(name = "imageServer")
    public ImageServer aliyunOSSImageServer(@Value("${oss.endpoint}")String endpoint,
                                            @Value("${oss.appKey}")String appKey,
                                            @Value("${oss.appSecret}")String appSecret,
                                            @Value("${oss.bucketName}")String bucketName){
        return new AliyunImageServer(endpoint,appKey, appSecret, bucketName);
    }

    @Bean(name = "fileServer")
    public FileServer aliyunOSSFileServer(@Value("${oss.endpoint}")String endpoint,
                                           @Value("${oss.appKey}")String appKey,
                                           @Value("${oss.appSecret}")String appSecret,
                                           @Value("${oss.bucketName}")String bucketName){
        return new AliyunFileServer(endpoint,appKey, appSecret, bucketName);
    }

    @Bean
    public EventBus eventBus(){
        return new AsyncEventBus(
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    @Bean
    public ObjectMapper nonNullObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    @ConditionalOnMissingBean(TypeAuthorizor.class)
    protected static class TypeAuthorizorConfiguration{
        @Bean
        public TypeAuthorizor typeAuthorizor(){
            return new DefaultTypeAuthorizor();
        }

    }

    @Configuration
    @ConditionalOnMissingBean(RoleAuthorizor.class)
    protected static class RoleAuthorizorConfiguration{
        @Bean
        public RoleAuthorizor roleAuthorizor(){
            return new DefaultRoleAuthorizor();
        }
    }
}
