package com.example.smartspacesproject2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;


import android.view.View;
import android.graphics.Point;

import java.util.Vector;

public class DrawView extends View
{
    // CONSTANT VARIABLES
    public static final int RECTANGLE_COLOR = Color.RED;
    public static final int RECTANGLE_ALPHA = 60;

    public static final int CIRCLE_ALPHA = 255;
    public static final int CIRCLE_COLOR = Color.BLUE;
    public static final int CIRCLE_RADIUS = 25;

    public static final int LARGE_CIRCLE_ALPHA = 125;
    public static final int LARGE_CIRCLE_COLOR = Color.BLUE;
    public static final int LARGE_CIRCLE_RADIUS_MODIFIER = 2;

    //the amount on each side the width should be stretched to reduce image stretching
    public static final double SIZE_MODIFIER = 0.165;

    // OTHER VARIABLES
    private Drawable mapImage;
    public static Rect imageBounds;
    private Vector<Rect> drawnBoxes = new Vector<>();
    public static CoordinatePair position;

    Paint paint = new Paint();
    public DrawView(Context context) {
        super(context);
        mapImage = context.getResources().getDrawable(R.mipmap.map_image_foreground);
    }

    /**
     * redraws the view
     * for when the boxes are changed and must be drawn again
     */
    public void updateView()
    {
        //drawnBoxes.clear();
        //drawnBoxes.addAll(boxes);
        invalidate();
        //drawnBoxes.clear();
    }

    @Override
    public void onDraw(Canvas canvas) {
        //draw the map background
        imageBounds = canvas.getClipBounds();

        //calculate the bounds of the background image
        Point size = new Point();
        MainActivity.display.getSize(size);
        int newLeft = -(int) Math.round((size.x*SIZE_MODIFIER));
        int newRight = size.x + (int) Math.round(size.x*SIZE_MODIFIER);
        int newTop = 0;
        int newBottom = size.y - (int) Math.round(size.y*SIZE_MODIFIER);

        mapImage.setBounds(newLeft, newTop, newRight, newBottom);
        mapImage.draw(canvas);

        //define the color, width and transparency
        paint.setColor(RECTANGLE_COLOR);
        paint.setAlpha(RECTANGLE_ALPHA);

        //draw all the rectangles
        for(int i = 0; i < drawnBoxes.size(); i++)
        {
            canvas.drawRect(drawnBoxes.get(i), paint);
        }

        //drawnBoxes.get(0).bottom+=5;
        drawnBoxes.clear();

        //redfine the color and transparency
        paint.setColor(CIRCLE_COLOR);
        paint.setAlpha(CIRCLE_ALPHA);

        //draw the position circle if it has been defined
        if(position != null)
            canvas.drawCircle((float) position.getX(), (float) position.getY(), CIRCLE_RADIUS, paint);

    }
    public void addBox(Rect box)
    {
        drawnBoxes.add(box);
    }
}
