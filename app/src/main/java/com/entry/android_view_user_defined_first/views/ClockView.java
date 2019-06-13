package com.entry.android_view_user_defined_first.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.entry.android_view_user_defined_first.R;

import java.util.Calendar;

public class ClockView extends View {

    private Canvas mCanvas;

    private float mSecondDegree;
    private float mMinuteDegree;
    private float mHourDegree;

    /* 亮色，用于分针、秒针、渐变终止色 */
    private int mLightColor;
    /* 秒针画笔 */
    private Paint mSecondHandPaint;

    /* 时针路径 */
    private Path mHourHandPath = new Path();
    /* 时针画笔 */
    private Paint mHourHandPaint;

    /* 分针路径 */
    private Path mMinuteHandPath = new Path();
    /* 分针画笔 */
    private Paint mMinuteHandPaint;
    /* 小时圆圈的外接矩形 */
    private RectF mCircleRectF = new RectF();
    /* 加一个默认的padding值，为了防止用camera旋转时钟时造成四周超出view大小 */
    private float mDefaultPadding;
    private float mPaddingLeft;
    private float mPaddingTop;
    private float mPaddingRight;
    private float mPaddingBottom;
    /* 时钟半径，不包括padding值 */
    private float mRadius;
    /* 刻度圆弧的外接矩形 */
    private RectF mScaleArcRectF = new RectF();
    /* 秒针路径 */
    private Path mSecondHandPath = new Path();

