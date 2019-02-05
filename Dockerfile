FROM openjdk:8-jdk-alpine

RUN apk add --no-cache docker
RUN apk add --no-cache bash
COPY target/task-executor-0.0.1-SNAPSHOT.jar task-executor-0.0.1-SNAPSHOT.jar
COPY src/main/resources/script.sh /usr/script.sh
RUN chmod 777 /usr/script.sh
RUN mkdir -p /usr/execute/box
ENTRYPOINT ["java","-jar","/task-executor-0.0.1-SNAPSHOT.jar"]