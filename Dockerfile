FROM openjdk:17
VOLUME /tmp
ARG JAR_FILE=build/libs/management-0.0.1.jar
MAINTAINER andrey.synkov
COPY ${JAR_FILE} /usr/app/
WORKDIR /usr/app
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar management-0.0.1.jar"]