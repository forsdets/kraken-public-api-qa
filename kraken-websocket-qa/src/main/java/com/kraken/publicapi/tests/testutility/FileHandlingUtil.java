package com.kraken.publicapi.tests.testutility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FileHandlingUtil {

    public static String getPropertyValue(String filePath, String propertyKey) throws IOException {
        Properties prop = new Properties();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        prop.load(fileInputStream);
        fileInputStream.close();
        return prop.getProperty(propertyKey);
    }
}
