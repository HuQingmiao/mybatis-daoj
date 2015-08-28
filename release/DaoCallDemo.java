package com.mucfc.act;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;
import com.mucfc.act.dao.BookDao;
import com.mucfc.act.po.Book;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring.xml"})
@TransactionConfiguration(defaultRollback = false)
public class DaoCallDemo {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BookDao bookDao;

    @Test
    @Transactional
    @Rollback(false)
    // 设为true, 表示测试执行后会回滚事务, 测试方法的执将不会对数据库中的数据产生影响
    public void testSave() {
        log.info("\ntestSave()>>>>>>>>>>>>>>>>>>>>");

        Book book = new Book();
        book = new Book();
        book.setBookId(new Long(1));
        book.setTitle(new String("三国演义"));
        book.setCost(new Double(30.0f));
        book.setUpdateTime(new java.sql.Timestamp(new Date().getTime()));
        bookDao.save(book);
    }

    @Test
    @Transactional
    @Rollback(false)
    // 设为true, 表示测试执行后会回滚事务, 测试方法的执将不会对数据库中的数据产生影响
    public void testSaveBatch() {
        log.info("\ntestSaveBatch()>>>>>>>>>>>>>>>>>>>>");

        int size = 6;
        Book[] BookArray = new Book[size];

        BookArray[0] = new Book();
        BookArray[0].setBookId(new Long(2));
        BookArray[0].setTitle(new String("UNIX-上册"));
        BookArray[0].setCost(new Double(100.0f));

        BookArray[1] = new Book();
        BookArray[1].setBookId(new Long(3));
        BookArray[1].setTitle(new String("UNIX-中册"));
        BookArray[1].setCost(new Double(32.0));

        BookArray[2] = new Book();
        BookArray[2].setBookId(new Long(4));
        BookArray[2].setTitle(new String("UNIX-下册"));
        BookArray[2].setCost(new Double(35.0f));
        BookArray[2].setUpdateTime(new java.sql.Timestamp(new Date().getTime()));

        BookArray[3] = new Book();
        BookArray[3].setBookId(new Long(5));
        BookArray[3].setTitle(new String("UNIX-下册"));
        BookArray[3].setCost(new Double(35.0f));
        BookArray[3].setUpdateTime(new java.sql.Timestamp(new Date().getTime()));

        BookArray[4] = new Book();
        BookArray[4].setBookId(new Long(6));
        BookArray[4].setTitle(new String("UNIX-下册"));
        BookArray[4].setCost(new Double(35.0f));
        BookArray[4].setUpdateTime(new java.sql.Timestamp(new Date().getTime()));

        BookArray[5] = new Book();
        BookArray[5].setBookId(new Long(7));
        BookArray[5].setTitle(new String("UNIssssssssssssssssssX-下册"));
        BookArray[5].setCost(new Double(90.0f));
        BookArray[5].setUpdateTime(new java.sql.Timestamp(new Date().getTime()));

        bookDao.saveBatch(Arrays.asList(BookArray));
    }

    @Test
    @Transactional
    @Rollback(false)
    // 设为true, 表示测试执行后会回滚事务, 测试方法的执将不会对数据库中的数据产生影响
    public void testUpdate() {
        log.info("\ntestUpdate()>>>>>>>>>>>>>>>>>>>>");

        Map<String,Object> paramMap = new HashMap<String, Object>();
        paramMap.put("bookId",new Long(6));

        Book book = (Book)bookDao.findByPK(paramMap);
        book.setBookId(new Long(4));
        bookDao.update(book);
    }

