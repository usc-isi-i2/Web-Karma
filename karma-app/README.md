Karma-app
=========

Karma app is built for linux, windows and mac for both ia32 and x64 versions as follows
1. Navigates to karma-web directory and creates a shaded .war file.
2. Copies the .war snapshot and other .war files in external_webapps to the target folder.
3. Downloads apache tomcat.
4. Copies the war files to webapps folder in tomcat.
5. Copies conf files to tomcat folder.
6. Navigates to the electron app folder (desktop) and copies the tomcat folder to app directory.
7. Then we install all the dependencies and create a pre-release.
8. We then use electron-packager to create packages for all the OSes and their flavors.
9. The packages built are then copied to the target folder.
