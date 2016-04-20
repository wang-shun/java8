package io.terminus.doctor.user;

import io.terminus.parana.article.impl.ArticleAutoConfig;
import io.terminus.parana.file.FileAutoConfig;
import io.terminus.parana.user.ExtraUserAutoConfig;
import io.terminus.parana.user.UserAutoConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Effet
 */
@Configuration
@ComponentScan(basePackages = {
        "io.terminus.doctor.user"
})
@Import({UserAutoConfig.class, ExtraUserAutoConfig.class, FileAutoConfig.class, ArticleAutoConfig.class})
public class DoctorUserConfiguration {
}
