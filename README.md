# kraken-public-api-qa

Description
-----------

This project has been built by using gradle build, and this contains 2 internal modules,

1. kraken-websocket-client
2. kraken-websocket-qa

The module kraken-websocket-client is having the Java WebSocket client which is consuming the Kraken public API.
This Client module is compiled and will act as a client library to the QA module.
The module kraken-websocket-qa is having the cucumber scenarios in BDD format for Kraken public data feed which will use the client module for connection and data processed by it.
The cucumber scenarios are divided into 2 parts, 1. Happy_Path, 2. Negative_Path

#### Happy Path Scenarios:
These scenarios are with the public data feed of Book, OHLC, Spread, Ticker, Trade feeds.
Each scenario has been validated with the "subscribe" and "unsubscribe" events and their appropriate schema.
Due to time constraints I have covered single currency validation in happy path.

#### Negative Path Scenarios:
Here I have covered with scenarios of invalid depth, invalid interval, invalid events, invalid feed name, un supported currencies
Still lots of scenarios in my mind based on the error message in "https://docs.kraken.com/websockets/#message-subscribe", but I couldn't make it on time.

Libraries Used
-------------------

Used most of all latest version of libraries.
- Java-WebSocket is for WebSocket client
- io.cucumber is for BDD approach
- hamcrest-all and junit assertion are for data validation
- jackson.core  is for serialization,de-serialization
- everit.json.schema is for schema validation
- slf4j-simple is for application logging purpose

Run Regression Suite
--------------------
We can run this Regression suite with the following ways.

#### Local Run
1. Via RunCuke file -> which is available in the qa module(src/test/java/RunCuke), by providing the @Regression tag
2. Via gradle task -> from the IDE Terminal or from gitBash provide the below command and run the whole suite, 
we can change the tag in the gradle file for Cucumber task so that will run the specified scenarios

#### Jenkins
I have configured a small standalone Jenkins job and made it work.


```
gradle clean test
```
Test Report
-----------
Cucumber Report will be generated and will be available in target folder. Relative report path is given below.
```
/target/cucumber-html-reports/overview-features.html
```
Logs
-----------
Simple logs will be generated and will be available in target folder. Relative log path is given below.
```
/target/_log/Logging.txt
```
Dockerfile
-----------
Docker file is available in the parent folder with ENTRYPOINT as mvn test verify. Once the execution is completed check the target folder on your local for logs and reports. Below is the sample command to create and run docker images. Please execute the below command from Windows PowerShell or find the equivalent command for the current folder in the respective OS.

To Build Docker Image, Run the below command from the folder where dockerfile present.
```
docker build -t websocketapi:1 .
```
To Run the Docker Image and copy target folder to your local, Run the below command in Windows PowerShell.
```
docker run -v ${PWD}/target:/home/kraken/target websocketapi:1
```
