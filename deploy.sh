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

docker compose -p app-${AFTER_COMPOSE_COLOR} -f docker-compose.yml up -d

sleep 10

echo "The $STOP_TARGET version is currently running on the server. Starting the $RUN_TARGET version."

# 새로운 컨테이너가 제대로 떴는지 확인
EXIST_AFTER=$(docker compose -p app-${AFTER_COMPOSE_COLOR} -f docker-compose.${AFTER_COMPOSE_COLOR}.yml ps | grep Up)
if [ -n "$EXIST_AFTER" ]; then
  # reload nginx
  NGINX_ID=$(sudo docker ps --filter "name=nginx" --quiet)
  NGINX_CONFIG="/etc/nginx/conf.d/default.conf"

  docker exec $NGINX_ID /bin/bash -c "sed -i 's/$BEFORE_COMPOSE_COLOR:8080/$AFTER_COMPOSE_COLOR:8080/' $NGINX_CONFIG"
  docker exec $NGINX_ID /bin/bash -c "nginx -s reload" || { ERR_MSG='Failed to reload nginx'; exit 1; }

  # 이전 컨테이너 종료
  docker stop app-${BEFORE_COMPOSE_COLOR}
  docker rm app-${BEFORE_COMPOSE_COLOR}
  docker image prune -af
fi

echo "Deployment success."
exit 0