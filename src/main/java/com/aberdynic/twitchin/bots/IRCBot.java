package com.aberdynic.twitchin.bots;

import org.apache.commons.lang3.ObjectUtils;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UserChannelDao;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.output.OutputChannel;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Created by Jeremy May on 7/28/16.
 */
public class IRCBot extends ListenerAdapter implements Bot, Runnable {

    private IRCConfig config;
    private PircBotX instance;
    private CompletableFuture<Boolean> ready = new CompletableFuture();

    public IRCBot(IRCConfig config) {
        if(config == null) {
            throw new NullPointerException("IRC Config cannot be null");
        }
        this.config = config;

        Configuration.Builder builder = new Configuration.Builder()
                .setName(config.getNick_1())
                .addServer(config.getServer());

        for(String chan : config.getChannels()) {
            builder.addAutoJoinChannel(chan);
        }

        builder.addListener(this);

        Configuration botConfig = builder.buildConfiguration();
        this.instance = new PircBotX(botConfig);
    }

    @Override
    public void run() {
        try {
            this.instance.startBot();
//            this.ready.complete(Boolean.TRUE);
            System.out.println("Bot ready to go");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {
        super.onJoin(event);
        System.out.println("Joined: " + event.getChannel().getName());

        UserChannelDao dao = this.instance.getUserChannelDao();
        Set<Channel> channels = dao.getAllChannels();
        boolean readyToAnnounce = true;
        for(String name : config.getChannels()) {
            boolean found = false;
            for(Channel chan : channels) {
                if(chan.getName().equals(name)) {
                    found = true;
                    break;
                }
            }

            // If we didn't find the channel then we aren't ready
            if(!found) {
                readyToAnnounce = false;
                break;
            }
        }

        if(readyToAnnounce && !this.ready.isDone()) {
            System.out.println("Bot is ready");
            this.ready.complete(Boolean.TRUE);
        }
    }

    @Override
    public void announce(String message) {
        UserChannelDao dao = this.instance.getUserChannelDao();

        for(Channel chan : ((Set<Channel>) dao.getAllChannels())) {
            OutputChannel output = chan.send();
            output.message(message);
        }
    }

    public CompletableFuture<Boolean> getReadyFuture() {
        return this.ready;
    }

    public void start() throws IOException, IrcException {
        (new Thread(this)).start();
    }

    public void stop() {
        this.instance.close();
    }
}
