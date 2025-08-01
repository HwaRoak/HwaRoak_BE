#!/bin/bash

set -e

ERR_MSG=''

trap 'echo "Error occured: $ERR_MSG. Exiting deploy script."; exit 1' ERR

# 현재 포트 파악
if sudo docker ps --filter "name=app-blue" --quiet | grep -E .; then
  echo "Blue down, Green Up "
  BEFORE_COMPOSE_COLOR="blue"
  AFTER_COMPOSE_COLOR="green"
else
  echo "Green down, Blue up"
  BEFORE_COMPOSE_COLOR="green"
  AFTER_COMPOSE_COLOR="blue"
fi

# container pull
docker compose pull app-${AFTER_COMPOSE_COLOR}
docker compose up -d --no-deps --force-recreate app-${AFTER_COMPOSE_COLOR}

sleep 10

echo "The $BEFORE_COMPOSE_COLOR version is currently running on the server. Starting the $AFTER_COMPOSE_COLOR version."

# 새로운 컨테이너가 제대로 떴는지 확인
if docker ps --filter "name=app-${AFTER_COMPOSE_COLOR}" --filter "status=running" --format '{{.Names}}' | grep -q .; then
  echo "New app-${AFTER_COMPOSE_COLOR} container is running."
  # reload nginx
  NGINX_ID=$(sudo docker ps --filter "name=nginx" --quiet)
  NGINX_CONFIG="/home/ubuntu/nginx/nginx.conf"

  echo "Switching Nginx upstream config..."
  if ! sed -i "s/app-${BEFORE_COMPOSE_COLOR}:8080/app-${AFTER_COMPOSE_COLOR}:8080/" $NGINX_CONFIG; then
        echo "Error occured: Failed to update Nginx config. Exiting deploy script."
        exit 1
  fi

  echo "Reloding Nginx in Container"
  if ! docker exec $NGINX_ID nginx -s reload; then
    ERR_MSG='Failed to update Nginx config'
    exit 1
  fi

  if ! docker compose restart nginx; then
    ERR_MSG='Failed to reload Nginx'
    exit 1
  fi

  # 이전 컨테이너 종료
  docker stop app-${BEFORE_COMPOSE_COLOR}
  docker rm app-${BEFORE_COMPOSE_COLOR}
  docker image prune -af
fi

echo "Deployment success."
exit 0