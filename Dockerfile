FROM openjdk:8-jdk-alpine

RUN apk add --update curl
RUN curl -O https://downloads.typesafe.com/typesafe-activator/1.3.10/typesafe-activator-1.3.10-minimal.zip
RUN unzip typesafe-activator-1.3.10-minimal.zip -d / && rm typesafe-activator-1.3.10-minimal.zip && chmod a+x /activator-1.3.10-minimal/bin/activator
ENV PATH $PATH:/activator-1.3.10

EXPOSE 9000
RUN mkdir /app
WORKDIR /app

CMD ["activator", "run"]
