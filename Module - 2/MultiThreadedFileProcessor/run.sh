#!/bin/bash
mkdir -p out
javac -d out src/model/*.java src/config/*.java src/processor/*.java src/report/*.java src/Main.java
if [ $? -eq 0 ]; then
    echo "Compilation successful! Running Multi-Threaded File Processor..."
    java -cp out Main "$@"
else
    echo "Compilation failed!"
fi
