package com.example.smartspacesproject2;

import org.altbeacon.beacon.Beacon;

import java.util.Comparator;

public class SortByDistance implements Comparator<BeaconIDAndDistance> {
    @Override
    public int compare(BeaconIDAndDistance beacon, BeaconIDAndDistance beacon1) {
        if(beacon.getDistance()-beacon1.getDistance()<0)
        {
            return -1;
        }
        else if(beacon.getDistance()-beacon1.getDistance()>0)
        {
            return 1;
        }
        else return 0;

    }
}
