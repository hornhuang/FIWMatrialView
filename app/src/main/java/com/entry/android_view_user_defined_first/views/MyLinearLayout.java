package com.entry.android_view_user_defined_first.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.entry.android_view_user_defined_first.utils.MyLog;

public class MyLinearLayout extends ViewGroup {

    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int currentHeight = 0;
        for (int i = 0 ; i < count ; i++){
            View view = getChildAt(i);
            int height = view.getMeasuredHeight();
            int width  = view.getMeasuredWidth();
            view.layout(l, currentHeight, l + width, currentHeight + height);
            currentHeight += height;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        final int widthMode  = MeasureSpec.getMode(widthMeasureSpec);
        final int width      = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int height     = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST){
            int groupWidth  = getMaxWidth();
            int groupHeight = getTotalHeight();
            MyLog.Companion.d("AT_MOST");
            setMeasuredDimension(groupWidth, groupHeight);
        }else if (widthMode == MeasureSpec.AT_MOST){
            MyLog.Companion.d("widthMode" + heightMode + " -" + widthMode + "-- " + MeasureSpec.AT_MOST);
            setMeasuredDimension(getMaxWidth(), height);
        }else if (heightMode == MeasureSpec.AT_MOST){
            setMeasuredDimension(width, getTotalHeight());
        }else {
            setMeasuredDimension(width, height);
        }
    }

    private int getMaxWidth(){
        int count = getChildCount();
        int maxWidth = 0;
        for (int i = 0 ; i < count ; i ++){
            int currentWidth = getChildAt(i).getMeasuredWidth();
            if (maxWidth < currentWidth){
                maxWidth = currentWidth;
            }
        }
        return maxWidth;
    }

    private int getTotalHeight(){
        int count = getChildCount();
        int totalHeight = 0;
        for (int i = 0 ; i < count ; i++){
            totalHeight += getChildAt(i).getMeasuredHeight();
        }
        return totalHeight;
    }
}
