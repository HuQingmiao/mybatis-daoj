package your.project.daoj;  //改为你工程的相应package

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * VO 基类
 * <p/>
 * Created by HuQingmiao on 2015-5-13.
 */
public abstract class BasicVo implements Serializable {

    private static Logger log = LoggerFactory.getLogger(BasicVo.class);

    //存放当前实体类的属性及类型
    private HashMap<String, Class<?>> fieldNameTypeMap = new HashMap<String, Class<?>>(10);


    @Override
    public String toString(){
        return ReflectionToStringBuilder.toString(this);
    }

}
