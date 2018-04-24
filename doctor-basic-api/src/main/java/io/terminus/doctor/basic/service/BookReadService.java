package io.terminus.doctor.basic.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.Book;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-04-12 21:08:10
 * Created by [ your name ]
 */
public interface BookReadService {

    /**
     * 鏌ヨ
     * @param id
     * @return book
     */
    Response<Book> findById(Long id);

    /**
     * 鍒嗛〉
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<Book>
     */
    Response<Paging<Book>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

   /**
    * 鍒楄〃
    * @param criteria
    * @return List<Book>
    */
    Response<List<Book>> list(Map<String, Object> criteria);
}