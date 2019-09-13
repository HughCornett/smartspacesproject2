package com.example.smartspacesproject2;

import android.util.Log;

import java.util.Collections;
import java.util.Vector;

public class BeaconIDAndDistance {

    private final double N = 1.5;

    private String mac;
    private int rssi;
    private int measuredPower;
    private Vector<Integer> rssis = new Vector<>();
    private double distance;


    public BeaconIDAndDistance(String mac, int measuredPower)
    {
        this.mac = new String(mac);
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
        int i = rssis.size()/10 ;
        for (; i<rssis.size()*9/10; ++i) {

            sum+=rssis.get(i).doubleValue();

        }

        double denominator = ((rssis.size()*9/10)-(rssis.size()/10));
        if(denominator != 0)
        {
            return sum/denominator;
        }
        else {
            return rssis.get(0);
        }
    }

    private double getRssiMin()
    {

        Collections.sort(rssis);
        //Vector<Double> result = new Vector<>();

        //double median;


        return rssis.get(rssis.size()/20);
    }



    public void calculateDistance()
    {

        //Log.d("calcdist", ""+measuredPower+ " " + getRssiAverage());
        distance = Math.pow(10.0, ((measuredPower-getRssiAverage())/(10*N)));


    }

    public void setMeasuredPower(int measuredPower) {
        this.measuredPower = measuredPower;
    }
}
