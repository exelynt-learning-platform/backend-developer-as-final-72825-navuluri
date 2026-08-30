@echo off
setlocal
set MAVEN_BASEDIR=%~dp0
if "%MAVEN_BASEDIR:~-1%"=="\" set MAVEN_BASEDIR=%MAVEN_BASEDIR:~0,-1%
set WRAPPER_JAR="%MAVEN_BASEDIR%\.mvn\wrapper\maven-wrapper.jar"

java "-Dmaven.multiModuleProjectDirectory=%MAVEN_BASEDIR%" -cp %WRAPPER_JAR% org.apache.maven.wrapper.MavenWrapperMain %*
@endlocal
