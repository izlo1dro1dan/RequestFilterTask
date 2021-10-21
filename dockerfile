FROM openjdk:8-jdk-alpine
VOLUME /tmp
RUN ./gradlew build
COPY build/libs/application-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/application-0.0.1-SNAPSHOT.jar"]