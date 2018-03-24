package direct.thither.onamove.comm;

import android.text.format.DateUtils;

import direct.thither.onamove.App;
import direct.thither.onamove.Globals;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Protocol;

public class Comm {
    Globals mGlobals;
    public Comm() {
       mGlobals = App.getInstance().globals;;
    }

    public void make_request(){make_request("");}
    public void make_request(final String q) {
        new Thread(new Runnable() {
        @Override
        public void run(){
            List<Protocol> protocols = new ArrayList<>();
            protocols.add(Protocol.HTTP_2);
            protocols.add(Protocol.HTTP_1_1);
            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .protocols(protocols)
                        .build();
                Request request = new Request.Builder()
                        .url("https://thither.direct/json/"+mGlobals.lang+"?" +q )
                        .header("Accept", "text/html")
                        .header("Accept-Encoding", "deflate")
                        .build();
                try {
                    process_response(
                            new JSONObject(
                                    decompress(client.newCall(request).execute().body().bytes())));
                    mGlobals.apply_pref("qParams");
                } catch (Exception e) {
                    process_response(e);
                }
            } catch (Exception e) {
                process_response(e);
            }
        }
        }).start();
    }
    private void process_response(JSONObject obj_rsp){
        Object o;
        String k;
        Iterator itr = obj_rsp.keys();
        while(itr.hasNext()) {
            try {
                o = itr.next();
                k = o.toString();
                switch (k){
                    case "dataTypes":
                        process_data_types(obj_rsp.getJSONArray(k));
                        break;
                }
            } catch (JSONException e) { process_response(e); }
        }
    }
    private void process_data_types(JSONArray jsonArray){
        JSONObject o;
        JSONArray arr;
        int dt_c, d_c;
        int i1,i2;
        String tt = "";
        try {
            dt_c = jsonArray.length();
            for(i1=0 ; i1< dt_c; i1++){
                o = jsonArray.getJSONObject(i1);
                switch (o.getString("name")) {
                    case "products":{
                        tt = "products-data-products";
                        arr = o.getJSONObject("data").getJSONArray("products");
                        d_c = arr.length();
                        for(i2=0 ; i2< d_c; i2++) {
                            process_response("1", arr.getJSONObject(i2));
                        }
                        break;
                    }
                    case "menu":{
                        tt = "menu-data-menu"+o.getJSONObject("data").toString();
                        arr = o.getJSONObject("data").getJSONArray("menu");
                        d_c = arr.length();
                        for(i2=0 ; i2< d_c; i2++) {
                            process_response("0", arr.getJSONObject(i2));
                        }
                        break;
                    }
                    case "store":{
                        tt = "products-data-products";
                        arr = o.getJSONObject("data").getJSONArray("store");
                        d_c = arr.length();
                        for(i2=0 ; i2< d_c; i2++) {
                            process_response("2", arr.getJSONObject(i2));
                        }
                        break;
                    }
                }
            }
        } catch (JSONException e) { process_response(e.toString()+tt); }

    }
    public void process_response(String renderId, JSONObject o){
        mGlobals.renders.get(renderId).data(o);
    }
    public void process_response(Exception e){
        mGlobals.renders.get("1").data(e.toString());
    }
    public void process_response(String s){
        mGlobals.renders.get("1").data(s);
    }


    public static String decompress(byte[] compressed) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        InflaterInputStream gis = new InflaterInputStream(bis);
        BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        gis.close();
        bis.close();
        return sb.toString();
    }
}
