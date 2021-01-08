package com.e.phonetest;

import android.os.AsyncTask;
import android.text.TextUtils;
import org.libplctag.Tag;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class AsyncWriteABTask extends AsyncTask<String, Void, String> {

    private static String tagABString = "protocol=ab_eip&";
    public String value = "", gateway_path_cpu = "", cpu = "", name = "", pidName = "";
    int timeout, elem_size, elem_count, customStringLength;
    private final Tag ABWriteMaster = new Tag();

    WriteABTaskCallback WritetaskCallback = MainActivity.WritetaskCallback;

    @Override
    protected String doInBackground(String... params) {

        gateway_path_cpu = params[0];
        cpu = gateway_path_cpu.substring(gateway_path_cpu.lastIndexOf('=') + 1);
        timeout = Integer.parseInt(params[1]);
        int tag_id, bitIndex = -1;

        String fullString = params[2];
        fullString = fullString.replace(" ", "");

        name = fullString.substring(0, fullString.indexOf(';'));

        if (name.contains("/")){
            bitIndex = Integer.parseInt(name.substring(name.indexOf('/') + 1));
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
            case "int16":
            case "uint16":
                elem_size = 2;
                elem_count = 1;
                if ((name.contains(".") && !name.contains(":")) || (name.contains(".") && name.lastIndexOf('.') > name.indexOf('.'))){
                    if (TextUtils.isDigitsOnly(name.substring(name.lastIndexOf('.') + 1)))
                        bitIndex = Integer.parseInt(name.substring(name.lastIndexOf('.') + 1));
                }
                break;
            case "int32":
            case "uint32":
            case "float32":
            case "bool array":
                elem_size = 2;
                elem_count = 2;
                if ((name.contains(".") && !name.contains(":")) || (name.contains(".") && name.lastIndexOf('.') > name.indexOf('.'))){
                    if (TextUtils.isDigitsOnly(name.substring(name.lastIndexOf('.') + 1)))
                        bitIndex = Integer.parseInt(name.substring(name.lastIndexOf('.') + 1));
                }
                break;
            case "int64":
            case "uint64":
            case "float64":
                elem_size = 2;
                elem_count = 4;
                if ((name.contains(".") && !name.contains(":")) || (name.contains(".") && name.lastIndexOf('.') > name.indexOf('.'))){
                    if (TextUtils.isDigitsOnly(name.substring(name.lastIndexOf('.') + 1)))
                        bitIndex = Integer.parseInt(name.substring(name.lastIndexOf('.') + 1));
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
                if ((cpu.equals("controllogix") || cpu.equals("micro800")) && name.endsWith(".PRE")) {
                    elem_size = 2;
                    elem_count = 2;
                    dataType = "int32";
                } else if ((cpu.equals("micrologix") || cpu.equals("slc500") || cpu.equals("plc5") || cpu.equals("logixpccc")) && name.endsWith(".PRE")) {
                    elem_size = 2;
                    elem_count = 1;
                    dataType = "int16";
                } else {
                    value = "AB Write Failed";
                    publishProgress();
                    return "FINISHED";
                }
                break;
            case "pid":
                elem_size = 2;
                elem_count = 23;
                if (name.contains(".")){
                    pidName = name;
                    name = name.substring(0, name.indexOf(".")); // Workaround
                }
                break;
        }

        if (dataType.equals("bool array")){
            if (name.contains("[") && !name.contains(",") && name.contains("]")){
                int tempBitIndex = Integer.parseInt(name.substring(name.indexOf('[') + 1, name.indexOf(']')));

                int wordStart = (int)Math.floor((tempBitIndex / (elem_size * elem_count * 8.0)));
                bitIndex = tempBitIndex - wordStart * (elem_size * elem_count * 8);

                name = name.substring(0, name.indexOf("[") + 1) + wordStart + "]"; // Workaround
                    dataType = "int32";
            }
        }

        tagABString += gateway_path_cpu + "&elem_size=" + elem_size + "&elem_count=" + elem_count + "&name=" + name;

        tag_id = ABWriteMaster.TagCreate(tagABString, timeout);

        while (ABWriteMaster.getStatus(tag_id) == 1){
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (ABWriteMaster.getStatus(tag_id) == 0){
            ABWriteMaster.read(tag_id, timeout);

            if (bitIndex > -1){
                if (dataType.equals("custom string") || (dataType.equals("string") && cpu.equals("controllogix"))) {
                    try {
                        ABWriteMaster.setUInt8(tag_id, bitIndex + 3, params[3].getBytes("UTF-8")[0]);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else if (dataType.equals("string")){
                    if (cpu.equals("micro800")) {
                        try {
                            ABWriteMaster.setUInt8(tag_id, bitIndex + 1, params[3].getBytes("UTF-8")[0]);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            int result = bitIndex % 2;

                            if (result == 0)
                                ABWriteMaster.setUInt8(tag_id, bitIndex, params[3].getBytes("UTF-8")[0]);
                            else
                                ABWriteMaster.setUInt8(tag_id, bitIndex + 2, params[3].getBytes("UTF-8")[0]);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (!(params[3].equals("0") || params[3].equals("false") || params[3].equals("False") || params[3].equals("1") || params[3].equals("true") || params[3].equals("True"))){
                        value = "AB Write Failed";
                        publishProgress();
                        ABWriteMaster.close(tag_id);
                        return "FINISHED";
                    } else {
                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                            ABWriteMaster.setBit(tag_id, bitIndex, 0);
                        else
                            ABWriteMaster.setBit(tag_id, bitIndex, 1);
                    }
                }
            } else {
                if ((!(dataType.equals("bool") || dataType.equals("string") || dataType.equals("custom string") ||
                        dataType.equals("float32") || dataType.equals("float64")) && !TextUtils.isDigitsOnly(params[3])) ||
                        ((dataType.equals("float32") || dataType.equals("float64")) && !TextUtils.isDigitsOnly(params[3].replace(".", "")))){

                    value = "AB Write Failed";
                    publishProgress();
                    ABWriteMaster.close(tag_id);
                    return "FINISHED";
                } else {
                    switch (dataType){
                        case "int8":
                            ABWriteMaster.setInt8(tag_id, 0, Integer.parseInt(params[3]));
                            break;
                        case "uint8":
                            ABWriteMaster.setUInt8(tag_id, 0, Short.parseShort(params[3]));
                            break;
                        case "int16":
                            ABWriteMaster.setInt16(tag_id, 0, Integer.parseInt(params[3]));
                            break;
                        case "uint16":
                            ABWriteMaster.setUInt16(tag_id, 0, Integer.parseInt(params[3]));
                            break;
                        case "int32":
                            ABWriteMaster.setInt32(tag_id, 0, Integer.parseInt(params[3]));
                            break;
                        case "uint32":
                            ABWriteMaster.setUInt32(tag_id, 0, Long.parseLong(params[3]));
                            break;
                        case "int64":
                            ABWriteMaster.setInt64(tag_id, 0, Long.parseLong(params[3]));
                            break;
                        case "uint64":
                            ABWriteMaster.setUInt64(tag_id, 0, new BigInteger(params[3]));
                            break;
                        case "int128":
                            ABWriteMaster.setInt128(tag_id, 0, new BigInteger(params[3]));
                            break;
                        case "uint128":
                            ABWriteMaster.setUInt128(tag_id, 0, new BigInteger(params[3]));
                            break;
                        case "float32":
                            ABWriteMaster.setFloat32(tag_id, 0, Float.parseFloat(params[3]));
                            break;
                        case "float64":
                            ABWriteMaster.setFloat64(tag_id, 0, Double.parseDouble(params[3]));
                            break;
                        case "bool":
                            if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                ABWriteMaster.setUInt8(tag_id, 0, (short) 0);
                            else if (params[3].equals("1") || params[3].equals("true") || params[3].equals("True"))
                                ABWriteMaster.setUInt8(tag_id, 0, (short) 1);
                            else {
                                value = "AB Write Failed";
                                publishProgress();
                                ABWriteMaster.close(tag_id);
                                return "FINISHED";
                            }
                            break;
                        case "custom string":
                            byte[] strBytes = null;

                            try {
                                strBytes = params[3].getBytes("UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            assert strBytes != null;

                            if (strBytes.length > customStringLength){
                                value = "AB Write Failed";
                                publishProgress();
                                ABWriteMaster.close(tag_id);
                                return "FINISHED";
                            } else {
                                byte[] allBytes, strLengthBytes;

                                strLengthBytes = ByteBuffer.allocate(4).putInt(strBytes.length).array();

                                allBytes = new byte[customStringLength + 4];

                                //Add data length bytes
                                allBytes[0] = strLengthBytes[3];
                                allBytes[1] = strLengthBytes[2];
                                allBytes[2] = strLengthBytes[1];
                                allBytes[3] = strLengthBytes[0];

                                //Add data bytes
                                System.arraycopy(strBytes, 0, allBytes, 4, strBytes.length);

                                //Set all byte values to the tag
                                for (int j = 0; j < allBytes.length; j++){
                                    ABWriteMaster.setUInt8(tag_id, j, allBytes[j]);
                                }
                            }

                            break;
                        case "string":
                            byte[] data = null, valBytes = new byte[]{}, lengthBytes;

                            if (cpu.equals("micro800")){
                                try {
                                    data = params[3].getBytes("UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }

                                if (data != null){
                                    lengthBytes = ByteBuffer.allocate(4).putInt(data.length).array();

                                    valBytes = new byte[data.length + 1];

                                    //Add data length bytes
                                    valBytes[0] = lengthBytes[3];

                                    //Add data bytes
                                    System.arraycopy(data, 0, valBytes, 1, data.length);
                                }
                            } else if (cpu.equals("controllogix")){
                                try {
                                    data = params[3].getBytes("UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }

                                if (data != null){
                                    lengthBytes = ByteBuffer.allocate(4).putInt(data.length).array();

                                    valBytes = new byte[data.length + 4];

                                    //Add data length bytes
                                    valBytes[0] = lengthBytes[3];
                                    valBytes[1] = lengthBytes[2];
                                    valBytes[2] = lengthBytes[1];
                                    valBytes[3] = lengthBytes[0];

                                    //Add data bytes
                                    System.arraycopy(data, 0, valBytes, 4, data.length);
                                }
                            } else {
                                int result = params[3].length() % 2;

                                if (result != 0)
                                    params[3] += ' ';

                                try {
                                    data = params[3].getBytes("UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }

                                if (data != null){
                                    lengthBytes = ByteBuffer.allocate(4).putInt(data.length).array();

                                    valBytes = new byte[84];

                                    //Add data length bytes
                                    valBytes[0] = lengthBytes[3];
                                    valBytes[1] = lengthBytes[2];

                                    // Reverse data bytes
                                    for (int z = 0; z < data.length - 1; z += 2)
                                    {
                                        byte temp = data[z];
                                        data[z] = data[z + 1];
                                        data[z + 1] = temp;
                                    }

                                    //Add data bytes
                                    System.arraycopy(data, 0, valBytes, 2, data.length);
                                }
                            }

                            //Set all byte values to the tag
                            for (int j = 0; j < valBytes.length; j++){
                                ABWriteMaster.setUInt8(tag_id, j, valBytes[j]);
                            }

                            break;
                        case "pid":
                            if (pidName.contains("."))
                            {
                                switch (pidName.substring(pidName.indexOf('.') + 1)){
                                    case "PV":
                                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                            ABWriteMaster.setBit(tag_id, 12, 0);
                                        else
                                            ABWriteMaster.setBit(tag_id, 12, 1);
                                        break;
                                    case "SP":
                                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                            ABWriteMaster.setBit(tag_id, 11, 0);
                                        else
                                            ABWriteMaster.setBit(tag_id, 11, 1);
                                        break;
                                    case "LL":
                                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                            ABWriteMaster.setBit(tag_id, 10, 0);
                                        else
                                            ABWriteMaster.setBit(tag_id, 10, 1);
                                        break;
                                    case "UL":
                                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                            ABWriteMaster.setBit(tag_id, 9, 0);
                                        else
                                            ABWriteMaster.setBit(tag_id, 9, 1);
                                        break;
                                    case "DB":
                                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                            ABWriteMaster.setBit(tag_id, 8, 0);
                                        else
                                            ABWriteMaster.setBit(tag_id, 8, 1);
                                        break;
                                    case "DA":
                                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                            ABWriteMaster.setBit(tag_id, 7, 0);
                                        else
                                            ABWriteMaster.setBit(tag_id, 7, 1);
                                        break;
                                    case "TF":
                                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                            ABWriteMaster.setBit(tag_id, 6, 0);
                                        else
                                            ABWriteMaster.setBit(tag_id, 6, 1);
                                        break;
                                    case "SC":
                                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                            ABWriteMaster.setBit(tag_id, 5, 0);
                                        else
                                            ABWriteMaster.setBit(tag_id, 5, 1);
                                        break;
                                    case "RG":
                                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                            ABWriteMaster.setBit(tag_id, 4, 0);
                                        else
                                            ABWriteMaster.setBit(tag_id, 4, 1);
                                        break;
                                    case "OL":
                                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                            ABWriteMaster.setBit(tag_id, 3, 0);
                                        else
                                            ABWriteMaster.setBit(tag_id, 3, 1);
                                        break;
                                    case "CM":
                                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                            ABWriteMaster.setBit(tag_id, 2, 0);
                                        else
                                            ABWriteMaster.setBit(tag_id, 2, 1);
                                        break;
                                    case "AM":
                                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                            ABWriteMaster.setBit(tag_id, 1, 0);
                                        else
                                            ABWriteMaster.setBit(tag_id, 1, 1);
                                        break;
                                    case "TM":
                                        if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False"))
                                            ABWriteMaster.setBit(tag_id, 0, 0);
                                        else
                                            ABWriteMaster.setBit(tag_id, 0, 1);
                                        break;
                                    case "SPS":
                                        ABWriteMaster.setInt16(tag_id, 4, Integer.parseInt(params[3]));
                                        break;
                                    case "KC":
                                        ABWriteMaster.setInt16(tag_id, 6, Integer.parseInt(params[3]));
                                        break;
                                    case "Ti":
                                        ABWriteMaster.setInt16(tag_id, 8, Integer.parseInt(params[3]));
                                        break;
                                    case "TD":
                                        ABWriteMaster.setInt16(tag_id, 10, Integer.parseInt(params[3]));
                                        break;
                                    case "MAXS":
                                        ABWriteMaster.setInt16(tag_id, 14, Integer.parseInt(params[3]));
                                        break;
                                    case "MINS":
                                        ABWriteMaster.setInt16(tag_id, 16, Integer.parseInt(params[3]));
                                        break;
                                    case "ZCD":
                                        ABWriteMaster.setInt16(tag_id, 18, Integer.parseInt(params[3]));
                                        break;
                                    case "CVH":
                                        ABWriteMaster.setInt16(tag_id, 22, Integer.parseInt(params[3]));
                                        break;
                                    case "CVL":
                                        ABWriteMaster.setInt16(tag_id, 24, Integer.parseInt(params[3]));
                                        break;
                                    case "LUT":
                                        ABWriteMaster.setInt16(tag_id, 26, Integer.parseInt(params[3]));
                                        break;
                                    case "SPV":
                                        ABWriteMaster.setInt16(tag_id, 28, Integer.parseInt(params[3]));
                                        break;
                                    case "CVP":
                                        ABWriteMaster.setInt16(tag_id, 32, Integer.parseInt(params[3]));
                                        break;
                                        default:
                                            value = "AB Write Failed";
                                            publishProgress();
                                            ABWriteMaster.close(tag_id);
                                            return "FINISHED";
                                }
                            }
                            else
                            {
                                value = "AB Write Failed";
                                publishProgress();
                                ABWriteMaster.close(tag_id);
                                return "FINISHED";
                            }
                            break;
                    }
                }
            }

            ABWriteMaster.write(tag_id, timeout);

            value = "AB Write Success";
        } else {
            value = "AB Write Failed";
        }

        publishProgress();

        ABWriteMaster.close(tag_id);

        return "FINISHED";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate();

        WritetaskCallback.WriteUpdateUI(value);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
