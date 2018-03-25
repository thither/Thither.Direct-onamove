package direct.thither.onamove.pages;
import direct.thither.onamove.App;
import direct.thither.onamove.Globals;
import direct.thither.onamove.R;
import direct.thither.onamove.comm.Comm;
import direct.thither.onamove.viewers.Image;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ProductsResult extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private Globals mGlobals;
    private Comm mComm;
    public Thread t;
    private float last_lat;
    private float last_lng;

    public ProductsResult() {
        mGlobals = App.getInstance().globals;
        mComm = App.getInstance().comm;
        t = new Thread(new Runnable() {
            public void run(){
                Globals.Render render = mGlobals.new Render(){
                    @Override
                    public void data(JSONObject o){set_data(o);};
                    @Override
                    public void data(String s){set_data(s);};
                    @Override
                    public Fragment getFragment() {return ProductsResult.this; }
                    @Override
                    public void set_active(){active();};
                    @Override
                    public void set_timer(){timer();};
                };
                mGlobals.renders.put("1", render);
                bg();
            }
        });
        t.start();
    }
    public ProductsResult getInstance() { return this; }

    List<String> q_str = new ArrayList<>();
    List<JSONObject> q_js = new ArrayList<>();

    private static String data;
    private int c=120;
    private View m_vw;
    private SwipeRefreshLayout m_vw_sr;
    private Timer updates_timer;
    private Timer status_timer;
    private static String title="Results";

    public String get_title() { return ""; }
    public String get_desc() { return ""; }
    public int get_icon() { return R.drawable.ic_sync_black_24dp; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        m_vw = inflater.inflate(R.layout.product_results, container, false);
        //TextView textView = m_vw.findViewById(R.id.section_label);
        //textView.setText("products_result:"+getString(R.string.section_format, c));
        m_vw_sr = m_vw.findViewById(R.id.products_result_swiper);
        m_vw_sr.setOnRefreshListener(this);
        return m_vw;
    }

    public synchronized void active() {
        if(m_vw == null)return;
        //c+=3;
        //if(c>=255)c=120;
        //m_vw.setBackgroundColor(Color.rgb(255,255,255));
        mComm.make_request(mGlobals.get_query_params());
    }

    @Override
    public void onRefresh() {
        mComm.make_request(mGlobals.get_query_params());
    }
    @Override
    public void onPause() {
        super.onPause();
        if(status_timer!=null)  status_timer.cancel();
        if(updates_timer!=null) updates_timer.cancel();
    }
    @Override
    public void onResume() {
        super.onResume();
        timer();
    }

    public void status_track() {
        getActivity().runOnUiThread(new Runnable() {
        public void run() {
            if(mGlobals.last_update!=0){
                mGlobals.get_status_bar().setText(
                        mGlobals.get_param("lat")+", "+mGlobals.get_param("lng")+
                        " Last update was "+
                                DateUtils.getRelativeTimeSpanString(mGlobals.last_update, System.currentTimeMillis(), 0));
            }}});
    }

    public void update_task() {
        if(mGlobals.get_param("rad_only") == null){
            if(updates_timer!=null) updates_timer.cancel();
            return;
        }
        String lat_p = mGlobals.get_param("lat");
        String lng_p = mGlobals.get_param("lng");
        String rad_p = mGlobals.get_param("rad");
        if(lat_p==null || lng_p==null || rad_p==null)return;
        float lat = 180+Float.parseFloat(lat_p)*100000;
        float lng = 180+Float.parseFloat(lng_p)*100000;
        float dif = Float.parseFloat(rad_p)/10;
        if(lat-last_lat<=dif || lng-last_lng<=dif)return;
        last_lat=lat;
        last_lng=lng;
        mComm.make_request(mGlobals.get_query_params());
    }
    public void timer() {
        if(updates_timer!=null) updates_timer.cancel();
        if(status_timer==null){
            status_timer = new Timer();
            status_timer.schedule(new TimerTask(){
                @Override
                public void run() {status_track();}
            }, 1000, 1000);
        }
        if(mGlobals.get_param("rad_only") == null)return;
        updates_timer = new Timer();
        updates_timer.schedule(new TimerTask(){
            @Override
            public void run() {update_task();}
        }, 1000, mGlobals.update_freq*1000);
    }

    public synchronized void bg(){
        timer();
        while (Boolean.TRUE) {
            try {
                wait();
            } catch (Exception e) {}
            render();
        }
    }

    public synchronized void set_data(String s) {
        q_str.add(s);
        notify();
    }
    public synchronized void set_data(JSONObject d) {
        q_js.add(d);
        notify();
    }

    private synchronized void render() {
        Boolean updates = Boolean.FALSE;
        while (!q_js.isEmpty()) {
            render_js(q_js.get(0));
            q_js.remove(0);
            updates = Boolean.TRUE;
        }
        if(updates)
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    status_track();
                    if(m_vw_sr.isRefreshing())m_vw_sr.setRefreshing(false);
                    mGlobals.last_update = System.currentTimeMillis();
                }
            });
        while (!q_str.isEmpty()) {
            render_str(q_str.get(0));
            q_str.remove(0);
        }

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                ProgressBar bar = mGlobals.get_progress_bar();
                if(bar==null)return;
                bar.setVisibility(View.GONE);
                mGlobals.set_progress_bar(null);
            }
        });
    }
    private void render_js(final JSONObject d) {
        try{
            if(!d.has("id") || d.getString("id").equals("store_item_products_similar"))return;
        }catch (Exception e){}

        final RelativeLayout vw = m_vw.findViewById(R.id.products_result);
        final GridLayout container = new GridLayout(vw.getContext());
        container.setPadding(20,50,20,5);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(p);

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                vw.removeAllViews();
                vw.addView(container);
            }});
        try{
            JSONArray items = d.getJSONArray("items");
            JSONObject params = d.getJSONObject("params");

            int c = items.length();
            int row = 0;
            int col = 0;
            int n_col = 1;
            for(int i =0;i<c;i++) {
                    render_products_item_grid(items.getJSONObject(i), container, row, col);
                    if (col == n_col-1) {
                        col = 0;
                        row += 1;
                    } else col += 1;
            }

            JSONObject vw_d = d.getJSONObject("vw");
            if(vw_d.has("page_next")){
                int cur_p=1;
                int nxt_p = Integer.parseInt(vw_d.getString("page_next"));
                if(nxt_p>2)
                    cur_p=nxt_p-1;
                set_pagination(cur_p, nxt_p, container, row);
            }

        }catch (Exception e){render_str(e.getMessage());}
    }

    private void render_products_item_grid(JSONObject d, final GridLayout container, final int row, final int col){
        int max_width = container.getWidth();
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

        LinearLayout item_vw = new LinearLayout(container.getContext());
        item_vw.setLayoutParams(p);
        item_vw.setOrientation(LinearLayout.VERTICAL);
        item_vw.setBackgroundColor(Color.rgb(255,255,255));
        item_vw.setPadding(3,5,3,3);

        final LinearLayout holder = new LinearLayout(container.getContext());
        holder.setOrientation(LinearLayout.HORIZONTAL);
        holder.setBackground(getResources().getDrawable(R.drawable.products_item_wrap));
        holder.addView(item_vw);
        holder.setClickable(Boolean.TRUE);

        try {
            set_item_actions(holder,
                    "view=store_info&S_S_ID="+d.getJSONObject("store").getString("store_id"));
        }catch (Exception e){}

        TextView i_name = new TextView(item_vw.getContext());
        i_name.setTextSize(16);
        i_name.setBackgroundColor(Color.rgb(250,250,250));
        i_name.setPadding(10, 10, 10, 10);
        i_name.setTypeface(i_name.getTypeface(), Typeface.BOLD);
        i_name.setLayoutParams(p);
        i_name.setWidth(m_vw.getWidth());

        item_vw.addView(i_name);

        TextView i_desc = new TextView(item_vw.getContext());
        i_desc.setTextSize(14);
        i_desc.setPadding(0,10,0,0);
        //i_desc.setWidth(max_width-100);

        TextView i_pr_s = new TextView(item_vw.getContext());
        TextView i_pr_u = null;

        LinearLayout item_d = new LinearLayout(container.getContext());
        item_d.setOrientation(LinearLayout.HORIZONTAL);
        item_d.setPadding(0,20,0,20);
        item_d.setLayoutParams(p);
        item_vw.addView(item_d);

        LinearLayout item_d_start = new LinearLayout(container.getContext());
        item_d_start.setOrientation(LinearLayout.VERTICAL);
        item_d.addView(item_d_start);

        LinearLayout item_info = new LinearLayout(container.getContext());
        item_info.setOrientation(LinearLayout.VERTICAL);
        item_info.setLayoutParams(p);
        item_d.addView(item_info);

        try{
            JSONObject data = d.getJSONObject("d");
            i_name.setText(data.getString("n"));
            i_desc.setText(data.getString("d"));


            if(data.has("i")) {
                JSONArray imgs = data.getJSONArray("i");
                int c = imgs.length();
                Image i_img ;
                for(int i =0;i<c;i++) {
                    i_img = new Image(item_vw.getContext(),
                            "https://thither.direct/images/"+imgs.getString(i),
                            "?sz=", 240,240);
                    i_img.setPadding(5,5,5,5);
                    item_d_start.addView(i_img);
                }
            }

            LinearLayout item_pr = new LinearLayout(container.getContext());
            item_pr.setOrientation(LinearLayout.HORIZONTAL);
            item_pr.setGravity(Gravity.END);
            item_pr.setPadding(15,0,15,0);
            item_info.addView(item_pr);
            item_info.addView(i_desc);

            JSONObject pr = data.getJSONObject("pr");
            i_pr_s.setText(pr.getString("price")+" "+pr.getString("store_cur"));
            if(pr.has("price_user")){
                i_pr_u = new TextView(item_vw.getContext());
                i_pr_u.setText(" "+pr.getString("price_user")+" "+pr.getString("price_cur"));
                i_pr_u.setTextSize(16);
                i_pr_u.setTypeface(i_pr_u.getTypeface(), Typeface.BOLD);

                i_pr_s.setTextSize(13);
                item_pr.addView(i_pr_s);
                item_pr.addView(i_pr_u);
            }else{
                i_pr_s.setTextSize(16);
                i_pr_s.setTypeface(i_pr_s.getTypeface(), Typeface.BOLD);
                item_pr.addView(i_pr_s);
            }

        }catch (Exception e){i_pr_s.setText(e.getMessage());}

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                GridLayout.LayoutParams p = new GridLayout.LayoutParams(
                        GridLayout.spec(row), GridLayout.spec(0));
                p.setMargins(30,30,30,30);
                container.addView(holder, p);
            }
        });
    }

    private void set_item_actions(final LinearLayout elem, final String q_param){
        elem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mComm.make_request(q_param);
            }});
    }

    private void set_pagination(int cur_p, int nxt_p, final GridLayout container, final int row){

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f);
        p.setMargins(3,0,3,0);
        final LinearLayout vw_paging = new LinearLayout(container.getContext());
        vw_paging.setOrientation(LinearLayout.HORIZONTAL);
        vw_paging.setPadding(0,5,0,200);
        vw_paging.setGravity(View.TEXT_ALIGNMENT_CENTER);
        Button btn;
        for(int n=(cur_p>2?cur_p-2:1); n<nxt_p+2;n++ ){
            btn = new Button(vw_paging.getContext());
            btn.setText(Integer.toString(n));
            btn.setLayoutParams(p);
            btn.setPadding(3,2,3,2);
            btn.setTextSize(13);
            btn.setMinHeight(24);
            btn.setMinWidth(24);
            if(cur_p==n) {
                btn.setBackgroundColor(Color.argb(48,77,222,70));
            }else{
                btn.setBackground(getResources().getDrawable(R.drawable.main_menu_item_wrap));
                btn.setClickable(Boolean.TRUE);
                set_paging_actions(btn, n);
            }
            vw_paging.addView(btn);
        }

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                GridLayout.LayoutParams p = new GridLayout.LayoutParams(
                        GridLayout.spec(row), GridLayout.spec(0));
                p.setMargins(30,30,30,30);
                container.addView(vw_paging, p);
            }});
    }
    private void set_paging_actions(final Button btn, final int p){

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                btn.setBackground(getResources().getDrawable(R.color.colorLinkClick));
                process_bar();
                mComm.make_request(mGlobals.get_query_params()+"&page="+Integer.toString(p));
            }});
    }

    private void render_str(final String s) {
        if(s==null)return;
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                TextView textView = m_vw.findViewById(R.id.section_label);
                textView.setText("Error: "+s);
            }
        });
    }

    private void process_bar(){

        if(mGlobals.get_progress_bar()==null)
            mGlobals.set_progress_bar(getActivity().findViewById(R.id.progressBar));
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                ProgressBar bar = mGlobals.get_progress_bar();
                if(bar==null)return;
                bar.setIndeterminate(true);
                bar.setVisibility(View.VISIBLE);
            }
        });
    }
}