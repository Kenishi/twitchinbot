package com.aberdynic.twitchin;

import com.aberdynic.twitchin.drivers.DriverType;

/**
 * Created by Jeremy May on 7/31/16.
 */
public class StreamData<K,U> {
    private DriverType type;
    private K user; // Username or ID for the streamer
    private U data; // Piece of data to get to the stream, possibly URL

    public StreamData(DriverType type, K user, U data) {
        this.type = type;
        this.user = user;
        this.data = data;
        validate();
    }

    private void validate() {
        if(this.type == null) throw new NullPointerException("Type cannot be null");
        if(this.user == null) throw new NullPointerException("User cannot be null");
        if(this.data == null) throw new NullPointerException("Data cannot be null");
        return;
    }

    public DriverType getType() { return this.type; }
    public K getUser() { return this.user; }
    public U getData() { return this.data; }

    @Override
    public int hashCode() {
        int hash = type.hashCode() ^ user.hashCode();
        hash ^= data.hashCode();

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(!(obj.getClass() == this.getClass())) return false;
        StreamData cast = (StreamData) obj;
        if(!cast.type.equals(this.type)) return false;
        if(!cast.user.equals(this.user)) return false;
        if(!cast.data.equals(this.data)) return false;

        return true;
    }

    @Override
    public String toString() {
        String out = "";
        out += this.getType().toString() + " " + this.getUser().toString() + " " + this.getData();
        return out;
    }
}
