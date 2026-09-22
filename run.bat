@echo off
setlocal

REM ===================================================================
REM Launch Script for Client Portal ^& Project Management (JV-CRM-005)
REM ===================================================================

set "JAVA_HOME=C:\Users\pujar\AppData\Local\Programs\IntelliJ IDEA 2025.3\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MVN=C:\Users\pujar\AppData\Local\Programs\IntelliJ IDEA 2025.3\plugins\maven\lib\maven3\bin\mvn.cmd"

echo ==========================================================
echo Starting Client Portal ^& Project Management [JV-CRM-005]
echo ==========================================================
echo Java Home: %JAVA_HOME%
echo Maven Path: %MVN%
echo ==========================================================

if "%1"=="mysql" goto run_mysql
if "%1"=="jar" goto run_jar

echo Starting with in-memory H2 profile for instant zero-friction evaluation...
echo [Tip: pass 'mysql' to connect to local MySQL: run.bat mysql]
echo [Tip: pass 'jar' to start precompiled JAR: run.bat jar]
call "%MVN%" spring-boot:run -Dspring-boot.run.profiles=test
goto end

:run_mysql
echo Starting with MySQL profile [port 3306]...
call "%MVN%" spring-boot:run
goto end

:run_jar
echo Starting packaged JAR directly...
"%JAVA_HOME%\bin\java.exe" -jar target\client-portal-1.0.0.jar
goto end

:end
endlocal
