package com.kraken.publicapi.tests.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraken.publicapi.client.websocketapp.KrakenWebSocketClient;
import com.kraken.publicapi.client.websocketapp.SocketConnection;
import com.kraken.publicapi.client.websocketbeans.*;
import com.kraken.publicapi.client.websocketcontexts.MessageContext;
import com.kraken.publicapi.client.websocketcontexts.SocketDataContext;
import com.kraken.publicapi.tests.constants.TestConstants;
import com.kraken.publicapi.tests.contexts.TestContext;
import io.cucumber.datatable.DataTable;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.kraken.publicapi.tests.testutility.FileHandlingUtil.getPropertyValue;
import static org.hamcrest.MatcherAssert.assertThat;


public class StepsImplementation {

    Logger logger = LoggerFactory.getLogger(KrakenWebSocketClient.class);

    public void openWebSocket() throws IOException {
        SocketDataContext context = new SocketDataContext(getPropertyValue(TestConstants.WEB_SOCKET_PROPERTY_FILE_PATH, TestConstants.WEB_SOCKET_API_URI), 60);
        SocketConnection socketConnection = new SocketConnection();
        socketConnection = socketConnection.connectToHost(context);

        ObjectMapper objectMapper = new ObjectMapper();
        SocketStatusBeans socketStatusBeans = objectMapper.readValue(socketConnection.getKrakenWebSocketClient().socketDataContext.getMessage(0).getReceivedMessage(), SocketStatusBeans.class);

        assertThat("The Socket status is offline", socketStatusBeans.getStatus().equals("online"));
        socketConnection.getKrakenWebSocketClient().socketDataContext.setStatus(socketStatusBeans.getStatus());
        TestContext.setContext("success_client_connection", socketConnection);
    }

    public void createSubscriptionRequest(DataTable dataTable) throws JsonProcessingException {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> row = rows.get(0);

        String event = row.get("event").trim();
        String currencyPair = row.get("currency_pair").trim();
        String feedName = row.get("feed_name").trim();

        SubscriptionBeans subscriptionBeans = new SubscriptionBeans();
        subscriptionBeans.setName(feedName);

        List<String> pair = new ArrayList<>();
        pair.add(currencyPair);

        RequestBeans requestBeans = new RequestBeans();
        requestBeans.setEvent(event);
        requestBeans.setPair(pair);
        requestBeans.setSubscription(subscriptionBeans);

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(requestBeans);

        JSONObject requestJSONObject = new JSONObject(jsonString);
        TestContext.setContext("Subscription_Request", requestJSONObject.toString());
    }

    public void submitRequest() throws InterruptedException {
        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("success_client_connection");
        String jsonString = (String) TestContext.getContext("Subscription_Request");

        socketConnection = socketConnection.subscribeMessage(jsonString);
        Thread.sleep(1000);
        TestContext.setContext("success_client_connection", socketConnection);
    }

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

    }

    public void validateUnSubscriptions(DataTable table) throws JsonProcessingException {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        Map<String, String> row = rows.get(0);

        String currencyPair = row.get("currency_pair").trim();
        String feedName = row.get("feed_name").trim();
        String error = row.get("error").trim();

        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("success_client_connection");

        List<MessageContext> subscriptionStatusList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                .stream()
                .filter(c -> c.getReceivedMessage().contains("subscriptionStatus"))
                .collect(Collectors.toList());

        assertThat("No Messages subscribed", subscriptionStatusList.size() == 1);

        ObjectMapper objectMapper = new ObjectMapper();
        StatusMessageBeans statusMessageBeans = objectMapper.readValue(subscriptionStatusList.get(0).getReceivedMessage(), StatusMessageBeans.class);

        assertThat("Currency pairs are not equal", statusMessageBeans.getPair().equals(currencyPair));
        assertThat("Subscribed status is not matched", statusMessageBeans.getStatus().equals("error"));
        assertThat("Subscribed name is not matched", statusMessageBeans.getSubscription().getName().equals(feedName));
        assertThat("Error message is not matched", statusMessageBeans.getErrorMessage().equals(error));
    }

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

        assertThat("No. of Subscription status is not equal to 1",subscriptionStatusList.size()==1);

        ObjectMapper objectMapper = new ObjectMapper();
        EventChannelSubscriptionBeans eventChannelSubscriptionBeans = objectMapper.readValue(subscriptionStatusList.get(0).getReceivedMessage(), EventChannelSubscriptionBeans.class);

        assertThat("Channel name is not contained with feed name", eventChannelSubscriptionBeans.getChannelName().contains(feedName));
        assertThat("Currency pair is not matched", eventChannelSubscriptionBeans.getPair().equals(currencyPair));
        assertThat("Status is not matched", eventChannelSubscriptionBeans.getStatus().equals("subscribed"));
        assertThat("Feed name is not matched", eventChannelSubscriptionBeans.getSubscription().getName().equals(feedName));

        TestContext.setContext("channel_number", eventChannelSubscriptionBeans.getChannelID());
    }

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

        ObjectMapper objectMapper = new ObjectMapper();
        EventChannelSubscriptionBeans eventChannelSubscriptionBeans = objectMapper.readValue(subscriptionStatusList.get(1).getReceivedMessage(), EventChannelSubscriptionBeans.class);

        Assert.assertTrue("Verify that channel name contains feed name", eventChannelSubscriptionBeans.getChannelName().contains(feedName));
        Assert.assertEquals("Verify the currency pairs", currencyPair, eventChannelSubscriptionBeans.getPair());
        Assert.assertEquals("Verify the status", "unsubscribed", eventChannelSubscriptionBeans.getStatus());
        Assert.assertEquals("Verify the name", feedName, eventChannelSubscriptionBeans.getSubscription().getName());
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
    }

    public void validateSchema(String schema) {

        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("success_client_connection");
        String schemaResourceName = null;
        if (schema.toLowerCase(Locale.ROOT).equalsIgnoreCase("subscriptionStatus")) {
            schemaResourceName = "schema_files/subscriptionstatus_schema.json";
        } else if (schema.toLowerCase(Locale.ROOT).equalsIgnoreCase("ohlc")) {
            schemaResourceName = "schema_files/ohlc_schema.json";
        } else if (schema.toLowerCase(Locale.ROOT).equalsIgnoreCase("trade")) {
            schemaResourceName = "schema_files/trade_schema.json";
        } else if (schema.toLowerCase(Locale.ROOT).equalsIgnoreCase("book")) {
            schemaResourceName = "schema_files/book_schema.json";
        } else if (schema.toLowerCase(Locale.ROOT).equalsIgnoreCase("ticker")) {
            schemaResourceName = "schema_files/ticker_schema.json";
        } else if (schema.toLowerCase(Locale.ROOT).equalsIgnoreCase("spread")) {
            schemaResourceName = "schema_files/spread_schema.json";
        }

        String actualMessage = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessage(1).getReceivedMessage();

        JSONObject jsonSchema = new JSONObject(
                new JSONTokener(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(schemaResourceName))));

        JSONObject jsonSubject = new JSONObject(actualMessage);

        Schema expectedSchema = SchemaLoader.load(jsonSchema);
        expectedSchema.validate(jsonSubject);
        System.out.printf("Schema of %s is successfully tested.%n", schema);

    }
}

