package io.terminus.doctor.event.dao.redis;

import com.google.common.base.Strings;
import io.terminus.common.redis.utils.JedisTemplate;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by chenzenghui on 16/8/29.
 * 历史日报
 */
@Repository
public class DailyReportHistoryDao {
    private static final String REDIS_KEY_DAILY_REPORT_HISTORY = "daily-report:history:";
    private final JedisTemplate jedisTemplate;
    private final DoctorDailyReportDao doctorDailyReportDao;

    @Autowired
    public DailyReportHistoryDao(JedisTemplate jedisTemplate,
                                 DoctorDailyReportDao doctorDailyReportDao){
        this.jedisTemplate = jedisTemplate;
        this.doctorDailyReportDao = doctorDailyReportDao;
    }

    private String getRedisKey(Long farmId, Date sumAt){
        return REDIS_KEY_DAILY_REPORT_HISTORY + farmId + ":" + DateUtil.toDateString(sumAt);
    }

    /**
     * 根据farmId和sumAt从redis查询日报dto. 如果redis中没有就从数据库查询并存入redis
     */
    public DoctorDailyReportDto getDailyReportWithRedis(Long farmId, Date sumAt) {
        String json = jedisTemplate.execute(jedis -> {
            return jedis.get(getRedisKey(farmId, sumAt));
        });
        if(json != null){
            return JsonMapper.JSON_NON_EMPTY_MAPPER.fromJson(json, DoctorDailyReportDto.class);
        }
        DoctorDailyReport report = doctorDailyReportDao.findByFarmIdAndSumAt(farmId, sumAt);
        //如果没有查到, 要返回null, 交给上层判断
        if (report == null || Strings.isNullOrEmpty(report.getData())) {
            return null;
        }else{
            this.saveDailyReport(report.getReportData(), farmId, sumAt);
            return report.getReportData();
        }
    }

    /**
     * 将日报存入redis, 1天后过期
     * @param reportDto
     * @param farmId
     * @param sumAt
     */
    public void saveDailyReport(DoctorDailyReportDto reportDto, Long farmId, Date sumAt){
        jedisTemplate.execute(jedis -> {
            jedis.setex(getRedisKey(farmId, sumAt), 86400, JsonMapper.JSON_NON_EMPTY_MAPPER.toJson(reportDto));
        });
    }

    /**
     * 删除redis中的日报
     * @param farmId
     * @param sumAt
     */
    public void deleteDailyReport(Long farmId, Date sumAt){
        jedisTemplate.execute(jedis -> {
            jedis.del(getRedisKey(farmId, sumAt));
        });
    }

}
