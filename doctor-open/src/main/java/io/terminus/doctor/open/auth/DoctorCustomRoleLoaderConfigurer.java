package io.terminus.doctor.open.auth;

import io.terminus.doctor.user.service.SubRoleReadService;
import io.terminus.parana.auth.role.CustomRoleLoaderConfigurer;
import io.terminus.parana.auth.role.CustomRoleLoaderRegistry;
import io.terminus.parana.auth.role.CustomRoleReadServiceWrapper;

/**
 * @author houly
 */
public class DoctorCustomRoleLoaderConfigurer implements CustomRoleLoaderConfigurer {

    private final SubRoleReadService subRoleReadService;

    public DoctorCustomRoleLoaderConfigurer(SubRoleReadService subRoleReadService) {
        this.subRoleReadService = subRoleReadService;
    }

    @Override
    public void configureCustomRoleLoader(CustomRoleLoaderRegistry registry) {
        registry.register("MOBILE", "SUB", new CustomRoleReadServiceWrapper(subRoleReadService));
    }
}
