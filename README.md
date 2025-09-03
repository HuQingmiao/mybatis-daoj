
### 项目说明

Mybatis-daoj 是一款dao层的代码生成工具, 还能生成表结构文档。主要特性：

1. 对数据库CURD抽象出14个标准的dao方法； 这些方法已经满足90%以上的使用场景，对于其它的10%的场景，也可通过扩展dao接口来实现。

2. 对于存在row_version列的表，生成的sql会自动支持乐观锁并发更新。

3. 可生成完整的表结构文档，包括每张表的表名、主键、唯一约束、注释。

5. 支持mysql、oarcle、tdsql、oceanbase、vertica、达梦等多种数据库。


### 运行环境
JAVA 8+


### 使用用法
1. 在你的项目pom中引入：
   ```
    <dependency>
        <groupId>walker</groupId>
        <artifactId>mybatis-daoj</artifactId>
        <version>2.1.0</version>
    </dependency>
   ```

2. 将mybatis-daoj.xml 复制到你的项目 resources/ 下, 配置数据库连接、表名、输出目录。<p/>
   
3. 编写main()方法，调用如下代码，即可生成代码和文档： 

   ```
    // 生成dao层代码, 即：为每张表生成1个po实体类、1个dao接口类、1个mapper.xml
    new Generator("mybatis-daoj.xml").generateCode();

    // 生成表结构文档， 即：为配置的所有表生成一份表结构文档
    new Generator("mybatis-daoj.xml").generateDoc();
   ```   

4. 将生成的代码复制到你的工程目录，确保包名正确。接下来就可以在业务层代码中调用dao接口了。<p/>


### 更新记录
* 2021-09-xx, 增加特性：在po类中为每个字段生成注释; 
* 2021-09-xx, 增加对达梦数据库的支持，并通过测试。
* 2021-10-13, 增加特性：针对有row_version字段的表，会在所有update相关语句中增加`set row_version = row_version + 1 `
的逻辑，并且会在dao类中多生成一个方法： `int updateWithOptiLock(BasicPo basicPo); // 采用row_version实现并发更新 `


* 2022-02-12, 增加对oceanbase的支持。

* 2022-07-28, 1)增加特性: 对oracle表生成的save方法，将自增的主键值自动注入主键属性。
              2)修改bug: oracle生成的saveBatch方法，在未定义select 列别名但列值又重复的情况下，在执行该SQL时会报异常。
            
             