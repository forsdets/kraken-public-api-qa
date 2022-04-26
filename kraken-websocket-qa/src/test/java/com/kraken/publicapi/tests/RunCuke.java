package com.kraken.publicapi.tests;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features",
        plugin = {"pretty",
                "html:target/cucumber.html",
                "json:target/cucumber.json",
                "junit:target/cucumber.xml"},
        glue = {"com.kraken.publicapi.tests.stepdefinitions"},
        tags = "@Regression"
)
public class RunCuke {
}
