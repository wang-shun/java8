/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.open;

import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.doctor.user.service.DoctorServiceStatusWriteService;
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
    public Publisher publish2Pigmall(ZKClientFactory zkClientFactory,
                                     @Value("${zookeeper.cacheTopic-pigmall:pigmall.cache.user.clear}") String cacheTopicPigmall) throws Exception {
        return new Publisher(zkClientFactory, cacheTopicPigmall);
    }
}
