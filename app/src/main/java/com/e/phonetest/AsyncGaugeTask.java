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

    @Override
    protected String doInBackground(String... params) {
        String gateway_path_cpu = params[0];
        String name = params[1];
        String tag = "";

        int timeout = Integer.parseInt(params[2]);
        int tag_id = -1;

        while (!isCancelled()){
            String tempValue = "";

            if (dict.size() != 1){
                String tagABString = "protocol=ab_eip&";
                tagABString += gateway_path_cpu + "&elem_size=4&elem_count=1&name=" + name;

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
                    tag = tagABString;
                } else {
                    if (GaugeMaster.getStatus(tag_id) == 1)
                        tempValue = "pending";
                    else
                        tempValue = "err " + GaugeMaster.getStatus(tag_id);

                    dict.clear();
                }
            }

            if (!tag.equals("")){
                Integer id = dict.get(tag);

                if (id != null){
                    if (GaugeMaster.getStatus(id) == 0){
                        GaugeMaster.read(id, timeout);
                        if (GaugeMaster.getStatus(id) == 0){
                            tempValue = String.valueOf(GaugeMaster.getFloat32(tag_id,0));
                        } else {
                            if (GaugeMaster.getStatus(id) == 1)
                                tempValue = "pending";
                            else
                                tempValue = "err " + GaugeMaster.getStatus(id);

                            GaugeMaster.close(id);
                            dict.remove(tag);
                            tag = "";
                        }

                    } else {
                        if (GaugeMaster.getStatus(id) == 1)
                            tempValue = "pending";
                        else
                            tempValue = "err " + GaugeMaster.getStatus(id);

                        GaugeMaster.close(id);
                        dict.remove(tag);
                        tag = "";
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
