package io.terminus.doctor.event.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.dto.DoctorDepartmentLinerDto;
import io.terminus.doctor.user.service.DoctorDepartmentReadService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by xjn on 18/1/10.
 * email:xiaojiannan@terminus.io
 * 组织维度缓存
 */
@Component
public class DoctorDepartmentCache {
    private LoadingCache<Long, DoctorDepartmentLinerDto> organizationCache;
//    @RpcConsumer
    private DoctorDepartmentReadService doctorDepartmentReadService;

    @PostConstruct
    public void init() {
        this.organizationCache =  CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).maximumSize(10000).build(new CacheLoader<Long, DoctorDepartmentLinerDto>() {
            @Override
            public DoctorDepartmentLinerDto load(Long key) throws Exception {
                return RespHelper.orServEx(doctorDepartmentReadService.findLinerBy(key));
            }
        });
    }

    public DoctorDepartmentLinerDto getUnchecked(Long farmId) {
        return organizationCache.getUnchecked(farmId);
    }
}
