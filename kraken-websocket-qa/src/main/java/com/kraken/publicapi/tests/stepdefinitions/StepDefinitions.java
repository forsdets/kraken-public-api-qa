package com.kraken.publicapi.tests.stepdefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kraken.publicapi.client.websocketapp.SocketConnection;
import com.kraken.publicapi.tests.contexts.TestContext;
import com.kraken.publicapi.tests.implementations.StepsImplementation;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StepDefinitions {

    Logger logger = LoggerFactory.getLogger(StepDefinitions.class);
    StepsImplementation stepsImplementation = new StepsImplementation();

    @Before
    public void inIt(Scenario scenario) {
        TestContext inItStatus = new TestContext();
        logger.info(scenario.getName());
    }

    @Given("^user connects to the WebSocket API$")
    public void connectWebSocket() throws IOException {
        stepsImplementation.openWebSocket();
    }

    @When("^user creates a subscription request for kraken public data feed$")
    public void createSubscription(DataTable dataTable) throws JsonProcessingException {
        stepsImplementation.createSubscriptionRequest(dataTable);
    }

    @Then("^user verifies that the subscription is successful$")
    public void verifySubscriptionSuccess(DataTable table) throws JsonProcessingException {
        stepsImplementation.validateSuccessfulSubscription(table);
    }

    @When("^user creates an un-subscription request for kraken public data feed$")
    public void createUnSubscription(DataTable dataTable) throws JsonProcessingException {
        stepsImplementation.createSubscriptionRequest(dataTable);
    }

    @Then("^user verifies that the un-subscription is successful$")
    public void verifySuccessfulUnSubscription(DataTable table) throws JsonProcessingException {
        stepsImplementation.validateSuccessfulUnSubscription(table);
    }

    @And("^user submits a request to subscribe public data feed$")
    public void requestToSubscribePublicDataFeed() throws InterruptedException {
        stepsImplementation.submitRequest();
    }

    @And("user verifies that the feed is received the subscription message")
    public void checkSubscription() throws InterruptedException {
        int delayTime = 40;
        stepsImplementation.validateSubscription(delayTime);
    }

    @And("^user submits a request to unsubscribe public data feed$")
    public void requestToUnSubscribePublicDataFeed() throws InterruptedException {
        stepsImplementation.submitRequest();
    }

    @And("user verifies that the feed is not received the un-subscription message")
    public void verifyUnSubscription() throws InterruptedException {
        int delayTime = 40;
        stepsImplementation.validateFeed(delayTime);
    }

    @And("^user verifies that the subscription is not successful$")
    public void checkError(DataTable table) throws JsonProcessingException {
        stepsImplementation.validateErrorMessages(table);
    }

    @And("^user verifies subscription message is not received$")
    public void checkUnSuccessfulMessage() throws InterruptedException {
        int delayTime = 10;
        stepsImplementation.validateFailedSubscription(delayTime);
    }

    @And("^user closes the connection$")
    public void i_close_the_connection() throws InterruptedException {
        SocketConnection socketConnection = (SocketConnection) TestContext.getContext("success_client_connection");
        socketConnection.closeConnection();
    }

    @Then("user performs the schema validation for {string}")
    public void performSchemaValidation(String schema) {
        stepsImplementation.validateSchema(schema);
    }
}
