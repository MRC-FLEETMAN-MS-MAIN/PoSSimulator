FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/micro3possim-0.0.1-SNAPSHOT.jar PositionSim.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","PositionSim.jar"]