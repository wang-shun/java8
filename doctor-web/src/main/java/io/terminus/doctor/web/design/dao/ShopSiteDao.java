package io.terminus.doctor.web.design.dao;

import io.terminus.common.redis.utils.JedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 15/1/13
 */
@Repository
public class ShopSiteDao {
    private JedisTemplate jedisTemplate;

    @Autowired
    public ShopSiteDao(@Qualifier("pampasJedisTemplate") JedisTemplate jedisTemplate) {
        this.jedisTemplate = jedisTemplate;
    }
}
