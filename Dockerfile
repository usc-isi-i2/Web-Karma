FROM maven:3-jdk-8

LABEL MAINTAINER <alexander.malic [at] maastrichtuniversity.nl>

WORKDIR /app

RUN git clone https://github.com/usc-isi-i2/Web-Karma.git && \
  cd Web-Karma && \
  mvn clean install

WORKDIR /app/Web-Karma/karma-web

ENV MAX_MEM="128g"
ENV MAVEN_OPTS="-Xmx$MAX_MEM"

EXPOSE 8080

ENTRYPOINT ["mvn", "jetty:run"]
