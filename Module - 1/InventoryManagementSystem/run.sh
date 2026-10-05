#!/bin/bash
mkdir -p out
javac -d out src/model/*.java src/exception/*.java src/service/*.java src/util/*.java src/report/*.java src/Main.java
if [ $? -eq 0 ]; then
    echo "Compilation successful! Running Smart Inventory Management System..."
    java -cp out Main "$@"
else
    echo "Compilation failed!"
fi
