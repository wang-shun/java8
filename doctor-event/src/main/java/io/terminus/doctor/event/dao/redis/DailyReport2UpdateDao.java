package io.terminus.doctor.event.dao.redis;

import io.terminus.common.redis.utils.JedisTemplate;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenzenghui on 16/8/29.
 * 将要被更新的日报
 */
@Repository
public class DailyReport2UpdateDao {
    private static final String REDIS_KEY_DAILY_REPORT_UPDATE = "daily-report:update:";
    private final JedisTemplate jedisTemplate;

    @Autowired
    public DailyReport2UpdateDao (JedisTemplate jedisTemplate){
        this.jedisTemplate = jedisTemplate;
    }

    /**
     * 记录需要更新的日报, 这些数据将会存入redis
     * 如果同一猪场多次存入, 则保留 beginDate 最早的一次
     * @param beginDate 自此日期之后(包括此日期)的日报将被job更新(重新统计)
     * @param farmId 猪场
     */
    public void saveDailyReport2Update(Date beginDate, Long farmId){
        String key = this.getKey(farmId);
        jedisTemplate.execute(jedis -> {
            if(!jedis.exists(key) || DateUtil.toDate(jedis.get(key)).after(beginDate)){
                jedis.set(key, DateUtil.toDateString(beginDate));
            }
        });
    }

    public void deleteDailyReport2Update(Long farmId){
        jedisTemplate.execute(jedis -> {
            jedis.del(this.getKey(farmId));
        });
    }

    public Map<Long, String> getDailyReport2Update(){
        Map<Long, String> farmAndDate = new HashMap<>();
        jedisTemplate.execute(jedis -> {
            for(String key : jedis.keys(REDIS_KEY_DAILY_REPORT_UPDATE + "*")){
                farmAndDate.put(Long.valueOf(key.replace(REDIS_KEY_DAILY_REPORT_UPDATE, "")), jedis.get(key));
            }
        });
        return farmAndDate;
    }

    private String getKey(Long farmId){
        return REDIS_KEY_DAILY_REPORT_UPDATE + farmId;
    }
}
