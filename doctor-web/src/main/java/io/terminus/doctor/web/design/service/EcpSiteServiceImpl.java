/*
 * <!--
 *   ~ Copyright (c) 2014 杭州端点网络科技有限公司
 *   -->
 */

package io.terminus.doctor.web.design.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.model.BaseUser;
import io.terminus.common.redis.utils.JedisTemplate;
import io.terminus.doctor.web.design.dao.TemplateDao;
import io.terminus.doctor.web.design.ext.EcpSiteHook;
import io.terminus.doctor.web.design.modal.EcpAppKey;
import io.terminus.doctor.web.design.modal.EcpLayoutType;
import io.terminus.doctor.web.design.modal.ShopSitePageCategory;
import io.terminus.doctor.web.design.modal.Template;
import io.terminus.pampas.design.dao.PageDao;
import io.terminus.pampas.design.dao.SiteDao;
import io.terminus.pampas.design.model.Page;
import io.terminus.pampas.design.model.Site;
import io.terminus.pampas.engine.MessageSources;
import io.terminus.pampas.engine.config.model.FrontConfig;
import io.terminus.pampas.engine.config.model.Render;
import io.terminus.pampas.engine.service.ConfigService;
import io.terminus.doctor.web.design.modal.ShopSite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by yangzefeng on 14/11/12
 */
@Service
public class EcpSiteServiceImpl implements EcpSiteService {
    @Autowired
    private ConfigService configService;
    @Autowired(required = false)
    private SiteDao siteDao;
    @Autowired(required = false)
    private PageDao pageDao;
    @Autowired
    private TemplateDao templateDao;
    @Autowired
    @Qualifier("pampasJedisTemplate")
    private JedisTemplate jedisTemplate;
    @Autowired(required = false)
    private EcpSiteHook ecpSiteHook;
    @Autowired
    private MessageSources messageSources;

    private static final List<ShopSite> EMPTY_SHOP_SITE_LIST = Collections.emptyList();

