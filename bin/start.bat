@echo off & setlocal  enabledelayedexpansion

cd "%~dp0"
cd ..\conf
set APPLICATION_NAME="mybatis-daoj"

set LIB_JARS=""
cd ..\lib
for %%i in (*) do set LIB_JARS=!LIB_JARS!;..\lib\%%i

cd ..\bin

set JVM_MEM_OPTS=-Xms512m -Xmx1024m -XX:MaxPermSize=256M

start "%APPLICATION_NAME%" java %JVM_MEM_OPTS% -classpath ..\conf;%LIB_JARS% com.github.walker.mybatis.daoj.core.Generator
goto end

:end
pause
