/*
 * <!--
 *   ~ Copyright (c) 2014 杭州端点网络科技有限公司
 *   -->
 */

package io.terminus.doctor.web.design.controller;

import io.terminus.doctor.web.design.service.EcpSiteService;
import io.terminus.pampas.design.container.DPageRender;
import io.terminus.pampas.design.service.SiteService;
import io.terminus.pampas.engine.request.ViewRender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;


/**
 * Desc: 特殊 url 入口
 * Date: 8/16/12 10:48 AM
 * Created by yangzefeng on 14/11/19
 */
@Controller
public class DoctorView {

    public static final Logger log = LoggerFactory.getLogger(DoctorView.class);

    @Autowired(required = false)
    private DPageRender dPageRender;
    @Autowired
    private ViewRender viewRender;
    @Autowired
    private EcpSiteService ecpSiteService;
    @Autowired(required = false)
    private SiteService siteService;
//    @Autowired(required = false)
//    private SubDomainReadService subDomainReadService;


    @PostConstruct
    public void init() {

    }



}
