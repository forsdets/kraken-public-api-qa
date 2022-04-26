package com.kraken.publicapi.tests.testutility;

import com.kraken.publicapi.client.websocketapp.SocketConnection;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Objects;

public class SchemaValidationUtil {
    public static void checkSchema(SocketConnection socketConnection, String schemaResourceName) {
        String actualMessage = socketConnection.getKrakenWebSocketClient().socketDataContext.getMessage(1).getReceivedMessage();

        JSONObject jsonSchema = new JSONObject(
                new JSONTokener(Objects.requireNonNull(SchemaValidationUtil.class.getClassLoader().getResourceAsStream(schemaResourceName))));

        JSONObject jsonSubject = new JSONObject(actualMessage);

        Schema expectedSchema = SchemaLoader.load(jsonSchema);
        expectedSchema.validate(jsonSubject);
    }
}
