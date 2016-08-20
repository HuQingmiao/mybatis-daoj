## 联系我
> 个人博客：[http://my.oschina.net/HuQingmiao](http://my.oschina.net/HuQingmiao)；
> QQ：443770574


## mybatis-daoj
&nbsp;&nbsp;&nbsp;&nbsp;mybatis-daoj，是我基于myBatis极简用法的思路，而编写的DAO层代码生成器。关于myBatis的极简用法，请阅读博文：http://my.oschina.net/HuQingmiao/blog/636161 。

<p/>
&nbsp;&nbsp;&nbsp;&nbsp;mybatis-daoj与其它代码生成器不一样，优点如下：
* 对DAO接口进行了高度抽象，把增、删、查、改的操作抽象成11个标准的接口方法，使得调用它的业务层代码简洁一体。
* 生成的代码非常简洁，仅包括vo实体类, dao接口, mapper.xml。
* 生成的代码可以适应90%以上的场景；对于另外10%的个性场景，你可以通过扩展dao接口，增加新的方法实现。


## 使用说明
&nbsp;&nbsp;&nbsp;1.先下载mybatis-daoj.zip(链接: https://pan.baidu.com/s/1dFdW3SX )，解压后打开mybatis-daoj.xml，配置数据库连接，设置实体类、DAO接口类的包名，以及生成代码的输出目录。

&nbsp;&nbsp;&nbsp;2.双击gen.bat 或执行:java -jar mybatis-daoj-xxx.jar 以运行本程序。本程序将生成三种文件：vo实体类,dao接口类,mapper.xml，
      将这些文件复制到你工程的对应目录。

&nbsp;&nbsp;&nbsp;3.要让生成的代码运行起来，还需要在mybatis.xml中增加如下配置(也可通过在spring.xml为vo指定别名):
``` 
      <typeAliases>
           <!-- 为vo包下的所有类自动定义别名, 因为生成的mapper.xml中的"resultType" 指定的是vo实体类的别名-->
           <package name="xx.xx.xx.vo"/>
      </typeAliases>
``` 

&nbsp;&nbsp;&nbsp;4.注意事项:
* 本程序生成的dao类没有任何接口方法，只是继承"BasicDao.java"；但你可以在子接口中扩展你的个性方法。
* 如果你没有使用数据库的自增主键特性，则在生成mapper.xml文件后，必须删除INSERT部分的：
                                                           'useGeneratedKeys="true" keyProperty="xx"' 。


## 开发&编译
     * Windows or Linux
     * Java 7
     * Maven 3.0.5+
     * IntelliJ IDEA / Eclipse


