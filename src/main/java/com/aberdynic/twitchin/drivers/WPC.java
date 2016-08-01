package com.aberdynic.twitchin.drivers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Jeremy May on 7/30/16.
 */
public class WPC {
    public static List<RedditInfo> scrape(URL reddit) throws IOException {
        return readSubreddit(reddit);
    }

    private static List<RedditInfo> readSubreddit(URL link) throws IOException {
        // Get the JSON from the subreddit
        URLConnection conn = link.openConnection();
        try {
            Thread.sleep(1000);
        } catch(Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        conn.setRequestProperty("User-Agent", "Chrome");
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String data = "", line;
        while((line = reader.readLine()) != null) {
            data += line;
        }
        reader.close();

        List<RedditInfo> list = new ArrayList<>();
        // Process the JSON
        JSONObject obj = new JSONObject(data);
        JSONObject json_data = obj.getJSONObject("data");
        JSONArray children = json_data.getJSONArray("children");
        for(Object child : children) {
            JSONObject temp = (JSONObject) child;
            String kind = temp.getString("kind");
            if(kind.equals("t3")) {
                JSONObject tempData = temp.getJSONObject("data");
                String url = "";
                try {
                     url = tempData.getString("url");
                } catch(JSONException e) {
                    System.out.println(tempData.toString());
                    throw e;
                }
                String author = tempData.getString("author");
                String title = tempData.getString("title");
                JSONObject media = tempData.optJSONObject("media");

                if(media != null) {
                    // Check wht type of link it is so the proper scrapper can be used
                    // to check Live status
                    String media_type = media.getString("type");
                    DriverType driver_type = null;
                    if (media_type.equals("twitch.tv")) {
                        driver_type = DriverType.TWITCH;
                    } else if (media_type.equals("youtube.com")) {
                        // TODO: Enable after Youtube scrapping is available
                        // driver_type = DriverType.YOUTTUBE;
                    } else {
                        continue;
                    }

                    if (driver_type != null) {
                        list.add(new RedditInfo(title, new URL(url), author, driver_type));
                    }
                }
            }
        }

        return list;
    }

    public static class RedditInfo {
        private String thread_title;
        private URL link;
        private String username;
        private DriverType type;

        public RedditInfo(String title, URL link, String username, DriverType type) {
            this.thread_title = title;
            this.link = link;
            this.username = username;
            this.type = type;
        }

        public String getThreadTitle() { return this.thread_title; }
        public URL getLink() { return this.link; }
        public String getUsername() { return this.username; }
        public DriverType getDriverType() { return this.type; }
    }
}
