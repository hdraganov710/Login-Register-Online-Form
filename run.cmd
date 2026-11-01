@echo off
javac -encoding UTF-8 --release 21 -d out/main -cp "lib/*" *.java || exit /b 1
java -cp "out/main;lib/*" Main
