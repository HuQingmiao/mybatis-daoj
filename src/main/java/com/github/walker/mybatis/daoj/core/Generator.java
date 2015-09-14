package com.github.walker.mybatis.daoj.core;


import com.github.walker.mybatis.daoj.utils.MappingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 程序名称：mybatis-daoj，即依赖mybatis的DAO层代码生成器。
 *
 * @author HuQingmiao
 *         <p/>
 *         <p>
 *         <pre>
 *
 *                 一. 使用说明:
 *
 *                 1. 打开mybatis-daoj.xml，配置数据库连接，设置实体类、DAO接口类所有的包、以及生成代码的输出目录。
 *
 *                 2. 双击gen.bat 或执行:java -jar mybatis-daoj-xxx.jar 以运行本程序。本程序将生成三种文件：PO实体类、DAO接口类、Mapper.xml，
 *                      将这些文件复制到你工程的对应目录。
 *
 *                 3. 要让生成的代码运行起来，还需要在mybatis.xml中增加如下配置:
 *                      因为生成的Mapper.xml中的"resultType" 指定的是PO实体类的别名，需要在mybatis.xml中通过以下配置定义别名：
 *                      <typeAliases>
 *                           <package name="com.mucfc.act.po"/><!-- 为po包下的所有类自动定义别名-->
 *                      </typeAliases>
 *
 *                 4. 要在mybatis.xml中增加分页插件，添加如下配置即可:
 *                      <plugins>
 *                          <!-- 分页查询拦截器 -->
 *                          <plugin interceptor="com.github.walker.mybatis.paginator.OffsetLimitInterceptor">
 *                      </plugins>
 *
 *                     并且，你的工程也要引入相应jar包:
 *                          <dependency><!-- mybatis分页控件 -->
 *                              <groupId>walker</groupId>
 *                              <artifactId>mybatis-paginator</artifactId>
 *                              <version>20150827</version>
 *                          </dependency>
 *
 *                 5. 现在你的Service代码就可以直接调用DAO层了，可参考：DaoCallDemo.java。
 *
 *                 二. 使用建议:
 *                 1. 本程序生成的DAO类没有任何接口方法，只是继承"BasicDao.java"；但可在此类中增加你的特性方法，比如复杂条件的查询。
 *                 2. 如果你没有使用数据库的自增主键特性，则在生成mapper.xml文件后，必须删除INSERT部分的'useGeneratedKeys="true" keyProperty="xx"'才可运行。
 *
 *                 </pre>
 *         </p>
 */
public class Generator {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        new Generator().generator();
    }


    private void generator() {
        //BasicEntity类名
        String basicEntity = ConfigLoader.getBasicEntity();

        //BasicDao类名
        String basicDao = ConfigLoader.getBasicDao();

        //生成的实体类所在的包
        String entityPackage = ConfigLoader.getEntityPackage();

        //生成的DAO类所在的包
        String daoPackage = ConfigLoader.getDaoPackage();

        //代码生成的输出目录
        String outputDircName = ConfigLoader.getOutputDirc();

        //列出要生成的表名
        String[] tables = ConfigLoader.getTables();

        File outputDirc = new File(outputDircName);
        if (!outputDirc.exists()) {
            outputDirc.mkdirs();
        }

        File poPackageDirc = new File(outputDirc, "vo");
        poPackageDirc.mkdirs();

        File daoPackageDirc = new File(outputDirc, "dao");
        daoPackageDirc.mkdirs();


        for (int i = 0; i < tables.length; i++) {
            try {
                CodeBuilder codeBuilder = new CodeBuilder(tables[i]);

                //生成实体类
                String codeStr = codeBuilder.buildEntitySource(basicEntity, entityPackage);
                this.createFile(codeStr, poPackageDirc.getCanonicalPath()
                        + File.separator + MappingUtil.getEntityName(tables[i]) + ".java");

                //生成DAO类
                codeStr = codeBuilder.buildDaoSource(basicDao, daoPackage);
                this.createFile(codeStr, daoPackageDirc.getCanonicalPath()
                        + File.separator + MappingUtil.getEntityName(tables[i]) + "Dao.java");

                //生成MAPPER
                codeStr = codeBuilder.buildMapperSource(daoPackage);
                this.createFile(codeStr, daoPackageDirc.getCanonicalPath()
                        + File.separator + MappingUtil.getEntityName(tables[i]) + "Mapper.xml");

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    /**
     * 将文本内容写入指定的文件
     *
     * @param fileContent
     * @param fileName
     */
    private void createFile(String fileContent, String fileName)
            throws IOException {
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(fileName));
            osw.write(fileContent, 0, fileContent.length());
            osw.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (osw != null) {
                osw.close();
            }
        }
    }
}
