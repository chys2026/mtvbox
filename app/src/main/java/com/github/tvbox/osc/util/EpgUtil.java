package com.github.tvbox.osc.util;

import android.content.res.AssetManager;

import android.util.Log;

import com.github.tvbox.osc.base.App;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;

public class EpgUtil {

    private static final String TAG = "EpgUtil";

    private static JsonObject epgDoc = null;
    private static HashMap<String, JsonObject> epgHashMap = new HashMap<>();

    public static void init() {
        if(epgDoc != null)
            return;
        try {
            AssetManager assetManager = App.getInstance().getAssets();
            try (InputStreamReader inputStreamReader = new InputStreamReader(assetManager.open("epg_data.json"),"UTF-8");
                 BufferedReader br = new BufferedReader(inputStreamReader)) {
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = br.readLine())!=null){
                    builder.append(line);
                }
                if(!builder.toString().isEmpty()){
                    epgDoc =  new Gson().fromJson(builder.toString(), (Type)JsonObject.class);
                    for (JsonElement opt : epgDoc.get("epgs").getAsJsonArray()) {
                        JsonObject obj = (JsonObject) opt;
                        String name = obj.get("name").getAsString().trim();
                        String[] names  = name.split(",");
                        for (String string : names) {
                            epgHashMap.put(string,obj);
                        }
                    }
                    return;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading EPG data", e);
        }
    }

    public static String[] getEpgInfo(String channelName) {
        try {
            if(epgHashMap.containsKey(channelName)){
                JsonObject obj = epgHashMap.get(channelName);
                return new String[] {
                        obj.get("logo").getAsString(),
                        obj.get("epgid").getAsString()
                };
            }
        }catch (Exception ex) {
            Log.e(TAG, "Error getting EPG info", ex);
        }
        return null;
    }
}
