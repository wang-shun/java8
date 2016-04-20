/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.admin.article;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.doctor.web.core.enums.ArticleType;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.article.enums.ArticleStatus;
import io.terminus.parana.article.model.Article;
import io.terminus.parana.article.service.ArticleReadService;
import io.terminus.parana.article.service.ArticleWriteService;
import io.terminus.parana.common.model.ParanaUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static io.terminus.common.utils.Arguments.notNull;

/**
 * 运营后台文章操作
 *
 * Author  : panxin
 * Date    : 5:14 PM 3/21/16
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/article")
public class AdminArticles {

    private final ArticleWriteService articleWriteService;

    private final ArticleReadService articleReadService;

    @Autowired
    public AdminArticles(ArticleWriteService articleWriteService,
                         ArticleReadService articleReadService) {
        this.articleReadService = articleReadService;
        this.articleWriteService = articleWriteService;
    }

    /**
     * 获取文章分类
     *
     * @return 分类列表[{id: value, name: value}, ...]
     */
    @RequestMapping(value = "/types", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, String>> getArticleTypes()  {
        List<Map<String, String>> typeList = Lists.newArrayList();
        Map<String, String> typeMap = null;
        for (ArticleType value : ArticleType.values()) {
            typeMap = Maps.newHashMap();
            typeMap.put("id", value.value()+"");
            typeMap.put("name", value.toString());
            typeList.add(typeMap);
        }
        return typeList;
    }

    /**
     * 新建文章
     *
     * @param article 文章信息
     * @return 新建文章ID
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createArticle(@RequestBody Article article) {
        ArticleType.from(article.getType());
        ParanaUser user = UserUtil.getCurrentUser();

        article.setOwnerId(user.getId());
        article.setStatus(ArticleStatus.UNPUBLISHED.value());
        article.setOwnerName(user.getName());

        Response<Long> resp = articleWriteService.create(article);
        if (!resp.isSuccess()) {
            log.error("failed to create article = {}, cause : {}", article, resp.getError());
            throw new JsonResponseException(500, resp.getError());
        }
        return resp.getResult();
    }

    /**
     * 更新文章
     *
     * @param article
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean updateArticle(@RequestBody Article article) {
        Response<Boolean> resp = articleWriteService.update(article);
        if (!resp.isSuccess()) {
            log.error("failed to update article({}), id = {}, cause : {}", article, article.getId(), resp.getError());
            throw new JsonResponseException(500, resp.getError());
        }
        return resp.getResult();
    }

    /**
     * 修改文章状态
     *
     * @param id 文章ID
     * @param status 文章状态
     * @return 是否更新成功
     */
    @RequestMapping(value = "/status/{id}/{status}", method = RequestMethod.PUT)
    public Boolean updateStatus(@PathVariable Long id,
                                @PathVariable Integer status) {
        checkArgument(notNull(id), "article.id.empty");
        checkArgument(notNull(status), "article.status.empty");
        Response<Boolean> resp = articleWriteService.setStatus(id, status);
        if (!resp.isSuccess()) {
            log.error("failed to set article status = {}, by id = {}, cause : {}",
                    status, id, resp.getError());
            throw new JsonResponseException(500, resp.getError());
        }
        return Boolean.TRUE;
    }

    /**
     * 删除文章
     *
     * @param id 文章ID
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean deleteArticleById(@RequestParam(value = "id") Long id) {
        checkArgument(notNull(id), "article.id.empty");
        Response<Boolean> resp = articleWriteService.delete(id);
        if (!resp.isSuccess()) {
            log.error("failed to delete article by id = {}, cause : ", id, resp.getError());
            throw new JsonResponseException(500, resp.getError());
        }
        return resp.getResult();
    }
}
