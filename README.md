# kraken-public-api-qa

Description
-----------

This project has been built by using gradle build, and this contains 2 internal modules,

1. kraken-websocket-client
2. kraken-websocket-qa

#### kraken-websocket-client
- This module is having the Java WebSocket client which is making connection to the Kraken public
APIs. This Client module is compiled and this will act as a client app to the QA module.
#### kraken-websocket-qa
- This module is having the cucumber scenarios in BDD format for Kraken public data feed which will use the client module for
connection and data processed by it. 
The cucumber scenarios are divided into 2 parts, 1. Happy_Path, 2. Negative_Path

#### Happy Path Scenarios:

These scenarios are with the public data feed of Book, OHLC, Spread, Ticker, Trade feeds. Each scenario has been
validated with the "subscribe" and "unsubscribe" events and their appropriate schema. Due to time constraints I have
covered single currency validation in happy path.

#### Negative Path Scenarios:

Here I have covered with scenarios of invalid depth, invalid interval, invalid events, invalid feed name, un supported
currencies Still lots of scenarios in my mind based on the error message
in "https://docs.kraken.com/websockets/#message-subscribe", but I couldn't make it on time.

Libraries Used
-------------------

Used most of all latest version of libraries.

- Java-WebSocket is for WebSocket client
- io.cucumber is for BDD approach
- hamcrest-all and junit assertion are for data validation
- jackson.core is for serialization,de-serialization
- everit.json.schema is for schema validation
- slf4j-simple is for application/test logging

Run Regression Suite
--------------------
We can run this Regression suite with the following ways.

#### Local Run

1. Via RunCuke file -> which is available in the qa module(src/test/java/RunCuke), by providing the @Regression tag
2. Via gradle task -> from the IDE Terminal or from gitBash provide the below command and run the whole suite, we can
   change the tag in the gradle file for Cucumber task so that will run the specified scenarios

```
gradle clean build
```

#### Jenkins Build

This can be plugged in any standalone Jenkins job or pipelines. This project has been successfully built and tests has
been executed. Intermittently some of happy path is failing but those are passed when executed separately.

Regression Test Results
-----------------------
Regression Cucumber Report will be generated and will be available in target folder. The local results can be found in
the below foler

```
kraken-websocket-qa/target/
```

Logging mechanism
-----------------
Here slf4j.Logger is helping to make the application logs as well as the cucumber test logs. Possible to store the logs
in a fie whatever we are getting in the feed but due to time constraints not it used to tail the logs in the console(
IDE/Jenkins)

