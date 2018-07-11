package your.project.demo.app;


import your.project.demo.service.DemoService;
import com.github.walker.mybatis.paginator.OffsetLimitInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.Test;

/**
 * Created by huqingmiao on 2017/3/7.
 */


public class DemoTest {
    protected Logger log = LoggerFactory.getLogger(getClass());


    @Test
    public void test() {
        try {
            DemoService t = new DemoService();

            t.clearTestData();
            t.addOneBook();


            t.addMultiBooks();

            t.updateOneBook();

            t.findBooks();

            t.updateMultiBooks();

           t.deleteOneBook();

            t.deleteMultiBooks();

            t.findABook();

            t.addEditors();

            t.findEditorAndBook();

            t.deleteBooks();

            t.findBooksForPager();

            System.out.println("<<< END");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放分页查询的线程池资源
            OffsetLimitInterceptor.release();
        }
    }

}
