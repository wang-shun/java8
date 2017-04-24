package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.ArticleDao;
import io.terminus.doctor.user.model.Article;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc: 
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/24
 */
@Slf4j
@Service
@RpcProvider
public class ArticleReadServiceImpl implements ArticleReadService {

    @Autowired
    private ArticleDao articleDao;

    @Override
    public Response<Article> findById(Long id) {
        try{
            return Response.ok(articleDao.findById(id));
        }catch (Exception e){
            log.error("failed to find article by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("article.find.fail");
        }
    }

    @Override
    public Response<Paging<Article>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(articleDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging article by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("article.paging.fail");
        }
    }

    @Override
    public Response<List<Article>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(articleDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list article , cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("article .list.fail");
        }
    }

}
