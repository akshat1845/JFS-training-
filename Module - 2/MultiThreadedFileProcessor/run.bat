@echo off
if not exist out mkdir out
javac -d out src\model\*.java src\config\*.java src\processor\*.java src\report\*.java src\Main.java
if %ERRORLEVEL% EQU 0 (
    echo Compilation successful! Running Multi-Threaded File Processor...
    java -cp out Main %*
) else (
    echo Compilation failed!
)
pause
