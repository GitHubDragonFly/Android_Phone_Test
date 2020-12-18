package com.e.phonetest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class PopUpAddressAB extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public PopUpAddressAB() { super(); }

    SetTags setTags = MainActivity.setTags;

    String cpu, callerName;
    EditText etABTag;
    Spinner spinABDataType, spinABBit, spinCustomStringLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_address_ab);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        callerName = MainActivity.callerName;
        cpu = MainActivity.abCPU;

        spinABDataType = findViewById(R.id.spinnerABDataType);
        spinABDataType.setOnItemSelectedListener(this);
        spinABDataType.setEnabled(false);

        spinABBit = findViewById(R.id.spinnerABBit);
        spinABBit.setEnabled(false);

        spinCustomStringLength = findViewById(R.id.spinnerCustomStringLength);
        spinCustomStringLength.setOnItemSelectedListener(this);
        spinCustomStringLength.setEnabled(false);

        etABTag = findViewById(R.id.etABtag);
        etABTag.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence chars, int start, int before, int count) {
                if(chars.toString().equals("")){
                    spinABDataType.setEnabled(false);
                    spinABBit.setEnabled(false);
                } else{
                    spinABDataType.setEnabled(true);

                    if (spinABDataType.getSelectedItem().toString().equals("bool") || spinABDataType.getSelectedItem().toString().equals("bool array") ||
                            spinABDataType.getSelectedItem().toString().equals("timer") || spinABDataType.getSelectedItem().toString().equals("counter") ||
                            spinABDataType.getSelectedItem().toString().equals("control") || callerName.equals("etABGaugeTag")){

                        spinABBit.setEnabled(false);
                    } else {
                        if(chars.toString().contains("/") || (chars.toString().contains(".") && !chars.toString().contains(":")) ||
                                (chars.toString().contains(".") && chars.toString().lastIndexOf(".") > chars.toString().indexOf('.')))
                            spinABBit.setEnabled(false);
                        else
                            spinABBit.setEnabled(true);
                    }
                }
            }

            public void beforeTextChanged(CharSequence chars, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        String[] stringArray;
        ArrayAdapter<String> dataAdapter;

        switch(cpu){
            case "controllogix":
                if (callerName.equals("etABGaugeTag"))
                    stringArray = getResources().getStringArray(R.array.gauge_data_type);
                else
                    stringArray = getResources().getStringArray(R.array.ab_data_type);

                dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringArray);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dataAdapter.notifyDataSetChanged();
                spinABDataType.setAdapter(dataAdapter);
                break;
            case "micrologix":
            case "logixpccc":
            case "slc500":
            case "plc5":
            case "micro800":
            case "njnx":
                if (callerName.equals("etABGaugeTag"))
                    stringArray = getResources().getStringArray(R.array.gauge_data_type);
                else
                    stringArray = getResources().getStringArray(R.array.ab_mlgx_data_type);

                dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringArray);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dataAdapter.notifyDataSetChanged();
                spinABDataType.setAdapter(dataAdapter);
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        // Initialize array with 8bit strings
        String[] stringArray = getResources().getStringArray(R.array.bits_8bit);
        ArrayAdapter<String> dataAdapter;

        switch(parent.getId()){
            case R.id.spinnerABDataType:
                // Disable the custom string length spinner
                spinCustomStringLength.setEnabled(false);

                switch (spinABDataType.getSelectedItem().toString()) {
                    case "int8":
                    case "uint8":
                        stringArray = getResources().getStringArray(R.array.bits_8bit);
                        if (spinABDataType.isEnabled())
                            spinABBit.setEnabled(true);
                        break;
                    case "int16":
                    case "uint16":
                        stringArray = getResources().getStringArray(R.array.bits_16bit);
                        spinABBit.setEnabled(true);
                        break;
                    case "int32":
                    case "uint32":
                    case "float32":
                        stringArray = getResources().getStringArray(R.array.bits_32bit);
                        spinABBit.setEnabled(true);
                        break;
                    case "int64":
                    case "uint64":
                    case "float64":
                        stringArray = getResources().getStringArray(R.array.bits_64bit);
                        spinABBit.setEnabled(true);
                        break;
                    case "int128":
                    case "uint128":
                        stringArray = getResources().getStringArray(R.array.bits_128bit);
                        spinABBit.setEnabled(true);
                        break;
                    case "custom string":
                        spinCustomStringLength.setEnabled(true);
                        stringArray = new String[] {"None", "1"};
                        spinABBit.setEnabled(true);
                        break;
                    case "string":
                        int strLength;

                        if (cpu.equals("micro800")){
                            strLength = 255;
                        } else if (cpu.equals("controllogix")){
                            strLength = 84;
                        } else {
                            strLength = 82;
                        }

                        stringArray = new String[strLength];
                        stringArray[0] = "None";

                        for (int i = 1; i < strLength; i++){
                            stringArray[i] = String.valueOf(i);
                        }

                        spinABBit.setEnabled(true);
                        break;
                    default:
                        spinABBit.setEnabled(false);
                        break;
                }

                if (callerName.equals("etABGaugeTag"))
                    spinABBit.setEnabled(false);

                dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringArray);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dataAdapter.notifyDataSetChanged();
                spinABBit.setAdapter(dataAdapter);
                break;
            case R.id.spinnerCustomStringLength:
                int index = Integer.parseInt(spinCustomStringLength.getSelectedItem().toString());
                stringArray = new String[index + 1];
                stringArray[0] = "None";

                for (int i = 1; i < index + 1; i++){
                    stringArray[i] = String.valueOf(i);
                }

                dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringArray);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dataAdapter.notifyDataSetChanged();
                spinABBit.setAdapter(dataAdapter);
                break;
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
    }

    public void sendMessageReturnTag(View v)
    {
        v.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));

        // Remove all the spaces from the tag
        String tag = (etABTag.getText().toString()).replace(" ", "");

        if (!TextUtils.isEmpty(tag)){
            if (spinCustomStringLength.isEnabled()){
                if (spinABBit.getSelectedItem().toString().equals("None"))
                    setTags.UpdateTags(callerName, (tag + "; " + spinABDataType.getSelectedItem() + "; " + spinCustomStringLength.getSelectedItem()));
                else
                    setTags.UpdateTags(callerName, (tag + "/" + spinABBit.getSelectedItem().toString() + "; " + spinABDataType.getSelectedItem() + "; " + spinCustomStringLength.getSelectedItem()));
            } else if (spinABBit.isEnabled() && !spinABBit.getSelectedItem().toString().equals("None")){
                if (cpu.equals("controllogix"))
                    if (spinABDataType.getSelectedItem().toString().equals("string"))
                        setTags.UpdateTags(callerName, (tag + "/" + spinABBit.getSelectedItem().toString() + "; " + spinABDataType.getSelectedItem()));
                    else
                        setTags.UpdateTags(callerName, (tag + "." + spinABBit.getSelectedItem().toString() + "; " + spinABDataType.getSelectedItem()));
                else
                    setTags.UpdateTags(callerName, (tag + "/" + spinABBit.getSelectedItem().toString() + "; " + spinABDataType.getSelectedItem()));
            } else {
                setTags.UpdateTags(callerName, (tag + "; " + spinABDataType.getSelectedItem()));
            }
        } else {
            setTags.UpdateTags(callerName, "");
        }

        this.finish();
    }
}
