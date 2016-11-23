package io.terminus.doctor.event.cache;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.terminus.doctor.event.dao.DoctorPigDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yaoqijun.
 * Date:2016-06-24
 * Email:yaoqj@terminus.io
 * Descirbe: 猪相关信息缓存
 */
@Component
@Slf4j
public class DoctorPigInfoCache {

    private final DoctorPigDao doctorPigDao;

    // pig code < key: orgId, value: pigCode list>
    private final LoadingCache<Long, List<String>> pigCodeCache;

    @Autowired
    public DoctorPigInfoCache(DoctorPigDao doctorPigDao){
        this.doctorPigDao = doctorPigDao;

        pigCodeCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(10000).build(new CacheLoader<Long, List<String>>() {
            @Override
            public List<String> load(Long key) throws Exception {
                return doctorPigDao.findPigCodesByCompanyId(key);
            }
        });
    }

    /**
     * 校验pigCode 是否存在
     * @param pigCode
     * @return
     */
    public Boolean judgePigCodeNotContain(Long orgId, String pigCode){
        try{
            return !pigCodeCache.get(orgId).contains(pigCode);
        }catch (Exception e){
            log.error("pig code cache validate error, orgId:{}, pigCode:{}, cause:{}", orgId, pigCode, Throwables.getStackTraceAsString(e));
            pigCodeCache.invalidate(orgId);
            return Boolean.FALSE;
        }
    }

    /**
     * 缓存添加Pig 数据信息
     * @param orgId
     * @param pigCode
     */
    public void addPigCodeToFarm(Long orgId, String pigCode){
        try{
            List<String> pigCodes = pigCodeCache.get(orgId);
            pigCodes.add(pigCode);
            pigCodeCache.put(orgId, pigCodes);
        }catch (Exception e){
            log.error("fail to add pig code to cache, orgId:{}, pigCode:{}, cause:{}", orgId, pigCode, Throwables.getStackTraceAsString(e));
            pigCodeCache.refresh(orgId);
        }
    }
}
