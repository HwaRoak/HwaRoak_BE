#!/usr/bin/env bash

# .env 파일 로딩
set -a
[ -f "/home/ubuntu/app/.env" ] && source /home/ubuntu/app/.env
set +a


PROJECT_ROOT="/home/ubuntu/app"
JAR_FILE="$PROJECT_ROOT/hwaroak-0.0.1-SNAPSHOT.jar"

APP_LOG="$PROJECT_ROOT/application.log"
ERROR_LOG="$PROJECT_ROOT/error.log"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

# build 파일 복사
echo "$TIME_NOW > $JAR_FILE 파일 복사" >> $DEPLOY_LOG
cp $PROJECT_ROOT/build/libs/hwaroak-0.0.1-SNAPSHOT.jar $JAR_FILE

# jar 파일 실행 (spring.profiles.active=dev 추가)
echo "$TIME_NOW > $JAR_FILE 파일 실행" >> $DEPLOY_LOG
nohup java -Dspring.profiles.active=dev -jar $JAR_FILE > $APP_LOG 2> $ERROR_LOG &

CURRENT_PID=$(pgrep -f $JAR_FILE)
echo "$TIME_NOW > 실행된 프로세스 아이디 $CURRENT_PID 입니다." >> $DEPLOY_LOG
