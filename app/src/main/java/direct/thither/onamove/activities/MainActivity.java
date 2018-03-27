package direct.thither.onamove.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;

import direct.thither.onamove.App;
import direct.thither.onamove.R;
import direct.thither.onamove.pages.ProductsResult;
import direct.thither.onamove.pages.SearchMenu;
import direct.thither.onamove.pages.SearchSettings;
import direct.thither.onamove.pages.StoreInfo;
import direct.thither.onamove.pages.WebKitPage;
import direct.thither.onamove.properties.PropsHolder;
import direct.thither.onamove.properties.Render;
import direct.thither.onamove.receivers.LocationReceiver;
import direct.thither.onamove.receivers.NetworkStateReceiver;

public class MainActivity extends AppCompatActivity {

    private PropsHolder m_props;
    private BroadcastReceiver networkStateReceiver;
    private BroadcastReceiver locationReceiver;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        if (m_props == null) {
            m_props = ((App) getApplication()).props;
            m_props.load_preferences(
                    getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE));

            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null)
                m_props.params.get("main").set_param("isoLoc", tm.getNetworkCountryIso());


            m_props.set_status_bar(findViewById(R.id.bottom_status));
            m_props.set_tabs_bar(findViewById(R.id.tab_layout));

            Toolbar toolbar = findViewById(R.id.toolbar);
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            new SearchSettings(this);
            run();
        }
        init_receivers();
    }

    private void init_receivers(){
        if(networkStateReceiver==null){
            networkStateReceiver = new NetworkStateReceiver();
            networkStateReceiver.onReceive(this, getIntent());
        }
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        if(locationReceiver==null){
            locationReceiver = new LocationReceiver();
            locationReceiver.onReceive(this, getIntent());
        }
        registerReceiver(locationReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }
    @Override
    public void onPause() {
        super.onPause();
        try{
            unregisterReceiver(networkStateReceiver);
            unregisterReceiver(locationReceiver);}
        catch (Exception e) {}
        m_props.stop_renders();
    }

    private void run(){
        new StoreInfo(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pos, float posOffset, int posOffsetPixels) {}
            @Override
            public void onPageSelected(int pos) { mSectionsPagerAdapter.selected(pos);}
            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(m_props.get_tabs_bar()));
        m_props.get_tabs_bar().setTabGravity(TabLayout.GRAVITY_FILL);
        m_props.get_tabs_bar().addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private SectionsPagerAdapter(FragmentManager fm) { super(fm); }
    private int last_page;
    private int num_pages=2;

    @Override
    public Fragment getItem(int pos) {
        Render r = m_props.renders.get(Integer.toString(pos));
        if(r!=null) return r.getFragment();

        Fragment f=null;
        switch (pos){
            case 2:
                new WebKitPage();
                break;
            case 1:
                f = new ProductsResult();
                break;
            default:
                f = new SearchMenu();
                break;
        }
        return f;
    }
    private void selected(int pos) {
        if(pos == last_page)return;
        last_page = pos;
        m_props.renders.get(Integer.toString(pos)).set_active();
    }
    @Override
    public int getCount() { return num_pages; }
}

}
