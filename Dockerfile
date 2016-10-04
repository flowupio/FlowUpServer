FROM openjdk:8-jdk-alpine

RUN apk add --update curl bash
RUN curl -O https://downloads.typesafe.com/typesafe-activator/1.3.10/typesafe-activator-1.3.10.zip
RUN unzip typesafe-activator-1.3.10.zip -d / && rm typesafe-activator-1.3.10.zip && chmod a+x /activator-dist-1.3.10/bin/activator
ENV PATH $PATH:/activator-dist-1.3.10/bin

EXPOSE 9000
ADD . /app

WORKDIR /app

CMD ["activator", "run"]
