@echo off
if not exist out mkdir out
javac -d out src\model\*.java src\exception\*.java src\service\*.java src\util\*.java src\report\*.java src\Main.java
if %ERRORLEVEL% EQU 0 (
    echo Compilation successful! Running Smart Inventory Management System...
    java -cp out Main %*
) else (
    echo Compilation failed!
)
pause
