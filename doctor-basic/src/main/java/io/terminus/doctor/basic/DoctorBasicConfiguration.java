package io.terminus.doctor.basic;

import io.terminus.doctor.basic.search.material.BaseMaterialQueryBuilder;
import io.terminus.doctor.basic.search.material.DefaultIndexedMaterialFactory;
import io.terminus.doctor.basic.search.material.DefaultMaterialQueryBuilder;
import io.terminus.doctor.basic.search.material.IndexedMaterial;
import io.terminus.doctor.basic.search.material.IndexedMaterialFactory;
import io.terminus.doctor.basic.search.material.MaterialSearchProperties;
import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.search.core.ESClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by yaoqijun.
 * Date:2016-04-22
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Configuration
@ComponentScan(basePackages = {
        "io.terminus.doctor.basic"
})
@Import({DoctorCommonConfiguration.class})
public class DoctorBasicConfiguration {


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
