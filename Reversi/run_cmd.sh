# !/local/bin/bash
# host=`hostname`
# export DISPLAY="${host}:0"
java -cp ../bin:.:Reversi_cmd.jar reversi.CommandLineGame $1 $2 $3
