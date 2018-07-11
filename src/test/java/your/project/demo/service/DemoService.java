package your.project.demo.service;

import your.project.demo.common.BasicService;
import your.project.demo.common.DateTimeUtil;
import your.project.demo.dao.BookDao;
import your.project.demo.dao.BookEditorDao;
import your.project.demo.dao.EditorDao;
import your.project.demo.vo.Book;
import your.project.demo.vo.BookEditor;
import your.project.demo.vo.Editor;
import com.github.walker.mybatis.paginator.OffsetLimitInterceptor;
import com.github.walker.mybatis.paginator.Order;
import com.github.walker.mybatis.paginator.PageBounds;
import com.github.walker.mybatis.paginator.PageList;
import org.apache.ibatis.session.SqlSession;

import java.util.*;

/**
 * @author HuQingmiao
 */
public class DemoService extends BasicService {

    /**
     * 清空测试数据，以备测试
     */
    public void clearTestData() {
        log.info("clearTestData() >>");

        SqlSession sqlSession = sessionFactory.openSession(false);
        try {
            BookDao bookDao = sqlSession.getMapper(BookDao.class);

            int cnt = bookDao.deleteAll();
            log.info(">>cnt: " + cnt);

            sqlSession.commit(true);
        } finally {
            sqlSession.close();
        }
    }


    public void deleteBooks() {
        log.info("deleteBooks() >>");

        SqlSession sqlSession = sessionFactory.openSession(false);
        try {
            BookDao bookDao = sqlSession.getMapper(BookDao.class);

            List<Long> idSets = new ArrayList<Long>();
            idSets.add(new Long(101));
            idSets.add(new Long(103));

            int cnt = bookDao.deleteByIds(idSets);
            log.info(">>cnt: " + cnt);

            sqlSession.commit(true);
        } finally {
            sqlSession.close();
        }
    }

    public void findBooks() {
        log.info("findBooks() >>");

        SqlSession sqlSession = sessionFactory.openSession(false);
        try {
            BookDao bookDao = sqlSession.getMapper(BookDao.class);

            HashMap<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("minCost", new Float(0));
            paramMap.put("maxCost", new Float(100));
            List<Book> bookList = bookDao.find(paramMap, new PageBounds(1, 6));

            for (Book book : bookList) {
                log.info(book.getBookId() + " " + book.getTitle() + " " + book.getPrice());
            }
            log.info("");

            sqlSession.commit(true);
        } finally {
            sqlSession.close();
        }
    }


    public void findABook()  {
        log.info("findABook() >>");

        SqlSession sqlSession = sessionFactory.openSession(false);
        try {
            BookDao bookDao = sqlSession.getMapper(BookDao.class);

            Book book = (Book) bookDao.findByPK(new Long(10002));
            if (book != null) {
                log.info(book.getBookId() + " " + book.getTitle() + " " + book.getPrice());
            } else {
                log.info("Not found!");
            }

            sqlSession.commit(true);
        } finally {
            sqlSession.close();
        }
    }

    /**
     * 增加一本书;
     */
    public void addOneBook() {
        log.info("addOneBook() >>");

        SqlSession sqlSession = sessionFactory.openSession(false);
        try {
            BookDao bookDao = sqlSession.getMapper(BookDao.class);

            Book book = new Book();
            book.setBookId(new Long(10002));
            book.setTitle(new String("三国演义"));
            book.setPrice(new Double(30.0));
            book.setPublishTime(DateTimeUtil.toSqlTimestamp(new Date()));

            // 开放此代码, 试图把一个不存在的文件写入数据库, 将抛出异常
            // book.setTextContent(new ETxtFile("d:\\三国.txt"));

            // 开放此代码, 试图向不存在与此属性对应的列写数据, EasyDB会对其忽略
            // book.setANotExistCol(new EString("asdff"));

            int cnt = bookDao.save(book);
            log.info(">>cnt: " + cnt);

            sqlSession.commit(true);
        } finally {
            sqlSession.close();
        }
    }


