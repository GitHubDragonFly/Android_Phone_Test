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

    TextView tvGaugeAddress, tvLEDBlinkAddress;
    String txtGauge = "", txtLED = "";
    String[] params = new String[6];
    AngleIndicator ai1;
    LEDLight led1, led2, led3;
    RoundGauge rg1;

    boolean switchTimerState;
    int addressesProvided = 2;
    float val1 = 1;

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
        led2 = findViewById(R.id.LEDLight2);
        led3 = findViewById(R.id.LEDLight3);
        rg1 = findViewById(R.id.roundGauge1);
        btnGaugeDemo = findViewById(R.id.buttonGaugeDemo);
        tvGaugeAddress = findViewById(R.id.tvGaugeAddress);
        tvLEDBlinkAddress = findViewById(R.id.tvLEDBlinkAddress);
        textColor = tvGaugeAddress.getTextColors();

        if (MainActivity.abGaugeAddress.equals("") || MainActivity.abLEDBlinkAddress.equals("")){
            if (MainActivity.abGaugeAddress.equals("")){
                txtGauge = "";
                tvGaugeAddress.setText(txtGauge);
                addressesProvided --;
            }

            if (MainActivity.abLEDBlinkAddress.equals("")){
                txtLED = "";
                tvLEDBlinkAddress.setText(txtLED);
                addressesProvided --;
            }
        }

        if (addressesProvided > 0) {
            String cpu, ipaddress, path, timeout;

            cpu = MainActivity.abCPU;
            ipaddress = MainActivity.abIPAddress;
            path = MainActivity.abPath;
            timeout = MainActivity.abTimeout;

            ipaddress = ipaddress.replace(" ", "");
            path = path.replace(" ", "");
            timeout = timeout.replace(" ", "");

            if (TextUtils.isEmpty(ipaddress) || !TextUtils.isDigitsOnly(timeout)){
                txtGauge = "PLC Parameter Error!";
                tvGaugeAddress.setText(txtGauge);
                txtLED = "PLC Parameter Error!";
                tvLEDBlinkAddress.setText(txtLED);
            } else {
                txtGauge = MainActivity.abGaugeAddress;
                tvGaugeAddress.setText(txtGauge);
                txtLED = MainActivity.abLEDBlinkAddress;
                tvLEDBlinkAddress.setText(txtLED);

                btnGaugeDemo.setVisibility(View.INVISIBLE);
                led2.setVisibility(View.INVISIBLE);
                led3.setVisibility(View.INVISIBLE);

                if (myGaugeTask == null) {
                    myGaugeTask = new AsyncGaugeTask();
                }

                params[0] = "gateway=" + ipaddress + "&path=" + path + "&cpu=" + cpu;

                if (txtGauge.equals("")){
                    params[1] = "";
                    params[2] = "";
                } else {
                    params[1] = txtGauge.substring(0, txtGauge.indexOf(";"));
                    params[2] = txtGauge.substring(txtGauge.indexOf(";") + 2);
                }

                if (txtLED.equals("")){
                    params[3] = "";
                    params[4] = "";
                } else {
                    params[3] = txtLED.substring(0, txtLED.indexOf(";"));
                    params[4] = txtLED.substring(txtLED.indexOf(";") + 2);
                }

                params[5] = timeout;

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
        @Override
        public void onTick(final long millisUntilFinished) {
            if (val1 == 360 || val1 == -360)
                val1 = 0;

            ai1.setCurrentValue(val1);
            rg1.setGaugeCurrentValue(val1);

            if (millisUntilFinished > 36000){
                val1++;

                if (!led3.isLED_ON()){
                    led3.setLED_ON(true);
                    led2.setLED_ON(false);
                }
            } else {
                val1--;

                if (led3.isLED_ON()){
                    led3.setLED_ON(false);
                    led2.setLED_ON(true);
                }
            }
        }

        @Override
        public void onFinish() {
            mTimer.start();
        }
    };

    public void sendMessageGaugeDemo(View v){
        led1.setLED_Blink(!led1.isLED_Blink());

        if (!switchTimerState){
            mTimer.start();
            switchTimerState = true;
            v.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        } else {
            mTimer.cancel();
            switchTimerState = false;
            val1 = 1;
            ai1.setCurrentValue(0);
            rg1.setGaugeCurrentValue(0);
            v.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));

            if (led2.isLED_ON())
                led2.setLED_ON(false);

            if (led3.isLED_ON())
                led3.setLED_ON(false);
        }
    }

    @Override
    public void UpdateGaugeValue(String value){
        if (value.equals("")){
            String nb = "No Gauge tag provided!";
            tvGaugeAddress.setText(nb);
        } else if (value.startsWith("err") || value.equals("pending")){
            tvGaugeAddress.setTextColor(Color.RED);
            tvGaugeAddress.setText(value);
            ai1.setCurrentValue(0);
            rg1.setGaugeCurrentValue(0);
        } else {
            tvGaugeAddress.setTextColor(textColor);
            tvGaugeAddress.setText(txtGauge);
            ai1.setCurrentValue(Float.parseFloat(value));
            rg1.setGaugeCurrentValue(Float.parseFloat(value));
        }
    }

    @Override
    public void UpdateLEDValue(String value){
        if (value.equals("")){
            String nb = "No LED Blink tag provided!";
            tvLEDBlinkAddress.setText(nb);
        } else if (value.startsWith("err") || value.equals("pending")){
            tvLEDBlinkAddress.setTextColor(Color.RED);
            tvLEDBlinkAddress.setText(value);
            led1.setLED_Blink(false);
        } else {
            tvLEDBlinkAddress.setTextColor(textColor);

            if (value.equals("0") || value.equals("false") || value.equals("False")){
                if (led1.isLED_Blink())
                    led1.setLED_Blink(false);

                tvLEDBlinkAddress.setText(txtLED);
            }
            else if (value.equals("1") || value.equals("true") || value.equals("True")){
                if (!led1.isLED_Blink())
                    led1.setLED_Blink(true);

                tvLEDBlinkAddress.setText(txtLED);
            }
            else {
                if (led1.isLED_Blink())
                    led1.setLED_Blink(false);

                String nb = txtLED + " - Not a boolean equivalent!";
                tvLEDBlinkAddress.setText(nb);
            }
        }
    }
}
