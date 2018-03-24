package direct.thither.onamove.pages;
import direct.thither.onamove.App;
import direct.thither.onamove.Globals;
import direct.thither.onamove.R;
import direct.thither.onamove.comm.Comm;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SearchMenu extends Fragment {
    private Globals mGlobals;
    private Comm mComm;
    public Thread t;

    public SearchMenu() {
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
                    public Fragment getFragment() {
                        return SearchMenu.this;
                    }
                    @Override
                    public void set_active(){active();};
                };
                mGlobals.renders.put("0", render);
                bg();
            }
        });
        t.start();
    }
    public SearchMenu getInstance() { return this; }

    List<String> q_str = new ArrayList<>();
    List<JSONObject> q_js = new ArrayList<>();

    private int c=120;
    private View m_vw;

    public static String title="Search";
    public String get_title() { return title; }
    public String get_desc() { return ""; }
    public int get_icon() { return R.drawable.ic_search_black_24dp; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        m_vw =  inflater.inflate(R.layout.fragment_main, container, false);
        //TextView textView = m_vw.findViewById(R.id.section_label);
        //textView.setText("SearchMenu:"+getString(R.string.section_format, c));
        //mGlobals.set_param("k111", "v111");
        mComm.make_request(mGlobals.get_query_params());
        return m_vw;
    }

    @Override
    public void onResume() {
        super.onResume();
        mComm.make_request(mGlobals.get_query_params());
    }

    public synchronized void active() {
        if(m_vw == null)return;
        //c+=5;
        //if(c>=255)c=120;
        //mGlobals.set_param("k"+Integer.toString(c), c);
        //set_data(mGlobals.get_query_params());
        //m_vw.setBackgroundColor(Color.rgb(c,c,c));
        //getActivity().runOnUiThread(new Runnable() {
        //   public void run() {
        //        TextView textView = m_vw.findViewById(R.id.section_label);
        //        textView.setText("SearchMenu:"+getString(R.string.section_format, c));
        //
        //    }
        //});
    }

    public synchronized void bg(){
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
        while(!q_js.isEmpty())  { render_js(q_js.get(0));   q_js.remove(0);  }
        while(!q_str.isEmpty()) { render_str(q_str.get(0)); q_str.remove(0); }


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
            switch (d.getString("id")) {

                case "mainside-menu":{
                    JSONArray items = d.getJSONArray("items");
                    JSONObject params = d.getJSONObject("params");
                    render_menu_mainside_items(items, params, m_vw.findViewById(R.id.main_menu));
                    break;
                }
                case "menu":{
                    break;
                }
            }
       }catch (Exception e){}
    }

    private void render_menu_mainside_items(JSONArray items, final JSONObject params, final View vw) {
        final GridLayout container = new GridLayout(vw.getContext());
        container.setPadding(0,0,0,100);
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (vw instanceof RelativeLayout) {
                    ((RelativeLayout) vw).removeAllViews();
                    ((RelativeLayout) vw).addView(container);
                } else if (vw instanceof LinearLayout) {
                    container.setPadding(0,0,0,10);
                    ((LinearLayout) vw).addView(container);
                }
            }});
        //Boolean active_g = Boolean.FALSE;
        int c = items.length();
        for(int i =0;i<c;i++) {
            try{
                render_menu_mainside_group(items.getJSONObject(i), params, container);
            }catch (final JSONException e){
            }
        }
    }
    private void render_menu_mainside_group(JSONObject d, final JSONObject params, final GridLayout container){
        final LinearLayout menuGroup = new LinearLayout(container.getContext());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        menuGroup.setLayoutParams(p);
        menuGroup.setOrientation(LinearLayout.VERTICAL);
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                container.addView(menuGroup,
                        new GridLayout.LayoutParams(GridLayout.spec(container.getRowCount()), GridLayout.spec(0)));
            }});

        try{
            if (d.has("header") && !d.has("items")) {
                render_menu_mainside_header(Html.fromHtml(d.getString("header")).toString(), menuGroup);

                //render_menu_mainside_header((d.has("name")?(d.getString("name")):d.getString("header")), menuGroup);
            }else if (d.has("title")) {
                render_menu_mainside_item(d, menuGroup, (d.has("active") && d.getBoolean("active")));
            }
            if (d.has("items")) {
                if(d.has("active") && d.getBoolean("active")) {
                    menuGroup.setBackground(getResources().getDrawable(R.drawable.main_menu_selected));
                }
                if(d.has("subHeader")){
                    final LinearLayout menuSubGroup = new LinearLayout(container.getContext());
                    LinearLayout.LayoutParams p_sub = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                    menuSubGroup.setPadding(25,0,25,15);
                    //menuSubGroup.setBackgroundColor(Color.argb(240,240,240,240));
                    menuSubGroup.setLayoutParams(p_sub);
                    menuSubGroup.setOrientation(LinearLayout.VERTICAL);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            menuGroup.addView(menuSubGroup);
                        }});
                    render_menu_mainside_items(d.getJSONArray("items"), params, menuSubGroup);
                }else{
                    render_menu_mainside_items(d.getJSONArray("items"), params, menuGroup);
                }
            }
        }catch (final JSONException e){
            getActivity().runOnUiThread(new Runnable() {
            public void run() {
                TextView textView = new TextView(menuGroup.getContext());
                textView.setText(e.getMessage());
                menuGroup.addView(textView);
            }
        });
        }
    }
    private void render_menu_mainside_header(final  String h, final LinearLayout menuGroup){
        final TextView txt = new TextView(menuGroup.getContext());
        txt.setPadding(5, 8, 5, 8);
        txt.setTextSize(16);
        txt.setText(h);
        txt.setWidth(m_vw.getWidth());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0,4,0,2);
        txt.setLayoutParams(p);
        txt.setBackground(getResources().getDrawable(R.drawable.main_menu_header_wrap));

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                menuGroup.addView(txt);
            }
        });
    }
    private void render_menu_mainside_item(JSONObject d, final LinearLayout menuGroup, Boolean active){
        final TextView txt = new TextView(menuGroup.getContext());
        try{ txt.setText(d.getString("title"));}catch (Exception e){}
        txt.setWidth(m_vw.getWidth());
        txt.setPadding(15, 8, 15, 8);
        txt.setTextSize(15);
        txt.setTypeface(txt.getTypeface(), Typeface.BOLD);
        txt.setTextColor(getResources().getColorStateList(R.color.mainmeanu_item_text));
        if(active)
            txt.setBackground(getResources().getDrawable(R.drawable.main_menu_item_selected));
        else
            txt.setBackground(getResources().getDrawable(R.drawable.main_menu_item_wrap));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0,2,0,2);
        txt.setLayoutParams(p);

        final JSONObject item_p = get_item_params(d);
        txt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                txt.setBackground(getResources().getDrawable(R.color.colorLinkClick));
                set_item_params(item_p);
            }});

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                menuGroup.addView(txt);
            }
        });
    }
    private JSONObject get_item_params(JSONObject d) {
        try{
        if (d.has("params")){
            return d.getJSONObject("params");
        }
        }catch (final JSONException e){}
        return null;
    }
    private void set_item_params(JSONObject d){
        if(d==null)return;
        Boolean updates = Boolean.FALSE;
        if(d.has("add")){
            try{
                JSONObject add = d.getJSONObject("add");
                String k;
                Iterator itr = add.keys();
                while(itr.hasNext()) {
                    try {
                        k = itr.next().toString();
                        mGlobals.set_param(k, add.getString(k));
                        updates = Boolean.TRUE;
                    } catch (JSONException e) { }
                }
            }catch (final JSONException e){}
        }
        if(d.has("del")){
            try{
                JSONArray del = d.getJSONArray("del");
                int c = del.length();
                for(int i =0;i<c;i++) {
                    mGlobals.unset_param(del.getString(i));
                    updates = Boolean.TRUE;
                }
            }catch (final JSONException e){}
        }

        if(updates){
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
            mComm.make_request(mGlobals.get_query_params());
        }
    }

    private void render_str(String s) {
        if(s==null)return;
    }

}