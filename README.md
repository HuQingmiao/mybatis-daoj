### 项目说明

   Mybatis-paginator 是一款支持mysql、oracle、dm、vertica、oceanbase等数据库的分页插件。 主要特性：
1. 可以查询任意起止行范围的记录。
2. 可以按任意列排序而不用修改sql。 
3. 支持mysql、oracle、dm、vertica、oceanbase、tdsql、达梦等多种数据库。


### 运行环境
 当前版本在JAVA21上编译。

### 使用说明

1.在你的项目pom中引入：
```
    <dependency>
        <groupId>com.github.walker</groupId>    
        <artifactId>mybatis-paginator</artifactId>
        <version>${version}</version>
    </dependency> 
```

2.打开你工程中的mybatis.xml，添加如下配置:

```
    <plugins>
        <plugin interceptor="walker.mybatis.paginator.OffsetLimitInterceptor">
            <property name="dialectClass" value="dialect.walker.mybatis.paginator.MySQLDialect"/>
        </plugin>
    </plugins>
```

3.你的程序可以这样调用分页接口：

```
     // 查询条件: 书名以 “UNIX” 开头
     HashMap<String, Object> paramMap = new HashMap<String, Object>();
     paramMap.put("title", "UNIX%");

     // 排序规则: 按书名正序、时间倒序
     String sortString = "title.asc, createTime.desc";

     // 查询，获得符合条件的第 3~13行
     PageBounds pageBounds = new PageBounds(3, 10, Order.formString(sortString));
     ArrayList<Book> rsList = bookDao.findBooks(paramMap, pageBounds);  
       
     // 打印结果  
     PageList<Book> pageList = (PageList<Book>) rsList;
     log.info("符合条件的总的记录数: " + pageList.size()); 
     log.info("本页记录数: " + rsList.size());
   

  另外，分页参数PageBounds还有更多丰富的功能：   
     new PageBounds();                                 // 采用默认构造函数，就相当于非分页方式
     new PageBounds(Order.formString(sortString));     // 按书名正序、时间倒序排列，获取符合条件的所有记录
     new PageBounds(10, Order.formString(sortString)); // 按书名正序、时间倒序排列, 获取符合条件的前10条记录

```
