package com.e.phonetest;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.graphics.ColorUtils;
import java.util.Locale;

public class AngleIndicator extends View {

    public static final int AI_CIRCLE_COLOR = Color.BLUE;
    public static final int AI_ARROW_COLOR = Color.RED;
    public static final int AI_ARC_PIE_COLOR = Color.GREEN;
    public static final int AI_ZERO_LINE_COLOR = Color.YELLOW;
    public static final int AI_ZERO_LINE_POSITION = 0;
    public static final float AI_ZERO_LINE_WIDTH = 4f;
    public static final float AI_CURRENT_VALUE = 0f;
    public static final float AI_CIRCLE_LIGHT_COLOR_RATIO = 0.95f;
    public static final float AI_CIRCLE_DARK_COLOR_RATIO = 0.35f;
    public static final String AI_DEGREE_SIGN = "°";
    public static final boolean AI_SHOW_ANGLE_ARC = false;
    public static final boolean AI_SHOW_ANGLE_PIE = false;
    public static final boolean AI_SHOW_DEGREE_SIGN = false;
    public static final boolean AI_SHOW_DIRECTION = false;
    public static final boolean AI_SHOW_ZERO_LINE = false;

    private boolean mShowAngleArc, mShowAnglePie, mShowDegreeSign, mShowDirection, mShowZeroLine;
    private int mArcPieColor, mArrowColor, mCircleColor, mZeroLineColor, mZeroLinePosition;
    private float mCurrentValue, mZeroLineWidth, mCircleLightColorRatio, mCircleDarkColorRatio;
    Path arrowPolygonPath = new Path(), arcPath = new Path();
    PointF[] arrowPolygonPoints;
    private RectF rect1, rectArc, rect2;

    private Paint bmpPaint, borderPaint, lightCirclePaint, darkCirclePaint, zeroLinePaint, angleArcPaint, anglePiePaint, lgBrush, textPaint;
    private Bitmap bmp;

    public float getCurrentValue() {return mCurrentValue;}

    public void setCurrentValue(float currentValue){
        mCurrentValue = currentValue;
        invalidate();
        requestLayout();
    }

    public AngleIndicator(Context context) {
        super(context);
        init(context, null, 0);
    }

