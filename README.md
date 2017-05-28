### 项目说明
&nbsp;&nbsp;&nbsp;&nbsp;Mybatis-daoj，是基于我主张的myBatis的极简用法，而编写的DAO层代码生成器。相比其它代码生成器不一样，其优点如下：

 - 对DAO接口进行了高度抽象，把增、删、查、改的操作抽象成11个标准的接口方法，使得调用它的业务层代码简洁一体。
 - 生成的代码非常简洁，仅包括vo实体类, dao接口, mapper.xml。
 - 生成的代码可以适应90%以上的场景；对于另外10%的个性场景，你可以通过扩展dao接口，增加新的方法实现。
 
### 联系我
> 个人博客：[http://my.oschina.net/HuQingmiao](http://my.oschina.net/HuQingmiao)；
> QQ：443770574

### 使用说明
&nbsp;&nbsp;&nbsp;1. 下载mybatis-daoj.tar.gz (链接: http://pan.baidu.com/s/1bpzUqfH )，解压后打开 conf/mybatis-daoj.xml，配置数据库连接，设置表名，以及生成代码的输出目录。

&nbsp;&nbsp;&nbsp;2. 运行 bin/start.bat, 将生成相应代码到设定的输出目录。 对于每个表，都将生成对应的1个vo实体类、1个dao接口类、1个mapper.xml; 将这些文件复制到你工程的对应目录。

&nbsp;&nbsp;&nbsp;3. 要让生成的代码运行起来，还需要在mybatis.xml中增加如下配置:
``` 
    <typeAliases>
         <!-- 为vo包下的所有类自动定义别名-->
         <package name="xx.xx.xx.vo"/>
    </typeAliases>
``` 
&nbsp;&nbsp;&nbsp;或者在spring配置文件中像下面这样增加vo类的别名配置 (如果你的项目用了spring的话)：

```
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource"/>

        <!-- 为其下的包定义别名 -->
        <property name="typeAliasesPackage" value="xx.xx.vo"/>

        <property name="configLocation" value="classpath:mybatis.xml"/>
        <property name="mapperLocations"value="classpath*:xx/xx/dao/*.xml"/>
    </bean>
```

&nbsp;&nbsp;&nbsp;4. 注意事项:
* 本程序生成的dao类没有任何接口方法，只是继承"BasicDao.java"；但你可以在子接口中扩展你的个性方法。
* 如果你没有使用数据库的自增主键特性，则在生成mapper.xml文件后，必须删除INSERT部分的：`useGeneratedKeys="true" keyProperty="xx"` 。
    


### 开发环境
     * Java 7
     * Maven 3.2.5+
     * IntelliJ IDEA
