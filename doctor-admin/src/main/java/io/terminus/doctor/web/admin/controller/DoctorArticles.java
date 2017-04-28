package io.terminus.doctor.web.admin.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorArticle;
import io.terminus.doctor.user.service.DoctorArticleReadService;
import io.terminus.doctor.user.service.DoctorArticleWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Desc: 
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/25
 */
@Slf4j
@RestController
@RequestMapping("/api/articles")
public class DoctorArticles {

    @RpcConsumer
    private DoctorArticleWriteService doctorArticleWriteService;

    @RpcConsumer
    private DoctorArticleReadService doctorArticleReadService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorArticle findArticle(@PathVariable Long id) {
        Response<DoctorArticle> response =  doctorArticleReadService.findById(id);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean deleteArticle(@PathVariable Long id) {
        Response<Boolean> response = doctorArticleWriteService.delete(id);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
     * 创建
     * @param
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createArticle(@RequestBody DoctorArticle article) {
        Response<Long> response = doctorArticleWriteService.create(article);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
     * 更新
     * @param
     */
    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean updateArticle(@RequestBody DoctorArticle article) {
        Response<Boolean> response = doctorArticleWriteService.update(article);
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
    public List<DoctorArticle> listArticle(@PathVariable Integer categoryId,
                                           @RequestParam(value = "status", required = false) Integer status) {

        Map<String, Object> params = Maps.newHashMap();
        params.put("categoryId", categoryId);
        if(status != null){
            params.put("status", status);
        }
        Response<List<DoctorArticle>> response =  doctorArticleReadService.list(params);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        if(!response.getResult().isEmpty()){
            response.getResult().stream().forEach(it -> it.setContent(""));
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
    public Paging<DoctorArticle> pagingArticle(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                               @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                               @RequestParam(value = "status", required = false) Integer status,
                                               @PathVariable Integer categoryId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("categoryId", categoryId);
        if(status != null){
            params.put("status", status);
        }
        Response<Paging<DoctorArticle>> result =  doctorArticleReadService.paging(pageNo, pageSize, params);
        if(!result.isSuccess()){
            throw new JsonResponseException(result.getError());
        }
        return result.getResult();
    }

}