    /**
     * 批量增加多本书;
     */
    public void addMultiBooks()  {
        log.info("addMultiBooks() >>");

        SqlSession sqlSession = sessionFactory.openSession(false);
        try {
            BookDao bookDao = sqlSession.getMapper(BookDao.class);

            int size = 3;
            Book[] BookArray = new Book[size];

            BookArray[0] = new Book();
            BookArray[0].setBookId(new Long(101));
            BookArray[0].setTitle(new String("UNIX-上册"));
            BookArray[0].setPrice(new Double(100.0f));

            BookArray[1] = new Book();
            BookArray[1].setBookId(new Long(105));
            BookArray[1].setTitle(new String("UNIX-中册"));
            BookArray[1].setPrice(new Double(52.0f));

            BookArray[2] = new Book();
            BookArray[2].setBookId(new Long(103));
            BookArray[2].setTitle(new String("UNIX-下册"));
            BookArray[2].setPrice(new Double(35.0f));
            BookArray[2].setPublishTime(DateTimeUtil.toSqlTimestamp(new Date()));

            int cnt = bookDao.saveBatch(Arrays.asList(BookArray));
            log.info(">>cnt: " + cnt);

            sqlSession.commit(true);
        } finally {
            sqlSession.close();
        }
    }


    /**
     * 根据主键更新某本书
     */
    public void updateOneBook() {
        log.info("updateOneBook() >>");

        SqlSession sqlSession = sessionFactory.openSession(false);
        try {
            BookDao bookDao = sqlSession.getMapper(BookDao.class);

            Book book = new Book();
            book.setTitle(new String("八国演义(第二版)"));
            book.setBookId(new Long(10002));
            book.setPrice(new Double(60));

            int cnt = bookDao.update(book);
            log.info(">>cnt: " + cnt);

            sqlSession.commit(true);
        } finally {
            sqlSession.close();
        }
    }


    /**
     * 对部分图书设定折扣价
     */
    public void updateMultiBooks()  {
        log.info("updateMultiBooks() >>");

        SqlSession sqlSession = sessionFactory.openSession(false);
        try {
            BookDao bookDao = sqlSession.getMapper(BookDao.class);

            //找出50元以上的书，然后打6折
            HashMap<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("minCost", new Float(50));
            List<Book> bookList = bookDao.find(paramMap, new PageBounds());

            for (Book book : bookList) {
                book.setPrice(new Double(33.0));
            }
            bookDao.updateBatch(bookList);
            sqlSession.commit();

            for (Book book : bookList) {
                log.info(book.getBookId() + " " + book.getTitle() + " " + book.getPrice());
            }
            log.info("");

            sqlSession.commit(true);
        } finally {
            sqlSession.close();
        }
    }


    public void deleteOneBook() {
        log.info("deleteOneBook() >>");

        SqlSession sqlSession = sessionFactory.openSession(false);
        try {
            BookDao bookDao = sqlSession.getMapper(BookDao.class);

            int cnt = bookDao.deleteByPK(new Long(101));
            log.info(">>cnt: " + cnt);

            sqlSession.commit(true);
        } finally {
            sqlSession.close();
        }
    }

    /**
     * 批量删除某部分书
     */
    public void deleteMultiBooks()  {
        log.info("deleteMultiBooks() >>");

        SqlSession sqlSession = sessionFactory.openSession(false);
        try {
            BookDao bookDao = sqlSession.getMapper(BookDao.class);

            //找出100元以下的书，然后打6折
            HashMap<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("maxCost", new Float(100));
            List<Book> bookList = bookDao.find(paramMap, new PageBounds());

            int cnt = bookDao.deleteBatch(bookList);
            log.info("cnt: "+cnt);

            sqlSession.commit(true);
        } finally {
            sqlSession.close();
        }
    }


