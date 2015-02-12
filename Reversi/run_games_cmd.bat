@echo off
REM This is an example script for running multiple games using the command line
REM version of the Reversi game framework. See the following example lines for usage.
@echo ---------------------------------------------------------------------------
@echo Running command line games for the Artificial Intelligence course exercise.
@echo ---------------------------------------------------------------------------
call run.bat CheapBlue LousyBlue 1
call run.bat LousyBlue CheapBlue 1
call run.bat CheapBlue LousyBlue 10
call run.bat LousyBlue CheapBlue 10

COPY /Y Game_Report* ..\..\Reports
DEL Game_Report*