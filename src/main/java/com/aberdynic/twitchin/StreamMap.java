package com.aberdynic.twitchin;

import com.aberdynic.twitchin.drivers.DriverType;

import java.util.*;

/**
 * Created by Jeremy May on 7/31/16.
 */
public class StreamMap {

    private HashMap<DriverType, HashMap<Object, StreamData>> map;

    public StreamMap() {
        map = new HashMap<>();
        for(DriverType type : DriverType.values()) {
            map.put(type, new HashMap<>());
        }
    }

    public List<StreamMapDiff> sync(Map<DriverType, Map<Object, StreamData>> streams) {
        List<StreamMapDiff> diff = new ArrayList<>();

        // Createa a temp lookup map to speed up looking up which streams are offline, later
        Map<DriverType, List<StreamMapDiff>> lookup = new HashMap<>();
        for(DriverType type: DriverType.values()) {
            lookup.put(type, new ArrayList<>());
        }

        // Do updates related to the new streams supplied
        for(DriverType type : streams.keySet()) {
            Map<Object, StreamData> driverMap = streams.get(type);
            for(StreamData value : driverMap.values()) {
                Status status = this.addOrUpdate(value);
                StreamMapDiff d = new StreamMapDiff(status, value);
                diff.add(d);
                lookup.get(value.getType()).add(d);
            }
        }

        // Check which entries weren't processed, these ones are offline now and need removed
        for(DriverType type : this.map.keySet()) {
            Map<Object, StreamData> driverMap = this.map.get(type);

            // Create an array so we can modify the driverMap on the fly
            // without having to worry about any concurrency exceptions
            Object[] keys = driverMap.keySet().toArray();
            for(int i=0; i < keys.length; i++) {
                Object key = keys[i];
                StreamData streamData = driverMap.get(key);

                List<StreamMapDiff> diffs = lookup.get(type);
                boolean stillLive = false;
                for(StreamMapDiff d : diffs) {
                    if(streamData.getUser().equals(d.stream.getUser())) {
                        stillLive = true;
                        break;
                    }
                }

                if(!stillLive) {
                    System.out.println("Removing:" + streamData.toString());
                    driverMap.remove(key);
                    diff.add(new StreamMapDiff(Status.OFFLINE, streamData));
                }
            }
        }

        return diff;
    }

    public List<StreamMapDiff> sync(Collection<StreamData> streams) {
        List<StreamMapDiff> diff = new ArrayList<>();
        // Createa a temp lookup map to speed up looking up which streams are offline, later
        Map<DriverType, List<StreamMapDiff>> lookup = new HashMap<>();
        for(DriverType type: DriverType.values()) {
            lookup.put(type, new ArrayList<>());
        }

        // Do updates related to the new streams supplied
        for(StreamData streamData : streams) {
            Status status = this.addOrUpdate(streamData);
            StreamMapDiff d = new StreamMapDiff(status, streamData);
            diff.add(d);
            lookup.get(streamData.getType()).add(d);
        }

        // Check which entries weren't processed, these ones are offline now and need removed
        for(DriverType type : this.map.keySet()) {
            Map<Object, StreamData> driverMap = this.map.get(type);

            // Create an array so we can modify the driverMap on the fly
            // without having to worry about any concurrency exceptions
            Object[] keys = driverMap.keySet().toArray();
            for(int i=0; i < keys.length; i++) {
                Object key = keys[i];
                StreamData streamData = driverMap.get(key);

                List<StreamMapDiff> diffs = lookup.get(type);
                boolean stillLive = false;
                for(StreamMapDiff d : diffs) {
                    if(streamData.getUser().equals(d.stream.getUser())) {
                        stillLive = true;
                        break;
                    }
                }

                if(!stillLive) {
                    System.out.println("Removing:" + streamData.toString());
                    driverMap.remove(key);
                    diff.add(new StreamMapDiff(Status.OFFLINE, streamData));
                }
            }
        }

        return diff;
    }

    private Status addOrUpdate(StreamData data) {
        Map<Object, StreamData> driverMap = this.map.get(data.getType());
        StreamData storedData = driverMap.get(data.getUser());
        if(storedData == null) {
            driverMap.put(data.getUser(), data);
            return Status.LIVE;
        }
        else {
            if(storedData.equals(data)) {
                return Status.NO_CHANGE;
            }
            else {
                driverMap.put(data.getUser(), data);
                return Status.DATA_CHANGED;
            }
        }
    }

    /**
     * Enum to help with the Status changes for currently
     * stored streams in the stream list
     */
    public enum Status {
        DATA_CHANGED("Data changed"),
        NO_CHANGE("No Change"),
        LIVE("Live"),
        OFFLINE("Offline");

        private String display_text;
        private Status(String text) {
            this.display_text = text;
        }
    }

    public class StreamMapDiff {
        public Status status;
        public StreamData stream;

        public StreamMapDiff(Status status, StreamData data) {
            this.status = status;
            this.stream = data;
        }
    }
}
