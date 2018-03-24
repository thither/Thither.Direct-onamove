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

import java.util.ArrayList;
import java.util.List;

import direct.thither.onamove.App;
import direct.thither.onamove.Globals;
import direct.thither.onamove.R;
import direct.thither.onamove.comm.Comm;


public class SearchSettings {
    private View vw;
    private RelativeLayout search_settings=null;

    private Globals mGlobals;
    private Comm mComm;
    private Context m_ctx;

    public SearchSettings(Context ctx) {
        mGlobals = App.getInstance().globals;;
        mComm = App.getInstance().comm;;
        m_ctx = ctx;

        vw = ((Activity)m_ctx).getWindow().getDecorView();
        //search_settings = vw.findViewById(R.id.search_settings);

        List<String> lengths = new ArrayList<>();
        for (int i=4;i<=24; i+=4) { lengths.add(Integer.toString(i));}
        Spinner length = vw.findViewById(R.id.length);
        ArrayAdapter<String> length_adapter = new ArrayAdapter<>(vw.getContext(), R.layout.setting_selector, lengths);
        length.setAdapter(length_adapter);
        length.setSelection((Integer.parseInt(mGlobals.get_param("length"))/4)-1);
        length.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                mGlobals.set_param("length", Integer.toString((position+1)*4));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Spinner currency = vw.findViewById(R.id.currency);
        String cur_curr = mGlobals.get_param("curr");
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
                mGlobals.set_param("curr", codes[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Switch rad_only = vw.findViewById(R.id.rad_only);
        if(mGlobals.get_param("rad_only")!=null) rad_only.setChecked(Boolean.TRUE);
        rad_only.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Boolean changed = Boolean.FALSE;
                if(!isChecked && mGlobals.get_param("rad_only")!=null)
                    changed = Boolean.TRUE;
                else if(isChecked && mGlobals.get_param("rad_only")==null)
                    changed = Boolean.TRUE;

                if(isChecked) mGlobals.set_param("rad_only", "1");
                else mGlobals.unset_param("rad_only");
                if(changed) {
                    mComm.make_request(mGlobals.get_query_params());
                    on_rad_options();
                }
            }
        });
        on_rad_options();
/**
        List<String> currencies = new ArrayList<>();
 currency_names
        for (Currency c: Currency.getAvailableCurrencies()) { currencies.add(c.getSymbol()); }
        Spinner currency = vw.findViewById(R.id.currency);
        ArrayAdapter<String> currency_adapter = new ArrayAdapter<String>(vw.getContext(), R.layout.setting_selector, currencies);
        currency.setAdapter(currency_adapter);
        currency.setSelection(0);
        currency.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
*/
    }
    private void on_rad_options(){

        if(mGlobals.get_param("rad_only")!=null) {
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
        final TextView update_freq_holder = vw.findViewById(R.id.update_freq_holder);
        SeekBar update_freq = vw.findViewById(R.id.update_freq);
        Switch sound_alert = vw.findViewById(R.id.sound_alert);
        on_radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onStartTrackingTouch(SeekBar elem){}
            @Override
            public void onStopTrackingTouch(SeekBar elem){}
            @Override
            public void onProgressChanged(SeekBar elem, int v, boolean state){
                mGlobals.set_param("rad", v);
                on_radius_holder.setText(Integer.toString(v)+"m");
            }});

        update_freq.setProgress(Integer.parseInt(Long.toString(mGlobals.update_freq)));
        final long old_freq = mGlobals.update_freq;
        update_freq_holder.setText(Long.toString(old_freq)+"s");

        update_freq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onStartTrackingTouch(SeekBar elem){}
            @Override
            public void onStopTrackingTouch(SeekBar elem){
                if(old_freq != mGlobals.update_freq){
                    mGlobals.location.set_location();
                }
            }
            @Override
            public void onProgressChanged(SeekBar elem, int v, boolean state){
                String v1 = Integer.toString(v);
                mGlobals.update_freq = Long.parseLong(v1);
                mGlobals.apply_pref("update_freq", mGlobals.update_freq);
                update_freq_holder.setText(v1+"s");
                mGlobals.renders.get("1").set_timer();
                // Globals.Render r = mGlobals.renders.get("1").set_timer();
               // if(r!=null) r.set_timer();
            }});

    }
}
