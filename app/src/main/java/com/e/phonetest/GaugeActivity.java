package com.e.phonetest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class GaugeActivity extends AppCompatActivity implements GaugeTaskCallback {
    public static GaugeTaskCallback gaugeTaskCallback;
    AsyncGaugeTask myGaugeTask = null;

    TextView tvGaugeAddress;
    String txt = "";
    String[] params = new String[4];
    AngleIndicator ai1;
    LEDLight led1;

    boolean blink, onoff, switchTimerState;

    Button btnGaugeDemo;

    ColorStateList textColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Keep the screen turned on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.gauge_activity);

        gaugeTaskCallback = this;

        ai1 = findViewById(R.id.angleIndicator1);
        led1 = findViewById(R.id.LEDLight1);
        btnGaugeDemo = findViewById(R.id.buttonGaugeDemo);
        tvGaugeAddress = findViewById(R.id.tvGaugeAddress);
        textColor = tvGaugeAddress.getTextColors();

        if (MainActivity.abGaugeAddress.equals("")){
            txt = "";
            tvGaugeAddress.setText(txt);
        }
        else {
            String cpu, ipaddress, path, timeout;

            cpu = MainActivity.abCPU;
            ipaddress = MainActivity.abIPAddress;
            path = MainActivity.abPath;
            timeout = MainActivity.abTimeout;

            ipaddress = ipaddress.replace(" ", "");
            path = path.replace(" ", "");
            timeout = timeout.replace(" ", "");

            if (TextUtils.isEmpty(ipaddress) || !TextUtils.isDigitsOnly(timeout)){
                txt = "PLC Parameter Error!";
                tvGaugeAddress.setText(txt);
            } else {
                txt = MainActivity.abGaugeAddress;
                tvGaugeAddress.setText(txt);
                btnGaugeDemo.setVisibility(View.INVISIBLE);

                if (myGaugeTask == null) {
                    myGaugeTask = new AsyncGaugeTask();
                }

                params[0] = "gateway=" + ipaddress + "&path=" + path + "&cpu=" + cpu;
                params[1] = txt.substring(0, txt.indexOf(";"));
                params[2] = txt.substring(txt.indexOf(";") + 2);
                params[3] = timeout;

                myGaugeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mTimer.cancel();

        if (myGaugeTask != null){
            myGaugeTask.cancel(true);
            myGaugeTask = null;
        }
    }

    private final CountDownTimer mTimer = new CountDownTimer(72000, 100) {
        float val1 = 1;

        @Override
        public void onTick(final long millisUntilFinished) {
            if (val1 == 360 || val1 == -360)
                val1 = 0;

            ai1.setCurrentValue(val1);

            if (millisUntilFinished > 36000)
                val1++;
            else
                val1--;
        }

        @Override
        public void onFinish() {
            mTimer.start();
        }
    };

    public void sendMessageGaugeDemo(View v){
        if (!switchTimerState){
            mTimer.start();
            switchTimerState = true;
            v.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
        } else {
            mTimer.cancel();
            switchTimerState = false;
            v.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }
    }

    public void sendMessageONOFF(View v){
        if (!led1.isLED_Blink()){
            if (!onoff){
                led1.setLED_ON(true);
                onoff = true;
                v.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
            } else {
                led1.setLED_ON(false);
                onoff = false;
                v.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
            }
        }
    }

    public void sendMessageBLINK(View v){
        if (!blink){
            led1.setLED_Blink(true);
            blink = true;
            v.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
        } else {
            led1.setLED_Blink(false);
            blink = false;
            v.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }
    }

    @Override
    public void UpdateGaugeValue(String value){
        if (value.startsWith("err") || value.equals("pending")){
            tvGaugeAddress.setTextColor(Color.RED);
            tvGaugeAddress.setText(value);
            ai1.setCurrentValue(0);
        } else {
            tvGaugeAddress.setTextColor(textColor);
            tvGaugeAddress.setText(txt);
            ai1.setCurrentValue(Float.parseFloat(value));
        }
    }
}
