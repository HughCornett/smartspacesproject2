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
import android.util.Log;
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
    //private Vector<Rect> boxes = new Vector<>();

    //Constant variables describing screen that are set on app launch
    public static Display display;
    //pixel distances to bottom and left sides of the actual map
    public static int BOTTOM_BORDER;
    public static int LEFT_BORDER;
    //pixel width and heigh of the map
    public static int WIDTH;
    public static int HEIGHT;
    //number of pixels per real world meter
    public static double PIXELS_PER_METER;

    //Dictionary of beacon name to pixel coordinates on screen
    Map<String, CoordinatePair> map = new HashMap<>();


    private BeaconManager beaconManager;

    private ArrayList<BeaconIDAndDistance> beaconList = new ArrayList<>();

    /**
     * takes a real world coordinate pair
     * (i.e. converts coordinates of beacon in the room to pixel coordinates on map)
     *
     * @param worldCoordinates
     *  the coordinates of something in the real world
     * @return
     *  the pixel coordinates of the something on the map
     */
    private static CoordinatePair translateWorldToMap(CoordinatePair worldCoordinates)
    {
        int newX = BOTTOM_BORDER + (int) Math.round((worldCoordinates.getY()/38)*HEIGHT);
        int newY = LEFT_BORDER + (int) Math.round((worldCoordinates.getX()/50)*WIDTH);
        return new CoordinatePair(newX, newY);

    }

    /**
     * takes a real world distance in meters and converts it to number of pixels on the map
     *
     * @param meters
     *  the distance to be converted
     * @return
     *  the number of pixels for that distance on the map
     */
    private static int translateMetersToPixels(double meters)
    {
        return (int) Math.round(meters * PIXELS_PER_METER);
    }

    /**
     * Adds a box to the list of boxes
     * @param x
     *  x coordinate of the center of the box
     * @param y
     *  y coordinate of the center of the box
     * @param radius
     *  the closest distance from the center to the edge of the box
     */
    private void addBox(int x, int y, int radius)
    {
        drawView.addBox(new Rect(x-radius,y+radius,x+radius,y-radius));
    }

    /**
     * Adds a box to the list of boxes
     * @param coordinates
     *  the CoordinatePair of the center of the box
     * @param radius
     *  the closest distance from the center of the edge of the box
     */
    private void addBox(CoordinatePair coordinates, int radius)
    {
        drawView.addBox(new Rect((int) coordinates.getX()-radius,(int) coordinates.getY()+radius,
                (int) coordinates.getX()+radius,(int) coordinates.getY()-radius));
    }

    /**
     * Redraws the list of boxes
     */
    private void updateDrawview()
    {
        drawView.updateView();


    }

    /**
     * Gets the Rect representing the intersection of three boxes if there is one
     * @param box1
     *  The first box of the (potential) intersection
     * @param box2
     *  The second box of the (potential) intersection
     * @param box3
     *  The third box of the (potential) intersection
     * @return
     *  The rect that is the intersection of the three boxes, null if there is none
     */
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

    /**
     * Reads the file containing beacon information and maps coordinates to MAC addresses in a hashmap
     * @throws java.io.IOException
     *  if there is a problem reading the file
     */
    private void initMap() throws java.io.IOException{
        //define the key and the x and y coordinates
        String k;
        double x;
        double y;

        //initialise the CSV reader and open the file
        final CSVReader csvReader = new CSVReader(new InputStreamReader(
                MainActivity.this.getApplicationContext().getAssets().open("ibeacons.csv")
        ));

        //while there is a next row
        String[] row;
        while ((row = csvReader.readNext()) != null) {
            //Log.d("mac", row[1]);
            //take the MAC address without the single quotes at the start and end
            k = row[1].substring(1, 18);
            Log.d("mac2", k);
            //take the x and y coords for the value as a CoordinatePair
            y  = Double.parseDouble(row[3]);
            Log.d("y",""+y);
            x = Double.parseDouble(row[2]);
            Log.d("x",""+x);
            //add to the hashmap
            map.put(k, new CoordinatePair(x, y));
        }
        //close the file
        csvReader.close();
    }

    /**
     * Initialises the beacon manager, asks for location permissions
     */
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

    /**
     * Initialisation for when the app starts
     * runs initMap(), initBeaconManager(), creates the drawView,
     * gets display information, starts thread
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //initialize the dictionary of beacon IDs to coordinates


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //call initMap(), if there is an IO error then print error to output
        try {
            initMap();
        }catch(java.io.IOException exeption) {
            System.out.println("!!! IO EXCEPTION INITIALIZING HASHMAP !!!");
        }

        initBeaconManager();

        //initialise the drawView
        drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.WHITE);
        setContentView(drawView);

        //initialise the display object and calculate relevant variables from this
        display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        MainActivity.display.getSize(size);

        BOTTOM_BORDER = (int) Math.round(size.x*0.11);
        LEFT_BORDER = (int) Math.round(size.y*0.11);
        HEIGHT = size.x - (int) Math.round(size.x*0.19);
        WIDTH = size.y - (int) Math.round(size.y*0.35);
        PIXELS_PER_METER = WIDTH / 50;

        //addBox(BOTTOM_BORDER, LEFT_BORDER, 10);
        //addBox(BOTTOM_BORDER + HEIGHT, LEFT_BORDER + WIDTH, 10);
        //addBox(translateWorldToMap(map.get("C0:F9:12:41:5F:A9")), translateMetersToPixels(19));

        //updateDrawview();

        //start the thread
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


                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }

    private void addOrUpdateList(Beacon beacon)
    {
        //Log.d("address",beacon.getBluetoothAddress());
        //update
        for(int i = 0; i<beaconList.size(); ++i)
        {
            BeaconIDAndDistance idAndDistance = beaconList.get(i);

            if(idAndDistance.getMac().equals(beacon.getBluetoothAddress()))
            {
                //idAndDistance.setMeasuredPower(beacon.getTxPower());
                idAndDistance.addRssi(beacon.getRssi());
                return;
            }
        }

        //add
        if(map.get(beacon.getBluetoothAddress()) != null)
        {
            beaconList.add(new BeaconIDAndDistance(beacon.getBluetoothAddress(), beacon.getTxPower()));
            beaconList.get(beaconList.size()-1).addRssi(beacon.getRssi());
        }
    }

    @Override
    public void run() {

        long currentTime = System.currentTimeMillis();
        long restartTime = System.currentTimeMillis();
        while (true)
        {
            if(System.currentTimeMillis()-currentTime>1000)
            {
                currentTime +=1000;

                Collections.sort(beaconList, new SortByDistance());

                if(beaconList.size()>=3) {
                    /*
                    int size=3;
                    for (int i = 0; i < size && i<beaconList.size(); ++i) {
                        //Log.d("mac attempt",""+beaconList.get(i).getMac());
                        //System.out.println(beaconList.get(i).getMac());
                        if(map.get(beaconList.get(i).getMac()) != null) {
                            addBox(translateWorldToMap(map.get(beaconList.get(i).getMac())), translateMetersToPixels(beaconList.get(i).getDistance()));
                        }
                        else ++size;


                    }
                    */
                    int count = 0;
                    int i = 0;

                    while(i < beaconList.size() && count<3)
                    {
                        Log.d("sortedlist",beaconList.get(i).getMac() + " " + beaconList.get(i).getDistance());
                        if(map.get(beaconList.get(i).getMac()) != null) {

                                addBox(translateWorldToMap(map.get(beaconList.get(i).getMac())), translateMetersToPixels(beaconList.get(i).getDistance()));

                                count++;

                        }
                        i++;
                    }
                }
                updateDrawview();
            }


        }



    }




}
