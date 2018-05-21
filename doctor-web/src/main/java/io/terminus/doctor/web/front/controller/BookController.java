package io.terminus.doctor.web.front.controller;

import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.Book;
import io.terminus.doctor.basic.service.BookReadService;
import io.terminus.doctor.basic.service.BookWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-04-12 21:08:10
 * Created by [ your name ]
 */
@Slf4j
@RestController
@RequestMapping("/api/book")
public class BookController {

    @RpcConsumer
    private BookWriteService bookWriteService;

    @RpcConsumer
    private BookReadService bookReadService;

    /**
     * 鏌ヨ
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Book findBook(@PathVariable Long id) {
        Response<Book> response =  bookReadService.findById(id);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
     * 鍒嗛〉
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/paging", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Paging<Book> pagingBook(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                 @RequestParam(value = "pageSize", required = false) Integer pageSize) {

        Map<String, Object> criteria = Maps.newHashMap();

        Response<Paging<Book>> result =  bookReadService.paging(pageNo, pageSize, criteria);
        if(!result.isSuccess()){
            throw new JsonResponseException(result.getError());
        }
        return result.getResult();
    }

    /**
     * 鍒涘缓
     * @param book
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createBook(@RequestBody Book book) {
        Response<Long> response = bookWriteService.create(book);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
     * 鏇存柊
     * @param book
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean updateBook(@RequestBody Book book) {
        Response<Boolean> response = bookWriteService.update(book);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
    * 鍒犻櫎
    * @param id
    * @return
    */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean deleteBook(@PathVariable Long id) {
        Response<Boolean> response = bookWriteService.delete(id);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

   /**
    * 鍒楄〃
    * @return
    */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Book> listBook() {
       Map<String, Object> criteria = Maps.newHashMap();
       Response<List<Book>> result =  bookReadService.list(criteria);
       if(!result.isSuccess()){
           throw new JsonResponseException(result.getError());
       }
       return result.getResult();
    }
}