package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorArticleDao;
import io.terminus.doctor.user.model.DoctorArticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/24
 */
@Slf4j
@Service
@RpcProvider
public class DoctorArticleWriteServiceImpl implements DoctorArticleWriteService {

    @Autowired
    private DoctorArticleDao doctorArticleDao;

    @Override
    public Response<Long> create(DoctorArticle article) {
        try{
            doctorArticleDao.create(article);
            return Response.ok(article.getId());
        }catch (Exception e){
            log.error("failed to create article, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("article.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorArticle article) {
        try{
            return Response.ok(doctorArticleDao.update(article));
        }catch (Exception e){
            log.error("failed to update article, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("article.update.fail");
        }
    }

   @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(doctorArticleDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete article by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("delete.article.fail");
        }
    }

}