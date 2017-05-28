package your.project.dao;  //改为你工程的相应package

import your.project.vo.BasicVo;
import com.github.walker.mybatis.paginator.PageBounds;

import java.util.ArrayList;
import java.util.Map;

/**
 * mybatis DAO基类
 * <p/>
 * Created by HuQingmiao on 2015-5-29.
 */
public interface BasicDao {

    int save(BasicVo basicVo);

    int saveBatch(List list);


    int update(BasicVo basicVo);

    int updateIgnoreNull(BasicVo basicVo);

    int updateBatch(List list);


    int delete(BasicVo basicVo);

    int deleteBatch(List list);

    int deleteByPK(Long id);

    int deleteAll();


    public long count();

    public BasicVo findByPK(Long id);

    public ArrayList find(Map<String, Object> paramMap, PageBounds pageBounds);
}
