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

public class RoundGauge extends View {

    public static final int GAUGE_LOWER_CIRCLE_COLOR = Color.BLUE;
    public static final int GAUGE_UPPER_CIRCLE_COLOR = Color.parseColor("#837D7D");
    public static final int GAUGE_RIM_COLOR = Color.LTGRAY;
    public static final int GAUGE_NEEDLE_COLOR = Color.RED;
    public static final int GAUGE_MAJOR_TICKS_COLOR = Color.BLACK;
    public static final int GAUGE_VALUE_TEXT_COLOR = Color.BLUE;
    public static final int GAUGE_DESCRIPTION_TEXT_COLOR = Color.YELLOW;
    public static final int GAUGE_DECIMAL_PLACES = 0;
    public static final float GAUGE_MIN_VALUE = 0f;
    public static final float GAUGE_MAX_VALUE = 100f;
    public static final float GAUGE_CURRENT_VALUE = 0f;
    public static final float GAUGE_LIGHT_COLOR_RATIO = 0.95f;
    public static final float GAUGE_DARK_COLOR_RATIO = 0.45f;

    private int mGaugeLowerCircleColor, mGaugeUpperCircleColor, mGaugeRimColor, mGaugeNeedleColor, mGaugeMajorTicksColor;
    private int mGaugeValueTextColor, mGaugeDescriptionTextColor, mGaugeDecimalPlaces;
    private float mGaugeMinValue, mGaugeMaxValue, mGaugeCurrentValue, mGaugeLightColorRatio, mGaugeDarkColorRatio;
    private String mGaugeDescriptionText;
    Path arrowPolygonPath = new Path();
    PointF[] arrowPolygonPoints;
    private RectF rect1, rect2, rect3;

    private Paint bmpPaint, borderPaint, rimPaint, pieLowerPaint, pieUpperPaint, majorTickPaint, lgBrush;
    private Paint outlinePaint, minMaxPaint, valueTextPaint, descriptionTextPaint;
    private Bitmap bmp;

    public float getGaugeCurrentValue() {return mGaugeCurrentValue;}

    public void setGaugeCurrentValue(float gaugeCurrentValue){
        if (gaugeCurrentValue < mGaugeMinValue)
            mGaugeCurrentValue = mGaugeMinValue;
        else
            mGaugeCurrentValue = Math.min(gaugeCurrentValue, mGaugeMaxValue);

        invalidate();
        requestLayout();
    }

    public RoundGauge(Context context) {
        super(context);
        init(context, null, 0);
    }

