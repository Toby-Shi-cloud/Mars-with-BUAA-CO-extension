@echo off

dir /S /B *.java > srcList.txt
javac  Mars.java @srcList.txt