    public ClockView(Context context) {
        super(context);
    }

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ClockView, 0, 0);
        mLightColor = ta.getColor(R.styleable.ClockView_clock_lightColor, Color.parseColor("#ffffff"));
        ta.recycle();

        mSecondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondHandPaint.setStyle(Paint.Style.FILL);
        mSecondHandPaint.setColor(Color.BLACK);

        mMinuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinuteHandPaint.setStyle(Paint.Style.FILL);
        mMinuteHandPaint.setColor(mLightColor);

        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mHourHandPaint.setColor(mLightColor);

        mScaleArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleArcPaint.setStyle(Paint.Style.STROKE);

        mGradientMatrix = new Matrix();

        mScaleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleLinePaint.setStyle(Paint.Style.STROKE);
        mScaleLinePaint.setColor(mLightColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width  = onMeasureSpec(widthMeasureSpec);
        int height = onMeasureSpec(heightMeasureSpec);
        if (width > height){
            width = height;
        }else {
            height = width;
        }
        setMeasuredDimension(width, height);
    }

    private int onMeasureSpec(int measureSpec){
        int defaultSize = 800;

        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        if (mode == MeasureSpec.UNSPECIFIED){
            return defaultSize;
        }else if (mode == MeasureSpec.AT_MOST){
            return Math.min(size, defaultSize);
        }else if (mode == MeasureSpec.EXACTLY){
            return size;
        }else {
            return defaultSize;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        getCurrentTime();
        drawScaleLine();
        drawSecondNeedle();
        drawMinuteNeedle();
        drawHourHand();
        invalidate();
    }

    private void getCurrentTime(){
        Calendar calendar = Calendar.getInstance();
        float milliSecond = calendar.get(Calendar.MILLISECOND);
        float second = calendar.get(Calendar.SECOND) + milliSecond / 1000;
        float minute = calendar.get(Calendar.MINUTE) + second / 60;
        float hour   = calendar.get(Calendar.HOUR)   + minute / 60;
        mSecondDegree = second / 60 * 360;
        mMinuteDegree = minute / 60 * 360;
        mHourDegree   = hour   / 60 * 360;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRadius = Math.min(w - getPaddingLeft() - getPaddingRight(),
                h - getPaddingTop() - getPaddingBottom()) / 2;// 各个指针长度
        mDefaultPadding = 0.12f * mRadius;
        mPaddingLeft = mDefaultPadding + w / 2 - mRadius + getPaddingLeft();// 钟离左边界距离
        mPaddingRight = mDefaultPadding + w / 2 - mRadius + getPaddingRight();// 钟离右边界距离
        mPaddingTop = mDefaultPadding + h / 2 - mRadius + getPaddingTop();// 钟离上边界距离
        mPaddingBottom = mDefaultPadding + h / 2 - mRadius + getPaddingBottom();// 钟离下边界距离

        mScaleLength = 0.12f * mRadius;// 根据比例确定刻度线长度
        mScaleLinePaint.setStrokeWidth(0.012f * mRadius);// 刻度圈的宽度

        mScaleArcPaint.setStrokeWidth(mScaleLength);

        //梯度扫描渐变，以(w/2,h/2)为中心点，两种起止颜色梯度渐变
        //float数组表示，[0,0.75)为起始颜色所占比例，[0.75,1}为起止颜色渐变所占比例
        mSweepGradient = new SweepGradient(w / 2, h / 2,
                new int[]{Color.RED, mLightColor}, new float[]{0.75f, 1});
    }

    /**
     * 秒针
     */
    private void drawSecondNeedle() {
        mCanvas.save();// ❑ save：用来保存Canvas的状态。save之后，可以调用Canvas的平移、放缩、旋转、错切、裁剪等操作。
        mCanvas.rotate(mSecondDegree, getWidth() / 2, getHeight() / 2);// 设置指针位置
        mSecondHandPath.reset();
        float offset = mPaddingTop;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        mSecondHandPath.moveTo(getWidth() / 2, offset + 0.26f * mRadius);// 这三行绘制三角尖
        mSecondHandPath.lineTo(getWidth() / 2 - 0.05f * mRadius, offset + 0.34f * mRadius);
        mSecondHandPath.lineTo(getWidth() / 2 + 0.05f * mRadius, offset + 0.34f * mRadius);
        mSecondHandPath.close();
        mCanvas.drawPath(mSecondHandPath, mSecondHandPaint);
        mCanvas.restore();// ❑ restore：用来恢复Canvas之前保存的状态。防止save后对Canvas执行的操作对后续的绘制有影响。
    }

    /**
     * 绘制分针
     */
    private void drawMinuteNeedle() {
        mCanvas.save();
        mCanvas.rotate(mMinuteDegree, getWidth() / 2, getHeight() / 2);
        mMinuteHandPath.reset();

        float offset = mPaddingTop ;
        mMinuteHandPath.moveTo(getWidth() / 2 - 0.01f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mMinuteHandPath.lineTo(getWidth() / 2 - 0.008f * mRadius, offset + 0.365f * mRadius);
        mMinuteHandPath.quadTo(getWidth() / 2, offset + 0.345f * mRadius,
                getWidth() / 2 + 0.008f * mRadius, offset + 0.365f * mRadius);
        mMinuteHandPath.lineTo(getWidth() / 2 + 0.01f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mMinuteHandPath.close();
        mMinuteHandPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(mMinuteHandPath, mMinuteHandPaint);

        mCircleRectF.set(getWidth() / 2 - 0.03f * mRadius, getHeight() / 2 - 0.03f * mRadius,//绘制指针轴的小圆圈
                getWidth() / 2 + 0.03f * mRadius, getHeight() / 2 + 0.03f * mRadius);
        mMinuteHandPaint.setStyle(Paint.Style.STROKE);
        mMinuteHandPaint.setStrokeWidth(0.02f * mRadius);
        mCanvas.drawArc(mCircleRectF, 0, 360, false, mMinuteHandPaint);
        mCanvas.restore();
    }

    /**
     * 绘制时针
     */
    private void drawHourHand() {
        mCanvas.save();
        mCanvas.rotate(mHourDegree, getWidth() / 2, getHeight() / 2);
        mHourHandPath.reset();
        float offset = mPaddingTop;
        mHourHandPath.moveTo(getWidth() / 2 - 0.018f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mHourHandPath.lineTo(getWidth() / 2 - 0.009f * mRadius, offset + 0.48f * mRadius);
        mHourHandPath.quadTo(getWidth() / 2, offset + 0.46f * mRadius,
                getWidth() / 2 + 0.009f * mRadius, offset + 0.48f * mRadius);
        mHourHandPath.lineTo(getWidth() / 2 + 0.018f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mHourHandPath.close();
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(mHourHandPath, mHourHandPaint);

        mCircleRectF.set(getWidth() / 2 - 0.03f * mRadius, getHeight() / 2 - 0.03f * mRadius,
                getWidth() / 2 + 0.03f * mRadius, getHeight() / 2 + 0.03f * mRadius);
        mHourHandPaint.setStyle(Paint.Style.STROKE);
        mHourHandPaint.setStrokeWidth(0.01f * mRadius);
        mCanvas.drawArc(mCircleRectF, 0, 360, false, mHourHandPaint);
        mCanvas.restore();
    }

    /* 渐变矩阵，作用在SweepGradient */
    private Matrix mGradientMatrix;
    /* 梯度扫描渐变 */
    private SweepGradient mSweepGradient;
    /* 刻度圆弧画笔 */
    private Paint mScaleArcPaint;
    /* 刻度线长度 */
    private float mScaleLength;
    /* 刻度线画笔 */
    private Paint mScaleLinePaint;
    /**
     * 画一圈梯度渲染的亮暗色渐变圆弧，重绘时不断旋转，上面盖一圈背景色的刻度线
     */
    private void drawScaleLine() {
        mCanvas.save();
        mScaleArcRectF.set(mPaddingLeft + 1.5f * mScaleLength ,
                mPaddingTop + 1.5f * mScaleLength ,
                getWidth() - mPaddingRight - 1.5f * mScaleLength,
                getHeight() - mPaddingBottom - 1.5f * mScaleLength);

        Path path = new Path();
        path.lineTo(75, 75);

        // matrix默认会在三点钟方向开始颜色的渐变，为了吻合钟表十二点钟顺时针旋转的方向，把秒针旋转的角度减去90度
        mGradientMatrix.setRotate(mSecondDegree - 90, getWidth() / 2, getHeight() / 2);
        mSweepGradient.setLocalMatrix(mGradientMatrix);
        mScaleArcPaint.setShader(mSweepGradient);
        mCanvas.drawPath(path, mScaleArcPaint);
//        mCanvas.drawArc(mScaleArcRectF, 0, 360, false, mScaleArcPaint);

//        // 画背景色刻度线
//        for (int i = 0; i < 200; i++) {
//            mCanvas.drawLine(getWidth() / 2, mPaddingTop + mScaleLength ,
//                    getWidth() / 2, mPaddingTop + 2 * mScaleLength , mScaleLinePaint);
//            mCanvas.rotate(1.8f, getWidth() / 2, getHeight() / 2);
//        }
        mCanvas.restore();
    }
}
