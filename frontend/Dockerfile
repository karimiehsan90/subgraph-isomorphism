FROM m.docker-registry.ir/nginx:1.16.1

COPY index.html vis-network.min.js /usr/share/nginx/html/

COPY default.conf /etc/nginx/conf.d/default.conf

RUN chmod 777 /usr/share/nginx/html/*
