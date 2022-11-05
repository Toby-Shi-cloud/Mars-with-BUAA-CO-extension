@echo off

dir /S /B mars\*.java > srcList.txt
javac -encoding gbk Mars.java @srcList.txt
