package com.example.smartspacesproject2;

import org.altbeacon.beacon.Beacon;

public class CoordinatePair
{
    private double x;
    private double y;


    public CoordinatePair(double x, double y)
    {
        this.x = x;
        this.y = y;

    }


    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }



    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }


}
