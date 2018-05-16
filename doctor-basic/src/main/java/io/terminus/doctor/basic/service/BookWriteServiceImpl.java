package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.BookDao;
import io.terminus.doctor.basic.model.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-04-12 21:08:10
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class BookWriteServiceImpl implements BookWriteService {

    @Autowired
    private BookDao bookDao;

    @Override
    public Response<Long> create(Book book) {
        try{
            bookDao.create(book);
            return Response.ok(book.getId());
        }catch (Exception e){
            log.error("failed to create book, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("book.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(Book book) {
        try{
            return Response.ok(bookDao.update(book));
        }catch (Exception e){
            log.error("failed to update book, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("book.update.fail");
        }
    }

   @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(bookDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete book by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("book.delete.fail");
        }
    }

}