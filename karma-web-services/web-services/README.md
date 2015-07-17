Karma Web Generation Service
================================

This service allows you to generate RDF, jsonld and publish the jsonld to elasticsearch. Check following examples.

## Generate RDF

**End Point:** ```/rdf/r2rml/rdf```

**Method:** POST

**Parameters:**

| Parameter | Description | Required |
| --------- | ----------- | -------- |
| R2rmlURI  | URI of the R2RML Model | Yes |
| ContentType | Type of the data: CSV, JSON, XML or EXCEL | Yes |
| DataURL | URL of the Data or Data Service | Yes, either RawData or DataURL need to be provided |
| RawData | Raw CSV/JSON or XML Content | Yes, either RawData or DataURL need to be provided |
| Encoding | Encoding of the data | No, if not provided, Karma will auto-detect the encoding |
| MaxNumLines | Maxinum number of lines or objects to import. -1 to import all | No, defaults to -1 to import all |
| ColumnDelimiter | Column Delimiter for delimiter data sources | No, "," assumed if source is CSV |
| HeaderStartIndex | 1 based index of the header | No, defaults to 1 or CSV and EXCEL sources |
| DataStartIndex | 1 based index where the data starts | No, defaults to 2 for CSV and EXCEL sources |
| TextQualifier | Text Qualifier for CSV and EXCEL | No, default to " |
| WorksheetIndex | 1 based index of the worksheet of the EXCEL spreadsheet that should be imported | No, defaults to 1 |

If both DatURL and RawData are provided, the service will only use DataURL

**Examples:**
```
curl --request POST --data 'R2rmlURI=file:///Users/karma/github/Web-Karma/karma-web-services/web-services-rdf/src/test/resources/metadata.json-model.ttl&ContentType=JSON&RawData={"metadata":{"GPSTimeStamp":"NOT_AVAILABLE","ISOSpeedRatings":"100","Orientation":"6","Model":"GT-N7100","WhiteBalance":"0","GPSLongitude":"NOT_AVAILABLE","ImageLength":"2448","FocalLength":"3.7","HasFaces":"1","ImageName":"20140707_134558.jpg","GPSDateStamp":"NOT_AVAILABLE","Flash":"0","DateTime":"2014:07:07 13:45:58","NumberOfFaces":"1","ExposureTime":"0.020","GPSProcessingMethod":"NOT_AVAILABLE","FNumber":"2.6","ImageWidth":"3264","GPSLatitude":"NOT_AVAILABLE","GPSAltitudeRef":"-1","Make":"SAMSUNG","GPSAltitude":"-1.0"}}' http://karma-server/rdf/r2rml/rdf
```

```
curl --request POST --data 'R2rmlURI=file:/Users/karma/karma-files/schedule-model.txt&ContentType=CSV&DataURL=file:/Users/karma/karma-files/schedule.csv' http://karma-server/rdf/r2rml/rdf
```


----------
## Publish RDF to Sesame / Virtuoso
**End Point:** ```/rdf/r2rml/sparql```

**Method:** POST

**Parameters:**

| Parameter | Description | Required |
| --------- | ----------- | -------- |
| R2rmlURI  | URI of the R2RML Model | Yes |
| ContentType | Type of the data: CSV, JSON, XML or EXCEL | Yes |
| DataURL | URL of the Data or Data Service | Yes, either RawData or DataURL need to be provided |
| RawData | Raw CSV/JSON or XML Content | Yes, either RawData or DataURL need to be provided |
| Encoding | Encoding of the data | No, if not provided, Karma will auto-detect the encoding |
| MaxNumLines | Maxinum number of lines or objects to import. -1 to import all | No, defaults to -1 to import all |
| ColumnDelimiter | Column Delimiter for delimiter data sources | No, "," assumed if source is CSV |
| HeaderStartIndex | 1 based index of the header | No, defaults to 1 or CSV and EXCEL sources |
| DataStartIndex | 1 based index where the data starts | No, defaults to 2 for CSV and EXCEL sources |
| TextQualifier | Text Qualifier for CSV and EXCEL | No, default to " |
| WorksheetIndex | 1 based index of the worksheet of the EXCEL spreadsheet that should be imported | No, defaults to 1 |
| SparqlEndPoint | Endpoint of the Sesame/Virtuoso Server | Yes |
| GraphURI | Graph URI to publish to | Yes |
| TripleStore | Indicates type of triple store: Sesame or Virtuoso | Yes |
| UserName | Username used for authenticating for Virtuoso | Only for Virtuoso |
| Password | Password used for authenticating for Virtuoso | Only for Virtuoso |

