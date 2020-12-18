package com.e.phonetest;

import android.os.AsyncTask;
import org.libplctag.Tag;
import java.util.HashMap;

@SuppressWarnings("deprecation")
public class AsyncGaugeTask extends AsyncTask<String, Void, String> {
    GaugeTaskCallback gaugeTaskCallback = GaugeActivity.gaugeTaskCallback;

    public String gaugeValue = "";

    HashMap<String, Integer> dict = new HashMap<>();
    private Tag GaugeMaster = new Tag();

    private int elem_size;

    @Override
    protected String doInBackground(String... params) {
        String gateway_path_cpu = params[0];
        String name = params[1];
        String dataType = params[2];
        String abTag = "";

        int timeout = Integer.parseInt(params[3]);
        int tag_id;

        while (!isCancelled()){
            String tempValue = "";

            if (dict.size() != 1){
                switch (dataType) {
                    case "int8":
                    case "uint8":
                        elem_size = 1;
                        break;
                    case "int16":
                    case "uint16":
                        elem_size = 2;
                        break;
                    case "int32":
                    case "uint32":
                    case "float32":
                        elem_size = 4;
                        break;
                    case "int64":
                    case "uint64":
                    case "float64":
                        elem_size = 8;
                        break;
                }

                String tagABString = "protocol=ab_eip&";
                tagABString += gateway_path_cpu + "&elem_size=" + elem_size + "&elem_count=1&name=" + name;

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
                    abTag = tagABString;
                } else {
                    if (GaugeMaster.getStatus(tag_id) == 1)
                        tempValue = "pending";
                    else
                        tempValue = "err " + GaugeMaster.getStatus(tag_id);

                    dict.clear();
                }
            }

            if (!abTag.equals("")){
                Integer id = dict.get(abTag);

                if (id != null){
                    if (GaugeMaster.getStatus(id) == 0){
                        GaugeMaster.read(id, timeout);
                        if (GaugeMaster.getStatus(id) == 0){
                            switch (dataType){
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
                        } else {
                            if (GaugeMaster.getStatus(id) == 1)
                                tempValue = "pending";
                            else
                                tempValue = "err " + GaugeMaster.getStatus(id);

                            GaugeMaster.close(id);
                            dict.remove(abTag);
                            abTag = "";
                        }

                    } else {
                        if (GaugeMaster.getStatus(id) == 1)
                            tempValue = "pending";
                        else
                            tempValue = "err " + GaugeMaster.getStatus(id);

                        GaugeMaster.close(id);
                        dict.remove(abTag);
                        abTag = "";
                    }
                }
            }

            // Publish progress on UI thread continuously, controlled with thread's sleep time.

            gaugeValue = tempValue.trim();

            publishProgress();

            // Adjust the sleep time if necessary.
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
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

        gaugeTaskCallback.UpdateGaugeValue(gaugeValue);
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
