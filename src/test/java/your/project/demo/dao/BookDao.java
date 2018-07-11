package your.project.demo.dao;

import your.project.demo.common.BasicDao;
import your.project.demo.vo.Book;
import com.github.walker.mybatis.paginator.PageBounds;

import java.util.List;
import java.util.Map;

public interface BookDao extends BasicDao {

    int deleteByIds(List<Long> ids);

    /**
     * 根据作者名称和标题查找该作者及其相应著作
     */
    List<Book> findEditorAndBooks(Map<String, Object> param, PageBounds pageBounds);
}
