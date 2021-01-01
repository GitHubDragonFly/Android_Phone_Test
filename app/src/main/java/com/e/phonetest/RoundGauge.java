package com.e.phonetest;

import android.view.View;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.core.graphics.ColorUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
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

    private int mGaugeLowerCircleColor, mGaugeUpperCircleColor, mGaugeRimColor, mGaugeNeedleColor, mGaugeMajorTicksColor, mGaugeValueTextColor, mGaugeDescriptionTextColor, mGaugeDecimalPlaces;
    private float mGaugeMinValue, mGaugeMaxValue, mGaugeCurrentValue, mGaugeLightColorRatio, mGaugeDarkColorRatio;
    private String mGaugeDescriptionText;
    Path polygonPath = new Path();
    PointF[] points;
    private RectF rect1, rect2, rect3;

    private Paint bmpPaint, paintBorder, paintRim, paintPieLower, paintPieUpper, paintMajorTick, lgBrush, paintMinMax, paintValueText, paintDescriptionText;
    private Bitmap bmp;

    public float getGaugeCurrentValue() {return mGaugeCurrentValue;}

    public void setGaugeCurrentValue(float currentValue){
        if (currentValue < mGaugeMinValue)
            mGaugeCurrentValue = mGaugeMinValue;
        else
            mGaugeCurrentValue = Math.min(currentValue, mGaugeMaxValue);

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

        paintValueText = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        paintMinMax = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        paintDescriptionText = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(2f);

        paintMajorTick = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintMajorTick.setStyle(Paint.Style.STROKE);
        paintMajorTick.setStrokeWidth(5f);

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(2f);

        paintPieLower = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPieLower.setStyle(Paint.Style.FILL_AND_STROKE);

        paintPieUpper = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPieUpper.setStyle(Paint.Style.FILL);

        paintRim = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintRim.setStyle(Paint.Style.FILL);

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
        rect2 = new RectF(12f, 12f, getWidth() - 12f, getHeight() - 12f);
        rect3 = new RectF(getWidth() / 2f - 16f, getHeight() / 2f - 16f, getWidth() / 2f + 16f, getHeight() / 2f + 16f);

        bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bmp);

        points = new PointF[] {
                new PointF(16f, getHeight() / 2f),
                new PointF(getWidth() / 2f - 21f, getHeight() / 2f - 20f),
                new PointF(getWidth() / 2f - 28f, getHeight() / 2f - 2f),
                new PointF(getWidth() / 2f, getHeight() / 2f - 2f),
                new PointF(getWidth() / 2f, getHeight() / 2f + 2f),
                new PointF(getWidth() / 2f - 28f, getHeight() / 2f + 2f),
                new PointF(getWidth() / 2f - 21f, getHeight() / 2f + 20f)
        };

        polygonPath.reset();
        polygonPath.moveTo(points[0].x, points[0].y);
        polygonPath.lineTo(points[1].x, points[1].y);
        polygonPath.lineTo(points[2].x, points[2].y);
        polygonPath.lineTo(points[3].x, points[3].y);
        polygonPath.lineTo(points[4].x, points[4].y);
        polygonPath.lineTo(points[5].x, points[5].y);
        polygonPath.lineTo(points[6].x, points[6].y);
        polygonPath.close();

        paintBorder.setColor(Color.BLACK);
        paintMajorTick.setColor(mGaugeMajorTicksColor);

        final float density = getResources().getDisplayMetrics().density;

        paintValueText.setColor(mGaugeValueTextColor);

        if (getWidth() < 100 * density)
            paintValueText.setTextSize(14 * density);
        else if (getWidth() < 200 * density)
            paintValueText.setTextSize(18 * density);
        else if (getWidth() < 300 * density)
            paintValueText.setTextSize(24 * density);
        else
            paintValueText.setTextSize(36 * density);

        paintDescriptionText.setColor(mGaugeDescriptionTextColor);

        if (getWidth() < 100 * density)
            paintDescriptionText.setTextSize(8 * density);
        else if (getWidth() < 200 * density)
            paintDescriptionText.setTextSize(14 * density);
        else if (getWidth() < 300 * density)
            paintDescriptionText.setTextSize(18 * density);
        else
            paintDescriptionText.setTextSize(24 * density);

        paintMinMax.setColor(mGaugeDescriptionTextColor);

        if (getWidth() < 100 * density)
            paintMinMax.setTextSize(6 * density);
        else if (getWidth() < 200 * density)
            paintMinMax.setTextSize(10 * density);
        else if (getWidth() < 300 * density)
            paintMinMax.setTextSize(14 * density);
        else
            paintMinMax.setTextSize(18 * density);

        paintPieLower.setShader(new RadialGradient(getWidth() / 2f, getHeight() / 2f, rect2.width() / 2f, mGaugeLowerCircleColor, ColorUtils.blendARGB(mGaugeLowerCircleColor, Color.BLACK, mGaugeDarkColorRatio), Shader.TileMode.MIRROR));
        paintPieUpper.setShader(new RadialGradient(getWidth() / 2f, getHeight() / 2f, rect2.width() / 3f, mGaugeUpperCircleColor, ColorUtils.blendARGB(mGaugeUpperCircleColor, Color.WHITE, mGaugeLightColorRatio), Shader.TileMode.MIRROR));

        paintRim.setColor(mGaugeRimColor);

        lgBrush.setShader(new LinearGradient(getWidth() / 2f, getHeight() / 2f - 14f, getWidth() / 2f, getHeight() / 2f + 14f, ColorUtils.blendARGB(mGaugeNeedleColor, Color.LTGRAY, 0.5f), mGaugeNeedleColor, Shader.TileMode.MIRROR));

        canvas.drawBitmap(bmp, 0, 0, bmpPaint);
    }

    private void drawCircles(Canvas canvas){
        canvas.drawOval(rect1, paintRim);
        canvas.drawOval(rect1, paintBorder);

        canvas.drawOval(rect2, paintPieUpper);
        canvas.drawArc(rect2, 45, 90, true, paintPieLower);
        canvas.drawArc(rect2, 45, 90, true, paintBorder);
        canvas.drawOval(rect2, paintBorder);
    }

    private void drawMajorTicks(Canvas canvas){
        // Major Ticks
        canvas.drawLine(0, getHeight() / 2f, getWidth() / 30f, getHeight() / 2f, paintMajorTick);
        canvas.drawLine(getWidth() / 2f, 0, getWidth() / 2f, getWidth() / 30f, paintMajorTick);
        canvas.drawLine(getWidth() - getWidth() / 30f, getHeight() / 2f, getWidth(), getHeight() / 2f, paintMajorTick);

        float rectOutWidth = (float)(Math.cos(Math.PI / 4f) * getWidth());
        float rectOutHeight = (float)(Math.sin(Math.PI / 4f) * getHeight());
        float rectOutLeft = (getWidth() - rectOutWidth) / 2f;
        float rectOutTop = (getHeight() - rectOutHeight) / 2f;
        float rectOutRight = rectOutLeft + rectOutWidth;

        float rectInWidth = (float)(Math.cos(Math.PI / 4f) * (getWidth() - 40f));
        float rectInHeight = (float)(Math.sin(Math.PI / 4f) * (getHeight() - 40f));
        float rectInLeft = (getWidth() - rectInWidth) / 2f;
        float rectInTop = (getHeight() - rectInHeight) / 2f;
        float rectInRight = rectInLeft + rectInWidth;

        canvas.drawLine(rectOutLeft, rectOutTop, rectInLeft, rectInTop, paintMajorTick);
        canvas.drawLine(rectOutRight, rectOutTop, rectInRight, rectInTop, paintMajorTick);
    }

    private void drawArrow(Canvas canvas){
        canvas.translate(getWidth() / 2f, getHeight() / 2f);

        if (mGaugeCurrentValue > mGaugeMaxValue)
            canvas.rotate((270 / (mGaugeMaxValue - mGaugeMinValue)) * (Math.min(mGaugeMaxValue, mGaugeCurrentValue) - mGaugeMinValue) - 45);
        else
            canvas.rotate((270 / (mGaugeMaxValue - mGaugeMinValue)) * (Math.max(mGaugeMinValue, mGaugeCurrentValue) - mGaugeMinValue) - 45);

        canvas.translate(-getWidth() / 2f, -getHeight() / 2f);

        canvas.drawPath(polygonPath, paintBorder);
        canvas.drawPath(polygonPath, lgBrush);
        canvas.drawOval(rect3, paintBorder);
        canvas.drawOval(rect3, lgBrush);
    }

    private void drawMinMaxText(Canvas canvas){
        canvas.drawText(String.format(Locale.ENGLISH , "%." + mGaugeDecimalPlaces + "f", mGaugeMinValue), getWidth() * 1 / 4f, getHeight() * 55 / 64f, paintMinMax);

        float tempTextLength = paintMinMax.measureText(String.format(Locale.ENGLISH , "%." + mGaugeDecimalPlaces + "f", mGaugeMaxValue));
        canvas.drawText(String.format(Locale.ENGLISH , "%." + mGaugeDecimalPlaces + "f", mGaugeMaxValue), getWidth() * 3 / 4f - tempTextLength, getHeight() * 55 / 64f, paintMinMax);

        if (mGaugeDescriptionText != null){
            if (!mGaugeDescriptionText.equals("")){
                tempTextLength = paintDescriptionText.measureText(mGaugeDescriptionText);
                canvas.drawText(mGaugeDescriptionText, getWidth() / 2f - tempTextLength / 2f, getHeight() * 11 / 15f, paintDescriptionText);
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

        float tempTextLength = paintValueText.measureText(tempText);
        canvas.drawText(tempText, getWidth() / 2f - tempTextLength / 2f, getHeight() * 3 / 10f, paintValueText);
    }
}
