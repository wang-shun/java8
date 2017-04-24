package io.terminus.doctor.web.front.controller;

import com.google.common.collect.ImmutableMap;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.Article;
import io.terminus.doctor.user.service.ArticleReadService;
import io.terminus.doctor.user.service.ArticleWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Desc: 
 * Mail: hehaiyang@terminus.io
 * Date: 24
 */
@Slf4j
@RestController
@RequestMapping
public class Articles {

    @RpcConsumer
    private ArticleWriteService articleWriteService;

    @RpcConsumer
    private ArticleReadService articleReadService;

    /**
     * 查询单个文章
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Article findArticle(@PathVariable Long id) {
        Response<Article> response =  articleReadService.findById(id);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
     * 文章列表
     * @return
     */
    @RequestMapping(value = "/category/{categoryId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Article> listArticle(@PathVariable Integer categoryId) {

        Response<List<Article>> response =  articleReadService.list(ImmutableMap.of("categoryId", categoryId));
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
     * 文章分页
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/paging/{categoryId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Paging<Article> pagingArticle(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                         @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                         @PathVariable Integer categoryId) {
        Response<Paging<Article>> result =  articleReadService.paging(pageNo, pageSize, ImmutableMap.of("categoryId", categoryId));
        if(!result.isSuccess()){
            throw new JsonResponseException(result.getError());
        }
        return result.getResult();
    }

}