@Regression @Invalid_Feed_Name @Negative_Path
Feature: Validate the Kraken WebSocket API by providing invalid feed name in public data feed

  @Invalid_Feed_Name_Validation
  Scenario Outline: Validate the Kraken public data feed with invalid feed name
    Given user connects to the WebSocket API
    When user creates a subscription request for kraken public data feed
      | event   | currency_pair | feed_name    |
      | <event> | XBT/USD       | invalid_feed |
    And user submits a request to subscribe public data feed
    Then user verifies that the subscription is not successful
      | event   | currency_pair | feed_name    | error_message   |
      | <event> | XBT/USD       | invalid_feed | <error_message> |
    And user verifies subscription message is not received
    And user closes the connection

    Examples:
      | event       | error_message             |
      | subscribe   | Subscription name invalid |
      | unsubscribe | Subscription name invalid |
