package com.e.phonetest;

import android.os.AsyncTask;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.libplctag.Tag;

@SuppressWarnings("deprecation")
public class AsyncTaskGetCLGXTags extends AsyncTask<String, Void, String> {

    public List<String> valsC = new ArrayList<>(), valsP = new ArrayList<>();
    int timeout;
    private final Tag GetCLGXTagsMaster = new Tag();

    GetCLGXTagsTaskCallback GetCLGXTagstaskCallback = MainActivity.GetCLGXTagstaskCallback;

    @Override
    protected String doInBackground(String... params) {
        String tagGetControllerTags = "protocol=ab_eip&" + params[0] + "&cpu=controllogix&name=@tags";
        String tagGetProgramTags = "protocol=ab_eip&" + params[0] + "&cpu=controllogix&name=Program:" + params[1] + ".@tags";
        timeout = Integer.parseInt(params[2]);

        int tag_id, tagSize, offset = 0;

        tag_id = GetCLGXTagsMaster.TagCreate(tagGetControllerTags, timeout);

        valsC.add("***  Controller Tags List  ***");

        while (GetCLGXTagsMaster.getStatus(tag_id) == 1)
        {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // if the status is not ok, we have to handle the error
        if (GetCLGXTagsMaster.getStatus(tag_id) < 0)
        {
            GetCLGXTagsMaster.close(tag_id);

            valsC.add("Failed to get controller tags!");
        } else {
            tagSize = GetCLGXTagsMaster.size(tag_id);

            while (offset < tagSize)
            {
                // tagId, tagLength and IsStructure variables can be calculated if needed.
                // They can also be diplayed by following the comments further below.

                //int tagId = (int)GetCLGXTagsMaster.getUInt32(tag_id, offset);
                int tagType = GetCLGXTagsMaster.getUInt16(tag_id, offset + 4);
                //int tagLength = GetCLGXTagsMaster.getUInt16(tag_id, offset + 6);

                Boolean systemBit = extractInt32Bit(tagType, 12); // bit 12

                if (!systemBit)
                {
                    //Boolean IsStructure = extractInt32Bit(tagType, 15); // bit 15

                    int x = (int)GetCLGXTagsMaster.getUInt32(tag_id, offset + 8);
                    int y = (int)GetCLGXTagsMaster.getUInt32(tag_id, offset + 12);
                    int z = (int)GetCLGXTagsMaster.getUInt32(tag_id, offset + 16);

                    String dimensions = "";

                    if (x != 0 && y != 0 && z != 0)
                        dimensions = "[" + x + ", " + y + ", " + z + "]";
                    else if (x != 0 && y != 0)
                        dimensions = "[" + x + ", " + y + "]";
                    else if (x != 0)
                        if (tagType == 8403)
                            dimensions = "[" + (x * 32) + "]";
                        else
                            dimensions = "[" + x + "]";

                    offset += 20;

                    int tagNameLength = GetCLGXTagsMaster.getUInt16(tag_id, offset);
                    byte[] tagNameBytes = new byte[tagNameLength];

                    offset += 2;

                    for (int i = 0; i < tagNameLength; i++)
                    {
                        tagNameBytes[i] = (byte)GetCLGXTagsMaster.getUInt8(tag_id, offset + i);
                    }

                    String tagName = "";

                    try {
                        tagName = new String(tagNameBytes, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if (!tagName.contains(":")){
                        // Display tag name and its dimensions only.
                        valsC.add(tagName + dimensions);

                        // Display tag name, its dimensions, tagType, IsStructure, tagLength and tagId (comment and uncomment appropriate lines above and below).
                        //valsC.add(tagName + dimensions + " ; Type = " + tagType + " ; IsStructure = " + IsStructure + " ; Length = " + tagLength + " Bytes ; Id = " + tagId);
                    }

                    offset += tagNameLength;
                }
                else
                {
                    offset += 20;
                    int tagNameLength = GetCLGXTagsMaster.getUInt16(tag_id, offset);
                    offset += 2 + tagNameLength;
                }
            }
        }

        Collections.sort(valsC);
        valsC.add("***  Program Tags List (" + params[1] + ")  ***");

        GetCLGXTagsMaster.close(tag_id);

        tag_id = GetCLGXTagsMaster.TagCreate(tagGetProgramTags, timeout);

        while (GetCLGXTagsMaster.getStatus(tag_id) == 1)
        {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // if the status is not ok, we have to handle the error
        if (GetCLGXTagsMaster.getStatus(tag_id) < 0)
        {
            GetCLGXTagsMaster.close(tag_id);

            valsP.add("Failed to get program tags!");
            valsC.addAll(valsP);

            publishProgress();

            return "Finished";
        } else {
            tagSize = GetCLGXTagsMaster.size(tag_id);
            offset = 0;

            while (offset < tagSize)
            {
                // tagId, tagLength and IsStructure variables can be calculated and used if needed.
                // They can also be diplayed by following the comments further below.

                //int tagId = (int)GetCLGXTagsMaster.getUInt32(tag_id, offset);
                int tagType = GetCLGXTagsMaster.getUInt16(tag_id, offset + 4);
                //int tagLength = GetCLGXTagsMaster.getUInt16(tag_id, offset + 6);

                Boolean systemBit = extractInt32Bit(tagType, 12); // bit 12

                if (!systemBit)
                {
                    //Boolean IsStructure = extractInt32Bit(tagType, 15); // bit 15

                    int x = (int)GetCLGXTagsMaster.getUInt32(tag_id, offset + 8);
                    int y = (int)GetCLGXTagsMaster.getUInt32(tag_id, offset + 12);
                    int z = (int)GetCLGXTagsMaster.getUInt32(tag_id, offset + 16);

                    String dimensions = "";

                    if (x != 0 && y != 0 && z != 0)
                        dimensions = "[" + x + ", " + y + ", " + z + "]";
                    else if (x != 0 && y != 0)
                        dimensions = "[" + x + ", " + y + "]";
                    else if (x != 0)
                        if (tagType == 8403)
                            dimensions = "[" + (x * 32) + "]";
                        else
                            dimensions = "[" + x + "]";

                    offset += 20;

                    int tagNameLength = GetCLGXTagsMaster.getUInt16(tag_id, offset);
                    byte[] tagNameBytes = new byte[tagNameLength];

                    offset += 2;

                    for (int i = 0; i < tagNameLength; i++)
                    {
                        tagNameBytes[i] = (byte)GetCLGXTagsMaster.getUInt8(tag_id, offset + i);
                    }

                    String tagName = "";

                    try {
                        tagName = new String(tagNameBytes, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if (!tagName.contains(":")){
                        // Display tag name and its dimensions only.
                        valsP.add("Program:" + params[1] + "." + tagName + dimensions);

                        // Display tag name, its dimensions, tagType, IsStructure, tagLength and tagId (comment and uncomment appropriate lines above and below).
                        //valsP.add("Program:" + params[1] + "." + tagName + dimensions + " ; Type = " + tagType + " ; IsStructure = " + IsStructure + " ; Length = " + tagLength + " Bytes ; Id = " + tagId);
                    }

                    offset += tagNameLength;
                }
                else
                {
                    offset += 20;
                    int tagNameLength = GetCLGXTagsMaster.getUInt16(tag_id, offset);
                    offset += 2 + tagNameLength;
                }
            }
        }

        GetCLGXTagsMaster.close(tag_id);

        Collections.sort(valsP);
        valsC.addAll(valsP);
        publishProgress();

        return "Finished";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate();

        GetCLGXTagstaskCallback.UpdateGetCLGXTagsUI(valsC);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    // bitPosition is currently used for bit 12 (systemBit) only but could be used for bit 15 as well (isStructure)
    private Boolean extractInt32Bit(int ReadValue, int bitPosition)
    {
        String bitString = Integer.toBinaryString(ReadValue);
        bitString = String.format("%32s", bitString).replace(' ', '0');

        return bitString.charAt(bitPosition) != '0';
    }
}
