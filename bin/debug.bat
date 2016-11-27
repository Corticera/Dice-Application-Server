@echo off

echo Starting Dice in debug mode.
call setenv
echo VM Arguments: %_VMARGS%
echo Start your debugger and connect to localhost:8848 and press any key.
pause > nul
call java %_VMARGS% -Xdebug -Xrunjdwp:transport=dt_socket,address=localhost:8848,server=y,suspend=y -jar startup.jar