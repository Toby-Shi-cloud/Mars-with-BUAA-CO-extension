@echo off

dir /S /B *.java > srcList.txt
javac -encoding gbk @srcList.txt
