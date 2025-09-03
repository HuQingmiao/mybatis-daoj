
import cn.com.walker.mybatis.paginator.PageBounds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * mybatis DAO基类
 * <p/>
 * @Author HuQingmiao
 */
public interface BasicDao {

    int save(BasicPo basicPo);

    int saveBatch(List list);


    int update(BasicPo basicPo);

    int updateIgnoreNull(BasicPo basicPo);

    int updateBatch(List list);


    int delete(BasicPo basicPo);

    int deleteBatch(List list);

    int deleteByPK(Long id);

    int deleteByPKs(Collection<Long> ids);

    int deleteAll();


    long count();

    BasicPo findByPK(Long id);

    ArrayList findByPKs(Collection<Long> ids);

    ArrayList find(Map<String, Object> paramMap, PageBounds pageBounds);
}
