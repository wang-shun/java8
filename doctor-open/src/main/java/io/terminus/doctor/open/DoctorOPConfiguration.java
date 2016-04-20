/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.open;

import io.terminus.doctor.open.common.MessageSources;
import io.terminus.doctor.web.core.image.FileHelper;
import io.terminus.lib.file.FileServer;
import io.terminus.lib.file.ImageServer;
import io.terminus.lib.file.aliyun.AliyunFileServer;
import io.terminus.lib.file.aliyun.AliyunImageServer;
import io.terminus.pampas.openplatform.annotations.EnableOpenPlatform;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-02-01
 */
@Configuration
@EnableWebMvc
@EnableOpenPlatform
@ComponentScan({"io.terminus.doctor.open.common"})
@EnableAutoConfiguration
public class DoctorOPConfiguration {
    @Bean
    public MessageSources messageSources() {
        return new MessageSources();
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
}
