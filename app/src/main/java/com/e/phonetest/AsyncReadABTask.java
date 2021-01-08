package com.e.phonetest;

import android.text.TextUtils;
import android.os.AsyncTask;
import org.libplctag.Tag;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class AsyncReadABTask extends AsyncTask<ArrayList<ArrayList<String>>, Void, String> {

    public String value = "", callerID = "";
    private String name = "";
    private int elem_size, elem_count, customStringLength;
    HashMap<String, Integer> dict = new HashMap<>();
    private final Tag ABMaster = new Tag();

    ReadABTaskCallback ABtaskCallback = MainActivity.ReadtaskCallback;

    @Override
    @SafeVarargs
    protected final String doInBackground(ArrayList<ArrayList<String>>... params) {
        String gateway_path_cpu = params[0].get(0).get(0);
        String cpu = gateway_path_cpu.substring(gateway_path_cpu.lastIndexOf('=') + 1);

        int timeout = Integer.parseInt(params[0].get(0).get(1));
        int number_of_addresses = params[0].get(1).size();

        //String[] values = new String[number_of_addresses];
        String[] tags = new String[number_of_addresses];
        String[] pidName = new String[number_of_addresses];
        String[] dType = new String[number_of_addresses];
        int[] bitIndex = new int[number_of_addresses];
        //Arrays.fill(values, "");
        Arrays.fill(tags, "");
        Arrays.fill(pidName, "");
        Arrays.fill(dType, "");
        Arrays.fill(bitIndex, -1);

        while (!isCancelled()){
            for (int i = 0; i < number_of_addresses; i++) {
                String tempValue = "", pidTagString;
                int tag_id;

                if (dict.size() != number_of_addresses){
                    String fullString = params[0].get(1).get(i);
                    fullString = fullString.replace(" ", "");

                    name = fullString.substring(0, fullString.indexOf(';'));

                    if (name.contains("/")){
                        bitIndex[i] = Integer.parseInt(name.substring(name.indexOf('/') + 1));
                        name = name.substring(0, name.indexOf('/'));
                    } else if (name.contains("[") && name.contains(",") && name.contains("]")){
                        if (name.indexOf(',') < name.lastIndexOf(',')){
                            // Array [x, y, z]
                            name = name.substring(0, name.indexOf(',')) + "][" + name.substring(name.indexOf(',') + 1, name.lastIndexOf(',')) + "][" + name.substring(name.lastIndexOf(',') + 1);
                        } else {
                            // Array [x, y]
                            name = name.substring(0, name.indexOf(',')) + "][" + name.substring(name.indexOf(',') + 1);
                        }
                    }

                    String dataType;
                    if (fullString.indexOf(';') < fullString.lastIndexOf(';'))
                        dataType = fullString.substring(fullString.indexOf(';') + 1, fullString.lastIndexOf(';'));
                    else
                        dataType = fullString.substring(fullString.indexOf(';') + 1);

                    if (dataType.equals("boolarray"))
                        dataType = "bool array";

                    if (dataType.equals("customstring")){
                        dataType = "custom string";
                        customStringLength = Integer.parseInt(fullString.substring(fullString.lastIndexOf(';') + 1));
                    }

                    switch (dataType) {
                        case "bool":
                            elem_size = 1;
                            elem_count = 1;
                            break;
                        case "int8":
                        case "uint8":
                            elem_size = 1;
                            elem_count = 1;
                            if ((name.contains(".") && !name.contains(":")) || (name.contains(".") && name.lastIndexOf('.') > name.indexOf('.'))){
                                if (TextUtils.isDigitsOnly(name.substring(name.lastIndexOf('.') + 1)))
                                    bitIndex[i] = Integer.parseInt(name.substring(name.lastIndexOf('.') + 1));
                            }
                            break;
                        case "int16":
                        case "uint16":
                            elem_size = 2;
                            elem_count = 1;
                            if ((name.contains(".") && !name.contains(":")) || (name.contains(".") && name.lastIndexOf('.') > name.indexOf('.'))){
                                if (TextUtils.isDigitsOnly(name.substring(name.lastIndexOf('.') + 1)))
                                    bitIndex[i] = Integer.parseInt(name.substring(name.lastIndexOf('.') + 1));
                            }
                            break;
                        case "bool array":
                            elem_size = 2;
                            elem_count = 2;
                            break;
                        case "int32":
                        case "uint32":
                        case "float32":
                            elem_size = 2;
                            elem_count = 2;
                            if ((name.contains(".") && !name.contains(":")) || (name.contains(".") && name.lastIndexOf('.') > name.indexOf('.'))){
                                if (TextUtils.isDigitsOnly(name.substring(name.lastIndexOf('.') + 1)))
                                    bitIndex[i] = Integer.parseInt(name.substring(name.lastIndexOf('.') + 1));
                            }
                            break;
                        case "int64":
                        case "uint64":
                        case "float64":
                            elem_size = 2;
                            elem_count = 4;
                            if ((name.contains(".") && !name.contains(":")) || (name.contains(".") && name.lastIndexOf('.') > name.indexOf('.'))){
                                if (TextUtils.isDigitsOnly(name.substring(name.lastIndexOf('.') + 1)))
                                    bitIndex[i] = Integer.parseInt(name.substring(name.lastIndexOf('.') + 1));
                            }
                            break;
                        case "int128":
                        case "uint128":
                            elem_size = 2;
                            elem_count = 8;
                            break;
                        case "custom string":
                            elem_size = (int)Math.ceil(customStringLength / 8F) * 8;
                            elem_count = 1;
                            break;
                        case "string":
                            if (cpu.equals("micro800")) {
                                elem_size = 256;
                            } else if (cpu.equals("controllogix")) {
                                elem_size = 88;
                            } else {
                                elem_size = 84;
                            }
                            elem_count = 1;
                            break;
                        case "timer":
                        case "counter":
                        case "control":
                            if (cpu.equals("controllogix") || cpu.equals("micro800"))
                            {
                                if (name.endsWith(".PRE") || name.endsWith(".ACC") || name.endsWith(".LEN") || name.endsWith(".POS"))
                                {
                                    elem_size = 2;
                                    elem_count = 2;
                                    dataType = "int32";
                                }
                                else if (name.endsWith(".EN") || name.endsWith(".TT") || name.endsWith(".DN") ||
                                        name.endsWith(".CU") || name.endsWith(".CD") || name.endsWith(".OV") ||
                                        name.endsWith(".UN") || name.endsWith(".UA") || name.endsWith(".EU") ||
                                        name.endsWith(".EM") || name.endsWith(".ER") || name.endsWith(".UL") ||
                                        name.endsWith(".IN") || name.endsWith(".FD"))
                                {
                                    elem_size = 1;
                                    elem_count = 1;
                                    dataType = "bool";
                                }
                                else {
                                    elem_size = 2;
                                    elem_count = 6;
                                }
                            }
                            else
                            {
                                if (name.endsWith(".PRE") || name.endsWith(".ACC") || name.endsWith(".LEN") || name.endsWith(".POS"))
                                {
                                    elem_size = 2;
                                    elem_count = 1;
                                    dataType = "int16";
                                }
                                else {
                                    elem_size = 2;
                                    elem_count = 3;
                                }
                            }
                            break;
                        case "pid":
                            elem_size = 2;
                            elem_count = 23;
                            if (name.contains(".")){
                                pidName[i] = name;
                                name = name.substring(0, name.indexOf(".")); // Workaround
                            }
                            break;
                    }

                    if (dataType.equals("bool array")){
                        if (name.contains("[") && !name.contains(",") && name.contains("]")){
                            int tempBitIndex = Integer.parseInt(name.substring(name.indexOf('[') + 1, name.indexOf(']')));

                            int wordStart = (int)Math.floor((tempBitIndex / (elem_size * elem_count * 8.0)));
                            bitIndex[i] = tempBitIndex - wordStart * (elem_size * elem_count * 8);

                            name = name.substring(0, name.indexOf("[") + 1) + wordStart + "]"; // Workaround
                            dataType = "int32";
                        }
                    }

                    String tagABString = "protocol=ab_eip&";
                    tagABString += gateway_path_cpu + "&elem_size=" + elem_size + "&elem_count=" + elem_count + "&name=" + name;

                    tag_id = ABMaster.TagCreate(tagABString, timeout);

                    while (ABMaster.getStatus(tag_id) == 1){
                        try {
                            TimeUnit.MILLISECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (ABMaster.getStatus(tag_id) == 0){
                        if (dataType.equals("pid")){
                            pidTagString = "protocol=ab_eip&" + gateway_path_cpu + "&elem_size=" + elem_size + "&elem_count=" + elem_count + "&name=" + pidName;
                            dict.put(pidTagString, tag_id);
                            tags[i] = pidTagString;
                        } else {
                            dict.put(tagABString, tag_id);
                            tags[i] = tagABString;
                        }

                        dType[i] = dataType;
                    } else {
                        if (ABMaster.getStatus(tag_id) == 1)
                            tempValue = "pending";
                        else
                            tempValue = "err " + ABMaster.getStatus(tag_id);
                    }
                }

                if (!tags[i].equals("")){
                    Integer id = dict.get(tags[i]);

                    if (id != null){
                        if (ABMaster.getStatus(id) == 0){
                            ABMaster.read(id, timeout);
                            if (ABMaster.getStatus(id) == 0){
                                tempValue = "";

                                if (bitIndex[i] > -1){
                                    if (dType[i].equals("custom string") || (dType[i].equals("string") && cpu.equals("controllogix"))) {
                                        try {
                                            tempValue = new String(new byte[]{(byte) ABMaster.getUInt8(id, bitIndex[i] + 3)}, "UTF-8");
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                    } else if (dType[i].equals("string")) {
                                        if (cpu.equals("micro800")) {
                                            try {
                                                tempValue = new String(new byte[]{(byte) ABMaster.getUInt8(id, bitIndex[i])}, "UTF-8");
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            try {
                                                int result = bitIndex[i] % 2;

                                                if (result == 0)
                                                    tempValue = new String(new byte[]{(byte) ABMaster.getUInt8(id, bitIndex[i])}, "UTF-8");
                                                else
                                                    tempValue = new String(new byte[]{(byte) ABMaster.getUInt8(id, bitIndex[i] + 2)}, "UTF-8");
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        tempValue = BooleanDisplay(ABMaster.getBit(id, bitIndex[i]));
                                    }
                                } else {
                                    switch (dType[i]){
                                        case "int8":
                                            tempValue = String.valueOf(ABMaster.getInt8(id,0));
                                            break;
                                        case "uint8":
                                            tempValue = String.valueOf(ABMaster.getUInt8(id,0));
                                            break;
                                        case "int16":
                                            tempValue = String.valueOf(ABMaster.getInt16(id,0));
                                            break;
                                        case "uint16":
                                            tempValue = String.valueOf(ABMaster.getUInt16(id,0));
                                            break;
                                        case "int32":
                                            tempValue = String.valueOf(ABMaster.getInt32(id,0));
                                            break;
                                        case "uint32":
                                            tempValue = String.valueOf(ABMaster.getUInt32(id,0));
                                            break;
                                        case "int64":
                                            tempValue = String.valueOf(ABMaster.getInt64(id,0));
                                            break;
                                        case "uint64":
                                            tempValue = String.valueOf(ABMaster.getUInt64(id,0));
                                            break;
                                        case "int128":
                                            tempValue = String.valueOf(ABMaster.getInt128(id, 0));
                                            break;
                                        case "uint128":
                                            tempValue = String.valueOf(ABMaster.getUInt128(id, 0));
                                            break;
                                        case "float32":
                                            tempValue = String.valueOf(ABMaster.getFloat32(id,0));
                                            break;
                                        case "float64":
                                            tempValue = String.valueOf(ABMaster.getFloat64(id,0));
                                            break;
                                        case "bool":
                                            int val = ABMaster.getBit(id,0);

                                            if (MainActivity.boolDisplay.equals("One : Zero")){
                                                tempValue = String.valueOf(val);
                                            } else if (MainActivity.boolDisplay.equals("On : Off")){
                                                if (val == 1){
                                                    tempValue = "On";
                                                } else {
                                                    tempValue = "Off";
                                                }
                                            } else {
                                                if (val == 1){
                                                    tempValue = "True";
                                                } else {
                                                    tempValue = "False";
                                                }
                                            }
                                            break;
                                        case "custom string":
                                            //Actual String Length from first 4 bytes
                                            int actStrLgth = ABMaster.getInt32(id, 0);

                                            byte[] csvalBytes = new byte[actStrLgth];

                                            for (int k = 0; k < actStrLgth; k++){
                                                csvalBytes[k] = (byte)ABMaster.getUInt8(id, k + 4);
                                            }

                                            try {
                                                tempValue = new String(csvalBytes, "UTF-8");
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }

                                            break;
                                        case "string":
                                            byte[] valBytes;

                                            if (cpu.equals("micro800")){
                                                //String Length from first byte
                                                short strLgth = ABMaster.getUInt8(id, 0);

                                                valBytes = new byte[strLgth];

                                                for (int k = 0; k < strLgth; k++){
                                                    valBytes[k] = (byte)ABMaster.getUInt8(id, k + 1);
                                                }
                                            } else if (cpu.equals("controllogix")){
                                                //String Length from first 4 bytes
                                                int strLgth = ABMaster.getInt32(id, 0);

                                                valBytes = new byte[strLgth];

                                                for (int k = 0; k < strLgth; k++){
                                                    valBytes[k] = (byte)ABMaster.getUInt8(id, k + 4);
                                                }
                                            } else {
                                                //String Length from first 2 bytes
                                                int strLgth = ABMaster.getInt16(id, 0);

                                                int result = strLgth % 2;

                                                if (result == 0)
                                                    valBytes = new byte[strLgth];
                                                else
                                                    valBytes = new byte[strLgth + 1];

                                                for (int k = 0; k < valBytes.length; k += 2) // Reverse bytes
                                                {
                                                    valBytes[k + 1] = (byte)ABMaster.getUInt8(id, k + 2);
                                                    valBytes[k] = (byte)ABMaster.getUInt8(id, k + 3);
                                                }
                                            }

                                            try {
                                                tempValue = new String(valBytes, "UTF-8");
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }

                                            break;
                                        case "timer":
                                            if (cpu.equals("controllogix") || cpu.equals("micro800"))
                                            {
                                                StringBuilder tempValueBuilder = new StringBuilder();
                                                for (int k = 0; k < 3; k++)
                                                {
                                                    if (k == 2)
                                                        tempValueBuilder.append(ABMaster.getInt32(id, k * 4));
                                                    else
                                                        tempValueBuilder.append(ABMaster.getInt32(id, k * 4)).append(", ");
                                                }
                                                tempValue = tempValueBuilder.toString();
                                            }
                                            else
                                            {
                                                if (name.contains("."))
                                                {
                                                    switch (name.substring(name.indexOf('.') + 1)){
                                                        case "EN":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 15));
                                                            break;
                                                        case "TT":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 14));
                                                            break;
                                                        case "DN":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 13));
                                                            break;
                                                        case "PRE":
                                                            tempValue = String.valueOf(ABMaster.getInt16(id, 2));
                                                            break;
                                                        case "ACC":
                                                            tempValue = String.valueOf(ABMaster.getInt16(id, 4));
                                                            break;
                                                    }
                                                }
                                                else
                                                {
                                                    StringBuilder tempValueBuilder = new StringBuilder();
                                                    for (int k = 0; k < 3; k++)
                                                    {
                                                        if (k == 2)
                                                            tempValueBuilder.append(ABMaster.getInt16(id, k * 2));
                                                        else
                                                            tempValueBuilder.append(ABMaster.getInt16(id, k * 2)).append(", ");
                                                    }
                                                    tempValue = tempValueBuilder.toString();
                                                }
                                            }
                                            break;
                                        case "counter":
                                            if (cpu.equals("controllogix") || cpu.equals("micro800"))
                                            {
                                                StringBuilder tempValueBuilder = new StringBuilder();
                                                for (int k = 0; k < 3; k++)
                                                {
                                                    if (k == 2)
                                                        tempValueBuilder.append(ABMaster.getInt32(id, k * 4));
                                                    else
                                                        tempValueBuilder.append(ABMaster.getInt32(id, k * 4)).append(", ");
                                                }
                                                tempValue = tempValueBuilder.toString();
                                            }
                                            else
                                            {
                                                if (name.contains("."))
                                                {
                                                    switch (name.substring(name.indexOf('.') + 1)){
                                                        case "CU":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 15));
                                                            break;
                                                        case "CD":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 14));
                                                            break;
                                                        case "DN":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 13));
                                                            break;
                                                        case "OV":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 12));
                                                            break;
                                                        case "UN":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 11));
                                                            break;
                                                        case "UA":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 10));
                                                            break;
                                                        case "PRE":
                                                            tempValue = String.valueOf(ABMaster.getInt16(id, 2));
                                                            break;
                                                        case "ACC":
                                                            tempValue = String.valueOf(ABMaster.getInt16(id, 4));
                                                            break;
                                                    }
                                                }
                                                else
                                                {
                                                    StringBuilder tempValueBuilder = new StringBuilder();
                                                    for (int k = 0; k < 3; k++)
                                                    {
                                                        if (k == 2)
                                                            tempValueBuilder.append(ABMaster.getInt16(id, k * 2));
                                                        else
                                                            tempValueBuilder.append(ABMaster.getInt16(id, k * 2)).append(", ");
                                                    }
                                                    tempValue = tempValueBuilder.toString();
                                                }
                                            }
                                            break;
                                        case "control":
                                            if (cpu.equals("controllogix") || cpu.equals("micro800"))
                                            {
                                                StringBuilder tempValueBuilder = new StringBuilder();
                                                for (int k = 0; k < 3; k++)
                                                {
                                                    if (k == 2)
                                                        tempValueBuilder.append(ABMaster.getInt32(id, k * 4));
                                                    else
                                                        tempValueBuilder.append(ABMaster.getInt32(id, k * 4)).append(", ");
                                                }
                                                tempValue = tempValueBuilder.toString();
                                            }
                                            else
                                            {
                                                if (name.contains("."))
                                                {
                                                    switch (name.substring(name.indexOf('.') + 1)){
                                                        case "EN":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 15));
                                                            break;
                                                        case "EU":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 14));
                                                            break;
                                                        case "DN":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 13));
                                                            break;
                                                        case "EM":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 12));
                                                            break;
                                                        case "ER":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 11));
                                                            break;
                                                        case "UL":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 10));
                                                            break;
                                                        case "IN":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 9));
                                                            break;
                                                        case "FD":
                                                            tempValue = String.valueOf(ABMaster.getBit(id, 8));
                                                            break;
                                                        case "LEN":
                                                            tempValue = String.valueOf(ABMaster.getInt16(id, 2));
                                                            break;
                                                        case "POS":
                                                            tempValue = String.valueOf(ABMaster.getInt16(id, 4));
                                                            break;
                                                    }
                                                }
                                                else
                                                {
                                                    StringBuilder tempValueBuilder = new StringBuilder();
                                                    for (int k = 0; k < 3; k++)
                                                    {
                                                        if (k == 2)
                                                            tempValueBuilder.append(ABMaster.getInt16(id, k * 2));
                                                        else
                                                            tempValueBuilder.append(ABMaster.getInt16(id, k * 2)).append(", ");
                                                    }
                                                    tempValue = tempValueBuilder.toString();
                                                }
                                            }
                                            break;
                                        case "pid":
                                            if (pidName[i].contains("."))
                                            {
                                                switch (pidName[i].substring(pidName[i].indexOf('.') + 1)){
                                                    case "EN":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 15));
                                                        break;
                                                    case "DN":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 13));
                                                        break;
                                                    case "PV":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 12));
                                                        break;
                                                    case "SP":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 11));
                                                        break;
                                                    case "LL":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 10));
                                                        break;
                                                    case "UL":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 9));
                                                        break;
                                                    case "DB":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 8));
                                                        break;
                                                    case "DA":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 7));
                                                        break;
                                                    case "TF":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 6));
                                                        break;
                                                    case "SC":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 5));
                                                        break;
                                                    case "RG":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 4));
                                                        break;
                                                    case "OL":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 3));
                                                        break;
                                                    case "CM":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 2));
                                                        break;
                                                    case "AM":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 1));
                                                        break;
                                                    case "TM":
                                                        tempValue = BooleanDisplay(ABMaster.getBit(id, 0));
                                                        break;
                                                    case "SPS":
                                                        tempValue = String.valueOf(ABMaster.getInt16(id,4));
                                                        break;
                                                    case "KC":
                                                        tempValue = String.valueOf(ABMaster.getInt16(id,6));
                                                        break;
                                                    case "Ti":
                                                        tempValue = String.valueOf(ABMaster.getInt16(id,8));
                                                        break;
                                                    case "TD":
                                                        tempValue = String.valueOf(ABMaster.getInt16(id,10));
                                                        break;
                                                    case "MAXS":
                                                        tempValue = String.valueOf(ABMaster.getInt16(id,14));
                                                        break;
                                                    case "MINS":
                                                        tempValue = String.valueOf(ABMaster.getInt16(id,16));
                                                        break;
                                                    case "ZCD":
                                                        tempValue = String.valueOf(ABMaster.getInt16(id,18));
                                                        break;
                                                    case "CVH":
                                                        tempValue = String.valueOf(ABMaster.getInt16(id,22));
                                                        break;
                                                    case "CVL":
                                                        tempValue = String.valueOf(ABMaster.getInt16(id,24));
                                                        break;
                                                    case "LUT":
                                                        tempValue = String.valueOf(ABMaster.getInt16(id,26));
                                                        break;
                                                    case "SPV":
                                                        tempValue = String.valueOf(ABMaster.getInt16(id,28));
                                                        break;
                                                    case "CVP":
                                                        tempValue = String.valueOf(ABMaster.getInt16(id,32));
                                                        break;
                                                }
                                            }
                                            else
                                            {
                                                StringBuilder tempValueBuilder = new StringBuilder();
                                                for (int k = 0; k < 23; k++)
                                                {
                                                    if (k == 22)
                                                        tempValueBuilder.append(ABMaster.getInt16(id, k * 2));
                                                    else
                                                        tempValueBuilder.append(ABMaster.getInt16(id, k * 2)).append(", ");
                                                }
                                                tempValue = tempValueBuilder.toString();
                                            }
                                            break;
                                    }
                                }
                            } else {
                                if (ABMaster.getStatus(id) == 1)
                                    tempValue = "pending";
                                else
                                    tempValue = "err " + ABMaster.getStatus(id);

                                ABMaster.close(id);
                                dict.remove(tags[i]);

                                tag_id = ABMaster.TagCreate(tags[i], timeout);

                                while (ABMaster.getStatus(tag_id) == 1){
                                    try {
                                        TimeUnit.MILLISECONDS.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (ABMaster.getStatus(tag_id) == 0){
                                    dict.put(tags[i], tag_id);
                                }
                            }
                        } else {
                            if (ABMaster.getStatus(id) == 1)
                                tempValue = "pending";
                            else
                                tempValue = "err " + ABMaster.getStatus(id);

                            ABMaster.close(id);
                            dict.remove(tags[i]);

                            tag_id = ABMaster.TagCreate(tags[i], timeout);

                            while (ABMaster.getStatus(tag_id) == 1){
                                try {
                                    TimeUnit.MILLISECONDS.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (ABMaster.getStatus(tag_id) == 0){
                                dict.put(tags[i], tag_id);
                            }
                        }
                    }
                }

                // Publish progress on UI thread when value has changed.

                //if (!values[i].equals(tempValue)){
                //    values[i] = tempValue.trim();
                //    value = tempValue.trim();
                //    tempValue = "";

                //    callerID = params[0].get(2).get(i);

                //    publishProgress();
                //}


                // Publish progress on UI thread continuously, controlled with thread's sleep time.

                value = tempValue.trim();

                callerID = params[0].get(2).get(i);

                publishProgress();

                // Slow down the communication to give time for UI to update all the values.
                // Adjust the sleep time if necessary.
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return "FINISHED";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate();

        ABtaskCallback.UpdateABUI(callerID, value);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        if (dict != null){
            for (String key : dict.keySet()){
                Integer keyIntVal = dict.get(key);
                if (keyIntVal != null)
                    ABMaster.close(keyIntVal);
            }
        }
    }

    private String BooleanDisplay(int boolValue){
        if (MainActivity.boolDisplay.equals("One : Zero")){
            if (boolValue == 1){
                return "1";
            } else {
                return "0";
            }
        } else if (MainActivity.boolDisplay.equals("On : Off")){
            if (boolValue == 1){
                return "On";
            } else {
                return "Off";
            }
        } else {
            if (boolValue == 1){
                return "True";
            } else {
                return "False";
            }
        }
    }
}
