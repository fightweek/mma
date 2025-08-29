FROM openjdk:17-jdk-alpine
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} demo-0.0.1-SNAPSHOT.jar
COPY ./etc/ufc_data.json /my-files/ufc_data.json
ENTRYPOINT ["java","-jar","/demo-0.0.1-SNAPSHOT.jar"]