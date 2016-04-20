/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.user.dao;

import io.terminus.boot.dubbo.autoconfigure.DubboAutoConfiguration;
import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 用户模块DAO层测试
 *
 *  updated by panxin@terminus.io
 */
@Configuration
@EnableAutoConfiguration(exclude = DubboAutoConfiguration.class)
@ComponentScan({"io.terminus.doctor.user.dao"})
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class DaoConfiguration {
}
