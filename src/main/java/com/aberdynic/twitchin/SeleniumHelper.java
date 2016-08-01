package com.aberdynic.twitchin;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.net.URL;

/**
 * Created by Jeremy May on 7/28/16.
 */
public class SeleniumHelper {

    public static WebDriver get(URL link) {
        WebDriver driver = new PhantomJSDriver();
        driver.get(link.toString());

        return driver;
    }
}
