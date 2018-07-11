### 项目说明
&nbsp;&nbsp;&nbsp;&nbsp;Mybatis-daoj，是基于我主张的myBatis的极简用法，而编写的DAO层代码生成器。相比其它代码生成器不一样，其优点如下：

 - 对DAO接口进行了高度抽象，把增、删、查、改的操作抽象成11个标准的接口方法，使得调用它的业务层代码简洁一体。
 - 生成的代码非常简洁，仅包括vo实体类, dao接口, mapper.xml。
 - 生成的代码可以适应90%以上的场景；对于另外10%的个性场景，你可以通过扩展dao接口，增加新的方法实现。


### 联系我
> 个人博客：[http://my.oschina.net/HuQingmiao](http://my.oschina.net/HuQingmiao)；
> QQ：443770574


### 使用说明
&nbsp;&nbsp;&nbsp;1. 下载源码，编译、打包，得到 mybatis-daoj.tar.gz 。

&nbsp;&nbsp;&nbsp;2. 解压 mybatis-daoj.tar.gz， 打开 conf/mybatis-daoj.xml，配置数据库连接、表名、输出目录。

&nbsp;&nbsp;&nbsp;3. 运行 bin/start.bat， 本工具会为每个表生成1个vo实体类、1个dao接口类、1个mapper.xml。

&nbsp;&nbsp;&nbsp;4. 将生成的代码复制到你的工程目录。注意:
* 本程序生成的dao类没有任何接口方法，只是继承"BasicDao.java"；但你可以在子接口中扩展你的个性方法。
* 如果你没有使用数据库的自增主键特性，则在生成mapper.xml文件后，必须删除INSERT部分的：`useGeneratedKeys="true" keyProperty="xx"` 。
    

### 开发环境
     * Java 7
     * Maven 3.2.5+
     * IntelliJ IDEA
