#!/usr/bin/env bash

# Get the Extra repos installed
sudo yum -y install java-1.8.0-openjdk-devel
sudo yum -y groupinstall 'Development Tools'
sudo yum -y install wget

cd /tmp
sudo wget http://apache-mirror.rbc.ru/pub/apache/maven/maven-3/3.3.3/binaries/apache-maven-3.3.3-bin.tar.gz
sudo tar -xvzpf apache-maven-3.3.3-bin.tar.gz
sudo mv apache-maven-3.3.3 /usr/local/
sudo ln -s /usr/local/apache-maven-3.3.3/bin/* /usr/local/bin/

cd /home/vagrant/
git clone https://github.com/usc-isi-i2/Web-Karma.git

export KARMA_UPDATE=1