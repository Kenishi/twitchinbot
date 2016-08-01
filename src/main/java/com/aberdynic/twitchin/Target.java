package com.aberdynic.twitchin;

import java.net.URL;

/**
 * Created by Jeremy May on 7/28/16.
 */
public class Target {
    private URL url;
    public Target(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return this.url;
    }
}
