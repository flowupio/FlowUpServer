FROM openjdk:8-jdk-alpine

RUN apk add --update bash

ADD . /app

RUN chmod a+x /app/bin/flowupserver

WORKDIR /app/bin

EXPOSE 9000

CMD ["./flowupserver"]
