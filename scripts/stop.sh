#!/usr/bin/env bash

PROJECT_ROOT="/home/ubuntu/app"
JAR_NAME="hwaroak-0.0.1-SNAPSHOT.jar"  # 🔧 수정: 경로 대신 파일명 기준으로 PID 찾기
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"
TIME_NOW=$(date +%c)

# 🔧 수정: 파일 경로가 아니라 파일명 기준으로 실행 중인 프로세스 PID 찾기
CURRENT_PID=$(ps -ef | grep $JAR_NAME | grep -v grep | awk '{print $2}')

if [ -z "$CURRENT_PID" ]; then
  echo "$TIME_NOW > 현재 실행중인 애플리케이션이 없습니다" >> $DEPLOY_LOG
else
  echo "$TIME_NOW > 실행중인 $CURRENT_PID 애플리케이션 종료 시도" >> $DEPLOY_LOG
  kill -15 $CURRENT_PID  # 정상 종료 시도
  sleep 5

  # 🔧 추가: 정상 종료 실패했을 경우 강제 종료
  if ps -p $CURRENT_PID > /dev/null; then
    echo "$TIME_NOW > 프로세스 $CURRENT_PID 강제 종료" >> $DEPLOY_LOG
    kill -9 $CURRENT_PID
  else
    echo "$TIME_NOW > 프로세스가 정상 종료되었습니다" >> $DEPLOY_LOG
  fi
fi
