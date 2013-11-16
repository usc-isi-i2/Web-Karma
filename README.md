Karma: A Data Integration Tool
================================
[![Build Status](http://colo-vm41.isi.edu:8080/buildStatus/icon?job=WebKarmaBuildCI)](http://colo-vm41.isi.edu:8080/job/WebKarmaBuildCI/)

Karma needs **Java 1.7, Maven 3.0**. Download Java SE from [http://www.oracle.com/technetwork/java/javase/downloads/index.html](http://www.oracle.com/technetwork/java/javase/downloads/index.html)


Karma is an information integration tool that enables users to quickly and easily integrate data from a variety of data sources including databases, spreadsheets, delimited text files, XML, JSON, KML and Web APIs. Users integrate information by modeling it according to an ontology of their choice using a graphical user interface that automates much of the process. Karma learns to recognize the mapping of data to ontology classes and then uses the ontology to propose a model that ties together these classes. Users then interact with the system to adjust the automatically generated model. During this process, users can transform the data as needed to normalize data expressed in different formats and to restructure it. Once the model is complete, users can published the integrated data as RDF or store it in a database.

You can find useful tutorials on the project Website: [http://www.isi.edu/integration/karma/](http://www.isi.edu/integration/karma/)

## Installation and Setup ##
System Requirements: **Java 1.7, Maven 3.0** and above.

To run the jetty server, execute the following command from webkarma top directory:
`mvn jetty:run`. Once the server has started point your browser to **http://localhost:8080/web-karma.html**. To start it on a port other than 8080 (e.g. Port number 9999) 
`mvn -Djetty.port=9999 jetty:run`

To start in logging mode (where all the logs are stored in the log folder), use the following command to start the server:
	`mvn -Dslf4j=false -Dlog4j.configuration=file:./config/log4j.properties jetty:run`


## Frequently Asked Questions ##
### How to perform offline RDF generation for a data source using a published model? ###
1. Model your source and publish it's model (the published models are located at `src/main/webapp/publish/R2RML/` inside the Karma directory).
2. To generate RDF of a CSV/JSON/XML file, go to the top level Karma directory and run the following command from terminal:
```
mvn exec:java -Dexec.mainClass="edu.isi.karma.rdf.OfflineRdfGenerator" -Dexec.args="--sourcetype 
<sourcetype> --filepath <filepath> --modelfilepath <modelfilepath> --outputfile <outputfile>"
```

	Valid argument values for sourcetype are: CSV, JSON, XML. Also, you need to escape the double quotes that go inside argument values. Example invocation for a JSON file:
```	
mvn exec:java -Dexec.mainClass="edu.isi.karma.rdf.OfflineRdfGenerator" -Dexec.args="
--sourcetype JSON 
--filepath \"/Users/shubhamgupta/Documents/wikipedia.json\" 
--modelfilepath \"/Users/shubhamgupta/Documents/model-wikipedia.n3\" 
--outputfile wikipedia-rdf.n3"
```
3. To generate RDF of a database table, go to the top level Karma directory and run the following command from terminal:
```
mvn exec:java -Dexec.mainClass="edu.isi.karma.rdf.OfflineRdfGenerator" -Dexec.args="--sourcetype DB
--modelfilepath <modelfilepath> --outputfile <outputfile> --dbtype <dbtype> --hostname <hostname> 
--username <username> --password <password> --portnumber <portnumber> --dbname <dbname> --tablename <tablename>"
```
	Valid argument values for `dbtype` are Oracle, MySQL, SQLServer, PostGIS. Example invocation:
```
mvn exec:java -Dexec.mainClass="edu.isi.karma.rdf.OfflineRdfGenerator" -Dexec.args="
--sourcetype DB --dbtype SQLServer 
--hostname example.com --username root --password secret 
--portnumber 1433 --dbname Employees --tablename Person 
--modelfilepath \"/Users/shubhamgupta/Documents/db-r2rml-model.ttl\"
--outputfile db-rdf.n3"
```

You can do `mvn exec:java -Dexec.mainClass="edu.isi.karma.rdf.OfflineRdfGenerator" -Dexec.args="--help"` to get information about required arguments.

### How to set up password protection for accessing Karma? ###
- in /src/main/config/jettyrealm.properties change user/password (if you wish)
- in /src/main/webapp/WEB-INF/web.xml uncomment security section at the end of the file
- in pom.xml uncomment security section (search for loginServices)

### Are there additional steps required to import data from Oracle database? ###
Yes. Due to Oracles binary license issues, we can't distribute the JAR file that is required for importing data from an Oracle database. Following are the steps to resolve the runtime error that you will get if you try to do it with the current source code:

1. Download the appropriate JDBC drive JAR file (for JDK 1.5 and above) that matches your Oracle DB version. Link: http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html
2. Put the downloaded JAR file inside `lib` folder of the Karma source code. 
3. Add the following snippet in the `pom.xml` file (present inside the top level folder of Karma source code) inside the dependencies XML element: 

```
<dependency> 
    <groupId>com.oracle</groupId> 
    <artifactId>ojdbc</artifactId> 
    <version>14</version> 
    <scope>system</scope> 
    <systemPath>${project.basedir}/lib/ojdbc14.jar</systemPath> 
</dependency> 
```
Make sure that the filename mentioned in the `systemPath` element matches with your downloaded JAR file.
