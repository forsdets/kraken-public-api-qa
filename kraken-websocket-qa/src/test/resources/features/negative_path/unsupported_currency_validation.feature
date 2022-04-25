@Regression
Feature: Validate the Kraken WebSocket API by providing invalid currency pair in public data feed

  @Unsupported_Currency_Validation_1
  Scenario Outline: Validate the Kraken public data feed with invalid currency pair for subscribe events
    Given user connects to the WebSocket API
    When user creates a subscription request for kraken public data feed
      | event     | currency_pair   | feed_name   |
      | subscribe | <currency_pair> | <feed_name> |
    And user submits a request to subscribe public data feed
    Then user verifies that the subscription is not successful
      | event     | currency_pair   | feed_name   | error_message   |
      | subscribe | <currency_pair> | <feed_name> | <error_message> |
    And user verifies subscription message is not received
    And user closes the connection

    Examples:
      | currency_pair | feed_name | error_message                       |
      | XBT/ABC       | book      | Currency pair not supported XBT/ABC |
      | XBT/ABC       | ohlc      | Currency pair not supported XBT/ABC |
      | XBT/ABC       | spread    | Currency pair not supported XBT/ABC |
      | XBT/ABC       | ticker    | Currency pair not supported XBT/ABC |
      | XBT/ABC       | trade     | Currency pair not supported XBT/ABC |

  @Unsupported_Currency_Validation_2
  Scenario Outline: Validate the Kraken public data feed with invalid currency pair for unsubscribe events
    Given user connects to the WebSocket API
    When user creates a subscription request for kraken public data feed
      | event       | currency_pair   | feed_name   |
      | unsubscribe | <currency_pair> | <feed_name> |
    And user submits a request to subscribe public data feed
    Then user verifies that the subscription is not successful
      | event       | currency_pair   | feed_name   | error_message   |
      | unsubscribe | <currency_pair> | <feed_name> | <error_message> |
    And user verifies subscription message is not received
    And user closes the connection

    Examples:
      | currency_pair | feed_name | error_message                       |
      | XBT/ABC       | book      | Currency pair not supported XBT/ABC |
      | XBT/ABC       | ohlc      | Currency pair not supported XBT/ABC |
      | XBT/ABC       | spread    | Currency pair not supported XBT/ABC |
      | XBT/ABC       | ticker    | Currency pair not supported XBT/ABC |
      | XBT/ABC       | trade     | Currency pair not supported XBT/ABC |