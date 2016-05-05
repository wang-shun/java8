/*
 * Copyright (c) 2015 杭州端点网络科技有限公司
 */

package io.terminus.doctor.web.core.auth;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import io.terminus.pampas.engine.Setting;
import io.terminus.pampas.engine.config.model.BaseConfig;
import io.terminus.pampas.engine.model.App;
import io.terminus.pampas.engine.utils.FileLoader;
import io.terminus.pampas.engine.utils.FileLoaderHelper;
import io.terminus.parana.auth.parser.ParseResult;
import io.terminus.parana.auth.parser.TreeParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Effet
 */
@Slf4j
@Component
public class AuthLoader implements Runnable {

    public static final String AUTH_CONFIG_FILE = "auth.yml";

    private LoadingCache<App, ParseResult> authTreeCache;

    /* file path => config */
    private final ConcurrentMap<String, BaseConfig> authFileConfigMap = Maps.newConcurrentMap();

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final FileLoaderHelper fileLoaderHelper;

    @Autowired
    public AuthLoader(final Setting setting, final FileLoaderHelper fileLoaderHelper) {
        this.fileLoaderHelper = fileLoaderHelper;
        authTreeCache = CacheBuilder.newBuilder().build(new CacheLoader<App, ParseResult>() {
            @Override
            public ParseResult load(App app) throws Exception {
                String path = getAuthPath(app);
                FileLoader.Resp resp = fileLoaderHelper.load(path);

                AuthFileConfig config = new AuthFileConfig();
                config.setLocation(path);
                config.setSign(resp.getSign());
                config.setLoadedAt(new Date());
                authFileConfigMap.put(path, config);

                if (resp.isNotFound()) {
                    log.error("auth file not found, app={}, path={}", app.getKey(), path);
                    throw new RuntimeException("auth file not found");
                }
                return parseTree(resp.asString());
            }
        });
        if (setting.isDevMode()) {
            executorService.scheduleAtFixedRate(this, 5, 5, TimeUnit.SECONDS);
        } else {
            executorService.scheduleAtFixedRate(this, 5, 5, TimeUnit.MINUTES);
        }
    }

    public ParseResult getTree(App app) {
        return authTreeCache.getUnchecked(app);
    }

    private ParseResult parseTree(String content) {
        try {
            return TreeParser.parse(content);
        } catch (Exception e) {
            log.error("parse tree failed, cause:{}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("auth file parse fail");
        }
    }

    private String getAuthPath(App app) {
        return app.getAssetsHome() + AUTH_CONFIG_FILE;
    }

    private void checkIfNeedReload() {
        for (Map.Entry<String, BaseConfig> entry : authFileConfigMap.entrySet()) {
            String path = entry.getKey();
            BaseConfig config = entry.getValue();
            FileLoader.Resp resp = fileLoaderHelper.load(path, config.getSign());
            if (resp.modified()) {
                log.info("auth tree config file changed {}", path);
                // refresh
                config.setLocation(path);
                config.setSign(resp.getSign());
                config.setLoadedAt(new Date());
//                authTreeCache.invalidate(config.getApp());
                authTreeCache.invalidateAll();
                break;
            }
        }
    }

    @Override
    public void run() {
        checkIfNeedReload();
    }
}
