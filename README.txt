To run the jetty server, execute the following command from webkarma top directory:
	-- mvn jetty:run
Point your browser to http://localhost:8080/web-karma.html

NOTE: To start it on a port other than 8080 (e.g. Port number 9999): mvn -D jetty.port=9999 jetty:run