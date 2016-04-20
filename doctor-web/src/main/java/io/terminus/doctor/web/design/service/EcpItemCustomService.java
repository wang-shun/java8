/*
 * <!--
 *   ~ Copyright (c) 2014 杭州端点网络科技有限公司
 *   -->
 */

package io.terminus.doctor.web.design.service;

import io.terminus.pampas.client.Export;

import java.util.Map;

/**
 * Created by yangzefeng on 14/11/19
 */
public interface EcpItemCustomService {

    /**
     * 获取商品详情装修hbs
     * @param itemId 商品id
     * @param spuId 商品的spuId
     * @param context 调用上下文，因为需要渲染handlebars 所以需要上下文
     * @return 渲染前的hbs
     */
    String getRenderHbs(Long itemId, Long spuId, Map<String, String> context);

    /**
     * 渲染对应的商品的详情模板内容
     *
     * @param spuId 模板关联的spuId
     * @return 渲染后的html
     */
    @Export(paramNames = {"spuId", "context"})
    String renderTemplate(Long spuId, Map<String, String> context);

    /**
     * 渲染对应的商品的装修内容
     *
     * @param itemId 商品id
     * @param spuId 商品的spuId
     * @param context 调用上下文，因为需要渲染handlebars 所以需要上下文
     * @return 渲染后的html
     */
    @Export(paramNames = {"itemId", "spuId", "context"})
    String render(Long itemId, Long spuId, Map<String, String> context);
}
