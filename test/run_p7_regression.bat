@echo off
setlocal

if "%~1"=="" (
  set "MARS_JAR=%~dp0mars.jar"
) else (
  set "MARS_JAR=%~f1"
)

pushd "%~dp0"

call :run status "" || exit /b 1
call :run bd_not_taken "db" || exit /b 1
call :run fetch_unaligned "db" || exit /b 1
call :run jump_far "db" || exit /b 1
call :run timer_write_count "" || exit /b 1
call :run eret_delay_slot "db" || exit /b 1
call :run cp0_mask "" || exit /b 1
call :run_no_efc status_legacy "p7\status.asm" || exit /b 1

del /q testTemp.txt testActual.txt testExpected.txt >nul 2>nul
popd
exit /b 0

:run
set "NAME=%~1"
set "EXTRA=%~2"
java -jar "%MARS_JAR%" nc mc CompactLargeText coL1 efc %EXTRA% "p7\%NAME%.asm" > testTemp.txt
findstr /v "^$" testTemp.txt > testActual.txt
findstr /v "^$" "p7\%NAME%.out" > testExpected.txt
fc testActual.txt testExpected.txt
exit /b %errorlevel%

:run_no_efc
set "NAME=%~1"
set "ASM=%~2"
java -jar "%MARS_JAR%" nc mc CompactLargeText coL1 "%ASM%" > testTemp.txt
findstr /v "^$" testTemp.txt > testActual.txt
findstr /v "^$" "p7\%NAME%.out" > testExpected.txt
fc testActual.txt testExpected.txt
exit /b %errorlevel%
