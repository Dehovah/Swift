package com.hovahs.swift.historyRecyclerView;

/**
 * Created by manel on 10/10/2017.
 */

public class HistoryObject {
    private String rideId;
    private String time;
    private String destination;

    public HistoryObject(String rideId, String time, String destination){
        this.rideId = rideId;
        this.time = time;
        this.destination = destination;
    }

    public String getRideId(){return rideId;}
    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getTime(){return time;}
    public void setTime(String time) {
        this.time = time;
    }


    public String getDestination(){return destination;}
}
