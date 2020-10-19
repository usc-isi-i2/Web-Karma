FROM maven:3-jdk-8

LABEL MAINTAINER <1655040956 [at] qq.com>

WORKDIR /app

RUN git clone https://github.com/zhoushaokun/Web-Karma.git && \
  cd Web-Karma && \
  mvn clean install

WORKDIR /app/Web-Karma/karma-web

ENV MAX_MEM="8g"
ENV MAVEN_OPTS="-Xmx$MAX_MEM"

EXPOSE 8080

ENTRYPOINT ["mvn", "jetty:run"]