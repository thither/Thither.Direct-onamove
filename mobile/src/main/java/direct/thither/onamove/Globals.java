package direct.thither.onamove;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import direct.thither.onamove.receivers.LocationReceiver;

public class Globals {

    public boolean internet = Boolean.FALSE;
    public LocationReceiver location;

    public ConcurrentHashMap<String, Render> renders;
    private JSONObject qParams;
    public long update_freq = 60;
    public String lang = "";

    public TextView m_status_bar;
    public ProgressBar m_progress_bar;
    public SharedPreferences m_preferences;

    public long last_update=0;

    public class Render {
        public Render (){}
        //public Fragment getFragment() { Fragment holder=null; return holder; }
        public void data(String s){}
        public void data(JSONObject s){}
        public void set_active(){}
        public void set_timer(){}
        public void run(String s){}
    }

    public Globals() {
        renders = new ConcurrentHashMap<String, Render>();
    }
    public synchronized void set_status_bar(View bar){
        m_status_bar = (TextView)bar;
    }
    public synchronized TextView get_status_bar(){
        return m_status_bar;
    }

    public synchronized void set_progress_bar(View bar){
        m_progress_bar = (ProgressBar)bar;
    }
    public synchronized ProgressBar get_progress_bar(){
        return m_progress_bar;
    }

    public synchronized void load_preferences(SharedPreferences pref){
        m_preferences = pref;
        String v=null;
        try {
            v = pref.getString("qParams", null);
            if(v!=null) qParams = new JSONObject(v);
        }catch (Exception e){ }
        if(v==null){
            qParams = new JSONObject();
            set_param("length", "4");
            set_param("rad", "1000");
            set_param("curr", Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        }

        update_freq = pref.getLong("update_freq", 60);
        Locale conf_loc = Locale.getDefault();
        lang = pref.getString("lang", conf_loc.getLanguage().toLowerCase());
        switch (lang){
            case "iw":
                lang = "he";
        }
    }
    public synchronized void apply_pref(String k){
        switch (k){
            case "qParams":
                apply_pref(k, qParams.toString());
        }
    }
    public synchronized void apply_pref(String k, String v){
        m_preferences.edit().putString(k, v).apply();
    }
    public synchronized void apply_pref(String k, Long v){
        m_preferences.edit().putLong(k, v).apply();
    }

    public synchronized void unset_param(String k){ qParams.remove(k);}
    public synchronized void set_param(String k, String v){
        try {qParams.put(k, v); } catch (JSONException e) { }
    }
    public void set_param(String k, int v){     set_param(k, String.valueOf(v)); }
    public void set_param(String k, boolean v){ set_param(k, String.valueOf(v)); }
    public void set_param(String k, long v){    set_param(k, String.valueOf(v)); }
    public void set_param(String k, double v){  set_param(k, String.valueOf(v)); }
    public synchronized String get_param(String k){
        try {return qParams.getString(k); } catch (JSONException e) { }
        return null;
    }

    public synchronized String get_query_params(){
        String k;
        StringBuilder output = new StringBuilder();
        output.append("app=onamove&flw=1&");
        Object p;
        Iterator itr = qParams.keys();
        while(itr.hasNext()) {
            try {
                p = itr.next();
                k = p.toString();
                output.append(k).append("=").append(qParams.getString(k)).append("&");
            } catch (JSONException e) { }
        }
        return output.toString();
    }


}

