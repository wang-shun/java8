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

    // pig code < key: farmId, value: pigCode list>
    private final LoadingCache<Long, List<String>> pigCodeCache;

    @Autowired
    public DoctorPigInfoCache(DoctorPigDao doctorPigDao){
        pigCodeCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).maximumSize(10000).build(new CacheLoader<Long, List<String>>() {
            @Override
            public List<String> load(Long key) throws Exception {
                return doctorPigDao.findPresentPigCodesByFarmId(key);
            }
        });
    }

    /**
     * 校验pigCode 是否存在
     * @param pigCode
     * @return 不存在则返回true
     */
    public Boolean judgePigCodeNotContain(Long farmId, String pigCode){
        try{
            return !pigCodeCache.get(farmId).contains(pigCode);
        }catch (Exception e){
            log.error("pig code cache validate error, farmId:{}, pigCode:{}, cause:{}", farmId, pigCode, Throwables.getStackTraceAsString(e));
            pigCodeCache.invalidate(farmId);
            return Boolean.FALSE;
        }
    }

    /**
     * 缓存添加Pig 数据信息
     * @param farmId
     * @param pigCode
     */
    public void addPigCodeToFarm(Long farmId, String pigCode){
        try{
            List<String> pigCodes = pigCodeCache.get(farmId);
            pigCodes.add(pigCode);
            pigCodeCache.put(farmId, pigCodes);
        }catch (Exception e){
            log.error("fail to add pig code to cache, farmId:{}, pigCode:{}, cause:{}", farmId, pigCode, Throwables.getStackTraceAsString(e));
            pigCodeCache.refresh(farmId);
        }
    }
}
