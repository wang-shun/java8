/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-31
 */
public class JsoupTest {

    public static void main(String[] args) {
        String input = "<span style=\"font-family: microsoft yahei;\"><span style=\"background-color: #ffff00;\">" +
                "官方标配默认为拆封激活的价格，</span></span>" +
                "<script>alert(1);</script>"+
                "<div data-role=\"content\" class=\"tab-content\">\n" +
                "    <ul class=\"spu-info clearfix\">\n" +
                "      \n" +
                "    </ul>\n" +
                "    <img src=\"http://zcy-test.img-cn-hangzhou.aliyuncs.com/users/2/20160118120201031.jpg\">\n" +
                "<img src=\"http://zcy-test.img-cn-hangzhou.aliyuncs.com/users/2/20160118120200935.jpg\">\n" +
                "<img src=\"http://zcy-test.img-cn-hangzhou.aliyuncs.com/users/2/20160118120200991.jpg\">\n" +
                "  </div>" ;

        System.out.println(Jsoup.clean(input,"", Whitelist.relaxed()
                .addAttributes(":all","class").addAttributes(":all","style")));
    }
}
