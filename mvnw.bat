@echo off
set MAVEN_OPTS=%MAVEN_OPTS% -Dmaven.wagon.http.ssl.insecure=true
mvn %*
