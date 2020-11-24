package com.e.phonetest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,ReadABTaskCallback,WriteABTaskCallback,GetCLGXTagsTaskCallback,SetTags,SetPLCParameters {
    public static ReadABTaskCallback ReadtaskCallback;
    public static WriteABTaskCallback WritetaskCallback;
    public static GetCLGXTagsTaskCallback GetCLGXTagstaskCallback;
    public static SetPLCParameters setPLCParameters;
    public static SetTags setTags;

    AsyncReadABTask myReadABTask = null;
    AsyncTaskGetCLGXTags myTaskGetCLGXTags = null;
    AsyncWriteABTask myWriteABTask = null;

    private static class ABAddressInfo
    {
        EditText etABTag;
        EditText etABTagValue;
        Button btnWriteAB;

        ABAddressInfo(){}
    }

    private List<ABAddressInfo> ABAddressList = new ArrayList<>();

    public static String callerName = "", abTimeout = "10000", boolDisplay = "True : False";
    public static String abCPU = "controllogix", abIPAddress = "192.168.1.21", abPath = "1,3", abProgram = "MainProgram";

    boolean clearingTags;

    EditText etAB1, etAB2, etAB3, etAB4, etAB5, tvAB1, tvAB2, tvAB3, tvAB4, tvAB5;
    Button btnGetCLGXTags, btnSettings, btnWriteCaller, btnWriteAB1, btnWriteAB2, btnWriteAB3, btnWriteAB4, btnWriteAB5;
    ToggleButton tbtnAutoRead;
    Spinner spinCLGXTags;

    ColorStateList textColor;
    TextWatcher tcListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set portrait screen mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        // Hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Keep the screen turned on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        ReadtaskCallback = this;
        WritetaskCallback = this;
        GetCLGXTagstaskCallback = this;
        setPLCParameters = this;
        setTags = this;

        spinCLGXTags = findViewById(R.id.spinnerCLGXTags);
        spinCLGXTags.setOnItemSelectedListener(this);

        btnGetCLGXTags = findViewById(R.id.buttonGetCLGXTags);
        btnSettings = findViewById(R.id.buttonSettings);
        tbtnAutoRead = findViewById(R.id.toggleAutoRead);

        tvAB1 = findViewById(R.id.tvABTagValue1);
        tvAB2 = findViewById(R.id.tvABTagValue2);
        tvAB3 = findViewById(R.id.tvABTagValue3);
        tvAB4 = findViewById(R.id.tvABTagValue4);
        tvAB5 = findViewById(R.id.tvABTagValue5);

        textColor = tvAB1.getTextColors();

        btnWriteAB1 = findViewById(R.id.btnWriteABTag1);
        btnWriteAB2 = findViewById(R.id.btnWriteABTag2);
        btnWriteAB3 = findViewById(R.id.btnWriteABTag3);
        btnWriteAB4 = findViewById(R.id.btnWriteABTag4);
        btnWriteAB5 = findViewById(R.id.btnWriteABTag5);

        tcListener = new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!clearingTags){
                    // Enable or disable the Read/Write display boxes
                    boolean textHasValue = !charSequence.toString().equals("");

                    switch (callerName){
                        case "etABTag1":
                            tvAB1.setEnabled(textHasValue);
                            break;
                        case "etABTag2":
                            tvAB2.setEnabled(textHasValue);
                            break;
                        case "etABTag3":
                            tvAB3.setEnabled(textHasValue);
                            break;
                        case "etABTag4":
                            tvAB4.setEnabled(textHasValue);
                            break;
                        case "etABTag5":
                            tvAB5.setEnabled(textHasValue);
                            break;
                    }

                    callerName = "";
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        etAB1 = findViewById(R.id.etABTag1);
        etAB1.addTextChangedListener(tcListener);
        etAB2 = findViewById(R.id.etABTag2);
        etAB2.addTextChangedListener(tcListener);
        etAB3 = findViewById(R.id.etABTag3);
        etAB3.addTextChangedListener(tcListener);
        etAB4 = findViewById(R.id.etABTag4);
        etAB4.addTextChangedListener(tcListener);
        etAB5 = findViewById(R.id.etABTag5);
        etAB5.addTextChangedListener(tcListener);

        //Hide keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (myReadABTask != null){
            myReadABTask.cancel(true);
            myReadABTask = null;
        }

        if (myWriteABTask != null){
            myWriteABTask.cancel(true);
            myWriteABTask = null;
        }

        if (myTaskGetCLGXTags != null){
            myTaskGetCLGXTags.cancel(true);
            myTaskGetCLGXTags = null;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.spinnerCLGXTags) {
            if (!(spinCLGXTags.getSelectedItem().toString().startsWith("*") || spinCLGXTags.getSelectedItem().toString().startsWith("Failed") ||
                    spinCLGXTags.getSelectedItem().toString().equals("Controller + Program Tags"))) {

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Selected CLGX Tag", spinCLGXTags.getSelectedItem().toString());
                clipboard.setPrimaryClip(clip);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void sendMessageGetCLGXTags(View v){
        String[] params = new String[3];
        String ipaddress, path, program, timeout;

        ipaddress = abIPAddress;
        path = abPath;
        program = abProgram;
        timeout = abTimeout;

        ipaddress = ipaddress.replace(" ", "");
        path = path.replace(" ", "");
        program = program.replace(" ", "");
        timeout = timeout.replace(" ", "");

        if (TextUtils.isEmpty(ipaddress) || TextUtils.isEmpty(path) || TextUtils.isEmpty(program) || !TextUtils.isDigitsOnly(timeout)){
            return;
        }

        params[0] = "gateway=" + ipaddress + "&path=" + path;
        params[1] = program;
        params[2] = timeout;

        if (myTaskGetCLGXTags == null) {
            myTaskGetCLGXTags = new AsyncTaskGetCLGXTags();
        } else {
            return;
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.ab_tags_please_wait));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter.notifyDataSetChanged();
        spinCLGXTags.setAdapter(dataAdapter);

        myTaskGetCLGXTags.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

        btnGetCLGXTags.setEnabled(false);
        btnGetCLGXTags.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
    }

    @SuppressWarnings("unchecked")
    public void sendMessageToggleAB(View v){
        if (tbtnAutoRead.getText().equals(tbtnAutoRead.getTextOn())){
            if (myReadABTask == null) {
                myReadABTask = new AsyncReadABTask();
            } else {
                tbtnAutoRead.setText(tbtnAutoRead.getTextOff());
                return;
            }

            if (TextUtils.isEmpty(etAB1.getText()) && TextUtils.isEmpty(etAB2.getText()) &&
                    TextUtils.isEmpty(etAB3.getText()) && TextUtils.isEmpty(etAB4.getText()) &&
                    TextUtils.isEmpty(etAB5.getText())){

                tbtnAutoRead.setText(tbtnAutoRead.getTextOff());
                return;
            }

            ArrayList<ArrayList<String>> params = new ArrayList<>();
            ArrayList<String> tagItems = new ArrayList<>(), plcAddresses = new ArrayList<>(), callerIDs = new ArrayList<>();

            String cpu, ipaddress, path, timeout;

            cpu = abCPU;
            ipaddress = abIPAddress;
            path = abPath;
            timeout = abTimeout;

            ipaddress = ipaddress.replace(" ", "");
            path = path.replace(" ", "");
            timeout = timeout.replace(" ", "");

            if (TextUtils.isEmpty(ipaddress) || !TextUtils.isDigitsOnly(timeout)){
                return;
            }

            tagItems.add("gateway=" + ipaddress + "&path=" + path + "&cpu=" + cpu);
            tagItems.add(timeout);

            params.add(tagItems);

            if (!TextUtils.isEmpty(etAB1.getText())){
                plcAddresses.add(etAB1.getText().toString());
                callerIDs.add("tvABTagValue1");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB1;
                ABinfo.etABTagValue = tvAB1;
                ABinfo.btnWriteAB = btnWriteAB1;
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB2.getText())){
                plcAddresses.add(etAB2.getText().toString());
                callerIDs.add("tvABTagValue2");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB2;
                ABinfo.etABTagValue = tvAB2;
                ABinfo.btnWriteAB = btnWriteAB2;
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB3.getText())){
                plcAddresses.add(etAB3.getText().toString());
                callerIDs.add("tvABTagValue3");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB3;
                ABinfo.etABTagValue = tvAB3;
                ABinfo.btnWriteAB = btnWriteAB3;
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB4.getText())){
                plcAddresses.add(etAB4.getText().toString());
                callerIDs.add("tvABTagValue4");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB4;
                ABinfo.etABTagValue = tvAB4;
                ABinfo.btnWriteAB = btnWriteAB4;
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB5.getText())){
                plcAddresses.add(etAB5.getText().toString());
                callerIDs.add("tvABTagValue5");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB5;
                ABinfo.etABTagValue = tvAB5;
                ABinfo.btnWriteAB = btnWriteAB5;
                ABAddressList.add(ABinfo);
            }

            for (ABAddressInfo abi: ABAddressList){
                abi.etABTag.setInputType(InputType.TYPE_NULL);
                abi.etABTag.setClickable(false);
                abi.etABTagValue.setInputType(InputType.TYPE_NULL);
                abi.btnWriteAB.setEnabled(false);
                abi.btnWriteAB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
            }

            params.add(plcAddresses);
            params.add(callerIDs);

            myReadABTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

            tbtnAutoRead.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
            btnSettings.setEnabled(false);
            btnSettings.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));

        } else {
            if (myReadABTask != null){
                myReadABTask.cancel(true);
                myReadABTask = null;
            }

            for (ABAddressInfo abi: ABAddressList){
                abi.etABTag.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                abi.etABTag.setClickable(true);
                abi.etABTagValue.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                abi.etABTagValue.setText("");
                abi.btnWriteAB.setEnabled(true);
                abi.btnWriteAB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
            }

            ABAddressList.clear();

            tbtnAutoRead.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
            btnSettings.setEnabled(true);
            btnSettings.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
        }
    }

    public void sendMessageWriteAB(View v) {
        if (myWriteABTask == null) {
            // Clear the label indicating write success/failure
            ((TextView)findViewById(R.id.labelWriteMessage)).setText("");
            // Start a new write task
            myWriteABTask = new AsyncWriteABTask();
        } else {
            // Write task is already running so exit the sub
            return;
        }

        String cpu, ipaddress, path, timeout;

        cpu = abCPU;
        ipaddress = abIPAddress;
        path = abPath;
        timeout = abTimeout;

        ipaddress = ipaddress.replace(" ", "");
        path = path.replace(" ", "");
        timeout = timeout.replace(" ", "");

        if (TextUtils.isEmpty(ipaddress) || !TextUtils.isDigitsOnly(timeout)){
            myWriteABTask = null;
            return;
        }

        String[] params = new String[4];

        if (cpu.equals("controllogix") || cpu.equals("logixpccc") || cpu.equals("njnx")){
            params[0] = "gateway=" + ipaddress + "&path=" + path + "&cpu=" + cpu;
        } else {
            params[0] = "gateway=" + ipaddress + "&cpu=" + cpu;
        }
        params[0] = "gateway=" + ipaddress + "&path=" + path + "&cpu=" + cpu;
        params[1] = timeout;

        switch(v.getId()){
            case R.id.btnWriteABTag1:
                if (TextUtils.isEmpty(etAB1.getText())){
                    myWriteABTask = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvAB1.getText())){
                        myWriteABTask = null;
                        return;
                    } else {
                        params[2] = etAB1.getText().toString();
                        params[3] = tvAB1.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteABTag2:
                if (TextUtils.isEmpty(etAB2.getText())){
                    myWriteABTask = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvAB2.getText())){
                        myWriteABTask = null;
                        return;
                    } else {
                        params[2] = etAB2.getText().toString();
                        params[3] = tvAB2.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteABTag3:
                if (TextUtils.isEmpty(etAB3.getText())){
                    myWriteABTask = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvAB3.getText())){
                        myWriteABTask = null;
                        return;
                    } else {
                        params[2] = etAB3.getText().toString();
                        params[3] = tvAB3.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteABTag4:
                if (TextUtils.isEmpty(etAB4.getText())){
                    myWriteABTask = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvAB4.getText())){
                        myWriteABTask = null;
                        return;
                    } else {
                        params[2] = etAB4.getText().toString();
                        params[3] = tvAB4.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteABTag5:
                if (TextUtils.isEmpty(etAB5.getText())){
                    myWriteABTask = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvAB5.getText())){
                        myWriteABTask = null;
                        return;
                    } else {
                        params[2] = etAB5.getText().toString();
                        params[3] = tvAB5.getText().toString();
                    }
                }
                break;
        }

        btnWriteCaller = (Button)v;
        btnWriteCaller.setEnabled(false);
        btnWriteCaller.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));

        myWriteABTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    public void sendMessageClearABTags(View v) {
        clearingTags = true;

        // Clear the label indicating write success/failure
        ((TextView)findViewById(R.id.labelWriteMessage)).setText("");

        if (!(etAB1.getInputType() == InputType.TYPE_NULL) && !etAB1.getText().toString().equals("")){
            etAB1.setText("");
            tvAB1.setText("");
            tvAB1.setEnabled(false);
        }

        if (!(etAB2.getInputType() == InputType.TYPE_NULL) && !etAB2.getText().toString().equals("")){
            etAB2.setText("");
            tvAB2.setText("");
            tvAB2.setEnabled(false);
        }

        if (!(etAB3.getInputType() == InputType.TYPE_NULL) && !etAB3.getText().toString().equals("")){
            etAB3.setText("");
            tvAB3.setText("");
            tvAB3.setEnabled(false);
        }

        if (!(etAB4.getInputType() == InputType.TYPE_NULL) && !etAB4.getText().toString().equals("")){
            etAB4.setText("");
            tvAB4.setText("");
            tvAB4.setEnabled(false);
        }

        if (!(etAB5.getInputType() == InputType.TYPE_NULL) && !etAB5.getText().toString().equals("")){
            etAB5.setText("");
            tvAB5.setText("");
            tvAB5.setEnabled(false);
        }

        clearingTags = false;
    }

    public void sendMessagePopUpAddressAB(View v)
    {
        callerName = getResources().getResourceEntryName(v.getId());

        Intent intent = new Intent(MainActivity.this, PopUpAddressAB.class);
        startActivity(intent);
    }

    public void sendMessageSettings(View v)
    {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);

        ((Button)v).setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
    }

    @Override
    public void UpdateABUI(String callerID, String value) {
        TextView tv = findViewById(getResources().getIdentifier(callerID, "id", getPackageName()));

        if (value.startsWith("err") || value.equals("pending")){
            tv.setTextColor(Color.RED);
        } else {
            tv.setTextColor(textColor);
        }
        tv.setText(value);
    }

    @Override
    public void UpdateGetCLGXTagsUI(List<String> values) {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter.notifyDataSetChanged();
        spinCLGXTags.setAdapter(dataAdapter);

        if (myTaskGetCLGXTags != null){
            myTaskGetCLGXTags.cancel(true);
            myTaskGetCLGXTags = null;
        }

        btnGetCLGXTags.setEnabled(true);
        btnGetCLGXTags.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
    }

    @Override
    public void WriteUpdateUI(String value) {
        if (myWriteABTask != null){
            myWriteABTask.cancel(true);
            myWriteABTask = null;
        }

        ((TextView)findViewById(R.id.labelWriteMessage)).setText(value.substring(3));

        btnWriteCaller.setEnabled(true);
        btnWriteCaller.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
    }

    @Override
    public void UpdatePLCParameters(String[] values) {
        btnSettings.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));

        if (values != null){
            abCPU = values[0];
            abProgram = values[1];
            abIPAddress = values[2];
            abPath = values[3];
            abTimeout = values[4];
            boolDisplay = values[5];

            if (abCPU.equals("controllogix")){
                btnGetCLGXTags.setEnabled(true);
                btnGetCLGXTags.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
                spinCLGXTags.setEnabled(true);
            }
            else{
                btnGetCLGXTags.setEnabled(false);
                btnGetCLGXTags.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
                spinCLGXTags.setEnabled(false);
            }
        }
    }

    @Override
    public void UpdateTags(String callerId, String value) {
        ((EditText)findViewById(getResources().getIdentifier(callerId, "id", getPackageName()))).setText(value);
        callerName = "";
    }
}