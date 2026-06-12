@echo off

dir /S /B *.java > srcList.txt
javac -encoding UTF-8 Mars.java @srcList.txt
