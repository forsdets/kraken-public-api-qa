package com.kraken.publicapi.tests.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraken.publicapi.client.websocketapp.SocketConnection;
import com.kraken.publicapi.client.websocketbeans.*;
import com.kraken.publicapi.client.websocketcontexts.MessageContext;
import com.kraken.publicapi.client.websocketcontexts.SocketDataContext;
import com.kraken.publicapi.tests.constants.TestConstants;
import com.kraken.publicapi.tests.contexts.TestContext;
import com.kraken.publicapi.tests.testutility.SchemaValidationUtil;
import io.cucumber.datatable.DataTable;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kraken.publicapi.tests.testutility.FileHandlingUtil.getPropertyValue;
import static org.hamcrest.MatcherAssert.assertThat;


public class StepsImplementation {
    Logger logger = LoggerFactory.getLogger(StepsImplementation.class);

    /*
    Create connection by calling the WebSocket client
     */
    public void openWebSocket() throws IOException {
        SocketDataContext context = new SocketDataContext(getPropertyValue(TestConstants.WEB_SOCKET_PROPERTY_FILE_PATH, TestConstants.WEB_SOCKET_API_URI), 60);
        SocketConnection socketConnection = new SocketConnection();
        socketConnection = socketConnection.connectToHost(context);

        ObjectMapper objectMapper = new ObjectMapper();
        SocketStatusBeans socketStatusBeans = objectMapper.readValue(socketConnection.getKrakenWebSocketClient().socketDataContext.getMessage(0).getReceivedMessage(), SocketStatusBeans.class);

        assertThat("The Socket status is offline", socketStatusBeans.getStatus().equals("online"));
        socketConnection.getKrakenWebSocketClient().socketDataContext.setStatus(socketStatusBeans.getStatus());
        TestContext.setContext("success_client_connection", socketConnection);
        logger.info(String.format("Client Connection Details --- %s", socketConnection));
    }

    /*
    Create a subscription request based on the user inputs from the Cucumber scenarios
     */
    public void createSubscriptionRequest(DataTable dataTable) throws JsonProcessingException {
        List<Map<String, String>> tableRows = dataTable.asMaps(String.class, String.class);
        Map<String, String> inputData = tableRows.get(0);

        String event = inputData.get("event").trim();
        String currencyPair = inputData.get("currency_pair").trim();
        String feedName = inputData.get("feed_name").trim();

        SubscriptionBeans subscriptionBeans = new SubscriptionBeans();
        subscriptionBeans.setName(feedName);

        List<String> pair = new ArrayList<>();
        pair.add(currencyPair);

        RequestBeans requestBeans = new RequestBeans();
        requestBeans.setEvent(event);
        requestBeans.setPair(pair);
        requestBeans.setSubscription(subscriptionBeans);

        ObjectMapper mapper = new ObjectMapper();
        JSONObject requestJSONObject = new JSONObject(mapper.writeValueAsString(requestBeans));

        if (inputData.get("interval") != null) {
            String interval = inputData.get("interval").trim();
            requestJSONObject.put("interval", Integer.parseInt(interval));
        } else if (inputData.get("depth") != null) {
            String depth = inputData.get("depth").trim();
            requestJSONObject.put("depth", Integer.parseInt(depth));
        }
        TestContext.setContext("Subscription_Request", requestJSONObject.toString());
        logger.info(String.format("Subscription Request --- %s", requestJSONObject));
    }

    /*
    Submit a subscription request based on the user inputs by calling the client method
     */
    public void submitRequest() throws InterruptedException {
        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("success_client_connection");
        String jsonString = (String) TestContext.getContext("Subscription_Request");

        socketConnection = socketConnection.subscribeMessage(jsonString);
        Thread.sleep(1000);
        TestContext.setContext("success_client_connection", socketConnection);
    }

    /*
    Validate the subscribed messages from feed based on channel id
     */
    public void validateSubscription(int delayTime) throws InterruptedException {
        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("success_client_connection");
        int channelId = (int) TestContext.getContext("channel_number");

        LocalDateTime start_time = LocalDateTime.now();
        LocalDateTime end_time = start_time.plusSeconds(delayTime + 2);
        Thread.sleep(delayTime * 1000L);

        List<MessageContext> subscribedMessageList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                .stream()
                .filter(c -> c.getReceivedMessage().contains(String.valueOf(channelId)) &&
                        c.getReceivedDateTime().isAfter(start_time) &&
                        c.getReceivedDateTime().isBefore(end_time))
                .collect(Collectors.toList());

        assertThat("No messages subscribed", subscribedMessageList.size() >= 1);

        subscribedMessageList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                .stream()
                .filter(c -> c.getReceivedDateTime().isAfter(start_time) &&
                        c.getReceivedDateTime().isBefore(end_time))
                .collect(Collectors.toList());

        Assert.assertTrue("Verify that at least one message received in every second", subscribedMessageList.size() >= delayTime);
        logger.info(String.format("Subscribed message size --- %s", subscribedMessageList.size()));
    }

