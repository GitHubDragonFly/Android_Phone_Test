package com.e.phonetest;

import android.os.AsyncTask;
import android.text.TextUtils;
import org.libplctag.Tag;
import java.util.Arrays;
import java.util.HashMap;

@SuppressWarnings("deprecation")
public class AsyncGaugeTask extends AsyncTask<String, String, String> {
    GaugeTaskCallback gaugeTaskCallback = GaugeActivity.gaugeTaskCallback;

    public String gaugeValue = "";

    private String[] name = new String[] {"", ""}, dataType = new String[] {"", ""};

    HashMap<String, Integer> dict = new HashMap<>();
    private Tag GaugeMaster = new Tag();

    private int elem_size;

    @Override
    protected String doInBackground(String... params) {
        String gateway_path_cpu = params[0];
        name[0] = params[1];
        dataType[0] = params[2];
        name[1] = params[3];
        dataType[1] = params[4];

        int timeout = Integer.parseInt(params[5]);
        int tag_id = -1;

        String[] tags = new String[2];
        int[] bitIndex = new int[2];
        Arrays.fill(tags, "");
        Arrays.fill(bitIndex, -1);

        while (!isCancelled()){
            for (int i = 0; i < 2; i++) {
                String tempValue = "";

                if (dict.size() != 2){
                    if (name[i].contains("/")){
                        bitIndex[i] = Integer.parseInt(name[i].substring(name[i].indexOf('/') + 1));
                        name[i] = name[i].substring(0, name[i].indexOf('/'));
                    }

                    switch (dataType[i]) {
                        case "bool":
                            elem_size = 1;
                            break;
                        case "int8":
                        case "uint8":
                            elem_size = 1;
                            if ((name[i].contains(".") && !name[i].contains(":")) || (name[i].contains(".") && name[i].lastIndexOf('.') > name[i].indexOf('.'))){
                                if (TextUtils.isDigitsOnly(name[i].substring(name[i].lastIndexOf('.') + 1)))
                                    bitIndex[i] = Integer.parseInt(name[i].substring(name[i].lastIndexOf('.') + 1));
                            }
                            break;
                        case "int16":
                        case "uint16":
                            elem_size = 2;
                            if ((name[i].contains(".") && !name[i].contains(":")) || (name[i].contains(".") && name[i].lastIndexOf('.') > name[i].indexOf('.'))){
                                if (TextUtils.isDigitsOnly(name[i].substring(name[i].lastIndexOf('.') + 1)))
                                    bitIndex[i] = Integer.parseInt(name[i].substring(name[i].lastIndexOf('.') + 1));
                            }
                            break;
                        case "bool array":
                            elem_size = 4;
                            break;
                        case "int32":
                        case "uint32":
                        case "float32":
                            elem_size = 4;
                            if ((name[i].contains(".") && !name[i].contains(":")) || (name[i].contains(".") && name[i].lastIndexOf('.') > name[i].indexOf('.'))){
                                if (TextUtils.isDigitsOnly(name[i].substring(name[i].lastIndexOf('.') + 1)))
                                    bitIndex[i] = Integer.parseInt(name[i].substring(name[i].lastIndexOf('.') + 1));
                            }
                            break;
                        case "int64":
                        case "uint64":
                        case "float64":
                            elem_size = 8;
                            if ((name[i].contains(".") && !name[i].contains(":")) || (name[i].contains(".") && name[i].lastIndexOf('.') > name[i].indexOf('.'))){
                                if (TextUtils.isDigitsOnly(name[i].substring(name[i].lastIndexOf('.') + 1)))
                                    bitIndex[i] = Integer.parseInt(name[i].substring(name[i].lastIndexOf('.') + 1));
                            }
                            break;
                    }

                    if (dataType[i].equals("bool array")){
                        if (name[i].contains("[") && !name[i].contains(",") && name[i].contains("]")){
                            int tempBitIndex = Integer.parseInt(name[i].substring(name[i].indexOf('[') + 1, name[i].indexOf(']')));

                            int wordStart = (int)Math.floor((tempBitIndex / (elem_size * 8.0)));
                            bitIndex[i] = tempBitIndex - wordStart * (elem_size * 8);

                            name[i] = name[i].substring(0, name[i].indexOf("[") + 1) + wordStart + "]"; // Workaround
                            dataType[i] = "int32";
                        }
                    }

                    if (name[i].equals("")){
                        tags[i] = "";
                        dict.put(tags[i], tag_id);
                    } else {
                        String tagABString = "protocol=ab_eip&";
                        tagABString += gateway_path_cpu + "&elem_size=" + elem_size + "&elem_count=1&name=" + name[i] + "&elem_type=" + dataType[i];

                        tag_id = GaugeMaster.TagCreate(tagABString, timeout);

                        while (GaugeMaster.getStatus(tag_id) == 1){
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (GaugeMaster.getStatus(tag_id) == 0){
                            dict.put(tagABString, tag_id);
                            tags[i] = tagABString;
                        } else {
                            if (GaugeMaster.getStatus(tag_id) == 1)
                                tempValue = "pending";
                            else
                                tempValue = "err " + GaugeMaster.getStatus(tag_id);

                            dict.clear();
                        }
                    }
                }

                if (!tags[i].equals("")){
                    Integer id = dict.get(tags[i]);

                    if (id != null){
                        if (GaugeMaster.getStatus(id) == 0){
                            GaugeMaster.read(id, timeout);
                            if (GaugeMaster.getStatus(id) == 0){
                                if (bitIndex[i] > -1){
                                    int val = GaugeMaster.getBit(id, bitIndex[i]);

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
                                } else {
                                    switch (dataType[i]){
                                        case "int8":
                                            tempValue = String.valueOf(GaugeMaster.getInt8(id,0));
                                            break;
                                        case "uint8":
                                            tempValue = String.valueOf(GaugeMaster.getUInt8(id,0));
                                            break;
                                        case "int16":
                                            tempValue = String.valueOf(GaugeMaster.getInt16(id,0));
                                            break;
                                        case "uint16":
                                            tempValue = String.valueOf(GaugeMaster.getUInt16(id,0));
                                            break;
                                        case "int32":
                                            tempValue = String.valueOf(GaugeMaster.getInt32(id,0));
                                            break;
                                        case "uint32":
                                            tempValue = String.valueOf(GaugeMaster.getUInt32(id,0));
                                            break;
                                        case "int64":
                                            tempValue = String.valueOf(GaugeMaster.getInt64(id,0));
                                            break;
                                        case "uint64":
                                            tempValue = String.valueOf(GaugeMaster.getUInt64(id,0));
                                            break;
                                        case "float32":
                                            tempValue = String.valueOf(GaugeMaster.getFloat32(id,0));
                                            break;
                                        case "float64":
                                            tempValue = String.valueOf(GaugeMaster.getFloat64(id,0));
                                            break;
                                    }
                                }
                            } else {
                                if (GaugeMaster.getStatus(id) == 1)
                                    tempValue = "pending";
                                else
                                    tempValue = "err " + GaugeMaster.getStatus(id);

                                GaugeMaster.close(id);
                                dict.remove(tags[i]);
                            }

                        } else {
                            if (GaugeMaster.getStatus(id) == 1)
                                tempValue = "pending";
                            else
                                tempValue = "err " + GaugeMaster.getStatus(id);

                            GaugeMaster.close(id);
                            dict.remove(tags[i]);
                        }
                    }
                }

                // Publish progress on UI thread continuously, controlled with thread's sleep time.

                gaugeValue = tempValue.trim();

                publishProgress(String.valueOf(i));

                // Adjust the sleep time if necessary.
                try {
                    Thread.sleep(50);
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
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate();

        if (values[0].equals("0"))
            gaugeTaskCallback.UpdateGaugeValue(gaugeValue);
        else
            gaugeTaskCallback.UpdateLEDValue(gaugeValue);
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
