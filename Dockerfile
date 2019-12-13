FROM maven:3.6.1-jdk-8-alpine AS build-env

RUN apk --no-cache add ca-certificates
RUN wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub
RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.29-r0/glibc-2.29-r0.apk
RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.29-r0/glibc-bin-2.29-r0.apk
RUN apk add glibc-2.29-r0.apk glibc-bin-2.29-r0.apk
RUN apk update && apk add bash && apk add libstdc++
ENV LC_ALL=C

WORKDIR /app
COPY . /app
RUN mvn clean package

FROM java:8

COPY --from=build-env /app/target/Jalas-0.0.1.jar /data/app.jar
WORKDIR /data
RUN mkdir /tz && mv /etc/timezone /tz/ && mv /etc/localtime /tz/ && ln -s /tz/timezone /etc/ && ln -s /tz/localtime /etc/
RUN echo "Asia/Tehran" > /etc/timezone && dpkg-reconfigure -f noninteractive tzdata && cp /etc/localtime /tz/
VOLUME /tz
CMD exec java -jar app.jar
