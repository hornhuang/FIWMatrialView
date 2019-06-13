package com.entry.android_view_user_defined_first.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class PathView extends View {

    Paint paint;

    public PathView(Context context) {
        super(context);
    }

    public PathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureSpec(widthMeasureSpec),
                measureSpec(heightMeasureSpec));
    }

    private int  measureSpec(int measureSpec){
        int defaultSize = 500;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        if (mode == MeasureSpec.UNSPECIFIED){
            return defaultSize;
        }else if (mode == MeasureSpec.AT_MOST){
            return Math.min(defaultSize, size);
        }else if (mode == MeasureSpec.EXACTLY){
            return size;
        }else {
            return defaultSize;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Path path = new Path();
        path.moveTo(5, 5);
        path.lineTo(25, 75);
        path.lineTo(75, 25);
        path.close();
        canvas.drawPath(path,paint);
    }
}
