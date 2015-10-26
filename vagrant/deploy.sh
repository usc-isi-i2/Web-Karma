#!/usr/bin/env bash
KARMA_UPDATE=$1
if [ ! -z $KARMA_UPDATE ]
	then
cd /home/vagrant/Web-Karma
git reset --hard
git pull
mvn clean install
fi

bash -c "cd /home/vagrant/Web-Karma/karma-web; mvn jetty:run &"
