package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.Book;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-04-12 21:08:10
 * Created by [ your name ]
 */
public interface BookWriteService {

    /**
     * 鍒涘缓
     * @param book
     * @return Boolean
     */
    Response<Long> create(Book book);

    /**
     * 鏇存柊
     * @param book
     * @return Boolean
     */
    Response<Boolean> update(Book book);

    /**
     * 鍒犻櫎
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}