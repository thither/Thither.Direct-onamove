package direct.thither.onamove.pages;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import direct.thither.onamove.App;
import direct.thither.onamove.R;
import direct.thither.onamove.properties.PropsHolder;
import direct.thither.onamove.properties.Render;

public class WebKitPage extends Fragment{

    private View vw;

    private PropsHolder m_props;
    private Context m_ctx;

    private WebView m_webkit_vw;
    private float last_lat;
    private float last_lng;

    private Timer updates_timer;
    private Timer status_timer;
    private long last_update=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vw = inflater.inflate(R.layout.webkitpage, container, false);
        m_webkit_vw = vw.findViewById(R.id.webviewer);

        m_webkit_vw.setVisibility(View.VISIBLE);
        m_webkit_vw.getSettings().setJavaScriptEnabled(true);

        m_webkit_vw.setWebViewClient(new WebViewClient(){
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(m_ctx, description, Toast.LENGTH_SHORT).show();
            }
            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
            @Override
            public void onPageFinished(WebView view, String url) {
            }
            @Override
            public void onLoadResource(WebView view, String req) {
                if(!req.startsWith("https://thither.direct/json"))return;
                Uri uri = Uri.parse(req);
                Toast.makeText(m_ctx, req, Toast.LENGTH_SHORT).show();
                last_update = System.currentTimeMillis();
                m_props.apply_pref();
            }

        });
        set_timers();
        return m_webkit_vw;
    }

    public WebKitPage() {
        m_props = App.getInstance().props;
        Render render = new Render(){
            @Override
            public Fragment getFragment() {return WebKitPage.this; }
            @Override
            public void set_timer(){set_timers();};
        };
        m_props.renders.put("3", render);
    }

    public void refresh(){
        ((Activity) m_ctx).runOnUiThread(new Runnable() {
            public void run() {
                m_webkit_vw.loadUrl(
                        "https://thither.direct/"+m_props.lang+"/?"+
                                m_props.params.get("main").get_query_params());
            }});
    }

    private void status_track() {
        ((Activity) m_ctx).runOnUiThread(new Runnable() {
            public void run() {
                if(last_update!=0){
                    m_props.get_status_bar().setText(
                            m_props.params.get("main").get_param("lat")+", "
                                    +m_props.params.get("main").get_param("lng")+
                                    " Last update was "+
                                    DateUtils.getRelativeTimeSpanString(last_update, System.currentTimeMillis(), 0));
                }}});
    }

    private void update_task() {
        if(m_props.params.get("main").get_param("rad_only") == null){
            if(updates_timer!=null) updates_timer.cancel();
            return;
        }
        String lat_p = m_props.params.get("main").get_param("lat");
        String lng_p = m_props.params.get("main").get_param("lng");
        long dif = m_props.get_rad_distance_for_update();
        if(lat_p==null || lng_p==null || dif==0)return;
        float lat = (180+Float.parseFloat(lat_p))*100000;
        float lng = (180+Float.parseFloat(lng_p))*100000;
        if((((lat-last_lat)<0?last_lat-lat:lat-last_lat)+
                ((lng-last_lng)<0?last_lng-lng:lng-last_lng))<dif)
            return;
        last_lat=lat;
        last_lng=lng;
        refresh();
    }

    public void set_timers() {
        if(updates_timer!=null) updates_timer.cancel();
        if(status_timer==null){
            status_timer = new Timer();
            status_timer.schedule(new TimerTask(){
                @Override
                public void run() {status_track();}
            }, 1000, 1000);
        }
        if(m_props.params.get("main").get_param("rad_only") == null)return;

        updates_timer = new Timer();
        updates_timer.schedule(new TimerTask(){
            @Override
            public void run() {update_task();}
        }, 1000, m_props.update_freq*1000);
    }

    public void stop_timers() {
        if(updates_timer!=null) updates_timer.cancel();
        if(status_timer!=null) status_timer.cancel();
    }

}
