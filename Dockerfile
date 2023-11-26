FROM openjdk:17
MAINTAINER andrey.synkov
COPY build/libs/management-0.0.1.jar /usr/app/
WORKDIR /usr/app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "management-0.0.1.jar"]