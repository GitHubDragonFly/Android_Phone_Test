package com.e.phonetest;

import android.view.View;
import android.content.Context;
import androidx.core.graphics.ColorUtils;
import java.util.Locale;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;

public class AngleIndicator extends View {

    public static final int CIRCLE_COLOR = Color.BLUE;
    public static final int ARROW_COLOR = Color.RED;
    public static final int ANGLE_ARC_PIE_COLOR = Color.GREEN;
    public static final int ZERO_LINE_COLOR = Color.YELLOW;
    public static final int ZERO_POSITION = 0;
    public static final float ZERO_LINE_WIDTH = 4f;
    public static final float CURRENT_VALUE = 0f;
    public static final float CIRCLE_LIGHT_COLOR_RATIO = 0.95f;
    public static final float CIRCLE_DARK_COLOR_RATIO = 0.15f;
    public static final String DEGREE_SIGN = "°";
    public static final boolean SHOW_ANGLE_ARC = false;
    public static final boolean SHOW_ANGLE_PIE = false;
    public static final boolean SHOW_DEGREE_SIGN = false;
    public static final boolean SHOW_DIRECTION = false;
    public static final boolean SHOW_ZERO_LINE = false;

    private boolean mShowAngleArc, mShowAnglePie, mShowDegreeSign, mShowDirection, mShowZeroLine;
    private int mAngleArcPieColor, mArrowColor, mCircleColor, mZeroLineColor, mZeroPosition;
    private float mCurrentValue, mZeroLineWidth, mCircleLightColorRatio, mCircleDarkColorRatio;
    Path polygonPath = new Path(), arcPath = new Path();
    PointF[] points;
    private RectF rect1, rectArc, rect2;

    private Paint bmpPaint, paintBorder, paintLightCircle, paintDarkCircle, paintZeroLine, paintAngleArc, paintAnglePie, lgBrush, textPaint;
    private Bitmap bmp;

    public float CurrentValue() {return mCurrentValue;}

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

        mCircleColor = typedArray.getColor(R.styleable.AngleIndicator_circleColor, CIRCLE_COLOR);
        mAngleArcPieColor = typedArray.getColor(R.styleable.AngleIndicator_angleArcPieColor, ANGLE_ARC_PIE_COLOR);
        mArrowColor = typedArray.getColor(R.styleable.AngleIndicator_arrowColor, ARROW_COLOR);
        mZeroLineColor = typedArray.getColor(R.styleable.AngleIndicator_zeroLineColor, ZERO_LINE_COLOR);
        mShowAngleArc = typedArray.getBoolean(R.styleable.AngleIndicator_showAngleArc, SHOW_ANGLE_ARC);
        mShowAnglePie = typedArray.getBoolean(R.styleable.AngleIndicator_showAnglePie, SHOW_ANGLE_PIE);
        mShowDegreeSign = typedArray.getBoolean(R.styleable.AngleIndicator_showDegreeSign, SHOW_DEGREE_SIGN);
        mShowDirection = typedArray.getBoolean(R.styleable.AngleIndicator_showDirection, SHOW_DIRECTION);
        mShowZeroLine = typedArray.getBoolean(R.styleable.AngleIndicator_showZeroLine, SHOW_ZERO_LINE);
        mZeroLineWidth = typedArray.getFloat(R.styleable.AngleIndicator_zeroLineWidth, ZERO_LINE_WIDTH);
        mCurrentValue = typedArray.getFloat(R.styleable.AngleIndicator_currentValue, CURRENT_VALUE);
        mCircleLightColorRatio = typedArray.getFloat(R.styleable.AngleIndicator_circleLightColorRatio, CIRCLE_LIGHT_COLOR_RATIO);
        mCircleDarkColorRatio = typedArray.getFloat(R.styleable.AngleIndicator_circleDarkColorRatio, CIRCLE_DARK_COLOR_RATIO);
        mZeroPosition = typedArray.getInt(R.styleable.AngleIndicator_zeroLinePosition, ZERO_POSITION);

        typedArray.recycle();

        bmpPaint = new Paint();
        bmpPaint.setFilterBitmap(true);

        textPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(2f);

        paintLightCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLightCircle.setStyle(Paint.Style.FILL);

        paintDarkCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDarkCircle.setStyle(Paint.Style.FILL);

        paintZeroLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintZeroLine.setStyle(Paint.Style.STROKE);

        paintAnglePie = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintAnglePie.setStyle(Paint.Style.FILL_AND_STROKE);
        paintAnglePie.setStrokeWidth(12f);

        paintAngleArc = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintAngleArc.setStyle(Paint.Style.STROKE);
        paintAngleArc.setStrokeWidth(12f);

        lgBrush = new Paint(Paint.ANTI_ALIAS_FLAG);
        lgBrush.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int canvasWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int canvasHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(canvasWidth, canvasHeight);
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

        rect1 = new RectF(0f, 0f,  getWidth(), getHeight());
        rectArc = new RectF(6f, 6f,  getWidth() - 6f, getHeight() - 6f);
        rect2 = new RectF(12f, 12f, getWidth() - 12f, getHeight() - 12f);
        RectF rect3 = new RectF(getWidth() / 2f - getWidth() * 0.3f / 7f, getHeight() * 3.1f / 7f, getWidth() / 2f + getWidth() * 0.4f / 7f, getHeight() * 3.9f / 7f);

        bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bmp);

        points = new PointF[] {
                new PointF(getWidth() / 2f, getHeight() * 3.1f / 7f),
                new PointF(getWidth() * 5.25f / 7f, getHeight() * 3.1f / 7f),
                new PointF(getWidth() * 5.25f / 7f, getHeight() * 6f / 16f),
                new PointF(getWidth() - 4f, getHeight() * 3.5f / 7f),
                new PointF(getWidth() * 5.25f / 7f, getHeight() * 10f / 16f),
                new PointF(getWidth() * 5.25f / 7f, getHeight() * 3.9f / 7f),
                new PointF(getWidth() / 2f, getHeight() * 3.9f / 7f)
        };

        polygonPath.reset();
        polygonPath.moveTo(points[0].x, points[0].y);
        polygonPath.lineTo(points[1].x, points[1].y);
        polygonPath.lineTo(points[2].x, points[2].y);
        polygonPath.lineTo(points[3].x, points[3].y);
        polygonPath.lineTo(points[4].x, points[4].y);
        polygonPath.lineTo(points[5].x, points[5].y);
        polygonPath.lineTo(points[6].x, points[6].y);
        polygonPath.addArc(rect3, 90, 180);
        polygonPath.close();

        final float density = getResources().getDisplayMetrics().density;

        if (getWidth() < 100 * density)
            textPaint.setTextSize(14 * density);
        else if (getWidth() < 200 * density)
            textPaint.setTextSize(18 * density);
        else if (getWidth() < 300 * density)
            textPaint.setTextSize(24 * density);
        else
            textPaint.setTextSize(36 * density);

        textPaint.setColor(mZeroLineColor);

        paintLightCircle.setShader(new RadialGradient(getWidth() / 2f, getHeight() / 2f, rect1.width() / 2f, mCircleColor, ColorUtils.blendARGB(mCircleColor, Color.WHITE, mCircleLightColorRatio), Shader.TileMode.MIRROR));
        paintDarkCircle.setShader(new RadialGradient(getWidth() / 2f, getHeight() / 2f, rect2.width() / 3f, mCircleColor, ColorUtils.blendARGB(mCircleColor, Color.BLACK, mCircleDarkColorRatio), Shader.TileMode.MIRROR));

        paintZeroLine.setStrokeWidth(mZeroLineWidth);
        paintZeroLine.setColor(mZeroLineColor);

        paintAnglePie.setColor(mAngleArcPieColor);
        paintAngleArc.setColor(mAngleArcPieColor);

        lgBrush.setShader(new LinearGradient(getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight() / 2.0f, ColorUtils.blendARGB(mArrowColor, Color.BLACK, 0.3f), mArrowColor, Shader.TileMode.MIRROR));

        canvas.drawBitmap(bmp, 0, 0, bmpPaint);
    }

    private void drawCircles(Canvas canvas){
        canvas.drawOval(rect1, paintLightCircle);
        canvas.drawOval(rect1, paintBorder);

        canvas.drawOval(rect2, paintDarkCircle);
        canvas.drawOval(rect2, paintBorder);

        if (mShowAnglePie)
            if (mZeroPosition == 90 || mZeroPosition == 270)
                canvas.drawArc(rectArc, mZeroPosition + 180, - mCurrentValue, true, paintAnglePie);
            else
                canvas.drawArc(rectArc, mZeroPosition, - mCurrentValue, true, paintAnglePie);
        else if (mShowAngleArc){
            if (mZeroPosition == 90 || mZeroPosition == 270){
                arcPath.reset();
                arcPath.arcTo(rectArc, mZeroPosition + 180, - mCurrentValue, true);
            }
            else{
                arcPath.reset();
                arcPath.arcTo(rectArc, mZeroPosition, - mCurrentValue, true);
            }

            canvas.drawPath(arcPath, paintAngleArc);
        }
    }

     private void drawZeroLine(Canvas canvas){
         if (mShowZeroLine){
             switch (mZeroPosition)
             {
                 case 0: // East
                     canvas.drawLine(getWidth() - 2f, getHeight() / 2f, getWidth() / 2f, getHeight() / 2f, paintZeroLine);
                     break;
                 case 90: // North
                     canvas.drawLine(getWidth() / 2f, 1, getWidth() / 2f, getHeight() / 2f, paintZeroLine);
                     break;
                 case 180: // West
                     canvas.drawLine(2, getWidth() / 2f, getWidth() / 2f, getHeight() / 2f, paintZeroLine);
                     break;
                 default: // South
                     canvas.drawLine(getWidth() / 2f, getHeight() - 2f, getWidth() / 2f, getHeight() / 2f, paintZeroLine);
                     break;
             }
         }
     }

    private void drawArrow(Canvas canvas){
        canvas.translate(getWidth() / 2f, getHeight() / 2f);
        if (mZeroPosition == 90 || mZeroPosition == 270)
            canvas.rotate(- mCurrentValue + mZeroPosition + 180);
        else
            canvas.rotate(- mCurrentValue + mZeroPosition);
        canvas.translate(-getWidth() / 2f, -getHeight() / 2f);

        canvas.drawPath(polygonPath, lgBrush);
    }

    private void drawText(Canvas canvas){
        String mDegreeSign = "", mDirection = "";

        if (mShowDegreeSign)
            mDegreeSign = DEGREE_SIGN;

        float value = mCurrentValue + mZeroPosition;
        float mod = value % 360;
        float modValue = Math.abs(mod);

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

        String tempText = String.format(Locale.ENGLISH , "%.1f", mod) + mDegreeSign + mDirection;
        float tempTextLength = textPaint.measureText(tempText);

        if (mZeroPosition == 0 || mZeroPosition == 90 || mZeroPosition == 180)
            canvas.drawText(tempText, getWidth() / 2f - tempTextLength / 2f, 3 * getHeight() / 4f, textPaint);
        else
            canvas.drawText(tempText, getWidth() / 2f - tempTextLength / 2f, getHeight() / 4f, textPaint);
    }
}
