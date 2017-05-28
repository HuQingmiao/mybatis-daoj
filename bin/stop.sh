#!/bin/bash
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf
LOGS_DIR=$DEPLOY_DIR/logs
START_REPORT_FILE=$LOGS_DIR/shell.log

# echo to $START_REPORT_FILE
reportTo()
{
   echo $* >> "$START_REPORT_FILE"
}
# echo to stdout and echo to $START_REPORT_FILE
echoReport()
{
   echo $* | tee -a "$START_REPORT_FILE"
}

SERVER_NAME=`sed '/^app.process.name/!d;s/.*=//' conf/dubbo.properties | tr -d '\r'`

if [ -z "$SERVER_NAME" ]; then
    SERVER_NAME=`hostname`
fi

reportTo "================ Time: `date '+%Y-%m-%d %H:%M:%S'` ================"
if [ ! -d "$LOGS_DIR" ]; then
    mkdir -p "$LOGS_DIR"
fi

PIDS=`ps -ef -ww | grep "java" | grep " -DappName=$SERVER_NAME " | awk '{print $2}'`
if [ -z "$PIDS" ]; then
    echoReport "ERROR: The $SERVER_NAME does not started!"
    exit 1
fi

if [ "$1" == "dump" ]; then
    $BIN_DIR/dump.sh
fi

reportTo "Stopping the $SERVER_NAME ..."
echo -e "Stopping the $SERVER_NAME ...\c"
KILL_PS=""
for PID in $PIDS ; do
    kill $PID >/dev/null &
    KILL_PS="$! $KILL_PS"
done

# sleep 10s to recycle vm
sleep 10
for kPID in $KILL_PS; do
    wait $kPID
done

# force kill
for kPID in $PIDS; do
    kill -9 $kPID >/dev/null 2>&1
done

REMAIN_PIDS=`ps -ef -ww | grep "java" | grep " -DappName=$SERVER_NAME " | awk '{print $2}'`
if [ -z "$REMAIN_PIDS" ]; then
    echoReport "OK!"
    echoReport "PID: $PIDS"
    exit 0
else
    echoReport "ERROR!"
    echoReport "PID: ${REMAIN_PIDS} is still alive."
    exit 1
fi
