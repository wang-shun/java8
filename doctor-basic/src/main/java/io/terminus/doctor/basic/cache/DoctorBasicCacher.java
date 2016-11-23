package io.terminus.doctor.basic.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/27
 */
@Slf4j
@Component
public class DoctorBasicCacher {

    private final DoctorBasicDao doctorBasicDao;

    @Getter
    private final LoadingCache<Integer, List<DoctorBasic>> basicCache;

    @Autowired
    public DoctorBasicCacher(DoctorBasicDao doctorBasicDao) {
        this.doctorBasicDao = doctorBasicDao;

        //基础数据缓存
        this.basicCache = CacheBuilder.newBuilder().expireAfterAccess(1L, TimeUnit.DAYS).maximumSize(10000).build(new CacheLoader<Integer, List<DoctorBasic>>() {
            @Override
            public List<DoctorBasic> load(Integer type) {
                return doctorBasicDao.findByType(type);
            }
        });
    }

    public void refresh(Integer type) {
        basicCache.refresh(type);
    }

    public void clear() {
        basicCache.invalidateAll();
    }
}
