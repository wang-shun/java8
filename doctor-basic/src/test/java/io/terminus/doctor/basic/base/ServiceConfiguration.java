package io.terminus.doctor.basic.base;

import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.boot.rpc.dubbo.config.DubboBaseAutoConfiguration;
import io.terminus.boot.search.autoconfigure.ESSearchAutoConfiguration;
import io.terminus.doctor.basic.search.material.BaseMaterialQueryBuilder;
import io.terminus.doctor.basic.search.material.DefaultIndexedMaterialFactory;
import io.terminus.doctor.basic.search.material.DefaultMaterialQueryBuilder;
import io.terminus.doctor.basic.search.material.IndexedMaterial;
import io.terminus.doctor.basic.search.material.IndexedMaterialFactory;
import io.terminus.doctor.basic.search.material.MaterialSearchProperties;
import io.terminus.search.core.ESClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Desc: basic基础测试类配置类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@Configuration
@EnableAutoConfiguration(exclude = {DubboBaseAutoConfiguration.class, ESSearchAutoConfiguration.class})
@ComponentScan({"io.terminus.doctor.basic.*"})
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class ServiceConfiguration {


    @Configuration
    @ConditionalOnClass(ESClient.class)
    @ComponentScan({"io.terminus.search.api"})
    public static class SearchConfiguration {
        @Bean
        public ESClient esClient(@Value("${search.host:localhost}") String host,
                                 @Value("${search.port:9200}") Integer port) {
            return new ESClient(host, port);
        }

        @Configuration
        @EnableConfigurationProperties(MaterialSearchProperties.class)
        protected static class MaterialSearchConfiguration {

            @Bean
            @ConditionalOnMissingBean(IndexedMaterialFactory.class)
            public IndexedMaterialFactory<? extends IndexedMaterial> indexedMaterialFactory() {
                return new DefaultIndexedMaterialFactory();
            }

            @Bean
            @ConditionalOnMissingBean(BaseMaterialQueryBuilder.class)
            public BaseMaterialQueryBuilder baseMaterialQueryBuilder() {
                return new DefaultMaterialQueryBuilder();
            }
        }
    }
}
