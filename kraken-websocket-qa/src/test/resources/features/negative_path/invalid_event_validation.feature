@Regression @Invalid_Event @Negative_Path
Feature: Validate the Kraken WebSocket API by providing invalid event in public data feed

  @Invalid_Event_Validation
  Scenario Outline: Validate the Kraken public data feed with invalid event
    Given user connects to the WebSocket API
    When user creates a subscription request for kraken public data feed
      | event      | currency_pair | feed_name   |
      | subscribed | XBT/USD       | <feed_name> |
    And user submits a request to subscribe public data feed
    Then user verifies that the subscription is not successful
      | event      | currency_pair | feed_name   | error_message   |
      | subscribed | XBT/USD       | <feed_name> | <error_message> |
    And user verifies subscription message is not received
    And user closes the connection

    Examples:
      | feed_name | error_message     |
      | book      | Unsupported event |
      | ohlc      | Unsupported event |
      | spread    | Unsupported event |
      | ticker    | Unsupported event |
      | trade     | Unsupported event |
