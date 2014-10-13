Karma RDF Generation Service
================================



## Generate RDF

**End Point:** ```/rdf/r2rml/rdf```

**Method:** POST

**Parameters:**

| Parameter | Description | Required |
| --------- | ----------- | -------- |
| R2rmlURI  | URI of the R2RML Model | Yes |
| ContentType | Type of the data: CSV, JSON or XML | Yes |
| DataURL | URL of the Data or Data Service | Yes, either RawData or DataURL need to be provided |
| RawData | Raw CSV/JSON or XML Content | Yes, either RawData or DataURL need to be provided |

If both DatURL and RawData are provided, the service will only use DataURL

**Examples:**
```
curl --request POST --data 'R2rmlURI=file:/Users/karma/github/Web-Karma/karma-web-services/web-services-rdf/src/test/resources/metadata.json-model.ttl&ContentType=JSON&RawData={"metadata":{"GPSTimeStamp":"NOT_AVAILABLE","ISOSpeedRatings":"100","Orientation":"6","Model":"GT-N7100","WhiteBalance":"0","GPSLongitude":"NOT_AVAILABLE","ImageLength":"2448","FocalLength":"3.7","HasFaces":"1","ImageName":"20140707_134558.jpg","GPSDateStamp":"NOT_AVAILABLE","Flash":"0","DateTime":"2014:07:07 13:45:58","NumberOfFaces":"1","ExposureTime":"0.020","GPSProcessingMethod":"NOT_AVAILABLE","FNumber":"2.6","ImageWidth":"3264","GPSLatitude":"NOT_AVAILABLE","GPSAltitudeRef":"-1","Make":"SAMSUNG","GPSAltitude":"-1.0"}}' http://karma-server/rdf/r2rml/rdf
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
| ContentType | Type of the data: CSV, JSON or XML | Yes |
| DataURL | URL of the Data or Data Service | Yes, either RawData or DataURL need to be provided |
| RawData | Raw CSV/JSON or XML Content | Yes, either RawData or DataURL need to be provided |
| SparqlEndPoint | Endpoint of the Sesame/Virtuoso Server | Yes |
| GraphURI | Graph URI to publish to | Yes |
| TripleStore | Indicates type of triple store: Sesame or Virtuoso | Yes |
| UserName | Username used for authenticating for Virtuoso | Only for Virtuoso |
| Password | Password used for authenticating for Virtuoso | Only for Virtuoso |

If both DatURL and RawData are provided, the service will only use DataURL

**Examples:**
```
curl --request POST --data 'R2rmlURI=file:/Users/karma/github/Web-Karma/karma-web-services/web-services-rdf/src/test/resources/metadata.json-model.ttl&ContentType=JSON&RawData={"metadata":{"GPSTimeStamp":"NOT_AVAILABLE","ISOSpeedRatings":"100","Orientation":"6","Model":"GT-N7100","WhiteBalance":"0","GPSLongitude":"NOT_AVAILABLE","ImageLength":"2448","FocalLength":"3.7","HasFaces":"1","ImageName":"20140707_134558.jpg","GPSDateStamp":"NOT_AVAILABLE","Flash":"0","DateTime":"2014:07:07 13:45:58","NumberOfFaces":"1","ExposureTime":"0.020","GPSProcessingMethod":"NOT_AVAILABLE","FNumber":"2.6","ImageWidth":"3264","GPSLatitude":"NOT_AVAILABLE","GPSAltitudeRef":"-1","Make":"SAMSUNG","GPSAltitude":"-1.0"}}&SparqlEndPoint=http://fusion-sqid.isi.edu:8890/sparql-graph-crud-auth/&GraphURI=http://fusion-sqid.isi.edu:8890/image-metadata&TripleStore=Virtuoso&UserName=test&Password=test' http://karma-server/rdf/r2rml/sparql
```

```
curl --request POST --data 'R2rmlURI=file:/Users/karma/karma-files/schedule-model.txt&ContentType=CSV&DataURL=file:/Users/karma/karma-files/schedule.csv&SparqlEndPoint=http://fusion-sqid.isi.edu:8890/sparql-graph-crud-auth/&GraphURI=http://fusion-sqid.isi.edu:8890/image-metadata&TripleStore=Virtuoso&UserName=test&Password=test' http://karma-server/rdf/r2rml/sparql
```

----------
## Publish RDF to Sesame / Virtuoso and return the RDF

**End Point:** ```/rdf/r2rml/rdf/sparql```

**Method:** POST

**Parameters:**

| Parameter | Description | Required |
| --------- | ----------- | -------- |
| R2rmlURI  | URI of the R2RML Model | Yes |
| ContentType | Type of the data: CSV, JSON or XML | Yes |
| DataURL | URL of the Data or Data Service | Yes, either RawData or DataURL need to be provided |
| RawData | Raw CSV/JSON or XML Content | Yes, either RawData or DataURL need to be provided |
| SparqlEndPoint | Endpoint of the Sesame/Virtuoso Server | Yes |
| GraphURI | Graph URI to publish to | Yes |
| TripleStore | Indicates type of triple store: Sesame or Virtuoso | Yes |
| UserName | Username used for authenticating for Virtuoso | Only for Virtuoso |
| Password | Password used for authenticating for Virtuoso | Only for Virtuoso |

If both DatURL and RawData are provided, the service will only use DataURL

**Examples:**
```
curl --request POST --data 'R2rmlURI=file:/Users/karma/github/Web-Karma/karma-web-services/web-services-rdf/src/test/resources/metadata.json-model.ttl&ContentType=JSON&RawData={"metadata":{"GPSTimeStamp":"NOT_AVAILABLE","ISOSpeedRatings":"100","Orientation":"6","Model":"GT-N7100","WhiteBalance":"0","GPSLongitude":"NOT_AVAILABLE","ImageLength":"2448","FocalLength":"3.7","HasFaces":"1","ImageName":"20140707_134558.jpg","GPSDateStamp":"NOT_AVAILABLE","Flash":"0","DateTime":"2014:07:07 13:45:58","NumberOfFaces":"1","ExposureTime":"0.020","GPSProcessingMethod":"NOT_AVAILABLE","FNumber":"2.6","ImageWidth":"3264","GPSLatitude":"NOT_AVAILABLE","GPSAltitudeRef":"-1","Make":"SAMSUNG","GPSAltitude":"-1.0"}}&SparqlEndPoint=http://fusion-sqid.isi.edu:8890/sparql-graph-crud-auth/&GraphURI=http://fusion-sqid.isi.edu:8890/image-metadata&TripleStore=Virtuoso&UserName=test&Password=test' http://karma-server/rdf/r2rml/rdf/sparql
```

```
curl --request POST --data 'R2rmlURI=file:/Users/karma/karma-files/schedule-model.txt&ContentType=CSV&DataURL=file:/Users/karma/karma-files/schedule.csv&SparqlEndPoint=http://fusion-sqid.isi.edu:8890/sparql-graph-crud-auth/&GraphURI=http://fusion-sqid.isi.edu:8890/image-metadata&TripleStore=Virtuoso&UserName=test&Password=test' http://karma-server/rdf/r2rml/rdf/sparql
```



	
