package com.e.phonetest;

import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;

public class SettingsActivity extends AppCompatActivity {
    SetPLCParameters setParameters = MainActivity.setPLCParameters;

    Spinner spinABCPU, spinBooleanDisplay;
    EditText etABIP, etABProgram, etABPath, etABTimeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set portrait screen mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        setContentView(R.layout.settings_activity);

        spinABCPU = findViewById(R.id.spinnerABCPU);
        spinBooleanDisplay = findViewById(R.id.spinnerBooleanDisplay);

        etABIP = findViewById(R.id.etABIPAddress);
        etABProgram = findViewById(R.id.etABProgram);
        etABPath = findViewById(R.id.etABPath);
        etABTimeout = findViewById(R.id.etABTimeout);

        //Set current values to initial screen

        for (int i = 0; i < spinABCPU.getCount(); i++){
            if (spinABCPU.getItemAtPosition(i).toString().equalsIgnoreCase(MainActivity.abCPU)){
                spinABCPU.setSelection(i);
                break;
            }
        }

        etABIP.setText(MainActivity.abIPAddress);
        etABProgram.setText(MainActivity.abProgram);
        etABPath.setText(MainActivity.abPath);
        etABTimeout.setText(MainActivity.abTimeout);

        for (int i = 0; i < spinBooleanDisplay.getCount(); i++){
            if (spinBooleanDisplay.getItemAtPosition(i).toString().equalsIgnoreCase(MainActivity.boolDisplay)){
                spinBooleanDisplay.setSelection(i);
                break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        View v = getCurrentFocus();

        if (v != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void sendMessageReturnPLCParameters(View v)
    {
        String[] values = new String[7];

        values[0] = spinABCPU.getSelectedItem().toString();
        values[1] = etABProgram.getText().toString();
        values[2] = etABIP.getText().toString();
        values[3] = etABPath.getText().toString();
        values[4] = etABTimeout.getText().toString();
        values[5] = spinBooleanDisplay.getSelectedItem().toString();

        setParameters.UpdatePLCParameters(values);

        this.finish();
    }
}