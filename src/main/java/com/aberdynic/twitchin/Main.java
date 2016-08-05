package com.aberdynic.twitchin;

import com.aberdynic.twitchin.bots.IRCBot;
import com.aberdynic.twitchin.bots.IRCConfig;
import com.aberdynic.twitchin.drivers.DriverType;
import org.pircbotx.exception.IrcException;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by Jeremy May on 7/28/16.
 */
public class Main {
    final static public long CHECK_EVERY = 1000 * 60;

    public Main() {

    }

    private static String getConfig(String file) throws IOException {
        BufferedReader br = null;
        String line = "";
        String data = "";
        br = new BufferedReader(new FileReader(file));
        while((line = br.readLine()) != null) {
            data += line + "\n";
        }

        br.close();
        return data;
    }

    public static void main(String[] args) throws IOException, IrcException {
        String path = args[0];

        // Load config file and get targets to scrap
        Yaml yaml = new Yaml();
        FileInputStream stream = new FileInputStream(path);
        Object loadedObject = yaml.load(stream);
        if(loadedObject instanceof String) {
            System.out.println(loadedObject);
        }
        Map map = (Map) loadedObject;
        // Get bot config
        IRCConfig irc_config = new IRCConfig(map);
        System.out.println(irc_config.toString());
        // Load IRC Bot
        IRCBot bot = new IRCBot(irc_config);
        bot.start();

        bot.getReadyFuture()
                .thenAccept((val) -> {
                    // Create Runner and feed it targets and IRC bot
                    // TODO: Maybe move this into a separate class somewhere?
                    Map<String, Object> scrapper_map = (Map) map.get("scrapper");
                    HashMap<DriverType, List<Target>> scrapper_config = new HashMap<>();
                    for(String key : scrapper_map.keySet()) {
                        List<String> locations = (List<String>) scrapper_map.get(key);
                        List<Target> targets = new ArrayList();
                        switch(key.toLowerCase()) {
                            case "twitch":
                                for(String tar: locations) {
                                    try {
                                        targets.add(new Target(new URL(tar)));
                                    } catch(MalformedURLException e) {
                                        e.printStackTrace();
                                        System.exit(1);
                                    }
                                }
                                scrapper_config.put(DriverType.TWITCH, targets);
                                break;
                            case "reddit":
                                for(String tar : locations) {
                                    try {
                                        targets.add(new Target(new URL(tar)));
                                    } catch(MalformedURLException e) {
                                        e.printStackTrace();
                                        System.exit(1);
                                    }
                                }
                                scrapper_config.put(DriverType.REDDIT, targets);
                                break;
                        }
                    }
                    Scrapper scrapper = new Scrapper(scrapper_config, bot);

                    // Start runner Timer
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(scrapper, 1000, CHECK_EVERY);
                    return;
                });
    }
}
