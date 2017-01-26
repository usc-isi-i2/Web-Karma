Karma: A Data Integration Tool
================================

The Karma tutorial at https://github.com/szeke/karma-tcdl-tutorial, also check out our [DIG web site](http://usc-isi-i2.github.io/dig/), where we use Karma extensively to process > 90M web pages.

See our [release stats](http://www.somsubhra.com/github-release-stats/?username=usc-isi-i2&repository=Web-Karma)


## What is Karma?

Karma is an information integration tool that enables users to quickly and easily integrate data from a variety of data sources including databases, spreadsheets, delimited text files, XML, JSON, KML and Web APIs. Users integrate information by modeling it according to an ontology of their choice using a graphical user interface that automates much of the process. Karma learns to recognize the mapping of data to ontology classes and then uses the ontology to propose a model that ties together these classes. Users then interact with the system to adjust the automatically generated model. During this process, users can transform the data as needed to normalize data expressed in different formats and to restructure it. Once the model is complete, users can published the integrated data as RDF or store it in a database.

You can find useful tutorials on the project Website: [http://www.isi.edu/integration/karma/](http://www.isi.edu/integration/karma/)

## Installation and Setup ##

Look in the Wiki [Installation](https://github.com/InformationIntegrationGroup/Web-Karma/wiki/Installation)

## Frequently Asked Questions ##
### How to perform offline RDF generation for a data source using a published model? ###
1. Model your source and publish it's model (the published models are located at `src/main/webapp/publish/R2RML/` inside the Karma directory).
2. To generate RDF of a CSV/JSON/XML file, go to the top level Karma directory and run the following command from terminal:
```
cd karma-offline
mvn exec:java -Dexec.mainClass="edu.isi.karma.rdf.OfflineRdfGenerator" -Dexec.args="--sourcetype 
<sourcetype> --filepath <filepath> --modelfilepath <modelfilepath> --sourcename <sourcename> --outputfile <outputfile> --JSONOutputFile<outputJSON-LD>" -Dexec.classpathScope=compile
```

	Valid argument values for sourcetype are: CSV, JSON, XML. Also, you need to escape the double quotes that go inside argument values. Example invocation for a JSON file:
```	
mvn exec:java -Dexec.mainClass="edu.isi.karma.rdf.OfflineRdfGenerator" -Dexec.args="
--sourcetype JSON 
--filepath \"/Users/shubhamgupta/Documents/wikipedia.json\" 
--modelfilepath \"/Users/shubhamgupta/Documents/model-wikipedia.n3\"
--sourcename wikipedia
--outputfile wikipedia-rdf.n3
--JSONOutputFile wikipedia-rdf.json" -Dexec.classpathScope=compile
```
3. To generate RDF of a database table, go to the top level Karma directory and run the following command from terminal:
```
cd karma-offline
mvn exec:java -Dexec.mainClass="edu.isi.karma.rdf.OfflineRdfGenerator" -Dexec.args="--sourcetype DB
--modelfilepath <modelfilepath> --outputfile <outputfile> --dbtype <dbtype> --hostname <hostname> 
--username <username> --password <password> --portnumber <portnumber> --dbname <dbname> --tablename <tablename> --JSONOutputFile<outputJSON-LD>" -Dexec.classpathScope=compile
```
	Valid argument values for `dbtype` are Oracle, MySQL, SQLServer, PostGIS, Sybase. Example invocation:
```
mvn exec:java -Dexec.mainClass="edu.isi.karma.rdf.OfflineRdfGenerator" -Dexec.args="
--sourcetype DB --dbtype SQLServer 
--hostname example.com --username root --password secret 
--portnumber 1433 --dbname Employees --tablename Person 
--modelfilepath \"/Users/shubhamgupta/Documents/db-r2rml-model.ttl\"
--outputfile db-rdf.n3
--JSONOutputFile db-rdf.json" -Dexec.classpathScope=compile
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
    <systemPath>/Users/karma/Web-Karma/lib/ojdbc14.jar</systemPath> 
</dependency> 
```
Make sure that the filename mentioned in the `systemPath` element matches with your downloaded JAR file; it is likely that your installation folder is different from `/Users/karma` so make sure you use the correct one.

### Are there additional steps required to import data from MySQL database? ###
Yes. Due to MySQL binary license issues, we can't distribute the JAR file that is required for importing data from an MySQL database. Following are the steps to resolve the runtime error that you will get if you try to do it with the current source code:

1. Download the appropriate MySQL driver JAR file (for JDK 1.5 and above) that matches your MySQL version. Link: http://dev.mysql.com/downloads/connector/j/
2. Put the downloaded JAR file inside `lib` folder of the Karma source code. 
3. Add the following snippet in the `pom.xml` file of the `karma-jdbc` project inside the dependencies XML element: 

```
<dependency> 
    <groupId>mysql</groupId> 
    <artifactId>mysql-connector-java</artifactId> 
    <version>5.1.32</version> 
    <scope>system</scope> 
    <systemPath>/Users/karma/Web-Karma/lib/mysql-connector-java-5.1.32-bin.jar</systemPath> 
</dependency> 
```
Make sure that the filename mentioned in the `systemPath` element matches with your downloaded JAR file; it is likely that your installation folder is different from `/Users/karma` so make sure you use the correct one. The `version` will be the version of the JAR that you downloaded.