    /*
    Asserting the subscribed messages from the feed based on the input provided in the gherkin scenarios
     */
    public void validateSuccessfulSubscription(DataTable table) throws JsonProcessingException {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        Map<String, String> row = rows.get(0);

        String currencyPair = row.get("currency_pair").trim();
        String feedName = row.get("feed_name").trim();

        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("success_client_connection");

        List<MessageContext> subscriptionStatusList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                .stream()
                .filter(c -> c.getReceivedMessage().contains("subscriptionStatus"))
                .collect(Collectors.toList());

        assertThat("No. of Subscription status is not equal to 1", subscriptionStatusList.size() == 1);
        logger.info(String.format("Subscribed event size from feed --- %s", subscriptionStatusList.size()));
        ObjectMapper objectMapper = new ObjectMapper();
        EventChannelSubscriptionBeans eventChannelSubscriptionBeans = objectMapper.readValue(subscriptionStatusList.get(0).getReceivedMessage(), EventChannelSubscriptionBeans.class);

        assertThat("Channel name is not contained with feed name", eventChannelSubscriptionBeans.getChannelName().contains(feedName));
        logger.info(String.format("Channel name from event subscription feed --- %s", eventChannelSubscriptionBeans.getChannelName()));
        logger.info(String.format("Channel name from user input --- %s", feedName));
        assertThat("Currency pair is not matched", eventChannelSubscriptionBeans.getPair().equals(currencyPair));
        logger.info(String.format("Subscribed event's currency from feed --- %s", eventChannelSubscriptionBeans.getPair()));
        logger.info(String.format("Current from user input --- %s", currencyPair));
        assertThat("Status is not matched", eventChannelSubscriptionBeans.getStatus().equals("subscribed"));
        logger.info(String.format("Event Subscription status from feed--- %s", eventChannelSubscriptionBeans.getStatus()));
        logger.info(String.format("Event Subscription status from user input--- %s", "subscribed"));
        assertThat("Feed name is not matched", eventChannelSubscriptionBeans.getSubscription().getName().equals(feedName));
        logger.info(String.format("Feed name from event subscription feed --- %s", eventChannelSubscriptionBeans.getSubscription().getName()));
        logger.info(String.format("Feed name from user input --- %s", feedName));
        TestContext.setContext("channel_number", eventChannelSubscriptionBeans.getChannelID());
    }

    /*
    Asserting the subscribed unsuccessful subscription messages from the feed based on the input provided in the gherkin scenarios
     */
    public void validateSuccessfulUnSubscription(DataTable table) throws JsonProcessingException {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        Map<String, String> row = rows.get(0);

        String currencyPair = row.get("currency_pair").trim();
        String feedName = row.get("feed_name").trim();

        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("success_client_connection");
        int channelID = (int) TestContext.getContext("channel_number");

        List<MessageContext> subscriptionStatusList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                .stream()
                .filter(c -> c.getReceivedMessage().contains("subscriptionStatus") &&
                        c.getReceivedMessage().contains(String.valueOf(channelID)))
                .collect(Collectors.toList());

        Assert.assertEquals("Verify the number of subscriptionStatus messages", 2, subscriptionStatusList.size());
        logger.info(String.format("Subscribed event size from feed --- %s", subscriptionStatusList.size()));

        ObjectMapper objectMapper = new ObjectMapper();
        EventChannelSubscriptionBeans eventChannelSubscriptionBeans = objectMapper.readValue(subscriptionStatusList.get(1).getReceivedMessage(), EventChannelSubscriptionBeans.class);

        Assert.assertTrue("Verify that channel name contains feed name", eventChannelSubscriptionBeans.getChannelName().contains(feedName));
        logger.info(String.format("Channel name from from feed --- %s", eventChannelSubscriptionBeans.getChannelName()));
        logger.info(String.format("Channel name from user input --- %s", feedName));

        Assert.assertEquals("Verify the currency pairs", currencyPair, eventChannelSubscriptionBeans.getPair());
        logger.info(String.format("Currency pair from feed --- %s", eventChannelSubscriptionBeans.getPair()));
        logger.info(String.format("Currency pair from user input --- %s", currencyPair));

        Assert.assertEquals("Verify the status", "unsubscribed", eventChannelSubscriptionBeans.getStatus());
        logger.info(String.format("Event name from feed --- %s", eventChannelSubscriptionBeans.getStatus()));
        logger.info(String.format("Event name from user input --- %s", "unsubscribed"));

        Assert.assertEquals("Verify the name", feedName, eventChannelSubscriptionBeans.getSubscription().getName());
        logger.info(String.format("Feed name from feed --- %s", subscriptionStatusList.size()));
        logger.info(String.format("Feed name from user input --- %s", subscriptionStatusList.size()));
    }

