package com.github.walker.mybatis.daoj.core;


import com.github.walker.mybatis.daoj.utils.MappingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * 程序名称：mybatis-daoj，即依赖mybatis的DAO层代码生成器。
 *
 * @author HuQingmiao
 *
 * 一. 使用说明:
 *
 * 1. 打开mybatis-daoj.xml，配置数据库连接，设置实体类、DAO接口类所在的包名，以及生成代码的输出目录。
 *
 * 2. 双击gen.bat 或执行:java -jar mybatis-daoj-xxx.jar 以运行本程序。本程序将生成三种文件：vo实体类,dao接口类,mapper.xml，
 *      将这些文件复制到你工程的对应目录。
 *
 * 3. 要让生成的代码运行起来，还需要在mybatis.xml中增加如下配置:
 *      <typeAliases>
 *           <!-- 为vo包下的所有类自动定义别名, 因为生成的mapper.xml中的"resultType" 指定的是vo实体类的别名-->
 *           <package name="com.mucfc.act.vo"/>
 *      </typeAliases>
 *
 * 4. 要在mybatis.xml中增加分页插件，添加如下配置即可:
 *      <plugins>
 *          <!-- mysql分页查询拦截器, 你可以根据你的数据库类型修改相应的dialectClass -->
 *          <plugin interceptor="com.github.walker.mybatis.paginator.OffsetLimitInterceptor">
 *               <property name="dialectClass" value="com.github.walker.mybatis.paginator.dialect.MySQLDialect"/>
 *          </plugin>
 *      </plugins>
 *
 *     并且，你的工程也要引入分页插件包mybatis-paginator.jar，您可以在这个链接页面找到下载地址:
 *                                https://github.com/HuQingmiao/mybatis-paginator
 *
 *
 * 5. 现在可以在你的service层代码调用dao层了。
 *
 * 6. 注意事项:
 *    *本程序生成的dao类没有任何接口方法，只是继承"BasicDao.java"；但你可以在其子接口中扩展你的个性方法。
 *    *如果你没有使用数据库的自增主键特性，则在生成mapper.xml文件后，必须删除INSERT部分的'useGeneratedKeys="true" keyProperty="xx"'。
 */
public class Generator {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        try {
            new Generator().generator();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                codeStr = codeBuilder.buildMapperSource(daoPackage, entityPackage);
                this.createFile(codeStr, daoPackageDirc.getCanonicalPath()
                        + File.separator + MappingUtil.getEntityName(tables[i]) + "Mapper.xml");

                //复制DAO/VO基类
                InputStream is = Generator.class.getClassLoader().getResourceAsStream("BasicDao.java");
                File basicDaoFile = new File(outputDirc, "BasicDao.java");
                this.writeToFile(is, basicDaoFile);

                is = Generator.class.getClassLoader().getResourceAsStream("BasicVo.java");
                File basicVoFile = new File(outputDirc, "BasicVo.java");
                this.writeToFile(is, basicVoFile);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void writeToFile(InputStream is, File file) throws Exception {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            byte[] bytes = new byte[5 * 1024];
            int len = 0;
            while ((len = is.read(bytes)) > 0) {
                os.write(bytes, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            log.error("", e);
            throw e;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                log.error("", e);
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
            osw = new OutputStreamWriter(new FileOutputStream(fileName),"UTF-8");
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
