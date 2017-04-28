package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorArticle;

/**
 * Desc: 
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/24
 */
public interface DoctorArticleWriteService {

    /**
     * 创建
     * @param article
     * @return Boolean
     */
    Response<Long> create(DoctorArticle article);

    /**
     * 更新
     * @param article
     * @return Boolean
     */
    Response<Boolean> update(DoctorArticle article);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}