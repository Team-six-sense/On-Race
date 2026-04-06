FROM nginx:latest

# 기존 기본 설정 삭제 후 커스텀 설정 복사
RUN rm /etc/nginx/conf.d/default.conf
COPY default.conf /etc/nginx/conf.d/default.conf

# 8080 포트 개방 명시
EXPOSE 8080