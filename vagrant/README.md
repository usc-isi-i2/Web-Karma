# vagrant-memex
DARPA MEMEX project Vagrant VM

Notes:
Since the Guest OS is behind a NAT, we set the SPARK_LOCAL_IP using the tun0 interface as the IP address.  This is done in the spark-env.sh file

Please make sure that vbguest is installed (https://github.com/dotless-de/vagrant-vbguest/).  It helps keep the VirtuaalBoxGuestAdditions up to date, install on the cmd line:
```
vagrant plugin install vagrant-vbguest
```
When starting this on Linux, do not use your distribution's Vagrant package to install vagrant. Install from https://www.vagrantup.com to ensure you're using the latest version of Vagrant. Using old versions of Vagrant will result in errors.