If both DatURL and RawData are provided, the service will only use DataURL

**Examples:**
```
curl --request POST --data 'R2rmlURI=file:///Users/karma/github/Web-Karma/karma-web-services/web-services-rdf/src/test/resources/metadata.json-model.ttl&ContentType=JSON&RawData={"metadata":{"GPSTimeStamp":"NOT_AVAILABLE","ISOSpeedRatings":"100","Orientation":"6","Model":"GT-N7100","WhiteBalance":"0","GPSLongitude":"NOT_AVAILABLE","ImageLength":"2448","FocalLength":"3.7","HasFaces":"1","ImageName":"20140707_134558.jpg","GPSDateStamp":"NOT_AVAILABLE","Flash":"0","DateTime":"2014:07:07 13:45:58","NumberOfFaces":"1","ExposureTime":"0.020","GPSProcessingMethod":"NOT_AVAILABLE","FNumber":"2.6","ImageWidth":"3264","GPSLatitude":"NOT_AVAILABLE","GPSAltitudeRef":"-1","Make":"SAMSUNG","GPSAltitude":"-1.0"}}&SparqlEndPoint=http://fusion-sqid.isi.edu:8890/sparql-graph-crud-auth/&GraphURI=http://fusion-sqid.isi.edu:8890/image-metadata&TripleStore=Virtuoso&UserName=test&Password=test' http://karma-server/rdf/r2rml/sparql
```

```
curl --request POST --data 'R2rmlURI=file:///Users/karma/karma-files/schedule-model.txt&ContentType=CSV&DataURL=file:///Users/karma/karma-files/schedule.csv&SparqlEndPoint=http://fusion-sqid.isi.edu:8890/sparql-graph-crud-auth/&GraphURI=http://fusion-sqid.isi.edu:8890/image-metadata&TripleStore=Virtuoso&UserName=test&Password=test' http://karma-server/rdf/r2rml/sparql
```

----------
## Publish RDF to Sesame / Virtuoso and return the RDF

**End Point:** ```/rdf/r2rml/rdf/sparql```

**Method:** POST

**Parameters:**

| Parameter | Description | Required |
| --------- | ----------- | -------- |
| R2rmlURI  | URI of the R2RML Model | Yes |
| ContentType | Type of the data: CSV, JSON, XML or EXCEL | Yes |
| DataURL | URL of the Data or Data Service | Yes, either RawData or DataURL need to be provided |
| RawData | Raw CSV/JSON or XML Content | Yes, either RawData or DataURL need to be provided |
| Encoding | Encoding of the data | No, if not provided, Karma will auto-detect the encoding |
| MaxNumLines | Maxinum number of lines or objects to import. -1 to import all | No, defaults to -1 to import all |
| ColumnDelimiter | Column Delimiter for delimiter data sources | No, "," assumed if source is CSV |
| HeaderStartIndex | 1 based index of the header | No, defaults to 1 or CSV and EXCEL sources |
| DataStartIndex | 1 based index where the data starts | No, defaults to 2 for CSV and EXCEL sources |
| TextQualifier | Text Qualifier for CSV and EXCEL | No, default to " |
| WorksheetIndex | 1 based index of the worksheet of the EXCEL spreadsheet that should be imported | No, defaults to 1 |
| SparqlEndPoint | Endpoint of the Sesame/Virtuoso Server | Yes |
| GraphURI | Graph URI to publish to | Yes |
| TripleStore | Indicates type of triple store: Sesame or Virtuoso | Yes |
| UserName | Username used for authenticating for Virtuoso | Only for Virtuoso |
| Password | Password used for authenticating for Virtuoso | Only for Virtuoso |

