package com.github.tvbox.osc.util;

import android.util.Log;

import android.content.res.AssetManager;
import com.github.tvbox.osc.base.App;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Hashtable;

public class EpgNameFuzzyMatch {
    private static final String TAG = "EpgNameFuzzyMatch";

    private static JsonObject epgNameDoc = null;
    private static Hashtable hsEpgName = new Hashtable();

    public static void init() {
        if(epgNameDoc != null)
            return;

        try {
            AssetManager assetManager = App.getInstance().getAssets();
            try (InputStreamReader inputStreamReader = new InputStreamReader(assetManager.open("Roinlong_Epg.json"),"UTF-8");
                 BufferedReader br = new BufferedReader(inputStreamReader)) {
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = br.readLine())!=null){
                    builder.append(line);
                }
                if(!builder.toString().isEmpty()){
                    JsonObject  jsonObj =  new Gson().fromJson(builder.toString(), (Type)JsonObject.class);
                    epgNameDoc = jsonObj;
                    hasAddData(epgNameDoc);
                    return;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading EPG name data", e);
        }

        //上述两种途径都失败后,读取网络自定义文件中的内容
        GetRequest<String> request = OkGo.<String>get("http://www.baidu.com/maotv/epg.json");
        request.headers("User-Agent", UA.random());
        request.execute(new AbsCallback<String>() {
            @Override
            public void onSuccess(Response<String> response) {
                JSONObject returnedData = new JSONObject();
                try {
                    String pageStr = response.body();
                    JsonObject infoJson = new Gson().fromJson(pageStr, (Type)JsonObject.class);
                    epgNameDoc = infoJson;
                    hasAddData(epgNameDoc);
                    return;
                } catch (Exception ex) {
                    Log.e("EpgNameFuzzyMatch", "error", ex);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }

            @Override
            public String convertResponse(okhttp3.Response response) throws Throwable {
                if (response.body() == null) return "";
                return response.body().string();
            }
        });
    }




    public static void hasAddData(JsonObject epgNameDoc){
        for (JsonElement opt : epgNameDoc.get("epgs").getAsJsonArray()) {
            JsonObject obj = (JsonObject) opt;
            String name = obj.get("name").getAsString().trim();
            String[] names  = name.split(",");
            for (String string : names) {
                hsEpgName.put(string,obj);
            }
        }
    }

    public static JsonObject getEpgNameInfo(String channelName) {

       if(hsEpgName.containsKey(channelName)){
           JsonObject obj = (JsonObject)hsEpgName.get(channelName);
           return  obj;
       }
       return null;
    }




}
