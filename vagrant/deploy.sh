#!/usr/bin/env bash

#HDFS and YARN
cd /home/vagrant/Web-Karma
git reset --hard
git pull
mvn clean install

bash -c "cd /home/vagrant/Web-Karma/karma-web; mvn jetty:run &"
