package com.e.phonetest;

import android.view.View;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.os.CountDownTimer;
import androidx.core.graphics.ColorUtils;
import android.graphics.RadialGradient;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;

public class LEDLight extends View {

    public static final int LED_COLOR = Color.parseColor("#186502");
    public static final float LED_LIGHT_COLOR_RATIO = 0.9f;
    public static final float LED_DARK_COLOR_RATIO = 0.05f;
    public static final boolean LED_ON = false;
    public static final boolean LED_BLINK = false;
    public static final int LED_BLINK_INTERVAL = 500;

    private boolean mLED_ON, mLED_Blink, switchONOFF;
    private int mLED_Color, mLED_Blink_Interval;
    private float mLED_LightColorRatio, mLED_DarkColorRatio;
    private RectF rect1, rect2;
    private CountDownTimer mTimer = null;

    private Paint bmpPaint, paintBorder, paintLightCircle, paintDarkCircle;
    private Bitmap bmp;

    public boolean isLED_ON() {return mLED_ON;}

    public void setLED_ON(boolean led_on){
        if (mLED_ON != led_on){
            mLED_ON = led_on;
            invalidate();
            requestLayout();
        }
    }

    public boolean isLED_Blink() {return mLED_Blink;}

    public void setLED_Blink(boolean led_blink){
        if (mLED_Blink != led_blink){
            mLED_Blink = led_blink;

            if (mLED_Blink)
                mTimer.start();
            else
                mTimer.cancel();

            invalidate();
            requestLayout();
        }
    }

    public LEDLight(Context context) {
        super(context);
        init(context, null, 0);
    }

    public LEDLight(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public LEDLight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, final AttributeSet attrs, final int defStyle){
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LEDLight, defStyle, 0);

        mLED_Color = typedArray.getColor(R.styleable.LEDLight_led_Color, LED_COLOR);
        mLED_ON = typedArray.getBoolean(R.styleable.LEDLight_led_ON, LED_ON);
        mLED_Blink = typedArray.getBoolean(R.styleable.LEDLight_led_Blink, LED_BLINK);
        mLED_Blink_Interval = typedArray.getInt(R.styleable.LEDLight_led_Blink_Interval, LED_BLINK_INTERVAL);
        mLED_LightColorRatio = typedArray.getFloat(R.styleable.LEDLight_led_LightColorRatio, LED_LIGHT_COLOR_RATIO);
        mLED_DarkColorRatio = typedArray.getFloat(R.styleable.LEDLight_led_DarkColorRatio, LED_DARK_COLOR_RATIO);

        typedArray.recycle();

        bmpPaint = new Paint();
        bmpPaint.setFilterBitmap(true);

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(2f);

        paintLightCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLightCircle.setStyle(Paint.Style.FILL);

        paintDarkCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDarkCircle.setStyle(Paint.Style.FILL);

        mTimer = new CountDownTimer(60000, mLED_Blink_Interval) {

            @Override
            public void onTick(final long millisUntilFinished) {
                invalidate();
            }

            @Override
            public void onFinish() {
                if (isLED_Blink())
                    mTimer.start();
            }
        };
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
        if (mLED_Blink){
            if (switchONOFF){
                drawLightLED(canvas);
                switchONOFF = false;
            } else{
                drawDarkLED(canvas);
                switchONOFF = true;
            }
        } else if (mLED_ON)
            drawLightLED(canvas);
        else
            drawDarkLED(canvas);

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

        rect1 = new RectF(0f, 0f,  getWidth(), getHeight());
        rect2 = new RectF(6f, 6f, getWidth() - 6f, getHeight() - 6f);

        bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bmp);

        canvas.drawBitmap(bmp, 0, 0, bmpPaint);
    }

    private void drawLightLED(Canvas canvas){
        float[] hslVals = new float[3];
        ColorUtils.colorToHSL(mLED_Color, hslVals);
        hslVals[1] = 1f;
        hslVals[2] = mLED_LightColorRatio;

        paintDarkCircle.setShader(new RadialGradient(getWidth() / 2f, getHeight() / 2f, rect2.width() / 3f, ColorUtils.HSLToColor(hslVals), mLED_Color, Shader.TileMode.MIRROR));

        canvas.drawOval(rect1, paintDarkCircle);
        canvas.drawOval(rect1, paintBorder);

        paintLightCircle.setShader(new RadialGradient(getWidth() / 2f, getHeight() / 2f, rect1.width() / 2f, ColorUtils.HSLToColor(hslVals), mLED_Color, Shader.TileMode.MIRROR));

        canvas.drawOval(rect2, paintLightCircle);
        canvas.drawOval(rect2, paintBorder);
    }

    private void drawDarkLED(Canvas canvas){
        float[] hslVals = new float[3];
        ColorUtils.colorToHSL(mLED_Color, hslVals);
        hslVals[1] = 0.75f;
        hslVals[2] = mLED_DarkColorRatio;

        paintDarkCircle.setShader(new RadialGradient(getWidth() / 2f, getHeight() / 2f, rect2.width() / 3f, ColorUtils.HSLToColor(hslVals), mLED_Color, Shader.TileMode.MIRROR));

        canvas.drawOval(rect1, paintDarkCircle);
        canvas.drawOval(rect1, paintBorder);

        paintLightCircle.setShader(new RadialGradient(getWidth() / 2f, getHeight() / 2f, rect1.width() / 2f, mLED_Color, ColorUtils.HSLToColor(hslVals), Shader.TileMode.MIRROR));

        canvas.drawOval(rect2, paintLightCircle);
        canvas.drawOval(rect2, paintBorder);
    }
}
