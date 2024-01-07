FROM amazoncorretto:21-alpine-jdk
MAINTAINER pukekocorp.co.nz
COPY target/msginf-1.0-SNAPSHOT.jar msginf-1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=docker","/msginf-1.0-SNAPSHOT.jar"]