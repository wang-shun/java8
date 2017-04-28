package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorArticle;

import java.util.Map;
import java.util.List;

/**
 * Desc: 
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/24
 */
public interface DoctorArticleReadService {

    /**
     * 查询
     * @param id
     * @return article
     */
    Response<DoctorArticle> findById(Long id);

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorArticle>
     */
    Response<Paging<DoctorArticle>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

   /**
    * 列表
    * @param criteria
    * @return List<DoctorArticle>
    */
    Response<List<DoctorArticle>> list(Map<String, Object> criteria);
}