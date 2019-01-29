package direct.thither.onamove.comm;

import direct.thither.onamove.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.InflaterInputStream;

import direct.thither.onamove.properties.PropsHolder;
import okhttp3.Request;

public class Comm {
    private PropsHolder m_props;

    public Comm() {
        m_props = App.getInstance().props;
    }

    public void make_request(){make_request("");}
    public void make_request(final String q) {
        new Thread(new Runnable() {
        @Override
        public void run(){
            try {
                Request request = new Request.Builder()
                        .url("https://thither.direct/json/"+m_props.lang+"?app=onamove&flw=1&" +q )
                        .header("Accept", "text/html")
                        .header("Accept-Encoding", "deflate")
                        .build();
                try {
                    process_response(
                            new JSONObject(
                                    decompress(
                                            m_props.get_client_http().newCall(request)
                                                    .execute().body().bytes()
                                    )));
                    m_props.apply_pref();
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
        m_props.renders.get(renderId).data(o);
    }
    public void process_response(Exception e){
        m_props.renders.get("1").data(e.toString());
    }
    public void process_response(String s){
        m_props.renders.get("1").data(s);
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