    public RoundGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public RoundGauge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, final AttributeSet attrs, final int defStyle){
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundGauge, defStyle, 0);

        mGaugeDescriptionText = typedArray.getString(R.styleable.RoundGauge_gaugeDescriptionText);
        mGaugeLowerCircleColor = typedArray.getColor(R.styleable.RoundGauge_gaugeLowerCircleColor, GAUGE_LOWER_CIRCLE_COLOR);
        mGaugeUpperCircleColor = typedArray.getColor(R.styleable.RoundGauge_gaugeUpperCircleColor, GAUGE_UPPER_CIRCLE_COLOR);
        mGaugeRimColor = typedArray.getColor(R.styleable.RoundGauge_gaugeRimColor, GAUGE_RIM_COLOR);
        mGaugeNeedleColor = typedArray.getColor(R.styleable.RoundGauge_gaugeNeedleColor, GAUGE_NEEDLE_COLOR);
        mGaugeMajorTicksColor = typedArray.getColor(R.styleable.RoundGauge_gaugeMajorTicksColor, GAUGE_MAJOR_TICKS_COLOR);
        mGaugeValueTextColor = typedArray.getColor(R.styleable.RoundGauge_gaugeValueTextColor, GAUGE_VALUE_TEXT_COLOR);
        mGaugeDescriptionTextColor = typedArray.getColor(R.styleable.RoundGauge_gaugeDescriptionTextColor, GAUGE_DESCRIPTION_TEXT_COLOR);
        mGaugeDecimalPlaces = typedArray.getInt(R.styleable.RoundGauge_gaugeDecimalPlaces, GAUGE_DECIMAL_PLACES);
        mGaugeMinValue = typedArray.getFloat(R.styleable.RoundGauge_gaugeMinValue, GAUGE_MIN_VALUE);
        mGaugeMaxValue = typedArray.getFloat(R.styleable.RoundGauge_gaugeMaxValue, GAUGE_MAX_VALUE);
        mGaugeCurrentValue = typedArray.getFloat(R.styleable.RoundGauge_gaugeCurrentValue, GAUGE_CURRENT_VALUE);
        mGaugeLightColorRatio = typedArray.getFloat(R.styleable.RoundGauge_gaugeLightColorRatio, GAUGE_LIGHT_COLOR_RATIO);
        mGaugeDarkColorRatio = typedArray.getFloat(R.styleable.RoundGauge_gaugeDarkColorRatio, GAUGE_DARK_COLOR_RATIO);

        typedArray.recycle();

        bmpPaint = new Paint();
        bmpPaint.setFilterBitmap(true);

        valueTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        minMaxPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        descriptionTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);

        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setStyle(Paint.Style.STROKE);

        majorTickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        majorTickPaint.setStyle(Paint.Style.STROKE);

        pieLowerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pieLowerPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        pieUpperPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pieUpperPaint.setStyle(Paint.Style.FILL);

        rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rimPaint.setStyle(Paint.Style.FILL);

        lgBrush = new Paint(Paint.ANTI_ALIAS_FLAG);
        lgBrush.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int canvasWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int canvasHeight = MeasureSpec.getSize(heightMeasureSpec);

        // maintain the square layout
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
        drawCircles(canvas);
        drawMajorTicks(canvas);
        drawMinMaxText(canvas);

        canvas.save();
        drawArrow(canvas);
        canvas.restore();

        drawValueText(canvas);

        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        reDraw();

        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void reDraw(){
        if (bmp != null){
            bmp.recycle();
        }

        rect1 = new RectF(0, 0,  getWidth(), getHeight());
        rect2 = new RectF(getWidth() / 50f, getHeight() / 50f, getWidth() - getWidth() / 50f, getHeight() - getHeight() / 50f);
        rect3 = new RectF(getWidth() / 2f - getWidth() / 45f, getHeight() / 2f - getHeight() / 45f, getWidth() / 2f + getWidth() / 45f, getHeight() / 2f + getHeight() / 45f);

        bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bmp);

        arrowPolygonPoints = new PointF[] {
                new PointF(getWidth() / 55f, getHeight() / 2f),
                new PointF(getWidth() / 2f - getWidth() / 35f, getHeight() / 2f - getHeight() / 40f),
                new PointF(getWidth() / 2f - getWidth() / 25f, getHeight() / 2f - 2f),
                new PointF(getWidth() / 2f, getHeight() / 2f - 2f),
                new PointF(getWidth() / 2f, getHeight() / 2f + 2f),
                new PointF(getWidth() / 2f - getWidth() / 25f, getHeight() / 2f + 2f),
                new PointF(getWidth() / 2f - getWidth() / 35f, getHeight() / 2f + getHeight() / 40f)
        };

        arrowPolygonPath.reset();
        arrowPolygonPath.moveTo(arrowPolygonPoints[0].x, arrowPolygonPoints[0].y);
        arrowPolygonPath.lineTo(arrowPolygonPoints[1].x, arrowPolygonPoints[1].y);
        arrowPolygonPath.lineTo(arrowPolygonPoints[2].x, arrowPolygonPoints[2].y);
        arrowPolygonPath.lineTo(arrowPolygonPoints[3].x, arrowPolygonPoints[3].y);
        arrowPolygonPath.lineTo(arrowPolygonPoints[4].x, arrowPolygonPoints[4].y);
        arrowPolygonPath.lineTo(arrowPolygonPoints[5].x, arrowPolygonPoints[5].y);
        arrowPolygonPath.lineTo(arrowPolygonPoints[6].x, arrowPolygonPoints[6].y);
        arrowPolygonPath.close();

        borderPaint.setColor(Color.BLACK);
        outlinePaint.setColor(Color.BLACK);
        majorTickPaint.setColor(mGaugeMajorTicksColor);
        valueTextPaint.setColor(mGaugeValueTextColor);
        descriptionTextPaint.setColor(mGaugeDescriptionTextColor);
        minMaxPaint.setColor(mGaugeDescriptionTextColor);

        final float density = getResources().getDisplayMetrics().density;

        if (getWidth() < 50 * density){
            borderPaint.setStrokeWidth(0.15f);
            outlinePaint.setStrokeWidth(0.15f);
            majorTickPaint.setStrokeWidth(0.75f);
            valueTextPaint.setTextSize(8 * density);
            descriptionTextPaint.setTextSize(4 * density);
            minMaxPaint.setTextSize(2 * density);
        }
        else if (getWidth() < 75 * density){
            borderPaint.setStrokeWidth(0.25f);
            outlinePaint.setStrokeWidth(0.25f);
            majorTickPaint.setStrokeWidth(1f);
            valueTextPaint.setTextSize(10 * density);
            descriptionTextPaint.setTextSize(6 * density);
            minMaxPaint.setTextSize(4 * density);
        }
        else if (getWidth() < 100 * density){
            borderPaint.setStrokeWidth(0.4f);
            outlinePaint.setStrokeWidth(0.4f);
            majorTickPaint.setStrokeWidth(1.25f);
            valueTextPaint.setTextSize(14 * density);
            descriptionTextPaint.setTextSize(8 * density);
            minMaxPaint.setTextSize(6 * density);
        }
        else if (getWidth() < 150 * density){
            borderPaint.setStrokeWidth(0.55f);
            outlinePaint.setStrokeWidth(0.55f);
            majorTickPaint.setStrokeWidth(1.5f);
            valueTextPaint.setTextSize(16 * density);
            descriptionTextPaint.setTextSize(11 * density);
            minMaxPaint.setTextSize(8 * density);
        }
        else if (getWidth() < 200 * density){
            borderPaint.setStrokeWidth(0.75f);
            outlinePaint.setStrokeWidth(0.75f);
            majorTickPaint.setStrokeWidth(2.0f);
            valueTextPaint.setTextSize(18 * density);
            descriptionTextPaint.setTextSize(14 * density);
            minMaxPaint.setTextSize(10 * density);
        }
        else if (getWidth() < 250 * density){
            borderPaint.setStrokeWidth(1f);
            outlinePaint.setStrokeWidth(1f);
            majorTickPaint.setStrokeWidth(2.5f);
            valueTextPaint.setTextSize(21 * density);
            descriptionTextPaint.setTextSize(16 * density);
            minMaxPaint.setTextSize(12 * density);
        }
        else if (getWidth() < 300 * density){
            borderPaint.setStrokeWidth(1.5f);
            outlinePaint.setStrokeWidth(1.5f);
            majorTickPaint.setStrokeWidth(3f);
            valueTextPaint.setTextSize(24 * density);
            descriptionTextPaint.setTextSize(18 * density);
            minMaxPaint.setTextSize(14 * density);
        }
        else{
            borderPaint.setStrokeWidth(2f);
            outlinePaint.setStrokeWidth(2f);
            majorTickPaint.setStrokeWidth(4f);
            valueTextPaint.setTextSize(36 * density);
            descriptionTextPaint.setTextSize(24 * density);
            minMaxPaint.setTextSize(18 * density);
        }

        pieLowerPaint.setShader(new RadialGradient(getWidth() / 2f, getHeight() / 2f, rect2.width() / 2f, mGaugeLowerCircleColor, ColorUtils.blendARGB(mGaugeLowerCircleColor, Color.BLACK, mGaugeDarkColorRatio), Shader.TileMode.MIRROR));
        pieUpperPaint.setShader(new RadialGradient(getWidth() / 2f, getHeight() / 2f, rect2.width() / 3f, mGaugeUpperCircleColor, ColorUtils.blendARGB(mGaugeUpperCircleColor, Color.WHITE, mGaugeLightColorRatio), Shader.TileMode.MIRROR));

        rimPaint.setColor(mGaugeRimColor);

        lgBrush.setShader(new LinearGradient(getWidth() / 2f, getHeight() / 2f - 14f, getWidth() / 2f, getHeight() / 2f + 14f, ColorUtils.blendARGB(mGaugeNeedleColor, Color.LTGRAY, 0.5f), mGaugeNeedleColor, Shader.TileMode.MIRROR));

        canvas.drawBitmap(bmp, 0, 0, bmpPaint);
    }

    private void drawCircles(Canvas canvas){
        canvas.drawOval(rect1, rimPaint);
        canvas.drawOval(rect1, borderPaint);

        canvas.drawOval(rect2, pieUpperPaint);
        canvas.drawArc(rect2, 45, 90, true, pieLowerPaint);
        canvas.drawArc(rect2, 45, 90, true, outlinePaint);
        canvas.drawOval(rect2, borderPaint);
    }

    private void drawMajorTicks(Canvas canvas){
        // Major Ticks
        canvas.drawLine(0, getHeight() / 2f, getWidth() / 30f, getHeight() / 2f, majorTickPaint);
        canvas.drawLine(getWidth() / 2f, 0, getWidth() / 2f, getWidth() / 30f, majorTickPaint);
        canvas.drawLine(getWidth() - getWidth() / 30f, getHeight() / 2f, getWidth(), getHeight() / 2f, majorTickPaint);

        float rectOutWidth = (float)(Math.cos(Math.PI / 4f) * getWidth());
        float rectOutHeight = (float)(Math.sin(Math.PI / 4f) * getHeight());
        float rectOutLeft = (getWidth() - rectOutWidth) / 2f;
        float rectOutTop = (getHeight() - rectOutHeight) / 2f;
        float rectOutRight = rectOutLeft + rectOutWidth;

        float rectInLeft = rectOutLeft + (float)(Math.cos(Math.PI / 4f)) * (getWidth() / 30f);
        float rectInTop = rectOutTop + (float)(Math.sin(Math.PI / 4f)) * (getHeight() / 30f);
        float rectInRight = rectOutRight - (float)(Math.cos(Math.PI / 4f)) * (getWidth() / 30f);

        canvas.drawLine(rectOutLeft, rectOutTop, rectInLeft, rectInTop, majorTickPaint);
        canvas.drawLine(rectOutRight, rectOutTop, rectInRight, rectInTop, majorTickPaint);
    }

    private void drawArrow(Canvas canvas){
        canvas.translate(getWidth() / 2f, getHeight() / 2f);

        if (mGaugeCurrentValue > mGaugeMaxValue)
            canvas.rotate((270 / (mGaugeMaxValue - mGaugeMinValue)) * (Math.min(mGaugeMaxValue, mGaugeCurrentValue) - mGaugeMinValue) - 45);
        else
            canvas.rotate((270 / (mGaugeMaxValue - mGaugeMinValue)) * (Math.max(mGaugeMinValue, mGaugeCurrentValue) - mGaugeMinValue) - 45);

        canvas.translate(-getWidth() / 2f, -getHeight() / 2f);

        canvas.drawPath(arrowPolygonPath, outlinePaint);
        canvas.drawPath(arrowPolygonPath, lgBrush);
        canvas.drawOval(rect3, outlinePaint);
        canvas.drawOval(rect3, lgBrush);
    }

    private void drawMinMaxText(Canvas canvas){
        canvas.drawText(String.format(Locale.ENGLISH , "%." + mGaugeDecimalPlaces + "f", mGaugeMinValue), getWidth() / 4f, getHeight() * 54.5f / 64f, minMaxPaint);

        float tempTextLength = minMaxPaint.measureText(String.format(Locale.ENGLISH , "%." + mGaugeDecimalPlaces + "f", mGaugeMaxValue));
        canvas.drawText(String.format(Locale.ENGLISH , "%." + mGaugeDecimalPlaces + "f", mGaugeMaxValue), getWidth() * 3 / 4f - tempTextLength, getHeight() * 54.5f / 64f, minMaxPaint);

        if (mGaugeDescriptionText != null){
            if (!mGaugeDescriptionText.equals("")){
                tempTextLength = descriptionTextPaint.measureText(mGaugeDescriptionText);
                canvas.drawText(mGaugeDescriptionText, getWidth() / 2f - tempTextLength / 2f, getHeight() * 11 / 15f, descriptionTextPaint);
            }
        }
    }

    private void drawValueText(Canvas canvas){
        String tempText;

        if (mGaugeMinValue >= mGaugeMaxValue)
            tempText = "min >= max";
        else {
            if (mGaugeCurrentValue > mGaugeMaxValue)
                tempText = String.valueOf(mGaugeMaxValue);
            else if (mGaugeCurrentValue < mGaugeMinValue)
                tempText = String.valueOf(mGaugeMinValue);
            else
                tempText = String.format(Locale.ENGLISH , "%." + mGaugeDecimalPlaces + "f", Math.max(mGaugeMinValue, mGaugeCurrentValue));
        }

        float tempTextLength = valueTextPaint.measureText(tempText);
        canvas.drawText(tempText, getWidth() / 2f - tempTextLength / 2f, getHeight() * 3 / 10f, valueTextPaint);
    }
}
