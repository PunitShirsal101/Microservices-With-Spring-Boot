@echo off
rem Run the Main class reliably on Windows (cmd.exe)
setlocal
REM Ensure Maven is on PATH
mvn clean compile -DskipTests && mvn exec:java -Dexec.mainClass=com.iot.pipeline.Main -Dexec.classpathScope=runtime
endlocal
pause

