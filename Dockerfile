FROM maven:3-jdk-8

LABEL MAINTAINER <alexander.malic [at] maastrichtuniversity.nl>

WORKDIR /app

COPY . .

RUN mvn clean install

WORKDIR /app/karma-web

ENV MAX_MEM="8g"
ENV MAVEN_OPTS="-Xmx$MAX_MEM"

VOLUME ["/data"]
ENV KARMA_USER_HOME="/data"

EXPOSE 8080

ENTRYPOINT ["mvn", "jetty:run"]