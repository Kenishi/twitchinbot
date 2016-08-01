package com.aberdynic.twitchin.bots;


import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Jeremy May on 7/28/16.
 */
public class IRCConfig {

    private String nick_1;
    private String nick_1_pass;
    private String nick_2;
    private String nick_2_pass;

    private String quit_msg;

    private String server;
    private List<String> channels;

    public IRCConfig(Map<String, Object> config) {
        Map<String, Object> irc = (Map<String, Object>) config.get("irc");
        this.nick_1 = (String) irc.get("nick_1");
        this.nick_2 = (String) irc.get("nick_2");

        this.nick_1_pass = (String) irc.get("nick_1_pass");
        this.nick_2_pass = (String) irc.get("nick_2_pass");

        this.quit_msg = (String) irc.get("quit_msg");

        this.server = (String) irc.get("server");
        this.channels = (List<String>) irc.get("channels");

        validate();
    }

    private void validate() {
        Random rand = new Random();
        if(this.nick_1 == null) {
            System.err.println("No primary nickname was specified using random one");
            this.nick_1 = "Twitchin" + Integer.toString(rand.nextInt(2000));
        }
        if(this.nick_2 == null) {
            System.err.println("No backup nickname specified, using random one.");
            this.nick_2 = "Twitchin" + Integer.toString(rand.nextInt(2000));
        }

        if(this.quit_msg == null) {
            this.quit_msg = "Twitchin right out!";
        }

        if(this.server == null) {
            throw new NullPointerException("IRC Server not provided in config.");
        }
        if(this.channels == null || this.channels.size() <= 0) {
            throw new RuntimeException("No channels set in config to join.");
        }
    }

    @Override
    public String toString() {
        String out = "";
        out += "Nick_1: " + this.getNick_1() + "\n";
        out += "Nick_2: " + this.getNick_2() + "\n";
        out += "Server: " + this.getServer() + "\n";
        out += "Channels: \n" + this.getChannels().toString();

        return out;
    }

    public String getNick_1() { return this.nick_1; }
    public String getNick_2() { return this.nick_2; }
    public String getNick_1_pass() { return this.nick_1_pass; }
    public String getNick_2_pass() { return this.nick_2_pass; }
    public String getQuit_msg() { return this.quit_msg; }
    public String getServer() { return this.server; }
    public List<String> getChannels() { return this.channels; }
}
