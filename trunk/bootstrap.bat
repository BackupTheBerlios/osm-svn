@echo off

REM bootstrap.bat
REM -------------
REM
REM Utility to build all of the tutorials and site.
REM

set ID=%1

CALL :build tutorial\tooling\101
IF ERRORLEVEL 1 GOTO :exit

CALL :build tutorial\tooling\102 %ID%
IF ERRORLEVEL 1 GOTO :exit

CALL :build tutorial\tooling\103 %ID%
IF ERRORLEVEL 1 GOTO :exit

CALL :build tutorial\tooling\104 %ID%
IF ERRORLEVEL 1 GOTO :exit

CALL :build tutorial\components\101 %ID%
IF ERRORLEVEL 1 GOTO :exit

CALL :build tutorial\components\102 %ID%
IF ERRORLEVEL 1 GOTO :exit

CALL :build tutorial\components\103 %ID%
IF ERRORLEVEL 1 GOTO :exit

CALL :build tutorial\components\104 %ID%
IF ERRORLEVEL 1 GOTO :exit

CALL :build tutorial\components\201 %ID%
IF ERRORLEVEL 1 GOTO :exit

CALL :build home %ID%
IF ERRORLEVEL 1 GOTO :exit

ECHO BOOTSTRAP SUCCESSFUL
GOTO :EOF

:exit
IF ERRORLEVEL 1 ECHO BOOTSTRAP FAILED
GOTO :EOF

:build

PUSHD %1
ECHO =========================================================================
ECHO BUILDING PROJECT IN %1
IF "%ID%" == "" set ID=SNAPSHOT
set BUILD_ID=-Dbuild.signature=%ID%
ECHO building project with release ID [%BUILD_ID%]
ECHO =========================================================================
CALL build %BUILD_ID% clean install
set BUILD_ID=""
POPD
goto :EOF


