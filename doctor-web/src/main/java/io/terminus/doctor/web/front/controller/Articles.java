package io.terminus.doctor.web.front.controller;

import com.google.common.collect.ImmutableMap;
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

/**
 * Desc: 
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/25
 */
@Slf4j
@RestController
@RequestMapping("/api/articles")
public class Articles {

    @RpcConsumer
    private DoctorArticleWriteService doctorArticleWriteService;

    @RpcConsumer
    private DoctorArticleReadService doctorArticleReadService;

    /**
     * 查询单个文章
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorArticle findArticle(@PathVariable Long id) {
        Response<DoctorArticle> response =  doctorArticleReadService.findById(id);
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

        Response<List<DoctorArticle>> response =  doctorArticleReadService.list(ImmutableMap.of("categoryId", categoryId, "status", status));
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
        Response<Paging<DoctorArticle>> result =  doctorArticleReadService.paging(pageNo, pageSize, ImmutableMap.of("categoryId", categoryId, "status", status));
        if(!result.isSuccess()){
            throw new JsonResponseException(result.getError());
        }
        return result.getResult();
    }

}