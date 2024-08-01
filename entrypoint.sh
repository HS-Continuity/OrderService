#!/bin/sh

# ECS 메타데이터 URI로부터 IP 주소를 획득
export ECS_INSTANCE_IP_TASK=$(curl --retry 5 --connect-timeout 3 -s ${ECS_CONTAINER_METADATA_URI})

# 호스트명 획득
export ECS_INSTANCE_HOSTNAME=$(cat /proc/sys/kernel/hostname)

# IP 주소 추출
export ECS_INSTANCE_IP_ADDRESS=$(echo ${ECS_INSTANCE_IP_TASK} | jq -r '.Networks[0] | .IPv4Addresses[0]')

# /etc/hosts 파일에 IP와 호스트명 맵핑 추가
echo "${ECS_INSTANCE_IP_ADDRESS} ${ECS_INSTANCE_HOSTNAME}" | tee -a /etc/hosts

# 환경 변수 출력
echo "ECS_INSTANCE_HOSTNAME: ${ECS_INSTANCE_HOSTNAME}"
echo "ECS_INSTANCE_IP_ADDRESS: ${ECS_INSTANCE_IP_ADDRESS}"

# 애플리케이션 시작
echo "The application will start in ${APP_SLEEP}s..." && sleep ${APP_SLEEP}
set -x
exec java ${JAVA_OPTS} -noverify -XX:+AlwaysPreTouch -Djava.security.egd=file:/dev/./urandom -jar /yeonieum_orderservice.jar
