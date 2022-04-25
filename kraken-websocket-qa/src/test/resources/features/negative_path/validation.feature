@Regression
Feature: Validate the Kraken WebSocket API by providing invalid depth in public data feed

  @Testing
  Scenario Outline: Validate the Kraken public data feed with invalid depth
    Given user connects to the WebSocket API
    When user creates a subscription request for kraken public data feed
      | event     | currency_pair | feed_name   | interval |
      | subscribe | XBT/USD       | <feed_name> | 25     |
    And user submits a request to subscribe public data feed
    Then user verifies that the subscription is not successful
      | event     | currency_pair | feed_name   | depth | error_message   |
      | subscribe | XBT/USD       | <feed_name> | 25    | <error_message> |
    And user verifies subscription message is not received
    And user closes the connection

    Examples:
      | feed_name | error_message                                                |
      | book      | Unsupported field: 'interval' for the given msg type: subscribe |
#      | ohlc      | Unsupported field: 'depth' for the given msg type: subscribe |
#      | spread    | Unsupported field: 'depth' for the given msg type: subscribe |
#      | ticker    | Unsupported field: 'depth' for the given msg type: subscribe |
#      | trade     | Unsupported field: 'depth' for the given msg type: subscribe |
