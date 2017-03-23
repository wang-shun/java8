/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.open;

import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.doctor.open.auth.DoctorCustomRoleLoaderConfigurer;
import io.terminus.doctor.user.service.DoctorServiceStatusWriteService;
import io.terminus.doctor.user.service.SubRoleReadService;
import io.terminus.doctor.web.core.DoctorCoreWebConfiguration;
import io.terminus.doctor.web.core.image.FileHelper;
import io.terminus.doctor.web.core.msg.email.CommonEmailServiceConfig;
import io.terminus.doctor.web.core.msg.sms.LuoSiMaoSmsServiceConfig;
import io.terminus.doctor.web.core.service.ServiceBetaStatusHandler;
import io.terminus.doctor.web.core.service.impl.ServiceBetaStatusHandlerImpl;
import io.terminus.lib.file.FileServer;
import io.terminus.lib.file.ImageServer;
import io.terminus.lib.file.aliyun.AliyunFileServer;
import io.terminus.lib.file.aliyun.AliyunImageServer;
import io.terminus.pampas.openplatform.annotations.EnableOpenPlatform;
import io.terminus.parana.auth.core.AuthenticationConfiguration;
import io.terminus.parana.auth.role.CustomRoleLoaderConfigurer;
import io.terminus.parana.auth.role.CustomRoleLoaderRegistry;
import io.terminus.parana.config.ConfigCenter;
import io.terminus.parana.web.msg.config.MsgWebConfig;
import io.terminus.zookeeper.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-02-01
 */
@Configuration
@EnableWebMvc
@EnableOpenPlatform
@ComponentScan(value = {"io.terminus.doctor.open"})
@EnableAutoConfiguration
@Import({
        DoctorCoreWebConfiguration.class,
        MsgWebConfig.class,
        LuoSiMaoSmsServiceConfig.class,
        CommonEmailServiceConfig.class,
        AuthenticationConfiguration.class,
        DoctorCommonConfiguration.class
})
public class DoctorOPConfiguration {
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
    public FileHelper fileHelper() {
        return new FileHelper();
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
    public ServiceBetaStatusHandler serviceBetaStatusHandler(ConfigCenter configCenter,
                                                             DoctorServiceStatusWriteService doctorServiceStatusWriteService){
        return new ServiceBetaStatusHandlerImpl(configCenter, doctorServiceStatusWriteService);
    }

    @Bean
    public Publisher publish2Pigmall(@Value("${zookeeper-pigmall.cacheTopic:pigmall.cache.user.clear}") String cacheTopicPigmall,
                                     @Value("${zookeeper-pigmall.host:127.0.0.1}")String host,
                                     @Value("${zookeeper-pigmall.port:2181}")String port) throws Exception {
        return new Publisher(new ZKClientFactory(host + ":" + port), cacheTopicPigmall);
    }

    @Bean
    public CustomRoleLoaderConfigurer customRoleLoaderConfigurer(CustomRoleLoaderRegistry customRoleLoaderRegistry, SubRoleReadService subRoleReadService) {
        CustomRoleLoaderConfigurer configurer = new DoctorCustomRoleLoaderConfigurer(subRoleReadService);
        configurer.configureCustomRoleLoader(customRoleLoaderRegistry);
        return configurer;
    }
}
