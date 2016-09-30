Karma-app
=========
Karma desktop app for linux, windows and mac using which you can easily launch Karma.

![](https://github.com/usc-isi-i2/Web-Karma/wiki/images/karma-app.png)

##Installation
*  Install [java](https://www.java.com/en/download/help/download_options.xml) if not already installed: Java version 1.7 or higher. Installation of the right version of java is important. For instance, if you are running a 64bit windows, you have to download 64bit java for windows.

* Download [karma app](https://github.com/usc-isi-i2/Web-Karma/releases) for your operating system. Unzip into a local folder

* Depending on the OS, do the following:
  ###Linux
  Navigate to the folder and run `./Karma` on terminal.
  
  ###Mac
  Navigate to the folder, copy `Karma.app` to your Applications Folder and then open `Karma.app` from your Applications folder

  ###Windows
  * [Set JRE_HOME](https://confluence.atlassian.com/doc/setting-the-java_home-variable-in-windows-8895.html) environmment variable. 
  * Navigate to the folder and open `Karma.exe`

##Usage
Once Karma app is opened, it automatically starts the server and launches on a web browser (It can take sometime the first time). In order to open a new instance on the browser, you can either click on "Open New Window" on the app or open `localhost:8080` on a new window in the browser.

##Configuration
You can set maximum heap size for karma on the main window. Once you change it, you need to __restart karma__.

![](https://github.com/usc-isi-i2/Web-Karma/wiki/images/karma-app-heap.png)


##Building the app from code.

1. Install:

  1. latest [node](https://nodejs.org/en/) 
  2. electron-packager - `sudo npm install -g electron-packager`
  3. wget - `brew install wget`
  4. wine -
      * if building on Mac[](https://www.davidbaumgold.com/tutorials/wine-mac/)
      
        ```
        brew doctor
        brew update
        brew tap caskroom/cask
        brew cask install xquartz
        brew install wine     
        ```
      * if building on Linux: `sudo apt-get install wine`
      
2. Clone this repository and navigate to `karma-app` folder.
3. Make sure the npm python points to 2.7 version `npm config set python python2.7`
4. Run `./build --archive` to build and generate the zip files for all OS
5. The builds for all operating systems will be found in the target folder in `karma-app` folder.

Karma app is built for linux, windows and mac for both ia32 and x64 versions as follows when `./build` is run:

1. Navigates to karma-web directory and creates a shaded .war file.
2. Copies the .war snapshot and other .war files in external_webapps to the target folder.
3. Downloads apache tomcat.
4. Copies the war files to webapps folder in tomcat.
5. Copies conf files to tomcat folder.
6. Navigates to the electron app folder (desktop) and copies the tomcat folder to app directory.
7. Then we install all the dependencies and create a pre-release.
8. We then use electron-packager to create packages for all the OSes and their flavors.
9. The packages built are then copied to the target folder.
10. If the build script is run wit `--archive` option, it will create zip archives of all the builds.
