package io.terminus.doctor.basic.dao.redis;

import io.terminus.common.redis.utils.JedisTemplate;
import io.terminus.doctor.basic.enums.SearchType;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Desc: 猪舍搜索redis dao
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/24
 */
@Repository
public class DoctorSearchHistoryDao {

    private static final String KEY = "search:history:";

    private static final long size = 10L;

    private final JedisTemplate jedisTemplate;

    public DoctorSearchHistoryDao(JedisTemplate jedisTemplate) {
        SearchType.BARN.name();
        this.jedisTemplate = jedisTemplate;
    }

    /**
     * 保存搜索记录
     * @param userId 用户id
     * @param type 搜索类型
     * @see io.terminus.doctor.basic.enums.SearchType
     * @param word 要保存的值
     */
    public void setWord(Long userId, SearchType type, String word) {
        jedisTemplate.execute(jedis -> {
            jedis.zadd(getKey(userId, type), getScore(), word);
        });
    }

    /**
     * 搜索记录下拉框
     * @param userId 用户id
     * @param type  搜索类型
     * @return 搜索记录列表
     */
    public Set<String> getWords(Long userId, SearchType type) {
        return getWords(userId, type, size);
    }

    /**
     * 搜索记录下拉框
     * @param userId 用户id
     * @param type  搜索类型
     * @param size  大小限制
     * @return 搜索记录列表
     */
    public Set<String> getWords(Long userId, SearchType type, Long size) {
        return jedisTemplate.execute(jedis -> {
            return jedis.zrangeByScore(getKey(userId, type), 0, size);
        });
    }

    /**
     * 删除所有搜索记录
     * @param userId 用户id
     * @param type 搜索类型
     */
    public void deleteAllWords(Long userId, SearchType type) {
        jedisTemplate.execute(jedis -> {
            getWords(userId, type, -1L).forEach(w -> jedis.zrem(getKey(userId, type), w));
        });
    }

    /**
     * 删除搜索记录
     * @param userId 用户id
     * @param type   搜索类型
     * @param word   关键字
     */
    public void deleteWord(Long userId, SearchType type, String word) {
        jedisTemplate.execute(jedis -> {
            jedis.zrem(getKey(userId, type), word);
        });
    }

    private static String getKey(Long userId, SearchType type) {
        return KEY + ":" + type.name() + ":" + userId;
    }

    private static double getScore() {
        return (double) DateTime.now().getMillis();
    }
}
