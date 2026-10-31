@echo off
set "MVN_BIN=C:\Users\HP\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd"
if not exist "%CD%\pom.xml" (
    if exist "%CD%\HospitalManagementWeb\pom.xml" (
        "%MVN_BIN%" -f "%CD%\HospitalManagementWeb\pom.xml" %*
        goto :eof
    )
)
"%MVN_BIN%" %*
