package io.terminus.doctor.move;

import com.google.common.collect.Maps;
import io.terminus.doctor.basic.DoctorBasicConfiguration;
import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.doctor.event.DoctorEventConfiguration;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.move.builder.group.DoctorChangeEventInputBuilder;
import io.terminus.doctor.move.builder.group.DoctorCloseEventInputBuilder;
import io.terminus.doctor.move.builder.group.DoctorGroupEventInputBuilder;
import io.terminus.doctor.move.builder.group.DoctorGroupEventInputBuilders;
import io.terminus.doctor.move.builder.group.DoctorGroupWeanEventInputBuilder;
import io.terminus.doctor.move.builder.group.DoctorMoveInEventInputBuilder;
import io.terminus.doctor.move.builder.group.DoctorNewEventInputBuilder;
import io.terminus.doctor.move.builder.group.DoctorTransGroupEventInputBuilder;
import io.terminus.doctor.move.builder.group.DoctorTurnSeedEventInputBuilder;
import io.terminus.doctor.move.builder.pig.DoctorChgLocationInputBuilder;
import io.terminus.doctor.move.builder.pig.DoctorEntryInputBuilder;
import io.terminus.doctor.move.builder.pig.DoctorFarrowInputBuilder;
import io.terminus.doctor.move.builder.pig.DoctorMateInputBuilder;
import io.terminus.doctor.move.builder.pig.DoctorPigEventInputBuilder;
import io.terminus.doctor.move.builder.pig.DoctorPigEventInputBuilders;
import io.terminus.doctor.move.builder.pig.DoctorPigLetsChgInputBuilder;
import io.terminus.doctor.move.builder.pig.DoctorPigWeanInputBuilder;
import io.terminus.doctor.move.builder.pig.DoctorPregCheckInputBuilder;
import io.terminus.doctor.move.builder.pig.DoctorRemoveInputBuilder;
import io.terminus.doctor.move.sql.DoctorSqlFactory;
import io.terminus.parana.article.impl.ArticleAutoConfig;
import io.terminus.parana.file.FileAutoConfig;
import io.terminus.parana.user.ExtraUserAutoConfig;
import io.terminus.parana.user.UserAutoConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;
import java.util.Map;

/**
 * Desc: 单例模式启动 move-data 配置
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/27
 */
@Configuration
@ComponentScan(basePackages = {"io.terminus.doctor.move", "io.terminus.doctor.user"})
@EnableWebMvc
@EnableAutoConfiguration
@Import({DoctorBasicConfiguration.class,
        DoctorEventConfiguration.class,
        UserAutoConfig.class, ExtraUserAutoConfig.class, FileAutoConfig.class, ArticleAutoConfig.class,
        DoctorCommonConfiguration.class
})
public class DoctorMoveDataConfiguation extends WebMvcConfigurerAdapter {

    @Bean
    public DoctorSqlFactory doctorSqlFactory() throws IOException {
        String path = "classpath*:/hbs/**/*.hbs";
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(path);
            return new DoctorSqlFactory(resources);
        } catch (Exception e) {
            return new DoctorSqlFactory(null);
        }
    }

    @Bean
    public DoctorPigEventInputBuilders getPigEvenInputBuilderMap(DoctorChgLocationInputBuilder chgLocationInputBuilder,
                                                                 DoctorEntryInputBuilder entryInputBuilder,
                                                                 DoctorFarrowInputBuilder farrowInputBuilder,
                                                                 DoctorMateInputBuilder mateInputBuilder,
                                                                 DoctorPigLetsChgInputBuilder pigLetsChgInputBuilder,
                                                                 DoctorPigWeanInputBuilder pigWeanInputBuilder,
                                                                 DoctorPregCheckInputBuilder pregCheckInputBuilder,
                                                                 DoctorRemoveInputBuilder removeInputBuilder) {
        Map<String, DoctorPigEventInputBuilder> pigEventInputBuilderMap = Maps.newHashMap();
        pigEventInputBuilderMap.put(PigEvent.CHG_LOCATION.getName(), chgLocationInputBuilder);
        pigEventInputBuilderMap.put(PigEvent.ENTRY.getName(), entryInputBuilder);
        pigEventInputBuilderMap.put(PigEvent.FARROWING.getName(), farrowInputBuilder);
        pigEventInputBuilderMap.put(PigEvent.MATING.getName(), mateInputBuilder);
        pigEventInputBuilderMap.put(PigEvent.PIGLETS_CHG.getName(), pigLetsChgInputBuilder);
        pigEventInputBuilderMap.put(PigEvent.WEAN.getName(), pigWeanInputBuilder);
        pigEventInputBuilderMap.put(PigEvent.PREG_CHECK.getName(), pregCheckInputBuilder);
        pigEventInputBuilderMap.put(PigEvent.REMOVAL.getName(), removeInputBuilder);
        return new DoctorPigEventInputBuilders(pigEventInputBuilderMap);
    }

    @Bean
    public DoctorGroupEventInputBuilders getGroupEventInputBuildMap(DoctorChangeEventInputBuilder changeEventInputBuilder,
                                                                    DoctorCloseEventInputBuilder closeEventInputBuilder,
                                                                    DoctorMoveInEventInputBuilder moveInEventInputBuilder,
                                                                    DoctorNewEventInputBuilder newEventInputBuilder,
                                                                    DoctorTransGroupEventInputBuilder transGroupEventInputBuilder,
                                                                    DoctorGroupWeanEventInputBuilder groupWeanEventInputBuilder,
                                                                    DoctorTurnSeedEventInputBuilder turnSeedEventInputBuilder) {
        Map<String, DoctorGroupEventInputBuilder> groupEventInputBuilderMap = Maps.newHashMap();
        groupEventInputBuilderMap.put(GroupEventType.CHANGE.getDesc(), changeEventInputBuilder);
        groupEventInputBuilderMap.put(GroupEventType.CLOSE.getDesc(), closeEventInputBuilder);
        groupEventInputBuilderMap.put(GroupEventType.MOVE_IN.getDesc(), moveInEventInputBuilder);
        groupEventInputBuilderMap.put(GroupEventType.NEW.getDesc(), newEventInputBuilder);
        groupEventInputBuilderMap.put(GroupEventType.TRANS_GROUP.getDesc(), transGroupEventInputBuilder);
        groupEventInputBuilderMap.put(GroupEventType.WEAN.getDesc(), groupWeanEventInputBuilder);
        groupEventInputBuilderMap.put(GroupEventType.TURN_SEED.getDesc(), turnSeedEventInputBuilder);
        return new DoctorGroupEventInputBuilders(groupEventInputBuilderMap);
    }
}
