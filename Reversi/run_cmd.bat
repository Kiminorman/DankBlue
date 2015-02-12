@java -cp .;Reversi_cmd.jar reversi.CommandLineGame %1 %2 %3

COPY /Y Game_Report* ..\Reports

DEL Game_Report*