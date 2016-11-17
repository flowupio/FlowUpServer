#!/usr/bin/env bash
docker run -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=flowupdb -e MYSQL_USER=flowupUser -e MYSQL_PASSWORD=flowupPassword -p 127.0.0.1:3306:3306 -d mysql:5.6
docker run -p 127.0.0.1:9200:9200 -d elasticsearch:2.3
docker run -p 3000:3000 -d grafana/grafana