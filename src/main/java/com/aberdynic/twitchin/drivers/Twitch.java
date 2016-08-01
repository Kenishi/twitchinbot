package com.aberdynic.twitchin.drivers;

import com.aberdynic.twitchin.SeleniumHelper;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeremy May on 7/28/16.
 */
public class Twitch {

    public static List<TwitchInfo> scrap(URL link) {
        if(link == null) {
            throw new NullPointerException("Link cannot be null");
        }

        System.out.println("Scrapping: " + link.toString());
        WebDriver driver = SeleniumHelper.get(link);

        // Gather the streams and go one by one gather the info
        ArrayList<TwitchInfo> streamData = new ArrayList<>(20);
        System.out.println(driver.getPageSource());
        List<WebElement> streams = driver.findElements(By.className("stream"));
        for(WebElement ele : streams) {
            // Grab the stream title which also gives us the stream username and url
            WebElement stream_anchor = ele.findElement(By.cssSelector(".meta > .title > a"));
            URL url;
            try {
                url = new URL(stream_anchor.getAttribute("href"));
            } catch(MalformedURLException e) {
                continue;
            }
            String username = url.getFile().substring(1);
            String title = stream_anchor.getText();

            // Get the tags
            List<WebElement> stream_tags = ele.findElements(By.cssSelector(".meta > .ct-tag"));
            ArrayList<String> tags = new ArrayList<>(10);
            for(WebElement tag_ele : stream_tags) {
                tags.add(tag_ele.getText());
            }

            // Build TwitchInfo
            TwitchInfo info = new TwitchInfo(username, url.toString(), title, tags);
            streamData.add(info);
        }

        System.out.println("Done with scrape");
        return streamData;
    }

    public static boolean isLive(String channel) throws IOException {
        URL url = new URL("https://api.twitch.tv/kraken/streams/" + channel);
        URLConnection conn = url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String data = "", line;
        while((line = reader.readLine()) != null) {
            data += line;
        }
        reader.close();

        JSONObject json = new JSONObject(data);
        JSONObject stream = json.optJSONObject("stream");
        return stream != null;
    }

    public static class TwitchInfo {
        private String username;
        private String stream_url;
        private String stream_title;
        private List<String> tags;

        public TwitchInfo(String username, String url, String title, List<String> tags) {
            this.username = username;
            this.stream_url = url;
            this.stream_title = title;
            this.tags = tags;
        }

        public String getUsername() { return this.username; }
        public String getStreamURL() { return this.stream_url; }
        public String getStreamTitle() { return this.stream_title; }
        public List<String> getTags() { return this.tags; }
    }
}
