@Regression @Invalid_Interval @Negative_Path
Feature: Validate the Kraken WebSocket API by providing invalid interval in public data feed

  @Invalid_Interval_Validation_1
  Scenario Outline: Validate the Kraken public data feed with invalid interval
    Given user connects to the WebSocket API
    When user creates a subscription request for kraken public data feed
      | event     | currency_pair | feed_name   | interval |
      | subscribe | XBT/USD       | <feed_name> | 25       |
    And user submits a request to subscribe public data feed
    Then user verifies that the subscription is not successful
      | event     | currency_pair | feed_name   | interval | error_message   |
      | subscribe | XBT/USD       | <feed_name> | 25       | <error_message> |
    And user verifies subscription message is not received
    And user closes the connection

    Examples:
      | feed_name | error_message                                                   |
      | book      | Unsupported field: 'interval' for the given msg type: subscribe |
      | ohlc      | Unsupported field: 'interval' for the given msg type: subscribe |
      | spread    | Unsupported field: 'interval' for the given msg type: subscribe |
      | ticker    | Unsupported field: 'interval' for the given msg type: subscribe |
      | trade     | Unsupported field: 'interval' for the given msg type: subscribe |

  @Invalid_Interval_Validation_2
  Scenario Outline: Validate the Kraken public data feed with invalid depth
    Given user connects to the WebSocket API
    When user creates a subscription request for kraken public data feed
      | event       | currency_pair | feed_name   | interval |
      | unsubscribe | XBT/USD       | <feed_name> | 35       |
    And user submits a request to subscribe public data feed
    Then user verifies that the subscription is not successful
      | event       | currency_pair | feed_name   | interval | error_message   |
      | unsubscribe | XBT/USD       | <feed_name> | 35       | <error_message> |
    And user verifies subscription message is not received
    And user closes the connection

    Examples:
      | feed_name | error_message                                                     |
      | book      | Unsupported field: 'interval' for the given msg type: unsubscribe |
      | ohlc      | Unsupported field: 'interval' for the given msg type: unsubscribe |
      | spread    | Unsupported field: 'interval' for the given msg type: unsubscribe |
      | ticker    | Unsupported field: 'interval' for the given msg type: unsubscribe |
      | trade     | Unsupported field: 'interval' for the given msg type: unsubscribe |