    @Override
    public Long getCurrentShopSiteIdByApp(final Long userId, final EcpAppKey appKey) {
        String siteIdStr = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(keyForActiveShopSite(userId, appKey.name()));
            }
        });
        if (Strings.isNullOrEmpty(siteIdStr)) {
            return _initShopSite(userId, appKey);
        }
        return Long.valueOf(siteIdStr);
    }

    @Override
    public void setCurrentShopSiteByMode(final Long userId, final Site site) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.set(keyForActiveShopSite(userId, site.getApp()), site.getId().toString());
            }
        });
    }

    @Override
    public Map<String, List<ShopSite>> listShopSites(BaseUser currentUser) {
        Long userId = currentUser.getId();
//        if (User.TYPE.fromNumber(currentUser.getType()) != User.TYPE.SELLER && User.TYPE.fromNumber(currentUser.getType()) != User.TYPE.SITE_OWNER) {
//            throw new IllegalArgumentException("user not a seller or site owner when list shop sites");
//        }
        Map<String, List<ShopSite>> result = Maps.newLinkedHashMap();
        //result.put(EcpAppKey.PC.name(), listShopSitesByApp(userId, EcpAppKey.PC));
        result.put(EcpAppKey.PC.name(), listShopSitesFromTemplate(userId, EcpAppKey.PC));
        //result.put(EcpAppKey.MOBILE.name(), listShopSitesByApp(userId, EcpAppKey.MOBILE));
        result.put(EcpAppKey.MOBILE.name(), listShopSitesFromTemplate(userId, EcpAppKey.MOBILE));
        return result;
    }

    @Override
    public Map<String, Object> listSitesWithLayouts() {
        Map<String, Object> result = Maps.newHashMap();
        List<Site> sites = siteDao.listAll();
        result.put("sites", sites);
        List<Render.Layout> layouts = configService.listLayouts(EcpLayoutType.SITE.name());
        result.put("layouts", layouts);
        return result;
    }

    @Override
    public void initShopSite(final Long userId) {
        // 初始化 PC_SHOP 和 MOBILE_SHOP 两种 site 各一个
        _initShopSite(userId, EcpAppKey.PC);
        _initShopSite(userId, EcpAppKey.MOBILE);
    }

    @Override
    public void createShopSite(final Long userId, final EcpAppKey appKey, final String layoutKey) {
        Site exists = findShopSiteByLayout(userId, appKey, layoutKey);
        if (exists != null) {
            return;
        }

        final Render.Layout layout = findLayout(appKey, layoutKey);
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                _createShopSite(userId, appKey.name(), layout, t);
                t.exec();
            }
        });
    }

    @Override
    public boolean isShopLayout(Site site) {
        if (site.getLayout().startsWith(Template.TEMPLATE_KEY_PREFIX)) {
            return true;
        }
        Render.Layout layout = configService.findLayout(site.getApp(), site.getLayout());
        if (EcpLayoutType.valueOf(layout.getType()) == EcpLayoutType.SHOP) {
            return true;
        }
        return false;
    }

    private Render.Layout findLayout(EcpAppKey appKey, String layoutKey) {
        Render.Layout layout;

        //该layout是存到redis的模板
        if (layoutKey.startsWith(Template.TEMPLATE_KEY_PREFIX)) {
            Template template = templateDao.findTemplate(appKey.name(), layoutKey);
            layout = templateConvert2Layout(template);
        } else {//该layout是在前端工程
            FrontConfig frontConfig = configService.getFrontConfig(appKey.name());
            layout = frontConfig.getRender().getLayouts().get(layoutKey);
        }

        return layout;
    }

    private Site findShopSiteByLayout(final Long userId, final EcpAppKey appKey, final String layoutKey) {
        String siteIdStr = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(keyForShopSite(userId, appKey.name(), layoutKey));
            }
        });

        if (Strings.isNullOrEmpty(siteIdStr)) {
            return null;
        }
        return siteDao.findById(Long.valueOf(siteIdStr));
    }

    private Long _initShopSite(final Long userId, final EcpAppKey appKey) {
        return jedisTemplate.execute(new JedisTemplate.JedisAction<Long>() {
            @Override
            public Long action(Jedis jedis) {
                String idStr = jedis.get(keyForActiveShopSite(userId, appKey.name()));
                if (!Strings.isNullOrEmpty(idStr)) {
                    return Long.valueOf(idStr);
                }

                //如果存在已发布的装修模板则直接使用装修的店铺模板，否则使用前端工程的layout
                Render.Layout layout;
                Template template = findReleaseTemplate(appKey);
                if (template != null) {
                    layout = templateConvert2Layout(template);
                } else {
                    List<Render.Layout> layouts = configService.listLayouts(appKey.name(), EcpLayoutType.SHOP.name());
                    layout = layouts.isEmpty() ? null : layouts.get(0);
                }
                if (layout == null) {
                    throw new IllegalStateException("no default layout found for app " + appKey.name());
                }
                Transaction t = jedis.multi();
                Long siteId = _createShopSite(userId, layout.getApp(), layout, t);
                t.set(keyForActiveShopSite(userId, appKey.name()), siteId.toString());
                t.exec();
                return siteId;
            }
        });
    }

    private Template findReleaseTemplate(EcpAppKey appKey) {
        List<Template> templates = templateDao.findReleaseTemplates(appKey.name());
        if (templates.isEmpty()) {
            return null;
        }
        //如果存在默认模板则返回
        for (Template template : templates) {
            if (template.isDefault()) {
                return template;
            }
        }
        //没有设置默认模板,则返回最新的
        return new TreeSet<>(templates).last();
    }

    private Long _createShopSite(Long userId, String app, Render.Layout layout, Transaction t) {
        Site site = new Site();
        site.setApp(app);
        site.setLayout(layout.getKey());
        site.setLayoutName(layout.getName());
        site.setUserId(userId);
        site.setId(siteDao.newId());
        siteDao.create(t, site, true);
        t.set(keyForShopSite(userId, app, layout.getKey()), site.getId().toString());
        for (ShopSitePageCategory pageCategory : ShopSitePageCategory.values()) {
            String pageName = MoreObjects.firstNonNull(messageSources.get(pageCategory.getKeyForName()), pageCategory.getDefaultName());
            Page page = new Page();
            page.setImmutable(true);
            page.setApp(site.getApp());
            page.setSiteId(site.getId());
            page.setName(pageName);
            page.setTitle(pageName);
            page.setPath(pageCategory.getPath());
            page.setType(Page.Type.PAGE);
            pageDao.create(page, t);
        }
        if (ecpSiteHook != null) {
            List<Page> morePages = ecpSiteHook.getPages();
            if (morePages != null && !morePages.isEmpty()) {
                for (Page page : morePages) {
                    page.setImmutable(true);
                    page.setApp(site.getApp());
                    page.setSiteId(site.getId());
                    page.setType(Page.Type.PAGE);
                    pageDao.create(page, t);
                }
            }
        }
        return site.getId();
    }

    private String keyForActiveShopSite(Long userId, String app) {
        // user:userId:site-mode:mode:active ->
        //   user:userId:app:app:active-site
        return "user:" + userId + ":app:" + app + ":active-shop-site";
    }

    private String keyForShopSite(Long userId, String app, String layoutKey) {
        // user:userId:layout:layoutKey:site ->
        //   user:userId:app:app:layout:layoutKey:site
        return "user:" + userId + ":app:" + app + ":layout:" + layoutKey + ":site";
    }

    private List<ShopSite> listShopSitesByApp(Long userId, EcpAppKey appKey) {
        List<ShopSite> shopSites = Lists.newArrayList();

        shopSites.addAll(listShopSitesFromFrontConfig(userId, appKey));
        shopSites.addAll(listShopSitesFromTemplate(userId, appKey));
        return shopSites;
    }

    private List<ShopSite> listShopSitesFromFrontConfig(Long userId, EcpAppKey appKey) {
        List<Render.Layout> layouts = configService.listLayouts(appKey.name(), EcpLayoutType.SHOP.name());
        return getShopSite(layouts, userId, appKey);
    }

    private List<ShopSite> listShopSitesFromTemplate(Long userId, EcpAppKey appKey) {

        List<Template> templates = templateDao.findReleaseTemplates(appKey.name());
        if (templates == null || templates.isEmpty()) {
            return EMPTY_SHOP_SITE_LIST;
        }

        List<Render.Layout> layouts = Lists.newArrayList();
        for (Template template : templates) {
            Render.Layout layout = templateConvert2Layout(template);
            layouts.add(layout);
        }
        return getShopSite(layouts, userId, appKey);
    }

    private List<ShopSite> getShopSite(List<Render.Layout> layouts, Long userId, EcpAppKey appKey) {
        if (layouts == null || layouts.isEmpty()) {
            return EMPTY_SHOP_SITE_LIST;
        }
        List<ShopSite> shopSites = Lists.newArrayList();
        Long activeSiteId = getCurrentShopSiteIdByApp(userId, appKey);

        for (Render.Layout layout : layouts) {
            ShopSite shopSite = new ShopSite();
            shopSite.setLayoutKey(layout.getKey());
            shopSite.setLayout(layout);
            Site site = findShopSiteByLayout(userId, appKey, layout.getKey());
            if (site != null) {
                shopSite.setSiteId(site.getId());
                if (Objects.equal(site.getId(), activeSiteId)) {
                    shopSite.setActive(true);
                }
            }
            shopSites.add(shopSite);
        }
        return shopSites;
    }

    private Render.Layout templateConvert2Layout(Template template) {
        Render.Layout layout = new Render.Layout();
        layout.setApp(template.getApp());
        layout.setKey(template.getKey());
        layout.setName(template.getName());
        layout.setDesc(template.getDesc());
        return layout;
    }

//    @Override
//    public Paging<Site> pagination(String app, Integer siteType, Integer pageNo, Integer size) {
//        return siteService.pagination(app, siteType, pageNo, size);
//    }
//
//    @Override
//    public Map<String, Object> listShopTemplates(BaseUser loginer, String app) {
//        return siteService.listShopTemplates(loginer, app);
//    }
}
