@echo off

REM bootstrap.bat
REM -------------
REM
REM Utility to build all of the tutorials and site.
REM

set ID=%1
CALL :tutorial-tooling-101
IF ERRORLEVEL 1 GOTO :exit
CALL :tutorial-tooling-102
IF ERRORLEVEL 1 GOTO :exit
CALL :tutorial-tooling-103
IF ERRORLEVEL 1 GOTO :exit
CALL :tutorial-tooling-104
IF ERRORLEVEL 1 GOTO :exit
CALL :tutorial-components-101
IF ERRORLEVEL 1 GOTO :exit
CALL :site
IF ERRORLEVEL 1 GOTO :exit
ECHO BOOTSTRAP SUCCESSFUL
GOTO :EOF

:exit
IF ERRORLEVEL 1 ECHO BOOTSTRAP FAILED
GOTO :EOF

:tutorial-tooling-101
PUSHD tutorial\tooling\101
CALL :build clean install
POPD
GOTO :EOF

:tutorial-tooling-102
PUSHD tutorial\tooling\102
CALL :build clean install
POPD
GOTO :EOF

:tutorial-tooling-103
PUSHD tutorial\tooling\103
CALL :build clean install
POPD
GOTO :EOF

:tutorial-tooling-104
PUSHD tutorial\tooling\104
CALL :build clean install
POPD
GOTO :EOF

:tutorial-components-101
PUSHD tutorial\tooling\101
CALL :build clean install
POPD
GOTO :EOF

:site
PUSHD home
CALL :build clean install
POPD
GOTO :EOF

:build
IF "%ID%" == "" set ID=SNAPSHOT
set BUILD_ID=-Dbuild.signature=%ID%
ECHO building project with release ID [%BUILD_ID%]
CALL build %BUILD_ID% %*
set BUILD_ID=""
goto :EOF


