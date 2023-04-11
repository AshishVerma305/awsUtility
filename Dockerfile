FROM openjdk:17-alpine

RUN apk add maven
RUN apk update openssl-libs
RUN apk update expat
RUN apk update zlib
RUN apk upgrade busybox ssl_client zlib libtasn1 ssl_client apk-tools
RUN apk upgrade libssl1.1 libcrypto1.1 libretls
WORKDIR /app
COPY . /app

RUN mvn clean package
WORKDIR /app
RUN ls -lrt /app/target/
ARG JAR_FILE=/app/target/*.jar

WORKDIR /app
RUN cp $JAR_FILE /app/azure-utilities.jar

ADD startup.sh /app

RUN chmod 744 /app/startup.sh
RUN chmod 777 /app

RUN ls -lrt /app/*
RUN chgrp -R 0 /app && \
    chmod -R g=u /app
RUN pwd

EXPOSE 9090


CMD "/app/startup.sh"
