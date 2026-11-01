@echo off
if exist out rmdir /s /q out
if exist coverage rmdir /s /q coverage
javac -encoding UTF-8 --release 21 -d out/main -cp "lib/*" *.java || exit /b 1
javac -encoding UTF-8 --release 21 -d out/test -cp "out/main;lib/test/*" test/*.java || exit /b 1
java -javaagent:lib/test/jacocoagent.jar=destfile=out/jacoco.exec -jar lib/test/junit-platform-console-standalone.jar execute --class-path "out/main;out/test;lib/mysql-connector-j-26.10.0.jar" --scan-class-path out/test || exit /b 1
java -jar lib/test/jacococli.jar report out/jacoco.exec --classfiles out/main --sourcefiles . --html coverage --csv out/coverage.csv --name "Login-Register" >nul
echo.
echo Coverage report: coverage\index.html
