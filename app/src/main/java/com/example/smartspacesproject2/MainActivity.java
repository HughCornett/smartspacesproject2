package com.example.smartspacesproject2;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Rect;
import android.os.Bundle;
import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    //CONSTANT VARIABLES
    public static final int BOTTOM_BORDER = 125;
    public static final int LEFT_BORDER = 205;
    public static final int WIDTH = 1185;
    public static final int HEIGHT = 865;

    //OTHER VARIABLES
    DrawView drawView;
    private static Vector<Rect> boxes = new Vector<>();

    //Dictionary of beacon name to pixel coordinates on screen
    Map<Integer, CoordinatePair> map = new HashMap<>();

    private static CoordinatePair translateWorldToMap(CoordinatePair worldCoordinates)
    {
        int newX = BOTTOM_BORDER + (int) Math.round((worldCoordinates.getY()/38)*HEIGHT);
        int newY = LEFT_BORDER + (int) Math.round((worldCoordinates.getX()/50)*WIDTH);
        return new CoordinatePair(newX, newY);

    }

    private void addBox(int x, int y, int radius)
    {
        boxes.add(new Rect(x-radius,y+radius,x+radius,y-radius));
    }
    private void addBox(CoordinatePair coordinates, int radius)
    {
        boxes.add(new Rect((int) coordinates.getX()-radius,(int) coordinates.getY()+radius,
                (int) coordinates.getX()+radius,(int) coordinates.getY()-radius));
    }
    private static void updateDrawview()
    {
        DrawView.updateBoxes(boxes);
    }
    private void initMap()
    {
        //add beacons and their locations to the hashmap
        map.put(189, new CoordinatePair(24.93, 18.73));
        map.put(189, new CoordinatePair(31.35, 25.41));
        map.put(53, new CoordinatePair(24.93, 18.73));
        map.put(79, new CoordinatePair(18.51, 11.14));
        map.put(228, new CoordinatePair(12.87, 12.88));
        map.put(130, new CoordinatePair(25.83, 11.44));
        map.put(136, new CoordinatePair(12.81, 23.08));
        map.put(69, new CoordinatePair(18.63, 1.83));
        map.put(231, new CoordinatePair(45.27, 5.35));
        map.put(173, new CoordinatePair(48.87, 18.67));
        map.put(220, new CoordinatePair(37.77, 0.70));
        map.put(142, new CoordinatePair(38.73, 36.87));
        map.put(253, new CoordinatePair(18.99, 25.35));
        map.put(15, new CoordinatePair(11.73, 36.52));
        map.put(230, new CoordinatePair(11.91, 26.31));
        map.put(52, new CoordinatePair(7.41, 21.89));
        map.put(144, new CoordinatePair(22.77, 36.76));
        map.put(265, new CoordinatePair(31.05, 35.80));
        map.put(139, new CoordinatePair(28.41, 1.23));
        map.put(229, new CoordinatePair(49.65, 32.52));
        map.put(61, new CoordinatePair(6.39, 1.83));
        map.put(223, new CoordinatePair(36.27, 17.65));
        map.put(3, new CoordinatePair(1.11, 36.28));
        map.put(258, new CoordinatePair(13.47, 30.31));
        map.put(221, new CoordinatePair(44.79, 32.16));
        map.put(60, new CoordinatePair(1.23, 6.25));
        map.put(54, new CoordinatePair(7.17, 9.47));
        map.put(178, new CoordinatePair(37.77, 25.05));
        map.put(172, new CoordinatePair(44.07, 12.04));
        map.put(207, new CoordinatePair(19.05, 31.80));
        map.put(252, new CoordinatePair(25.17, 30.13));
        map.put(188, new CoordinatePair(37.71, 30.01));
        map.put(174, new CoordinatePair(14.67, 36.70));
        map.put(135, new CoordinatePair(49.83, 25.47));
        map.put(4, new CoordinatePair(0.99, 26.43));
        map.put(134, new CoordinatePair(31.29, 18.79));
        map.put(24, new CoordinatePair(12.87, 17.95));
        map.put(191, new CoordinatePair(1.17, 31.44));
        map.put(141, new CoordinatePair(38.91, 6.07));
        map.put(171, new CoordinatePair(48.93, 2.85));
        map.put(183, new CoordinatePair(18.93, 18.67));
        map.put(108, new CoordinatePair(38.13, 12.10));
        map.put(182, new CoordinatePair(44.13, 0.88));
        map.put(190, new CoordinatePair(31.29, 30.49));
        map.put(177, new CoordinatePair(49.65, 12.58));
        map.put(125, new CoordinatePair(25.41, 5.89));
        map.put(116, new CoordinatePair(31.41, 11.50));
        map.put(143, new CoordinatePair(7.23, 12.76));
        map.put(180, new CoordinatePair(44.07, 25.11));
        map.put(181, new CoordinatePair(48.33, 36.81));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //initialize the dictionary of beacon IDs to coordinates
        initMap();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.WHITE);
        setContentView(drawView);
        addBox(125, 205, 10);
        addBox(990, 1390, 10);

        addBox(translateWorldToMap(map.get(108)), 100);

        updateDrawview();

    }
}
