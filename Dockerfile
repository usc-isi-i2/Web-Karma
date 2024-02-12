FROM openjdk:8-jdk-alpine
RUN apk add --no-cache curl tar git

WORKDIR /karma

RUN wget https://dlcdn.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz

RUN tar xzvf apache-maven-3.9.5-bin.tar.gz

RUN mkdir /karma/Web-Karma

RUN git clone https://github.com/usc-isi-i2/Web-Karma /karma/Web-Karma

RUN cd /karma/Web-Karma && /karma/apache-maven-3.9.5/bin/mvn clean install

CMD cd /karma/Web-Karma/karma-web && /karma/apache-maven-3.9.5/bin/mvn jetty:run
