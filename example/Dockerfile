FROM registry.opensource.zalan.do/library/openjdk-11-jre-slim:latest

LABEL Description="Java trace spammer"

WORKDIR /

COPY target/example-0.30.5-jar-with-dependencies.jar /example.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/example.jar"]
