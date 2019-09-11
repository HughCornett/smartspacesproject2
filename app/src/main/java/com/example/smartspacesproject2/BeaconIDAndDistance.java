package com.example.smartspacesproject2;

public class BeaconIDAndDistance {

    private int id;
    private double distance;

    public BeaconIDAndDistance(int id, double distance)
    {
        this.id=id;
        this.distance = distance;

    }


    public double getDistance() {
        return distance;
    }

    public int getId() {
        return id;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setId(int id) {
        this.id = id;
    }
}
