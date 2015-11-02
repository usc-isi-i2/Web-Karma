# vagrant-karma
Web Karma Vagrant VM

##Prerequisites:
Install vagrant - http://www.vagrantup.com/downloads




##Instructions to run karma:
 1. ```git clone https://github.com/usc-isi-i2/Web-Karma.git```
 2. ```cd Web-Karma/vagrant```
 3. ```vagrant up```
 4. Open localhost:8080 in a browser

The karma folder in Web-Karma/vagrant is set as KARMA_HOME. To use a custom karma home, copy your karma folder to Web-Karma/vagrant.

Notes:
Since the Guest OS is behind a NAT, we set the SPARK_LOCAL_IP using the tun0 interface as the IP address.  This is done in the spark-env.sh file

Please make sure that vbguest is installed (https://github.com/dotless-de/vagrant-vbguest/).  It helps keep the VirtuaalBoxGuestAdditions up to date, install on the cmd line:
```
vagrant plugin install vagrant-vbguest
```
When starting this on Linux, do not use your distribution's Vagrant package to install vagrant. Install from https://www.vagrantup.com to ensure you're using the latest version of Vagrant. Using old versions of Vagrant will result in errors.
