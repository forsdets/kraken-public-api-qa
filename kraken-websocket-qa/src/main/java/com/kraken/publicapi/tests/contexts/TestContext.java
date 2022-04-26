package com.kraken.publicapi.tests.contexts;

import java.util.HashMap;
import java.util.Map;

/*
This context is for transferring the data from one class into another class
 */
public class TestContext {
    private static Map<String, Object> testContext;

    public TestContext() {
        testContext = new HashMap<>();
    }

    public static void setContext(String key, Object value) {
        testContext.put(key, value);
    }

    public static Object getContext(String key) {
        return testContext.get(key);
    }
}