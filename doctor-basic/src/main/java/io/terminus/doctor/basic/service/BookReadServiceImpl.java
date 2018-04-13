package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.BookDao;
import io.terminus.doctor.basic.model.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-04-12 21:08:10
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class BookReadServiceImpl implements BookReadService {

    @Autowired
    private BookDao bookDao;

    @Override
    public Response<Book> findById(Long id) {
        try{
            return Response.ok(bookDao.findById(id));
        }catch (Exception e){
            log.error("failed to find book by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("book.find.fail");
        }
    }

    @Override
    public Response<Paging<Book>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(bookDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging book by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("book.paging.fail");
        }
    }

    @Override
    public Response<List<Book>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(bookDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list book, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("book.list.fail");
        }
    }

}
