# !/local/bin/bash
# host=`hostname`
# export DISPLAY="${host}:0"
java -cp .:Reversi.jar reversi.CommandLineGame $1 $2 $3
