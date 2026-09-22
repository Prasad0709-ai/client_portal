@echo off
REM ===================================================================
REM 1-Click Fast Launcher for Client Portal ^& Project Management
REM ===================================================================

set "JAVA_HOME=C:\Users\pujar\AppData\Local\Programs\IntelliJ IDEA 2025.3\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ==========================================================
echo Starting Client Portal ^& Project Management (JV-CRM-005)
echo ==========================================================
echo Running compiled executable JAR...
echo Portal URL: http://localhost:8080/login
echo ==========================================================

"%JAVA_HOME%\bin\java.exe" -jar target\client-portal-1.0.0.jar

pause
