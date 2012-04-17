To run the jetty server, execute the following command from webkarma top directory:
	mvn jetty:run
Point your browser to http://localhost:8080/web-karma.html

NOTE: To start it on a port other than 8080 (e.g. Port number 9999): mvn -Djetty.port=9999 jetty:run

To start in logging mode (where all the logs are stored in the log folder), use the following command to start the server:
	mvn -Dslf4j=false -Dlog4j.configuration=file:./config/log4j.properties jetty:run

**To set up password protection
- in /config/jettyrealm.properties change user/password (if you wish)
- in /src/main/webapp/WEB-INF/web.xml uncomment security section at the end of the file
- in pom.xml uncomment security section (search for loginServices)
