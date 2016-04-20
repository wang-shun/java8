/*
 * <!--
 *   ~ Copyright (c) 2014 杭州端点网络科技有限公司
 *   -->
 */

package io.terminus.doctor.web.design.service;

import io.terminus.pampas.design.dao.ItemCustomRedisDao;
import io.terminus.pampas.design.service.ItemCustomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by yangzefeng on 14/11/19
 */
@Service
public class EcpItemCustomServiceImpl implements EcpItemCustomService {

    @Autowired(required = false)
    private ItemCustomRedisDao itemCustomRedisDao;

    @Autowired(required = false)
    private ItemCustomService itemCustomService;

    @Override
    public String getRenderHbs(Long itemId, Long spuId, Map<String, String> context) {

        return itemCustomRedisDao.findById(itemId);
    }

    @Override
    public String renderTemplate(Long spuId, Map<String, String> context) {
        return itemCustomService.renderTemplate(spuId, context);
    }

    @Override
    public String render(Long itemId, Long spuId, Map<String, String> context) {
        return itemCustomService.render(itemId, spuId, context);
    }
}
