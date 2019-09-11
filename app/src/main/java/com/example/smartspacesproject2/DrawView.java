package com.example.smartspacesproject2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import android.view.View;

import java.util.Vector;

public class DrawView extends View
{
    // CONSTANT VARIABLES
    public static final int DEFAULT_COLOR = Color.RED;
    public static final int DEFAULT_WIDTH = 3;
    public static final int DEFAULT_ALPHA = 60;

    //the amount on each side the width should be stretched to reduce image stretching
    public static final double WIDTH_MODIFIER = 0.165;

    // OTHER VARIABLES
    private Drawable mapImage;
    private static Vector<Rect> drawnBoxes = new Vector<>();

    Paint paint = new Paint();
    public DrawView(Context context) {
        super(context);
        mapImage = context.getResources().getDrawable(R.mipmap.map_image_foreground);
    }

    public static void updateBoxes(Vector<Rect> boxes)
    {
        drawnBoxes = boxes;
    }

    @Override
    public void onDraw(Canvas canvas) {
        //draw the map background
        Rect imageBounds = canvas.getClipBounds();
        int newLeft = (int) (imageBounds.left - imageBounds.width()*WIDTH_MODIFIER);
        int newRight = (int) (imageBounds.right + imageBounds.width()*WIDTH_MODIFIER);
        int newTop = imageBounds.top;
        int newBottom = imageBounds.bottom;

        mapImage.setBounds(newLeft, newTop, newRight, newBottom);
        mapImage.draw(canvas);

        //draw all the rectangles
        paint.setColor(DEFAULT_COLOR);
        paint.setStrokeWidth(DEFAULT_WIDTH);
        paint.setAlpha(DEFAULT_ALPHA);
        //paint.setStyle(Paint.Style.STROKE);
        for(int i = 0; i < drawnBoxes.size(); i++)
        {
            canvas.drawRect(drawnBoxes.get(i), paint);
        }

    }
}
