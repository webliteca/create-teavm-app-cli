@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Apache Maven Wrapper startup script for Windows

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)

@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE__=%PSModulePath%
@SET PSModulePath=

@FOR /F "usebackq tokens=1* delims==" %%A IN ("%~dp0.mvn\wrapper\maven-wrapper.properties") DO @(
    @IF "%%A"=="wrapperUrl" SET __MVNW_WRAPPERURL__=%%B
)

@IF NOT EXIST "%~dp0.mvn\wrapper\maven-wrapper.jar" (
    @IF "%__MVNW_WRAPPERURL__%"=="" SET __MVNW_WRAPPERURL__=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
    powershell -Command "Invoke-WebRequest -Uri '%__MVNW_WRAPPERURL__%' -OutFile '%~dp0.mvn\wrapper\maven-wrapper.jar'"
)

@SET PSModulePath=%__MVNW_PSMODULEP_SAVE__%

@SET MAVEN_PROJECTBASEDIR=%~dp0
@SET MAVEN_CONFIG=
@IF EXIST "%~dp0.mvn\maven.config" (
    @FOR /F "usebackq delims=" %%A IN ("%~dp0.mvn\maven.config") DO @SET MAVEN_CONFIG=%%A
)

@IF "%JAVA_HOME%"=="" (
    SET JAVACMD=java
) ELSE (
    SET JAVACMD=%JAVA_HOME%\bin\java
)

%JAVACMD% %MAVEN_OPTS% -classpath "%~dp0.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %MAVEN_CONFIG% %*

@IF %ERRORLEVEL% NEQ 0 GOTO error
GOTO end

:error
@SET __MVNW_ERROR__=1

:end
@IF NOT "%__MVNW_CMD__%"=="" @ENDLOCAL & SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR%" & SET "MAVEN_CONFIG=%MAVEN_CONFIG%"
@EXIT /B %__MVNW_ERROR__%
