package direct.thither.onamove.pages;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import direct.thither.onamove.properties.PropsHolder;

import direct.thither.onamove.App;
import direct.thither.onamove.R;


public class SearchSettings {
    private View vw;
    private RelativeLayout search_settings=null;

    private PropsHolder m_props;
    private Context m_ctx;

    public SearchSettings(Context ctx) {
        m_props = App.getInstance().props;;
        m_ctx = ctx;

        vw = ((Activity)m_ctx).getWindow().getDecorView();

        List<String> lengths = new ArrayList<>();
        for (int i=4;i<=24; i+=4) { lengths.add(Integer.toString(i));}
        Spinner length = vw.findViewById(R.id.length);
        ArrayAdapter<String> length_adapter = new ArrayAdapter<>(vw.getContext(), R.layout.setting_selector, lengths);
        length.setAdapter(length_adapter);
        length.setSelection((Integer.parseInt(m_props.params.get("main").get_param("length"))/4)-1);
        length.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                m_props.params.get("main").set_param("length", Integer.toString((position+1)*4));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Spinner currency = vw.findViewById(R.id.currency);
        String cur_curr = m_props.params.get("main").get_param("curr");
        int n=-1;
        for(String c:m_ctx.getResources().getStringArray(R.array.currency_codes)){
            n++;
            if(!cur_curr.equals(c))continue;
            currency.setSelection(n);
            break;
        }
        currency.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String[] codes = m_ctx.getResources().getStringArray(R.array.currency_codes);
                m_props.params.get("main").set_param("curr", codes[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Switch rad_only = vw.findViewById(R.id.rad_only);
        if(m_props.params.get("main").get_param("rad_only")!=null) rad_only.setChecked(Boolean.TRUE);
        rad_only.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Boolean changed = Boolean.FALSE;
                if(!isChecked && m_props.params.get("main").get_param("rad_only")!=null)
                    changed = Boolean.TRUE;
                else if(isChecked && m_props.params.get("main").get_param("rad_only")==null)
                    changed = Boolean.TRUE;

                if(isChecked) {
                    m_props.params.get("main").set_param("rad_only", "1");
                    m_props.renders.get("2").set_timers();
                }else {
                    m_props.params.get("main").unset_param("rad_only");
                    m_props.renders.get("2").stop_timers();
                }
                if(changed) {
                    on_rad_options();
                    m_props.renders.get("1").refresh();
                }
            }
        });
        on_rad_options();
    }
    private void on_rad_options(){

        if(m_props.params.get("main").get_param("rad_only")!=null) {
            vw.findViewById(R.id.on_radius_container).setVisibility(View.VISIBLE);
            vw.findViewById(R.id.update_freq_container).setVisibility(View.VISIBLE);
            vw.findViewById(R.id.sound_alert_container).setVisibility(View.VISIBLE);
        }else{
            vw.findViewById(R.id.on_radius_container).setVisibility(View.GONE);
            vw.findViewById(R.id.update_freq_container).setVisibility(View.GONE);
            vw.findViewById(R.id.sound_alert_container).setVisibility(View.GONE);
            return;
        }

        final TextView on_radius_holder = vw.findViewById(R.id.on_radius_holder);
        SeekBar on_radius = vw.findViewById(R.id.on_radius);
        on_radius_holder.setText(m_props.params.get("main").get_param("rad")+"m");
        on_radius.setProgress(Integer.parseInt(m_props.params.get("main").get_param("rad")));

        on_radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onStartTrackingTouch(SeekBar elem){}
            @Override
            public void onStopTrackingTouch(SeekBar elem){}
            @Override
            public void onProgressChanged(SeekBar elem, int v, boolean state){
                if(v<10)v=10;
                m_props.params.get("main").set_param("rad", v);
                on_radius_holder.setText(Integer.toString(v)+"m");
                m_props.renders.get("1").refresh();
            }});

        SeekBar update_freq = vw.findViewById(R.id.update_freq);
        update_freq.setProgress(Integer.parseInt(Long.toString(m_props.update_freq)));
        final long old_freq = m_props.update_freq;
        final TextView update_freq_holder = vw.findViewById(R.id.update_freq_holder);
        update_freq_holder.setText(Long.toString(old_freq)+"s");
        update_freq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onStartTrackingTouch(SeekBar elem){}
            @Override
            public void onStopTrackingTouch(SeekBar elem){
                if(old_freq != m_props.update_freq && m_props.get_location()!=null){
                    m_props.get_location().set_location();
                }
            }
            @Override
            public void onProgressChanged(SeekBar elem, int v, boolean state){
                m_props.update_freq = (long)v;
                m_props.apply_pref("update_freq", m_props.update_freq);
                update_freq_holder.setText(Integer.toString(v)+"s");
                m_props.renders.get("1").set_timers();
            }});

        Switch sound_alert = vw.findViewById(R.id.sound_alert);
    }
}
