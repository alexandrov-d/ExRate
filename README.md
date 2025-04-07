# Test Challenge
**All 4 Required API operations are covered by 2 endpoints with optional parameters**


## Requirements:
- java 21
- free 8080 port

## How to run application
- switch to application folder
- build project with on Unix:
  ```./mvnw clean package``` or ```/\mvnw.cmd clean package``` for windows
- run application
```shell
java -jar target/ExRate-0.0.1-SNAPSHOT.jar 
```
- OR run application with https://freecurrencyapi.com/ api key
```shell
API_KEY=<YOUR_API_KEY>
java -jar target/ExRate-0.0.1-SNAPSHOT.jar  --freeCurrency.apiKey=$API_KEY
```

## Swagger documentation
http://localhost:8080/swagger-ui/index.html

## Assumptions
- no need to keep historical data for any analysis
- If rates are unavailable for more than 1 minute, respond with an error and do not fall back to previously known values to avoid any exploits

## Possible Improvements
- apiKey must not be in version control and can be provided via external variable or other way
- Integration tests to test all layers at once
- if https://freecurrencyapi.com/ api unavailable better to fallback ot another exchange rate provider   