    public void validateFeed(int delayTime) throws InterruptedException {
        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("success_client_connection");
        int channelId = (int) TestContext.getContext("channel_number");

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusSeconds(delayTime + 2);
        Thread.sleep(delayTime * 1000L);

        List<MessageContext> subscribedMessageList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                .stream()
                .filter(c -> c.getReceivedMessage().contains(String.valueOf(channelId)) &&
                        c.getReceivedDateTime().isAfter(startTime) &&
                        c.getReceivedDateTime().isBefore(endTime))
                .collect(Collectors.toList());
        Assert.assertEquals("Verify that no subscribed message is received after the unsubscribe", 0, subscribedMessageList.size());

        subscribedMessageList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                .stream()
                .filter(c -> c.getReceivedDateTime().isAfter(startTime) &&
                        c.getReceivedDateTime().isBefore(endTime))
                .collect(Collectors.toList());

        Assert.assertTrue("Verify that at least one message received in every second", subscribedMessageList.size() >= delayTime);
        logger.info(String.format("Subscription List size---%s", subscribedMessageList.size()));
    }

    /*
    Schema validation for all the public feeds
     */
    public void validateSchema(String schema) {

        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("success_client_connection");
        String actualSchemaFilePath = null;
        if (schema.toLowerCase(Locale.ROOT).equalsIgnoreCase("subscriptionStatus")) {
            actualSchemaFilePath = "schema_files/subscriptionstatus_schema.json";
        } else if (schema.toLowerCase(Locale.ROOT).equalsIgnoreCase("ohlc")) {
            actualSchemaFilePath = "schema_files/ohlc_schema.json";
        } else if (schema.toLowerCase(Locale.ROOT).equalsIgnoreCase("trade")) {
            actualSchemaFilePath = "schema_files/trade_schema.json";
        } else if (schema.toLowerCase(Locale.ROOT).equalsIgnoreCase("book")) {
            actualSchemaFilePath = "schema_files/book_schema.json";
        } else if (schema.toLowerCase(Locale.ROOT).equalsIgnoreCase("ticker")) {
            actualSchemaFilePath = "schema_files/ticker_schema.json";
        } else if (schema.toLowerCase(Locale.ROOT).equalsIgnoreCase("spread")) {
            actualSchemaFilePath = "schema_files/spread_schema.json";
        }
        SchemaValidationUtil.checkSchema(socketConnection, actualSchemaFilePath);
    }

    public void validateErrorMessages(DataTable table) throws JsonProcessingException {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        Map<String, String> row = rows.get(0);

        String currencyPair = row.get("currency_pair").trim();
        String feedName = row.get("feed_name").trim();
        String errorMessage = row.get("error_message").trim();

        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("success_client_connection");

        List<MessageContext> subscriptionStatusList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                .stream()
                .filter(c -> c.getReceivedMessage().contains("subscriptionStatus"))
                .collect(Collectors.toList());

        assertThat("No. of Subscription status is not equal to 1", subscriptionStatusList.size() == 1);

        ObjectMapper objectMapper = new ObjectMapper();
        StatusMessageBeans statusMessageBeans = objectMapper.readValue(subscriptionStatusList.get(0).getReceivedMessage(), StatusMessageBeans.class);
        logger.info(String.format("Currency pair from feed ---%s", statusMessageBeans));

        assertThat("Currency pair is not matched", statusMessageBeans.getPair().equalsIgnoreCase(currencyPair));
        logger.info(String.format("Currency pair from feed ---%s", statusMessageBeans.getPair()));
        logger.info(String.format("Currency pair from user input ---%s", currencyPair));
        assertThat("Error is not available in the message", statusMessageBeans.getStatus().equals("error"));
        logger.info(String.format("Status message from feed ---%s", statusMessageBeans.getStatus()));
        logger.info(String.format("Status message from user input ---%s", "error"));
        assertThat("Feed name is not matched", statusMessageBeans.getSubscription().getName().equals(feedName));
        logger.info(String.format("Feed name from feed ---%s", statusMessageBeans.getSubscription().getName()));
        logger.info(String.format("Feed name from user input ---%s", feedName));
        assertThat("Feed name is not matched", statusMessageBeans.getErrorMessage().equals(errorMessage));
        logger.info(String.format("Error message from feed ---%s", statusMessageBeans.getErrorMessage()));
        logger.info(String.format("Error message from user input ---%s", errorMessage));

    }

    public void validateFailedSubscription(int delayTime) throws InterruptedException {
        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("success_client_connection");

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusSeconds(delayTime + 2);
        Thread.sleep(delayTime * 1000L);

        List<MessageContext> subscribedMessageList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                .stream()
                .filter(c -> c.getReceivedDateTime().isAfter(startTime) &&
                        c.getReceivedDateTime().isBefore(endTime))
                .collect(Collectors.toList());
        Assert.assertEquals("Verify that no subscribed message is received when subscription unsuccessful", 0, subscribedMessageList.size());
    }
}

