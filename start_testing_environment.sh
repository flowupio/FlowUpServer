#!/usr/bin/env bash
docker run -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=flowupdb -e MYSQL_USER=flowupUser -e MYSQL_PASSWORD=flowupPassword -p 127.0.0.1:3306:3306 -d mysql:5.6
docker run -p 127.0.0.1:6379:6379 -d redis:3.2
