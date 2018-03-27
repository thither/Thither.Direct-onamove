package direct.thither.onamove.properties;

import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Currency;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import direct.thither.onamove.receivers.LocationReceiver;

public class PropsHolder {


    public ConcurrentHashMap<String, QueryParams> params;
    public ConcurrentHashMap<String, Render> renders;
    public PropsHolder() {
        params = new ConcurrentHashMap<String, QueryParams>();
        renders = new ConcurrentHashMap<String, Render>();
    }
    public synchronized void stop_renders(){
        for (ConcurrentHashMap.Entry<String, Render> entry : renders.entrySet()){
            entry.getValue().stop_timers();
        }
    }
    public synchronized void start_renders(){
        for (ConcurrentHashMap.Entry<String, Render> entry : renders.entrySet()){
            entry.getValue().set_timers();
        }
    }

    public boolean internet = Boolean.FALSE;

    private LocationReceiver m_location = null;
    public synchronized void set_location(LocationReceiver location){
        m_location = location;
    }
    public synchronized LocationReceiver get_location(){
        return  m_location;
    }
    public synchronized long get_rad_distance_for_update(){
        return Math.round(Integer.parseInt(params.get("main").get_param("rad"))/10);
    }


    private TextView m_status_bar;
    public synchronized void set_status_bar(View bar){
        m_status_bar = (TextView)bar;
    }
    public synchronized TextView get_status_bar(){
        return m_status_bar;
    }




    public long update_freq = 60;
    public String lang = "";

    private SharedPreferences m_preferences;
    public synchronized void load_preferences(SharedPreferences pref){
        m_preferences = pref;
        params.put("main", new QueryParams(pref.getString("main", null)));
        QueryParams p = params.get("main");
        if(p.get_param("length") == null){
            p.set_param("length", "4");
            p.set_param("rad", "1000");
            p.set_param("curr", Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        }

        update_freq = pref.getLong("update_freq", 60);
        lang = pref.getString("lang", Locale.getDefault().getLanguage().toLowerCase());
        switch (lang){
            case "iw":
                lang = "he";
        }
    }
    public synchronized void apply_pref(){
        for (ConcurrentHashMap.Entry<String, QueryParams> entry : params.entrySet()){
            apply_pref(entry.getKey(), entry.getValue().toString());
        }
    }
    public synchronized void apply_pref(String k){
        m_preferences.edit().putString(k, params.get(k).toString()).apply();
    }
    public synchronized void apply_pref(String k, String v){
        m_preferences.edit().putString(k, v).apply();
    }
    public synchronized void apply_pref(String k, Long v){
        m_preferences.edit().putLong(k, v).apply();
    }


    public ProgressBar m_progress_bar;
    public synchronized void set_progress_bar(View bar){
        m_progress_bar = (ProgressBar)bar;
    }
    public synchronized ProgressBar get_progress_bar(){
        return m_progress_bar;
    }

    public TabLayout m_tabs_bar;
    public synchronized void set_tabs_bar(View bar){
        m_tabs_bar = (TabLayout)bar;
    }
    public synchronized TabLayout get_tabs_bar(){
        return m_tabs_bar;
    }


}
