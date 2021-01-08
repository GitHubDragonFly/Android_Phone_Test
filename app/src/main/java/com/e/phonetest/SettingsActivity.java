package com.e.phonetest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    SetPLCParameters setParameters = MainActivity.setPLCParameters;

    Spinner spinABCPU, spinBooleanDisplay;
    EditText etABIP, etABProgram, etABPath, etABTimeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        spinABCPU = findViewById(R.id.spinnerABCPU);
        spinABCPU.setOnItemSelectedListener(this);

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
    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        if (parent.getId() == R.id.spinnerABCPU) {
            if (spinABCPU.getSelectedItem().toString().equals("controllogix") ||
                    spinABCPU.getSelectedItem().toString().equals("logixpccc") ||
                    spinABCPU.getSelectedItem().toString().equals("njnx")) {

                String ipAddress = "192.168.1.21";
                etABIP.setText(ipAddress);
                etABPath.setText("1,3");
                etABProgram.setEnabled(spinABCPU.getSelectedItem().toString().equals("controllogix"));
            } else {
                etABProgram.setEnabled(false);
                String ipAddress = "192.168.1.10";
                etABIP.setText(ipAddress);
                etABPath.setText("");
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    protected void onPause() {
        super.onPause();

        View v = getCurrentFocus();

        if (v != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        setParameters.UpdatePLCParameters(null);
    }

    public void sendMessageReturnPLCParameters(View v)
    {
        v.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));

        String[] values = new String[6];

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