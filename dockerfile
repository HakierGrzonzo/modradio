# vi: ft=dockerfile
FROM maven:3.5-jdk-8-alpine as builder

COPY src /usr/src/app/src
COPY pom.xml /usr/src/app

RUN mvn -f /usr/src/app/pom.xml assembly:assembly

FROM archlinux:latest

RUN pacman -Syu --noconfirm jre8-openjdk-headless ffmpeg unzip

VOLUME /app
VOLUME /infiles

COPY ./feeder.sh /bin/feeder

COPY --from=builder /usr/src/app/target/modradio-1.0-SNAPSHOT-jar-with-dependencies.jar \
    /usr/bin/modradio.jar 

WORKDIR /app
ENTRYPOINT ["bash", "-c", "feeder /infiles | java -jar /usr/bin/modradio.jar"]
