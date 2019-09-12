package com.example.smartspacesproject2;

import android.util.Log;

import java.util.Collections;
import java.util.Vector;

public class BeaconIDAndDistance {

    private final int N = 2;

    private String mac = new String();
    private int rssi;
    private int measuredPower;
    private Vector<Integer> rssis = new Vector<>();
    private double distance;


    public BeaconIDAndDistance(String mac, int measuredPower)
    {
        this.mac.concat(mac);
        //this.rssi = rssi;
        this.measuredPower=measuredPower;

    }


    /*public double getDistanceAndClear()
    {
        double result = getDistancesAverage();
        rssis.clear();
        return result;
    }*/

    public double getDistance()
    {
        calculateDistance();
        return distance;
    }

    public String getMac() {
        return mac;
    }

    public void addRssi(int rssi)
    {
        this.rssis.add(rssi);
    }

    public void setMac(String id) {
        this.mac = id;
    }
    
    
    private double getRssiAverage()
    {
        double sum=0;
        //Vector<Double> temp = new Vector<>();
        //temp.addAll(distances);
        Collections.sort(rssis);
        int i = 0 ;
        for (; i<rssis.size()*8/10; ++i) {

            sum+=rssis.get(i).doubleValue();

        }

        return sum/i;

    }

    public void calculateDistance()
    {

        Log.d("calcdist", ""+measuredPower+ " " + getRssiAverage());
        distance = Math.pow(10.0, ((measuredPower-getRssiAverage())/20));


    }

    public void setMeasuredPower(int measuredPower) {
        this.measuredPower = measuredPower;
    }
}