If both DatURL and RawData are provided, the service will only use DataURL

**Examples:**
```
curl --request POST --data 'R2rmlURI=file:///Users/karma/github/Web-Karma/karma-web-services/web-services-rdf/src/test/resources/metadata.json-model.ttl&ContentType=JSON&RawData={"metadata":{"GPSTimeStamp":"NOT_AVAILABLE","ISOSpeedRatings":"100","Orientation":"6","Model":"GT-N7100","WhiteBalance":"0","GPSLongitude":"NOT_AVAILABLE","ImageLength":"2448","FocalLength":"3.7","HasFaces":"1","ImageName":"20140707_134558.jpg","GPSDateStamp":"NOT_AVAILABLE","Flash":"0","DateTime":"2014:07:07 13:45:58","NumberOfFaces":"1","ExposureTime":"0.020","GPSProcessingMethod":"NOT_AVAILABLE","FNumber":"2.6","ImageWidth":"3264","GPSLatitude":"NOT_AVAILABLE","GPSAltitudeRef":"-1","Make":"SAMSUNG","GPSAltitude":"-1.0"}}&SparqlEndPoint=http://fusion-sqid.isi.edu:8890/sparql-graph-crud-auth/&GraphURI=http://fusion-sqid.isi.edu:8890/image-metadata&TripleStore=Virtuoso&UserName=test&Password=test' http://karma-server/rdf/r2rml/rdf/sparql
```

```
curl --request POST --data 'R2rmlURI=file:///Users/karma/karma-files/schedule-model.txt&ContentType=CSV&DataURL=file:///Users/karma/karma-files/schedule.csv&SparqlEndPoint=http://fusion-sqid.isi.edu:8890/sparql-graph-crud-auth/&GraphURI=http://fusion-sqid.isi.edu:8890/image-metadata&TripleStore=Virtuoso&UserName=test&Password=test' http://karma-server/rdf/r2rml/rdf/sparql
```


## Generate JSON

**End Point:** ```/rdf/r2rml/json```

**Method:** POST

**Parameters:**

| Parameter | Description | Required |
| --------- | ----------- | -------- |
| R2rmlURI  | URI of the R2RML Model | Yes |
| ContentType | Type of the data: CSV, JSON, XML or EXCEL | Yes |
| DataURL | URL of the Data or Data Service | Yes, either RawData or DataURL need to be provided |
| RawData | Raw CSV/JSON or XML Content | Yes, either RawData or DataURL need to be provided |
| Encoding | Encoding of the data | No, if not provided, Karma will auto-detect the encoding |
| MaxNumLines | Maxinum number of lines or objects to import. -1 to import all | No, defaults to -1 to import all |
| ColumnDelimiter | Column Delimiter for delimiter data sources | No, "," assumed if source is CSV |
| HeaderStartIndex | 1 based index of the header | No, defaults to 1 or CSV and EXCEL sources |
| DataStartIndex | 1 based index where the data starts | No, defaults to 2 for CSV and EXCEL sources |
| TextQualifier | Text Qualifier for CSV and EXCEL | No, default to " |
| WorksheetIndex | 1 based index of the worksheet of the EXCEL spreadsheet that should be imported | No, defaults to 1 |

If both DatURL and RawData are provided, the service will only use DataURL
The context is generated from the model.

**Examples:**
```
curl --request POST --data 'R2rmlURI=file:///Users/karma/github/Web-Karma/karma-web-services/web-services-rdf/src/test/resources/metadata.json-model.ttl&ContentType=JSON&RawData={"metadata":{"GPSTimeStamp":"NOT_AVAILABLE","ISOSpeedRatings":"100","Orientation":"6","Model":"GT-N7100","WhiteBalance":"0","GPSLongitude":"NOT_AVAILABLE","ImageLength":"2448","FocalLength":"3.7","HasFaces":"1","ImageName":"20140707_134558.jpg","GPSDateStamp":"NOT_AVAILABLE","Flash":"0","DateTime":"2014:07:07 13:45:58","NumberOfFaces":"1","ExposureTime":"0.020","GPSProcessingMethod":"NOT_AVAILABLE","FNumber":"2.6","ImageWidth":"3264","GPSLatitude":"NOT_AVAILABLE","GPSAltitudeRef":"-1","Make":"SAMSUNG","GPSAltitude":"-1.0"}}' http://karma-server/rdf/r2rml/json
```

