package io.terminus.doctor.basic.dao;

import io.terminus.common.model.Paging;
import io.terminus.doctor.basic.model.Book;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Desc:
* Mail: [ your email ]
* Date: 2018-04-13 10:17:08
* Created by [ your name ]
*/
public class BookDaoTest extends BaseDaoTest {
    private Book book;

    @Autowired
    private BookDao bookDao;

    @Before
    public void setUp() {
        //book.setbookName();
        //book.setbookAuth();
        //book.setbookType();
        //book.setbookPrice();
        //book.setbookTime();
        bookDao.create(book);
    }

    @Test
    public void testCreate() {
        Book actual = bookDao.findById(book.getId());
        Assert.assertNotNull(actual.getId());
    }

    @Test
    public void testDelete() {
        bookDao.delete(book.getId());
        Book actual = bookDao.findById(book.getId());
        Assert.assertNull(actual);
    }


    @Test
    public void testFindById() {
        Book actual = bookDao.findById(book.getId());
        Assert.assertNotNull(actual);
    }

    @Test
    public void testUpdate() {
        Book actual1 = bookDao.findById(book.getId());
        Assert.assertNotNull(actual1);
        //actual1.setbookName();
        //actual1.setbookAuth();
        //actual1.setbookType();
        //actual1.setbookPrice();
        //actual1.setbookTime();
        bookDao.update(actual1);
        Book actual2 = bookDao.findById(book.getId());
        //Assert.assertEquals(actual2.getbookName(), null);
        //Assert.assertEquals(actual2.getbookAuth(), null);
        //Assert.assertEquals(actual2.getbookType(), null);
        //Assert.assertEquals(actual2.getbookPrice(), null);
        //Assert.assertEquals(actual2.getbookTime(), null);
        
    }

    @Test
    public void testList() {
        Map<String, Object> params = new HashMap<>();
        List<Book> actual = bookDao.list(params);
        Assert.assertNotNull(actual.get(0));
    }

    @Test
    public void testPaging() {
        Paging<Book> paging = bookDao.paging(0, 20, book);
        Assert.assertTrue(!paging.getData().isEmpty());
    }
}
