@java -cp .;Reversi.jar reversi.CommandLineGame %1 %2 %3
COPY /Y Game_Report* ..\..\Reports
DEL Game_Report*