    @Test
    @Transactional
    @Rollback(false)
    // 设为true, 表示测试执行后会回滚事务, 测试方法的执将不会对数据库中的数据产生影响
    public void testUpdateBatch() {
        log.info("\nupdateMultiBooks>>>>>>>>>>>>>>>>>>>>");

        // 找出50元以上的书，然后打6折
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("cost", new Float(36));
        //  paramMap.put("maxCost", new Float(50));
        List<Book> bookList = bookDao.find(paramMap, new PageBounds());

        for (Book book : bookList) {
            book.setCost(new Double(book.getCost() * 0.9));
            bookDao.update(book);
        }

        for (Book book : bookList) {
            log.info(book.getBookId() + " " + book.getTitle() + " " + book.getCost());
        }
        log.info("\n");
    }

    @Test
    @Transactional
    @Rollback(false)
    // 设为true, 表示测试执行后会回滚事务, 测试方法的执将不会对数据库中的数据产生影响
    public void testDelete() {
        log.info("\ntestDelete()>>>>>>>>>>>>>>>>>>>>");
        Map paramMap = new HashMap();
        paramMap.put("title", "UNIssssssssssssssssssX-下册");
        paramMap.put("bookId", new Long(7));
        Book book = (Book) bookDao.findByPK(paramMap);
        bookDao.delete(book);
    }

    @Test
    @Transactional
    @Rollback(false)
    // 设为true, 表示测试执行后会回滚事务, 测试方法的执将不会对数据库中的数据产生影响
    public void testDeleteBatch() {
        log.info("\ntestDeleteBatch()>>>>>>>>>>>>>>>>>>>>");

        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("cost", new Float(36));
        List<Book> bookList = bookDao.find(paramMap, new PageBounds());

        if (!bookList.isEmpty()) {
            bookDao.deleteBatch(bookList);
        }
    }

    @Test
    @Transactional
    @Rollback(false)
    // 设为true, 表示测试执行后会回滚事务, 测试方法的执将不会对数据库中的数据产生影响
    public void testDeleteByPK() {
        log.info("\ntestDeleteByPK()>>>>>>>>>>>>>>>>>>>>");

        Map paramMap = new HashMap();
        paramMap.put("title", "UNIssssssssssssssssssX-下册");
        paramMap.put("bookId", new Long(7));
        bookDao.deleteByPK(paramMap);

        paramMap.put("title", "UNIX-下册");
        paramMap.put("bookId", new Long(6));
        bookDao.deleteByPK(paramMap);
    }


    @Test
    @Transactional
    @Rollback(false)
    // 设为true, 表示测试执行后会回滚事务, 测试方法的执将不会对数据库中的数据产生影响
    public void testFind() {
        log.info("\ntestFind()>>>>>>>>>>>>>>>>>>>>");

        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        //paramMap.put("title", "%UNIX%");
        //paramMap.put("minCost", new Float(20));
        //paramMap.put("maxCost", new Float(101));

        // new PageBounds(); //默认构造函数不提供分页，返回ArrayList
        // new PageBounds(int limit); //取TOP N条记录，返回ArrayList
        // new PageBounds(Order... order); //只排序不分页，返回ArrayList
        //String sortString = "title.asc, cost.desc";// 如果想排序的话,以逗号分隔多项排 序列
        //Order.formString(sortString)
        //
        // new PageBounds(int page, int limit);//默认分页，返回PageList
        // new PageBounds(int page, int limit, Order...order);//分页加排序，返回PageList

        int pageIndex = 1; // 页码
        int rowcntPerPage = 2; // 每页的数据条数

        PageBounds pageBounds = new PageBounds(pageIndex, rowcntPerPage);
        List<Book> bookList = bookDao.find(paramMap, pageBounds);
        //List<Book> bookList = bookDao.find(paramMap, new PageBounds());//不分页
        log.info("currCount: " + bookList.size());

        PageList<Book> pageList = (PageList<Book>) bookList;// 获得结果集条总数
        log.info("totalCount: " + pageList.getPaginator().getTotalCount());

        for (Book book : bookList) {
            log.info(book.getBookId() + " " + book.getTitle() + " " + book.getCost());
        }
        log.info("\n");
    }
}