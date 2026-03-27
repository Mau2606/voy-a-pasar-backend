@echo off
setlocal

REM ---------------------------------------------------------------------------
REM  mvnw.cmd — usa Maven descargado en %USERPROFILE%\.m2\apache-maven-3.9.6
REM ---------------------------------------------------------------------------

set MAVEN_HOME=%USERPROFILE%\.m2\apache-maven-3.9.6
set JAVA_HOME=C:\Program Files\Java\jdk-17

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Maven no encontrado en %MAVEN_HOME%
    echo Por favor ejecuta el comando de descarga de Maven primero.
    exit /B 1
)

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo JAVA_HOME invalido: %JAVA_HOME%
    exit /B 1
)

set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo Usando Java: %JAVA_HOME%
echo Usando Maven: %MAVEN_HOME%
echo.

call "%MAVEN_HOME%\bin\mvn.cmd" %*

exit /B %ERRORLEVEL%
