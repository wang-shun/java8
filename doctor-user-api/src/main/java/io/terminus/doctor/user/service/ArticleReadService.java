package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.Article;

import java.util.Map;
import java.util.List;

/**
 * Desc: 
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/24
 */
public interface ArticleReadService {

    /**
     * 查询
     * @param id
     * @return article
     */
    Response<Article> findById(Long id);

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<Article>
     */
    Response<Paging<Article>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

   /**
    * 列表
    * @param criteria
    * @return List<Article>
    */
    Response<List<Article>> list(Map<String, Object> criteria);
}