```
curl --request POST --data 'R2rmlURI=file:///Users/karma/karma-files/schedule-model.txt&ContentType=CSV&DataURL=file:///Users/karma/karma-files/schedule.csv' http://karma-server/rdf/r2rml/json
```

## Generate JSON-LD and publish to Elastic Search

**End Point:** ```/publish/es/data```

**Method:** POST

**Parameters:**

| Parameter | Description | Required |
| --------- | ----------- | -------- |
| R2rmlURI  | URI of the R2RML Model | No, if not provided, takes the value configured in web.xml |
| ContentType | Type of the data: CSV, JSON, XML or EXCEL | NO, default=JSON |
| DataURL | URL of the Data or Data Service | Yes, either RawData or DataURL need to be provided |
| RawData | Raw CSV/JSON or XML Content | Yes, either RawData or DataURL need to be provided |
| Encoding | Encoding of the data | No, if not provided, Karma will auto-detect the encoding |
| MaxNumLines | Maxinum number of lines or objects to import. -1 to import all | No, defaults to -1 to import all |
| ColumnDelimiter | Column Delimiter for delimiter data sources | No, "," assumed if source is CSV |
| HeaderStartIndex | 1 based index of the header | No, defaults to 1 or CSV and EXCEL sources |
| DataStartIndex | 1 based index where the data starts | No, defaults to 2 for CSV and EXCEL sources |
| TextQualifier | Text Qualifier for CSV and EXCEL | No, default to " |
| WorksheetIndex | 1 based index of the worksheet of the EXCEL spreadsheet that should be imported | No, defaults to 1 |
| ESHostname | Elastic Search Server Hostname | No, if not provided, takes the value configured in web.xml |
| ESPort | Elastic Search Port | No, if not provided, takes the value configured in web.xml |
| ESProtocol | Elastic Search Protocol(http/https) | No, if not provided, takes the value configured in web.xml |
| ESIndex | Elastic Search IndexName | No, if not provided, takes the value configured in web.xml |
| ESType | Elastic Search Document Type | No, if not provided, takes the value configured in web.xml |
| ContextRoot | Root for generating the JSON-LD | No, if not provided, takes the value configured in web.xml |
| ContextURL | URL of the context file | No, if not provided, takes the value configured in web.xml |

If both DatURL and RawData are provided, the service will only use DataURL

**Examples:**
```
curl --request POST --data 'DataURL=file:///Users/karma/webpage-sample.json' http://localhost:8080/publish/es/data
```


## Publish JSON-LD to Elastic Search

**End Point:** ```/publish/es/jsonld```

**Method:** POST

**Parameters:**

| Parameter | Description | Required |
| --------- | ----------- | -------- |
| DataURL | URL of the Data or Data Service | Yes, either RawData or DataURL need to be provided |
| RawData | Raw CSV/JSON or XML Content | Yes, either RawData or DataURL need to be provided |
| Encoding | Encoding of the data | No, if not provided, Karma will auto-detect the encoding |
| ESHostname | Elastic Search Server Hostname | No, if not provided, takes the value configured in web.xml |
| ESPort | Elastic Search Port | No, if not provided, takes the value configured in web.xml |
| ESProtocol | Elastic Search Protocol(http/https) | No, if not provided, takes the value configured in web.xml |
| ESIndex | Elastic Search IndexName | No, if not provided, takes the value configured in web.xml |
| ESType | Elastic Search Document Type | No, if not provided, takes the value configured in web.xml |

If both DatURL and RawData are provided, the service will only use DataURL

**Examples:**
```
curl --request POST --data 'DataURL=file:///Users/karma/webpage-jsonld.json' http://localhost:8080/publish/es/jsonld
```
	