    public void addEditors() {
        log.info("addEditors() >>");

        SqlSession sqlSession = sessionFactory.openSession(false);
        try {
            EditorDao editorDao = sqlSession.getMapper(EditorDao.class);
            BookEditorDao bookEditorDao = sqlSession.getMapper(BookEditorDao.class);

            int cnt = editorDao.deleteAll();
            log.info(">>cnt: " + cnt);

            cnt = bookEditorDao.deleteAll();
            log.info(">>cnt: " + cnt);

            List<Editor> editorList = new ArrayList<Editor>();
            Editor editor1 = new Editor();
            editor1.setEditorId(new Long(22));
            editor1.setName("徐静蕾");
            editor1.setSex("F");
            editorList.add(editor1);

            Editor editor2 = new Editor();
            editor2.setEditorId(new Long(33));
            editor2.setName("张德芬");
            editor2.setSex("F");
            editorList.add(editor2);

            editorDao.saveBatch(editorList);


            List<BookEditor> bookEditorList = new ArrayList<BookEditor>();
            BookEditor be1 = new BookEditor();
            be1.setBookId(new Long(101));
            be1.setEditorId(new Long(22));
            bookEditorList.add(be1);

            BookEditor be2 = new BookEditor();
            be2.setBookId(new Long(103));
            be2.setEditorId(new Long(22));
            bookEditorList.add(be2);

            BookEditor be3 = new BookEditor();
            be3.setBookId(new Long(10002));
            be3.setEditorId(new Long(33));
            bookEditorList.add(be3);

            bookEditorDao.saveBatch(bookEditorList);
            log.info("");

            sqlSession.commit(true);
        } finally {
            sqlSession.close();
        }
    }

    /*
     * 关联表分页查询
     */
    public void findEditorAndBook(){
        log.info("findEditorAndBook() >>");

        SqlSession sqlSession = sessionFactory.openSession(false);
        try {
            BookDao bookDao = sqlSession.getMapper(BookDao.class);

            HashMap<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("editorName", "徐静蕾");

            PageBounds pageBounds = new PageBounds(0, 2);
            List<Book> bookList = bookDao.findEditorAndBooks(paramMap, pageBounds);

            for (Book book : bookList) {
                log.info(book.getEditorName() + "(" + book.getEditorSex() + "): " + book.getBookId() + " " + book.getTitle() + " " + book.getPrice());
            }
            log.info("");

            sqlSession.commit(true);
        } finally {
            sqlSession.close();
        }
    }


    public void findBooksForPager() {
        log.info("findBooksForPager() >>");

        SqlSession sqlSession = sessionFactory.openSession(false);
        try {
            BookDao bookDao = sqlSession.getMapper(BookDao.class);

            HashMap<String, Object> paramMap = new HashMap<String, Object>();
          //  paramMap.put("title", "%UNIX%");


//		new PageBounds();				//默认构造函数不提供分页，返回ArrayList
//		new PageBounds(int limit);		//取TOP N条记录，返回ArrayList
//		new PageBounds(Order... order);	//只排序不分页，返回ArrayList
//
//		new PageBounds(int page, int limit);//默认分页，返回PageList
//		new PageBounds(int page, int limit, Order... order);//分页加排序，返回PageList

            int offset = 0; //起始行号
            int count = 2;  //获取条数
            String sortString = "title.asc, price.desc";//如果想排序的话,以逗号分隔多项排 序列
            PageBounds pageBounds = new PageBounds(offset, count, Order.formString(sortString));
            List<Book> bookList = bookDao.find(paramMap, pageBounds);


            PageList<Book> pageList = (PageList<Book>) bookList;//获得结果集条总数
            log.info("totalCount: " + pageList.getTotalCount());

            for (Book book : bookList) {
                log.info(book.getBookId() + " " + book.getTitle() + " " + book.getPrice());
            }
            log.info("");

            sqlSession.commit(true);
        } finally {
            sqlSession.close();
        }
    }

}