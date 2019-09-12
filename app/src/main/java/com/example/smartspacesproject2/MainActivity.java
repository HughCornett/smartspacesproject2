package com.example.smartspacesproject2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.os.RemoteException;
import android.view.Display;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;

import com.opencsv.CSVReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends FragmentActivity implements BeaconConsumer, Runnable{
    //CONSTANT VARIABLES


    //OTHER VARIABLES
    DrawView drawView;
    private Vector<Rect> boxes = new Vector<>();

    public static Display display;
    public static int BOTTOM_BORDER;
    public static int LEFT_BORDER;
    public static int WIDTH;
    public static int HEIGHT;
    public static double PIXELS_PER_METER;

    //Dictionary of beacon name to pixel coordinates on screen
    Map<String, CoordinatePair> map = new HashMap<>();


    private BeaconManager beaconManager;

    private ArrayList<BeaconIDAndDistance> beaconList = new ArrayList<>();

    private static CoordinatePair translateWorldToMap(CoordinatePair worldCoordinates)
    {
        int newX = BOTTOM_BORDER + (int) Math.round((worldCoordinates.getY()/38)*HEIGHT);
        int newY = LEFT_BORDER + (int) Math.round((worldCoordinates.getX()/50)*WIDTH);
        return new CoordinatePair(newX, newY);

    }
    private static int translateMetersToPixels(double meters)
    {
        return (int) Math.round(meters * PIXELS_PER_METER);
    }

    private void addBox(int x, int y, int radius)
    {
        boxes.add(new Rect(x-radius,y+radius,x+radius,y-radius));
    }
    private void addBox(CoordinatePair coordinates, int radius)
    {
        drawView.addBox(new Rect((int) coordinates.getX()-radius,(int) coordinates.getY()+radius,
                (int) coordinates.getX()+radius,(int) coordinates.getY()-radius));
    }
    private void clearBoxes()
    {
        boxes.clear();
    }

    private void updateDrawview()
    {
        drawView.updateView();


    }

    private static Rect getBoxIntersection(Rect box1, Rect box2, Rect box3)
    {

        //create a rectangle for the intersection of the 3 given rectangles
        Rect intersection = new Rect();
        //set intersection to the intersect of the first 2 boxes
        //and set success to true if there was an intersection, false otherwise
        boolean success = intersection.setIntersect(box1, box2);
        //set intersection to the intersect between itself and the 3rd box
        //and set success to true if *both* were intersections, false otherwise
        success = success && intersection.setIntersect(intersection, box3);

        if(success) { return intersection; }
        else {return null; }
    }

    private void initMap() throws java.io.IOException{
        String k;
        double x;
        double y;

        final CSVReader csvReader = new CSVReader(new InputStreamReader(
                MainActivity.this.getApplicationContext().getAssets().open("ibeacons.csv")
        ));
        String[] row;
        while ((row = csvReader.readNext()) != null) {
            System.out.println(row[1]);
            //take the MAC address without the single quotes at the start and end
            k = row[1].substring(1, 18);
            //take the x and y coords for the value as a CoordinatePair
            y  = Double.parseDouble(row[3]);
            x = Double.parseDouble(row[2]);
            //add to the hashmap
            map.put(k, new CoordinatePair(x, y));
        }
        csvReader.close();
    }

    private void initBeaconManager()
    {
        this.beaconManager = BeaconManager.getInstanceForApplication(this);
        this.beaconManager.getBeaconParsers().add(new BeaconParser(). setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));

        BeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
        this.beaconManager.bind(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            initMap();
        }catch(java.io.IOException exeption) {
            System.out.println("!!! IO EXCEPTION INITIALIZING HASHMAP !!!");
        }

        initBeaconManager();

        drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.WHITE);
        setContentView(drawView);

        display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        MainActivity.display.getSize(size);


        //initialize variables for image size etc.

        BOTTOM_BORDER = (int) Math.round(size.x*0.11);
        LEFT_BORDER = (int) Math.round(size.y*0.11);
        HEIGHT = size.x - (int) Math.round(size.x*0.19);
        WIDTH = size.y - (int) Math.round(size.y*0.35);
        PIXELS_PER_METER = WIDTH / 50;

        //addBox(BOTTOM_BORDER, LEFT_BORDER, 10);
        //addBox(BOTTOM_BORDER + HEIGHT, LEFT_BORDER + WIDTH, 10);
        addBox(translateWorldToMap(map.get("C0:F9:12:41:5F:A9")), translateMetersToPixels(19));

        //updateDrawview();

        Thread thread = new Thread(this);
        thread.start();

    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0)
                {
                    //beaconList.clear();
                    for(Iterator<Beacon> iterator = beacons.iterator(); iterator.hasNext();) {
                        Beacon beacon = iterator.next();
                        addOrUpdateList(beacon);
                    }

                    Collections.sort(beaconList,new SortByDistance());
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }

    private void addOrUpdateList(Beacon beacon)
    {

        //update
        for(int i = 0; i<beaconList.size(); ++i)
        {
            BeaconIDAndDistance idAndDistance = beaconList.get(i);

            if(idAndDistance.getId()==beacon.getId3().toInt())
            {
                idAndDistance.setDistance(beacon.getDistance());
                return;
            }
        }

        //add
        if(beacon.getId3().toInt()!=0) //not an iBeacon
                beaconList.add(new BeaconIDAndDistance(beacon.getId3().toInt(),beacon.getDistance()));
    }

    @Override
    public void run() {

        long currentTime = System.currentTimeMillis();
        while (true)
        {
            if(System.currentTimeMillis()-currentTime>1000)
            {
                currentTime +=1000;

                //drawView.addBox(new Rect(0,0,300,300));
                if(beaconList.size()>=3) {
                    for (int i = 0; i < 3; ++i) {
                        addBox(translateWorldToMap(map.get(beaconList.get(i).getId())), translateMetersToPixels(5+beaconList.get(i).getDistance()));
                    }
                }
                updateDrawview();
            }
        }



    }




}
