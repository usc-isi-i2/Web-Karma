Karma Elastic Search Publish Service
================================



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
curl --request POST --data 'DataURL=file:/Users/karma/webpage-sample.json' http://localhost:8080/publish/es/data
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
curl --request POST --data 'DataURL=file:/Users/karma/webpage-jsonld.json' http://localhost:8080/publish/es/jsonld
```
