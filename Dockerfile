FROM openjdk:21-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} demo-0.0.1-SNAPSHOT.jar
COPY ./etc/ufc_data.json /my-files/ufc_data.json
COPY ./etc/mma-project-7dfc2-firebase-adminsdk-fbsvc-1be46bb027.json /my-files/mma-project-7dfc2-firebase-adminsdk-fbsvc-1be46bb027.json
ENTRYPOINT ["java","-jar","/demo-0.0.1-SNAPSHOT.jar"]