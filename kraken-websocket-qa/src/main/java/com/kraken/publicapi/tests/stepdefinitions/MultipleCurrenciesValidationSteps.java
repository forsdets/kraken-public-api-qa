package com.kraken.publicapi.tests.stepdefinitions;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraken.publicapi.client.websocketapp.SocketConnection;
import com.kraken.publicapi.client.websocketbeans.EventChannelSubscriptionBeans;
import com.kraken.publicapi.client.websocketbeans.RequestBeans;
import com.kraken.publicapi.client.websocketbeans.SubscriptionBeans;
import com.kraken.publicapi.client.websocketcontexts.MessageContext;
import com.kraken.publicapi.tests.contexts.TestContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONObject;
import org.junit.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MultipleCurrenciesValidationSteps {

    @When("^I create a unsubscription request  with multiple currencies for a public-data feed$")
    @When("^I create a subscription request with multiple currencies for a public-data feed$")
    public void i_create_a_subscription_request_with_multiple_currencies_for_a_publicdata_feed(DataTable dataTable) throws Throwable {

        //Convert to input data table to Map
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> row = rows.get(0);

        String sub_event = row.get("event").trim();
        String sub_pair = row.get("pair").trim();
        String sub_name = row.get("name").trim();

        //Create objects for JSON Serialization
        SubscriptionBeans subscriptionBeans = new SubscriptionBeans();
        subscriptionBeans.setName(sub_name);


        List<String> pair = new ArrayList<String>();
        pair.add(sub_pair);

        //Add additional currencies
        if (row.get("pair1") != null && row.get("pair1").trim().length() > 0) {
            sub_pair = row.get("pair1").trim();
            pair.add(sub_pair);
        }
        if (row.get("pair2") != null && row.get("pair2").trim().length() > 0) {
            sub_pair = row.get("pair2").trim();
            pair.add(sub_pair);
        }
        if (row.get("pair3") != null && row.get("pair3").trim().length() > 0) {
            sub_pair = row.get("pair3").trim();
            pair.add(sub_pair);
        }
        if (row.get("pair4") != null && row.get("pair4").trim().length() > 0) {
            sub_pair = row.get("pair4").trim();
            pair.add(sub_pair);
        }

        RequestBeans requestBeans = new RequestBeans();
        requestBeans.setEvent(sub_event);
        requestBeans.setPair(pair);
        requestBeans.setSubscription(subscriptionBeans);

        //Adding optional properties to the request based on the input
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(requestBeans);

        JSONObject requestJSONObject = new JSONObject(jsonString);
        JSONObject subscriptionJSONObject = (JSONObject) requestJSONObject.get("subscription");

        if (row.get("reqid") != null && row.get("reqid").trim().length() > 0) {
            String sub_reqid = row.get("reqid").trim();
            requestJSONObject.put("reqid", Integer.parseInt(sub_reqid));
        }

        if (row.get("depth") != null && row.get("depth").trim().length() > 0) {
            String sub_depth = row.get("depth").trim();
            subscriptionJSONObject.put("depth", Integer.parseInt(sub_depth));
        }

        if (row.get("interval") != null && row.get("interval").trim().length() > 0) {
            String sub_interval = row.get("interval").trim();
            subscriptionJSONObject.put("interval", Integer.parseInt(sub_interval));
        }

        if (row.get("ratecounter") != null && row.get("ratecounter").trim().length() > 0) {
            String sub_ratecounter = row.get("ratecounter").trim();
            subscriptionJSONObject.put("ratecounter", Boolean.parseBoolean(sub_ratecounter));
        }

        if (row.get("snapshot") != null && row.get("snapshot").trim().length() > 0) {
            String sub_snapshot = row.get("snapshot").trim();
            subscriptionJSONObject.put("snapshot", Boolean.parseBoolean(sub_snapshot));
        }

        if (row.get("token") != null && row.get("token").trim().length() > 0) {
            String sub_token = row.get("token").trim();
            subscriptionJSONObject.put("token", sub_token);
        }
        //Storing objects for the use in subsequent steps
        TestContext.setContext("SubRequest", requestJSONObject.toString());
        TestContext.setContext("CurrencyList", pair);
    }


    @Then("^I verify that subscription with multiple currencies is successful$")
    public void i_verify_that_subscription_with_multiple_currencies_is_successful(DataTable dataTable) throws Throwable {

        //Convert to input data table to Map
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> row = rows.get(0);

        String sub_name = row.get("name").trim();

        //Retrieving objects stored in the previous steps
        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("Client");
        @SuppressWarnings("unchecked")
        List<String> currencyPair = (List<String>) TestContext.getContext("CurrencyList");

        //Filtering the message which has subscriptionStatus
        List<MessageContext> subscriptionStatusList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                .stream()
                .filter(c -> c.getReceivedMessage().contains("subscriptionStatus"))
                .collect(Collectors.toList());

        Assert.assertEquals("Verify the number of subscriptionStatus message", currencyPair.size(), subscriptionStatusList.size());
        List<Integer> channelIDList = new ArrayList<Integer>();

        for (String s : currencyPair) {
            List<MessageContext> currencySubscriptionStatusList = subscriptionStatusList
                    .stream()
                    .filter(c -> c.getReceivedMessage().contains(s))
                    .collect(Collectors.toList());

            ObjectMapper objectMapper = new ObjectMapper();
            EventChannelSubscriptionBeans eventChannelSubscriptionBeans = objectMapper.readValue(currencySubscriptionStatusList.get(0).getReceivedMessage(), EventChannelSubscriptionBeans.class);

            Assert.assertTrue("Verify that channel name contains feed name", eventChannelSubscriptionBeans.getChannelName().contains(sub_name));
            Assert.assertEquals("Verify the currency pairs", s, eventChannelSubscriptionBeans.getPair());
            Assert.assertEquals("Verify the status", "subscribed", eventChannelSubscriptionBeans.getStatus());
            Assert.assertEquals("Verify the name", sub_name, eventChannelSubscriptionBeans.getSubscription().getName());

            if (row.get("depth") != null && row.get("depth").trim().length() > 0) {
                String sub_depth = row.get("depth").trim();
                Assert.assertTrue("Verify that channel name contains depth", eventChannelSubscriptionBeans.getChannelName().contains(sub_depth));
            }

            if (row.get("interval") != null && row.get("interval").trim().length() > 0) {
                String sub_interval = row.get("interval").trim();
                Assert.assertTrue("Verify that channel name contains interval", eventChannelSubscriptionBeans.getChannelName().contains(sub_interval));
            }
            channelIDList.add(eventChannelSubscriptionBeans.getChannelID());
        }

        //Storing objects for the use in subsequent steps
        TestContext.setContext("ChannelIDList", channelIDList);
    }

    @Then("^I verify that unsubscription with multiple currencies is successful$")
    public void i_verify_that_unsubscription_with_multiple_currencies_is_successful(DataTable dataTable) throws Throwable {
        //Convert to input data table to Map
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> row = rows.get(0);

        String sub_name = row.get("name").trim();

        //Retrieving objects stored in the previous steps
        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("Client");
        @SuppressWarnings("unchecked")
        List<String> pair = (List<String>) TestContext.getContext("CurrencyList");
        @SuppressWarnings("unchecked")
        List<Integer> channelIDList = (List<Integer>) TestContext.getContext("ChannelIDList");

        //Repeat steps for all the currency pairs
        for (int i = 0; i < pair.size(); i++) {
            String sub_pair = pair.get(i).toString();
            int channelID = channelIDList.get(i);

            //Filtering the message which has eventChannelSubscriptionBeans and channel id
            List<MessageContext> subscriptionStatusList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                    .stream()
                    .filter(c -> c.getReceivedMessage().contains("eventChannelSubscriptionBeans") &&
                            c.getReceivedMessage().contains(String.valueOf(channelID)))
                    .collect(Collectors.toList());

            Assert.assertEquals("Verify the number of eventChannelSubscriptionBeans messages", 2, subscriptionStatusList.size());

            //Deserialization
            ObjectMapper objectMapper = new ObjectMapper();
            EventChannelSubscriptionBeans eventChannelSubscriptionBeans = objectMapper.readValue(subscriptionStatusList.get(1).getReceivedMessage(), EventChannelSubscriptionBeans.class);

            Assert.assertTrue("Verify that channel name contains feed name", eventChannelSubscriptionBeans.getChannelName().contains(sub_name));
            Assert.assertEquals("Verify the currency pairs", sub_pair, eventChannelSubscriptionBeans.getPair());
            Assert.assertEquals("Verify the status", "unsubscribed", eventChannelSubscriptionBeans.getStatus());
            Assert.assertEquals("Verify the name", sub_name, eventChannelSubscriptionBeans.getSubscription().getName());
        }
    }

    @And("I verify that subscription feed for multiple currencies are received for {int} seconds")
    public void i_verify_that_subscription_feed_for_multiple_currencies_are_received_for_30_seconds(int elapsedTime) throws Throwable {

        //Retrieving objects stored in the previous steps
        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("Client");

        //Wait for a given seconds and check the messages received during this time
        LocalDateTime start_time = LocalDateTime.now();
        LocalDateTime end_time = start_time.plusSeconds(elapsedTime + 2);
        Thread.sleep(elapsedTime * 1000);

        @SuppressWarnings("unchecked")
        List<String> pair = (List<String>) TestContext.getContext("CurrencyList");
        @SuppressWarnings("unchecked")
        List<Integer> channelIDList = (List<Integer>) TestContext.getContext("ChannelIDList");

        //Repeat steps for all the currency pairs
        for (int i = 0; i < pair.size(); i++) {
            String sub_pair = pair.get(i).toString();
            int channelID = channelIDList.get(i);

            //Filter messages which contains channel id and received between the start and end time
            List<MessageContext> subscribedMessageList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                    .stream()
                    .filter(c -> c.getReceivedMessage().contains(String.valueOf(channelID)) &&
                            c.getReceivedDateTime().isAfter(start_time) &&
                            c.getReceivedDateTime().isBefore(end_time))
                    .collect(Collectors.toList());

            Assert.assertTrue("Verify that atleast one subscribed message is received", subscribedMessageList.size() >= 1);
            Assert.assertTrue("Verify that currency is matching", subscribedMessageList.get(0).getReceivedMessage().contains(sub_pair));

            //Filter messages received between the start and end time - including heart beat
            subscribedMessageList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                    .stream()
                    .filter(c -> c.getReceivedDateTime().isAfter(start_time) &&
                            c.getReceivedDateTime().isBefore(end_time))
                    .collect(Collectors.toList());

            Assert.assertTrue("Verify that at least one message received in every second", subscribedMessageList.size() >= elapsedTime); // including 'heartbeat'
        }
    }

    @And("I verify that subscription feed for multiple currencies are not received for {int} seconds")
    public void i_verify_that_subscription_feed_for_multiple_currencies_are_not_received_for_30_seconds(int elapsedTime) throws Throwable {
        //Retrieving objects stored in the previous steps
        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("Client");

        //Wait for a given seconds and check the messages received during this time
        LocalDateTime start_time = LocalDateTime.now();
        LocalDateTime end_time = start_time.plusSeconds(elapsedTime + 2);
        Thread.sleep(elapsedTime * 1000);

        //Retrieving objects stored in the previous steps
        @SuppressWarnings("unchecked")
        List<String> pair = (List<String>) TestContext.getContext("CurrencyList");
        @SuppressWarnings("unchecked")
        List<Integer> channelIDList = (List<Integer>) TestContext.getContext("ChannelIDList");

        //Repeat steps for all the currency pairs
        for (int i = 0; i < pair.size(); i++) {
            int channelID = channelIDList.get(i);

            //Filter messages which contains channel id and received between the start and end time
            List<MessageContext> subscribedMessageList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                    .stream()
                    .filter(c -> c.getReceivedMessage().contains(String.valueOf(channelID)) &&
                            c.getReceivedDateTime().isAfter(start_time) &&
                            c.getReceivedDateTime().isBefore(end_time))
                    .collect(Collectors.toList());

            Assert.assertTrue("Verify that no subscribed message is received after the unsubscribe", subscribedMessageList.size() == 0);

            //Filter messages received between the start and end time - including heart beat
            subscribedMessageList = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessageList()
                    .stream()
                    .filter(c -> c.getReceivedDateTime().isAfter(start_time) &&
                            c.getReceivedDateTime().isBefore(end_time))
                    .collect(Collectors.toList());

            Assert.assertTrue("Verify that at least one message received in every second", subscribedMessageList.size() >= elapsedTime); // 'heartbeat' received after unsubscription
        }

    }

}
