@Regression
Feature: Validate the Kraken WebSocket API by subscribing the Trade public-data feed

  @Trade_Feed_Validation
  Scenario Outline: Validate the Ticker public-data feed for Kraken events subscription and un-subscription
    Given user connects to the WebSocket API
    When user creates a subscription request for kraken public data feed
      | event     | currency_pair   | feed_name |
      | subscribe | <currency_pair> | trade     |
    And user submits a request to subscribe public data feed
    Then user verifies that the subscription is successful
      | event     | currency_pair   | feed_name |
      | subscribe | <currency_pair> | trade     |
    And user verifies that the feed is received the subscription message
    Then user performs the schema validation on "subscriptionStatus"
    Then user performs the schema validation on "trade"
    When user creates an un-subscription request for kraken public data feed
      | event       | currency_pair   | feed_name |
      | unsubscribe | <currency_pair> | trade     |
    And user submits a request to unsubscribe public data feed
    Then user verifies that the un-subscription is successful
      | event       | currency_pair   | feed_name |
      | unsubscribe | <currency_pair> | trade     |
    And user verifies that the feed is not received the un-subscription message
    And user closes the connection
    Examples:
      | currency_pair |
      | ETH/USD       |
#      | XBT/USD       |
#      | ETH/GBP       |
#      | BAL/USD       |
#      | FIDA/USD      |
