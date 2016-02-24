#!/usr/bin/env bash
KARMA_UPDATE=$1
if [ ! -z $KARMA_UPDATE ]
	then
cd /home/vagrant/Web-Karma
git reset --hard
git pull
fi

bash -c "cd /home/vagrant/Web-Karma; mvn clean install -Dmaven.test.skip=true"
bash -c "cd /home/vagrant/Web-Karma/karma-web; nohup mvn jetty:run &"
