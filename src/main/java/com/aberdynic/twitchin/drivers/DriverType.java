package com.aberdynic.twitchin.drivers;

/**
 * Created by Jeremy May on 7/28/16.
 */
public enum DriverType {
    TWITCH("Twitch"),
    REDDIT("Reddit"),
    YOUTUBE("YouTube");

    private String type_str;
    private DriverType(String type) {
        type_str = type;
    }
}
