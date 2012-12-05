Requirements: Java 1.6, Maven 3.0 and above
To run the jetty server, execute the following command from webkarma top directory:
	mvn jetty:run
Point your browser to http://localhost:8080/web-karma.html

NOTE: To start it on a port other than 8080 (e.g. Port number 9999): mvn -Djetty.port=9999 jetty:run

To start in logging mode (where all the logs are stored in the log folder), use the following command to start the server:
	mvn -Dslf4j=false -Dlog4j.configuration=file:./config/log4j.properties jetty:run

*** To set up password protection ***
- in /config/jettyrealm.properties change user/password (if you wish)
- in /src/main/webapp/WEB-INF/web.xml uncomment security section at the end of the file
- in pom.xml uncomment security section (search for loginServices)

*** Offline RDF Generation from a database ***
1. Model your source and publish it's model.
2. From the command line, go to the top level Karma directory and run the following command:
	mvn exec:java -Dexec.mainClass="edu.isi.karma.rdf.OfflineDbRdfGenerator" -Dexec.args="[PATH TO YOUR MODEL FILE] [OUTPUT RDF FILE NAME/PATH] [DATABASE PASSWORD]"

	e.g. mvn exec:java -Dexec.mainClass="edu.isi.karma.rdf.OfflineDbRdfGenerator" -Dexec.args="model.n3 result.n3 secretPassword"
	Above command will use the ObjCurLocView.n3 model file to pubish a RDF file named result.n3

*** Offline RDF Generation from a CSV file (Feature courtesy of https://github.com/cgueret) ***
1. Model your source and publish it's model.
2. From the command line, go to the top level Karma directory and run the following command:
	mvn exec:java -Dexec.mainClass="edu.isi.karma.rdf.OfflineCSVGenerator" -Dexec.args="[PATH TO YOUR MODEL FILE] [CSV DATA FILE] [OUTPUT RDF FILE NAME/PATH]"
NOTE: Currently only tab-delimited kind of CSV files are supported.

	e.g. mvn exec:java -Dexec.mainClass="edu.isi.karma.rdf.OfflineCSVGenerator" -Dexec.args="model.n3 data.csv result.n3"
	Above command will use the model.n3 model file to pubish a RDF file named result.n3
	
NOTE: In Maven Jetty plugin based Karma deployment, the published models are located at src/main/webapp/repository/sources/ inside the Karma directory.


*** Steps for importing data from an Oracle database ***
Due to Oracles binary license issues, we can't distribute the JAR file that is required for importing data from an Oracle database. Following are the steps to resolve the runtime error that you will get if you try to do it with the current source code:

1. Download the appropriate JDBC drive JAR file (for JDK 1.5 and above) that matches your Oracle DB version. Link: http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html
2. Put the downloaded JAR file inside "lib" folder of the Karma source code. 
3. Add the following snippet in the "pom.xml" file (present inside the top level folder of Karma source code) inside the dependencies XML element: 

<dependency> 
    <groupId>com.oracle</groupId> 
    <artifactId>ojdbc</artifactId> 
    <version>14</version> 
    <scope>system</scope> 
    <systemPath>${project.basedir}/lib/ojdbc14.jar</systemPath> 
</dependency> 

Make sure that the filename mentioned in the "systemPath" element matches with your downloaded JAR file.