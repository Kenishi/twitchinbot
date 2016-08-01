package com.aberdynic.twitchin;

import com.aberdynic.twitchin.bots.Bot;
import com.aberdynic.twitchin.drivers.DriverType;
import com.aberdynic.twitchin.drivers.Twitch;
import com.aberdynic.twitchin.drivers.WPC;
import org.apache.commons.collections.map.HashedMap;
import org.pircbotx.Colors;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by Jeremy May on 7/28/16.
 */
public class Scrapper extends TimerTask {

    private HashMap<DriverType, List<Target>> config;
    private Bot bot;
    private StreamMap status;

    /**
     * Constructor
     * @param scrapperConfig a map of Driver types and Targets to watch (ex: specific twitch categories)
     * @param bot
     */
    public Scrapper(HashMap<DriverType, List<Target>> scrapperConfig, Bot bot) {
        if(scrapperConfig == null) {
            throw new NullPointerException("Scrapper requires a config");
        }
        if(bot == null) {
            throw new NullPointerException("Scrapper requires a bot");
        }

        this.config = scrapperConfig;
        this.bot = bot;
        this.status = new StreamMap();
    }

    @Override
    public void run() {
        Map<DriverType, Map<Object, StreamData>> newStreams = new HashMap<>();
        for(DriverType key : this.config.keySet()) {
            List<Target> targets = this.config.get(key);

            // Scrape for the data on this Driver type for all the targets
            Set<StreamData> syncData = null;
            for(Target target : targets) {
                URL url = target.getUrl();
                syncData = this.doScrape(key, target);
            }

            if(syncData != null) {
                Map<Object, StreamData> temp = newStreams.get(key);
                if(temp == null) {
                    temp = new HashMap<>();
                }

                // Add the all the streams to the temp map
                // and then store it in the map for syncing later
                for(StreamData stream : syncData) {
                    temp.put(stream.getUser(), stream);
                }

                newStreams.put(key, temp);
            }
        }

        List<StreamMap.StreamMapDiff> syncDiff = status.sync(newStreams);
        for(StreamMap.StreamMapDiff diff : syncDiff) {
            StreamMap.Status status = diff.status;
            StreamData streamData = diff.stream;
            if(status == StreamMap.Status.LIVE) {
                this.doAnnounce(streamData, this.bot);
            }
        }
    }

    private Set<StreamData> doScrape(DriverType type, Target target) {
        Set<StreamData> out = new HashSet<>();

        switch(type) {
            case TWITCH:
                List<Twitch.TwitchInfo> scrapeResult = Twitch.scrap(target.getUrl());
                for(Twitch.TwitchInfo info : scrapeResult) {
                    String username = info.getUsername();
                    String title = info.getStreamTitle();
                    StreamData<String, String> streamData = new StreamData<>(type, username, title);
                    out.add(streamData);
                }

                break;
            case REDDIT:
                System.out.println("Scrapping Reddit: " + target.getUrl().toString());
                List<WPC.RedditInfo> redditScrapeResult = null;
                try {
                    redditScrapeResult = WPC.scrape(target.getUrl());
                } catch(IOException ex) {
                    System.err.println("Failed to scrap WPC");
                    ex.printStackTrace();
                }
                if(redditScrapeResult != null) {
                    // Verify Reddit links to streams are still Live
                    // If so, then create the relevant Stream info object
                    for (WPC.RedditInfo info : redditScrapeResult) {
                        DriverType redditStreamType = info.getDriverType();
                        switch (redditStreamType) {
                            case TWITCH:
                                String channel = info.getLink().getPath().substring(1);
                                try {
                                    if (Twitch.isLive(channel)) {
                                        out.add(new StreamData(redditStreamType, channel, info.getThreadTitle()));
                                    }
                                } catch(IOException ex) {
                                    System.err.println("Failed to check if channel was live: " + channel);
                                    ex.printStackTrace();
                                }
                                break;
                            case YOUTUBE:
                                // TODO: Add the stream lookup for Youtube
                                break;
                        }
                    }
                }
                break;
        }

        return out;
    }

    private void doAnnounce(StreamData data, Bot bot) {
        String msg = "";
        DriverType type = data.getType();
        switch(type) {
            case TWITCH:
                msg = Colors.RED + Colors.BOLD + data.getUser() + Colors.NORMAL + " is Live: " + data.getData() + Colors.BOLD + " http://twitch.tv/" + data.getUser();
                break;
        }

        bot.announce(msg);
        System.out.println(msg);
    }
}
