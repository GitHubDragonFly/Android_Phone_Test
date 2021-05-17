package com.e.phonetest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    SetPLCParameters setParameters = MainActivity.setPLCParameters;

    Spinner spinABCPU, spinBooleanDisplay;
    EditText etABIP, etABProgram, etABPath, etABTimeout;
    Button btnOK;
    TextWatcher stcListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        spinABCPU = findViewById(R.id.spinnerABCPU);
        spinABCPU.setOnItemSelectedListener(this);

        spinBooleanDisplay = findViewById(R.id.spinnerBooleanDisplay);

        btnOK = findViewById(R.id.buttonSettingsOK);

        stcListener = new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Enable or disable the OK button if either IP or Timeout are empty
                btnOK.setEnabled(!charSequence.toString().replace(" ", "").equals(""));
                if (btnOK.isEnabled())
                    btnOK.setBackground(ContextCompat.getDrawable(SettingsActivity.this, android.R.drawable.button_onoff_indicator_on));
                else
                    btnOK.setBackground(ContextCompat.getDrawable(SettingsActivity.this, android.R.drawable.button_onoff_indicator_off));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        etABProgram = findViewById(R.id.etABProgram);
        etABPath = findViewById(R.id.etABPath);
        etABIP = findViewById(R.id.etABIPAddress);
        etABIP.addTextChangedListener(stcListener);
        etABTimeout = findViewById(R.id.etABTimeout);
        etABTimeout.addTextChangedListener(stcListener);

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

                etABProgram.setEnabled(spinABCPU.getSelectedItem().toString().equals("controllogix"));
            } else {
                etABProgram.setEnabled(false);
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