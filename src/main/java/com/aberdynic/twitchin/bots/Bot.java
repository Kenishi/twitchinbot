package com.aberdynic.twitchin.bots;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Jeremy May on 7/28/16.
 */
public interface Bot {
    public void announce(String message);
    public CompletableFuture getReadyFuture();
}