    public AngleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AngleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, final AttributeSet attrs, final int defStyle){
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AngleIndicator, defStyle, 0);

        mCircleColor = typedArray.getColor(R.styleable.AngleIndicator_aiCircleColor, AI_CIRCLE_COLOR);
        mArcPieColor = typedArray.getColor(R.styleable.AngleIndicator_aiArcPieColor, AI_ARC_PIE_COLOR);
        mArrowColor = typedArray.getColor(R.styleable.AngleIndicator_aiArrowColor, AI_ARROW_COLOR);
        mZeroLineColor = typedArray.getColor(R.styleable.AngleIndicator_aiZeroLineColor, AI_ZERO_LINE_COLOR);
        mShowAngleArc = typedArray.getBoolean(R.styleable.AngleIndicator_aiShowAngleArc, AI_SHOW_ANGLE_ARC);
        mShowAnglePie = typedArray.getBoolean(R.styleable.AngleIndicator_aiShowAnglePie, AI_SHOW_ANGLE_PIE);
        mShowDegreeSign = typedArray.getBoolean(R.styleable.AngleIndicator_aiShowDegreeSign, AI_SHOW_DEGREE_SIGN);
        mShowDirection = typedArray.getBoolean(R.styleable.AngleIndicator_aiShowDirection, AI_SHOW_DIRECTION);
        mShowZeroLine = typedArray.getBoolean(R.styleable.AngleIndicator_aiShowZeroLine, AI_SHOW_ZERO_LINE);
        mZeroLineWidth = typedArray.getFloat(R.styleable.AngleIndicator_aiZeroLineWidth, AI_ZERO_LINE_WIDTH);
        mCurrentValue = typedArray.getFloat(R.styleable.AngleIndicator_aiCurrentValue, AI_CURRENT_VALUE);
        mCircleLightColorRatio = typedArray.getFloat(R.styleable.AngleIndicator_aiCircleLightColorRatio, AI_CIRCLE_LIGHT_COLOR_RATIO);
        mCircleDarkColorRatio = typedArray.getFloat(R.styleable.AngleIndicator_aiCircleDarkColorRatio, AI_CIRCLE_DARK_COLOR_RATIO);
        mZeroLinePosition = typedArray.getInt(R.styleable.AngleIndicator_aiZeroLinePosition, AI_ZERO_LINE_POSITION);

        typedArray.recycle();

        bmpPaint = new Paint();
        bmpPaint.setFilterBitmap(true);

        textPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);

        lightCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightCirclePaint.setStyle(Paint.Style.FILL);

        darkCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        darkCirclePaint.setStyle(Paint.Style.FILL);

        zeroLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zeroLinePaint.setStyle(Paint.Style.STROKE);

        anglePiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        anglePiePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        angleArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        angleArcPaint.setStyle(Paint.Style.STROKE);

        lgBrush = new Paint(Paint.ANTI_ALIAS_FLAG);
        lgBrush.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // maintain the square layout
        final int canvasWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int canvasHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (canvasWidth > canvasHeight) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(canvasHeight, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(canvasHeight, MeasureSpec.EXACTLY)
            );
        } else {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(canvasWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(canvasWidth, MeasureSpec.EXACTLY)
            );
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        drawCircles(canvas);
        drawZeroLine(canvas);

        canvas.save();
        drawArrow(canvas);
        canvas.restore();

        drawText(canvas);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        reDraw();
    }

    private void reDraw(){
        if (bmp != null){
            bmp.recycle();
        }

        rect1 = new RectF(0, 0,  getWidth(), getHeight());
        rect2 = new RectF(getWidth() / 50f, getHeight() / 50f, getWidth() - getWidth() / 50f, getHeight() - getHeight() / 50f);
        rectArc = new RectF(getWidth() / 100f, getWidth() / 100f,  getWidth() - getWidth() / 100f, getHeight() - getWidth() / 100f);
        RectF rect3 = new RectF(getWidth() / 2f - getWidth() * 0.3f / 7f, getHeight() * 3.1f / 7f, getWidth() / 2f + getWidth() * 0.4f / 7f, getHeight() * 3.9f / 7f);

        bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bmp);

        arrowPolygonPoints = new PointF[] {
                new PointF(getWidth() / 2f, getHeight() * 3.1f / 7f),
                new PointF(getWidth() * 5.25f / 7f, getHeight() * 3.1f / 7f),
                new PointF(getWidth() * 5.25f / 7f, getHeight() * 6f / 16f),
                new PointF(getWidth() - 4f, getHeight() * 3.5f / 7f),
                new PointF(getWidth() * 5.25f / 7f, getHeight() * 10f / 16f),
                new PointF(getWidth() * 5.25f / 7f, getHeight() * 3.9f / 7f),
                new PointF(getWidth() / 2f, getHeight() * 3.9f / 7f)
        };

        arrowPolygonPath.reset();
        arrowPolygonPath.moveTo(arrowPolygonPoints[0].x, arrowPolygonPoints[0].y);
        arrowPolygonPath.lineTo(arrowPolygonPoints[1].x, arrowPolygonPoints[1].y);
        arrowPolygonPath.lineTo(arrowPolygonPoints[2].x, arrowPolygonPoints[2].y);
        arrowPolygonPath.lineTo(arrowPolygonPoints[3].x, arrowPolygonPoints[3].y);
        arrowPolygonPath.lineTo(arrowPolygonPoints[4].x, arrowPolygonPoints[4].y);
        arrowPolygonPath.lineTo(arrowPolygonPoints[5].x, arrowPolygonPoints[5].y);
        arrowPolygonPath.lineTo(arrowPolygonPoints[6].x, arrowPolygonPoints[6].y);
        arrowPolygonPath.addArc(rect3, 90, 180);
        arrowPolygonPath.close();

        final float density = getResources().getDisplayMetrics().density;

        borderPaint.setStrokeWidth(2f);

        if (getWidth() < 50 * density){
            textPaint.setTextSize(5 * density);
            borderPaint.setStrokeWidth(0.5f);
            anglePiePaint.setStrokeWidth(2f);
            angleArcPaint.setStrokeWidth(2f);
            mZeroLineWidth = mZeroLineWidth / 4;
        }
        else if (getWidth() < 75 * density){
            textPaint.setTextSize(8 * density);
            borderPaint.setStrokeWidth(0.75f);
            anglePiePaint.setStrokeWidth(3f);
            angleArcPaint.setStrokeWidth(3f);
            mZeroLineWidth = mZeroLineWidth / 3;
        }
        else if (getWidth() < 100 * density){
            textPaint.setTextSize(12 * density);
            borderPaint.setStrokeWidth(1f);
            anglePiePaint.setStrokeWidth(4f);
            angleArcPaint.setStrokeWidth(4f);
            mZeroLineWidth = mZeroLineWidth / 2;
        }
        else if (getWidth() < 150 * density){
            textPaint.setTextSize(16 * density);
            borderPaint.setStrokeWidth(1.5f);
            anglePiePaint.setStrokeWidth(4f);
            angleArcPaint.setStrokeWidth(4f);
            mZeroLineWidth = mZeroLineWidth * 2 / 3;
        }
        else if (getWidth() < 200 * density){
            textPaint.setTextSize(18 * density);
            anglePiePaint.setStrokeWidth(5f);
            angleArcPaint.setStrokeWidth(5f);
            mZeroLineWidth = mZeroLineWidth * 3 / 4;
        }
        else if (getWidth() < 250 * density){
            textPaint.setTextSize(21 * density);
            anglePiePaint.setStrokeWidth(6f);
            angleArcPaint.setStrokeWidth(6f);
        }
        else if (getWidth() < 300 * density){
            textPaint.setTextSize(24 * density);
            anglePiePaint.setStrokeWidth(8f);
            angleArcPaint.setStrokeWidth(8f);
        }
        else{
            textPaint.setTextSize(36 * density);
            anglePiePaint.setStrokeWidth(10f);
            angleArcPaint.setStrokeWidth(10f);
        }

        textPaint.setColor(mZeroLineColor);

        lightCirclePaint.setShader(new RadialGradient(getWidth() / 2f, getHeight() / 2f, rect1.width() / 2f, mCircleColor, ColorUtils.blendARGB(mCircleColor, Color.WHITE, mCircleLightColorRatio), Shader.TileMode.MIRROR));
        darkCirclePaint.setShader(new RadialGradient(getWidth() / 2f, getHeight() / 2f, rect2.width() / 3f, mCircleColor, ColorUtils.blendARGB(mCircleColor, Color.BLACK, mCircleDarkColorRatio), Shader.TileMode.MIRROR));

        zeroLinePaint.setStrokeWidth(mZeroLineWidth);
        zeroLinePaint.setColor(mZeroLineColor);

        anglePiePaint.setColor(mArcPieColor);
        angleArcPaint.setColor(mArcPieColor);

        lgBrush.setShader(new LinearGradient(getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight() / 2.0f, ColorUtils.blendARGB(mArrowColor, Color.BLACK, 0.3f), mArrowColor, Shader.TileMode.MIRROR));

        canvas.drawBitmap(bmp, 0, 0, bmpPaint);
    }

    private void drawCircles(Canvas canvas){
        canvas.drawOval(rect1, lightCirclePaint);
        canvas.drawOval(rect1, borderPaint);

        canvas.drawOval(rect2, darkCirclePaint);
        canvas.drawOval(rect2, borderPaint);

        if (mShowAnglePie)
            if (mZeroLinePosition == 90 || mZeroLinePosition == 270)
                canvas.drawArc(rectArc, mZeroLinePosition + 180, -(mCurrentValue % 360), true, anglePiePaint);
            else
                canvas.drawArc(rectArc, mZeroLinePosition, -(mCurrentValue % 360), true, anglePiePaint);
        else if (mShowAngleArc){
            if (mZeroLinePosition == 90 || mZeroLinePosition == 270){
                arcPath.reset();
                arcPath.arcTo(rectArc, mZeroLinePosition + 180, -(mCurrentValue % 360), true);
            }
            else{
                arcPath.reset();
                arcPath.arcTo(rectArc, mZeroLinePosition, -(mCurrentValue % 360), true);
            }

            canvas.drawPath(arcPath, angleArcPaint);
        }
    }

     private void drawZeroLine(Canvas canvas){
         if (mShowZeroLine){
             switch (mZeroLinePosition)
             {
                 case 0: // East
                     canvas.drawLine(getWidth() - 2f, getHeight() / 2f, getWidth() / 2f, getHeight() / 2f, zeroLinePaint);
                     break;
                 case 90: // North
                     canvas.drawLine(getWidth() / 2f, 1, getWidth() / 2f, getHeight() / 2f, zeroLinePaint);
                     break;
                 case 180: // West
                     canvas.drawLine(2, getWidth() / 2f, getWidth() / 2f, getHeight() / 2f, zeroLinePaint);
                     break;
                 default: // South
                     canvas.drawLine(getWidth() / 2f, getHeight() - 2f, getWidth() / 2f, getHeight() / 2f, zeroLinePaint);
                     break;
             }
         }
     }

    private void drawArrow(Canvas canvas){
        canvas.translate(getWidth() / 2f, getHeight() / 2f);

        if (mZeroLinePosition == 90 || mZeroLinePosition == 270)
            canvas.rotate(-(mCurrentValue % 360) + mZeroLinePosition + 180);
        else
            canvas.rotate(-(mCurrentValue % 360) + mZeroLinePosition);

        canvas.translate(-getWidth() / 2f, -getHeight() / 2f);

        canvas.drawPath(arrowPolygonPath, lgBrush);
    }

    private void drawText(Canvas canvas){
        String mDegreeSign = "", mDirection = "";

        if (mShowDegreeSign)
            mDegreeSign = AI_DEGREE_SIGN;

        float value = mCurrentValue + mZeroLinePosition;
        float value2show = mCurrentValue % 360;
        float modValue = Math.abs(value % 360);

        if (mShowDirection){
            if ((modValue >= 337.5 && modValue <= 360) || (modValue >= 0 && modValue < 22.5))
                mDirection = " E";
			else if (modValue >= 22.5 && modValue < 67.5)
            {
                if (value < 0)
                    mDirection = " SE";
                else
                    mDirection = " NE";
            }
			else if (modValue >= 67.5 && modValue < 112.5)
            {
                if (value < 0)
                    mDirection = " S";
                else
                    mDirection = " N";
            }
			else if (modValue >= 112.5 && modValue < 157.5)
            {
                if (value < 0)
                    mDirection = " SW";
                else
                    mDirection = " NW";
            }
			else if (modValue >= 157.5 && modValue < 202.5)
            {
                mDirection = " W";
            }
			else if (modValue >= 202.5 && modValue < 247.5)
            {
                if (value < 0)
                    mDirection = " NW";
                else
                    mDirection = " SW";
            }
			else if (modValue >= 247.5 && modValue < 292.5)
            {
                if (value < 0)
                    mDirection = " N";
                else
                    mDirection = " S";
            }
            else
            {
                if (value < 0)
                    mDirection = " NE";
                else
                    mDirection = " SE";
            }
        }

        String tempText = String.format(Locale.ENGLISH , "%.1f", value2show) + mDegreeSign + mDirection;
        float tempTextLength = textPaint.measureText(tempText);

        if (mZeroLinePosition == 0 || mZeroLinePosition == 90 || mZeroLinePosition == 180)
            canvas.drawText(tempText, getWidth() / 2f - tempTextLength / 2f, 3 * getHeight() / 4f, textPaint);
        else
            canvas.drawText(tempText, getWidth() / 2f - tempTextLength / 2f, getHeight() / 4f, textPaint);
    }
}
